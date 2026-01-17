package mark.tofu.crossRaid.raids;

import io.lumine.mythic.bukkit.MythicBukkit;
import mark.tofu.crossRaid.CrossRaid;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

public class RaidManager {

    private final CrossRaid plugin;

    // 進行中のセッション (PlayerUUID -> Session)
    private final Map<UUID, RaidSession> activeSessions = new HashMap<>();

    // ステージデータキャッシュ (階層番号 -> データ)
    private final Map<Integer, StageInfo> stageData = new HashMap<>();

    private long nextStageDelay = 60L;

    public RaidManager(CrossRaid plugin) {
        this.plugin = plugin;
        loadStages();
    }

    // --- 設定読み込み ---
    public void loadStages() {
        stageData.clear();
        File file = new File(plugin.getDataFolder(), "stages.yml");
        if (!file.exists()) plugin.saveResource("stages.yml", false);

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        this.nextStageDelay = config.getLong("settings.next-stage-delay", 60L);

        ConfigurationSection sec = config.getConfigurationSection("stages");
        if (sec != null) {
            for (String key : sec.getKeys(false)) {
                try {
                    int id = Integer.parseInt(key);
                    stageData.put(id, new StageInfo(
                            sec.getString(key + ".display-name"),
                            parseLocation(sec.getString(key + ".player-spawn")),
                            parseLocation(sec.getString(key + ".boss-spawn")),
                            sec.getString(key + ".mythic-mob-id"),
                            sec.getInt(key + ".reward-gold")
                    ));
                } catch (Exception e) {
                    plugin.getLogger().warning("ステージ設定エラー: " + key + " - " + e.getMessage());
                }
            }
        }
        plugin.getLogger().info("ロード完了: " + stageData.size() + " ステージ");
    }

    // --- ゲーム進行 ---

    // レイド開始
    public void startRaid(Player leader) {
        if (activeSessions.containsKey(leader.getUniqueId())) {
            leader.sendMessage("§c既にレイドに参加しています。");
            return;
        }

        // ここではソロ開始ですが、パーティ機能があるならメンバー全員をSetに入れます
        Set<UUID> members = new HashSet<>();
        members.add(leader.getUniqueId());

        RaidSession session = new RaidSession(members);
        for (UUID uid : members) activeSessions.put(uid, session);

        leader.sendMessage("§aレイドを開始します！");
        playStage(session);
    }

    // 階層のセットアップ
    private void playStage(RaidSession session) {
        int stageIdx = session.getCurrentStageIndex();
        StageInfo info = stageData.get(stageIdx);

        // 次のステージがない ＝ 全クリ
        if (info == null) {
            completeRaid(session);
            return;
        }

        // 1. プレイヤー転送
        for (Player p : session.getOnlinePlayers()) {
            p.teleport(info.playerSpawn);
            p.sendMessage(info.displayName + " §eへ移動しました。");
            p.sendTitle(info.displayName, "§7Boss Approaching...", 10, 60, 20);
        }

        // 2. MythicMobs召喚
        try {
            Entity boss = MythicBukkit.inst().getAPIHelper().spawnMythicMob(info.mythicMobId, info.bossSpawn);
            if (boss != null) {
                session.setCurrentBossUuid(boss.getUniqueId());
            } else {
                broadcast(session, "§cボス召喚エラー: MobIDを確認してください (" + info.mythicMobId + ")");
            }
        } catch (Exception e) {
            e.printStackTrace();
            broadcast(session, "§c内部エラーによりボスを召喚できませんでした。");
        }
    }

    // ボス死亡検知（Listenerから呼ばれる）
    public void handleBossDeath(Entity deadEntity) {
        UUID deadId = deadEntity.getUniqueId();

        // このボスと戦っているセッションを探す
        RaidSession targetSession = null;
        for (RaidSession session : activeSessions.values()) {
            if (deadId.equals(session.getCurrentBossUuid())) {
                targetSession = session;
                break;
            }
        }

        if (targetSession != null) {
            processStageClear(targetSession);
        }
    }

    // ステージクリア処理
    private void processStageClear(RaidSession session) {
        StageInfo info = stageData.get(session.getCurrentStageIndex());

        // 報酬配布 (既存のPlayerDataシステムを使う想定)
        for (Player p : session.getOnlinePlayers()) {
            // plugin.getPlayerDataManager().addGold(p.getUniqueId(), info.rewardGold);
            p.sendMessage("§eステージクリア！ §6+" + info.rewardGold + "G");
        }

        // 次へ進む
        session.advanceStage();
        session.setCurrentBossUuid(null); // ボスIDリセット

        // 遅延して次の部屋へ
        final RaidSession finalSession = session;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!finalSession.isEmpty()) {
                    playStage(finalSession);
                }
            }
        }.runTaskLater(plugin, nextStageDelay);
    }

    // 全クリ処理
    private void completeRaid(RaidSession session) {
        broadcast(session, "§6§lCONGRATULATIONS! §r全てのステージを攻略しました！");
        endSession(session);
    }

    // 強制終了・解散
    public void endSession(RaidSession session) {
        for (UUID uid : session.getMemberIds()) {
            activeSessions.remove(uid);
            Player p = Bukkit.getPlayer(uid);
            if (p != null) {
                p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation()); // ロビーへ戻す
            }
        }
    }

    public void quitPlayer(UUID uuid) {
        if (activeSessions.containsKey(uuid)) {
            activeSessions.remove(uuid);
            // ※必要に応じてセッション自体の解散や、残ったメンバーへの通知を入れる
        }
    }

    // --- Utils ---
    private void broadcast(RaidSession session, String msg) {
        session.getOnlinePlayers().forEach(p -> p.sendMessage(msg));
    }

    private Location parseLocation(String str) {
        if (str == null) return null;
        String[] part = str.split(",");
        World w = Bukkit.getWorld(part[0].trim());
        double x = Double.parseDouble(part[1].trim());
        double y = Double.parseDouble(part[2].trim());
        double z = Double.parseDouble(part[3].trim());
        return new Location(w, x, y, z);
    }

    // データ保持用インナークラス
    private static class StageInfo {
        final String displayName;
        final Location playerSpawn;
        final Location bossSpawn;
        final String mythicMobId;
        final int rewardGold;

        public StageInfo(String d, Location p, Location b, String m, int r) {
            this.displayName = d; this.playerSpawn = p; this.bossSpawn = b; this.mythicMobId = m; this.rewardGold = r;
        }
    }
}