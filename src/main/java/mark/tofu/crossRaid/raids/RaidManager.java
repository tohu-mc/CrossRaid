package mark.tofu.crossRaid.raids;

import io.lumine.mythic.bukkit.MythicBukkit;
import mark.tofu.crossRaid.CrossRaid;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class RaidManager {

    private final CrossRaid plugin;

    // プレイ中のセッション管理 (プレイヤーUUID -> セッションインスタンス)
    // 複数のプレイヤーが「同じセッションインスタンス」を参照します
    private final Map<UUID, RaidSession> activeSessions = new HashMap<>();

    // ステージデータキャッシュ
    private final Map<Integer, StageData> stages = new HashMap<>();

    // --- 簡易パーティ管理用 ---
    // リーダーUUID -> メンバーリスト(リーダー含む)
    private final Map<UUID, Set<UUID>> lobbies = new HashMap<>();
    // 招待されている人 -> 招待してくれたリーダー
    private final Map<UUID, UUID> invites = new HashMap<>();

    public RaidManager(CrossRaid plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        stages.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("levels");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            try {
                int level = Integer.parseInt(key);
                String spawnStr = section.getString(key + ".spawn-loc");
                String bossLocStr = section.getString(key + ".boss-loc");
                String mobName = section.getString(key + ".mythic-mob");
                stages.put(level, new StageData(parseLoc(spawnStr), parseLoc(bossLocStr), mobName));
            } catch (Exception e) {
                plugin.getLogger().warning("Level " + key + " Error: " + e.getMessage());
            }
        }
    }

    // --- パーティ機能 ---

    // 招待を送る
    public void invitePlayer(Player leader, Player target) {
        if (activeSessions.containsKey(leader.getUniqueId()) || activeSessions.containsKey(target.getUniqueId())) {
            leader.sendMessage("§c現在レイド中のプレイヤーは招待できません。");
            return;
        }

        // ロビー作成（まだなければ）
        lobbies.putIfAbsent(leader.getUniqueId(), new HashSet<>(Collections.singletonList(leader.getUniqueId())));

        invites.put(target.getUniqueId(), leader.getUniqueId());
        target.sendMessage("§e" + leader.getName() + " からレイドの招待が届きました。 §a/raidspire join §eで参加します。");
        leader.sendMessage("§a" + target.getName() + " に招待を送りました。");
    }

    // 招待を受ける
    public void joinParty(Player player) {
        if (!invites.containsKey(player.getUniqueId())) {
            player.sendMessage("§c招待されていません。");
            return;
        }
        UUID leaderId = invites.remove(player.getUniqueId());
        Set<UUID> party = lobbies.get(leaderId);

        if (party == null) {
            player.sendMessage("§cパーティは解散されました。");
            return;
        }

        party.add(player.getUniqueId());
        player.sendMessage("§aパーティに参加しました！");
        Player leader = Bukkit.getPlayer(leaderId);
        if (leader != null) leader.sendMessage("§a" + player.getName() + " がパーティに参加しました。");
    }

    // --- レイド進行 ---

    // 開始（リーダーが実行）
    public void startRaid(Player leader) {
        Set<UUID> members = lobbies.getOrDefault(leader.getUniqueId(), new HashSet<>(Collections.singletonList(leader.getUniqueId())));

        // 全員がレイド中でないか確認
        for (UUID uid : members) {
            if (activeSessions.containsKey(uid)) {
                leader.sendMessage("§cメンバーの中に既にレイド中の人がいます。");
                return;
            }
        }

        // セッション作成
        RaidSession session = new RaidSession(leader.getUniqueId(), members);

        // マップに登録（全員が同じセッションを参照するように）
        for (UUID uid : members) {
            activeSessions.put(uid, session);
            invites.remove(uid); // 招待情報クリア
        }
        lobbies.remove(leader.getUniqueId()); // ロビークリア

        // 最初の処理へ
        broadcast(session, "§aレイドバトルを開始します！ 参加人数: " + members.size() + "人");
        processStage(session);
    }

    // ステージ処理（転送＆召喚）
    private void processStage(RaidSession session) {
        int level = session.getCurrentLevel();
        StageData data = stages.get(level);

        // クリア判定
        if (data == null) {
            completeRaid(session);
            return;
        }

        // 1. 全員テレポート
        Set<Player> onlinePlayers = session.getOnlinePlayers();
        if (onlinePlayers.isEmpty()) {
            endSession(session); // 全員落ちてたら終了
            return;
        }

        for (Player p : onlinePlayers) {
            p.teleport(data.playerSpawn);
            p.sendMessage("§e§l[LEVEL " + level + "] §rボス部屋に入場しました！");
        }

        // 2. ボス召喚
        try {
            Entity boss = MythicBukkit.inst().getAPIHelper().spawnMythicMob(data.mobName, data.bossSpawn);
            if (boss != null) {
                session.setCurrentBossUuid(boss.getUniqueId());
            } else {
                broadcast(session, "§cボスの召喚に失敗しました。管理者に連絡してください。");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ボス討伐検知
    public void onBossDeath(Entity entity) {
        UUID deadId = entity.getUniqueId();

        // どのセッションのボスか探す
        // (values()で回すと重複するので注意が必要だが、breakすればOK)
        RaidSession targetSession = null;
        for (RaidSession s : activeSessions.values()) {
            if (deadId.equals(s.getCurrentBossUuid())) {
                targetSession = s;
                break;
            }
        }

        if (targetSession != null) {
            broadcast(targetSession, "§bボス撃破！ 次の階層へ進みます...");
            targetSession.nextLevel();
            targetSession.setCurrentBossUuid(null);

            final RaidSession finalSession = targetSession;
            long delay = plugin.getConfig().getLong("settings.next-room-delay", 60L);

            new BukkitRunnable() {
                @Override
                public void run() {
                    // まだセッションが有効なら次へ
                    if (!finalSession.isEmpty()) {
                        processStage(finalSession);
                    }
                }
            }.runTaskLater(plugin, delay);
        }
    }

    private void completeRaid(RaidSession session) {
        broadcast(session, "§6§lCONGRATULATIONS! §rレイド完全制覇！");
        // 報酬処理など...
        endSession(session);
    }

    // セッション終了（解散）
    public void endSession(RaidSession session) {
        for (UUID uid : session.getMembers()) {
            activeSessions.remove(uid);
        }
    }

    // プレイヤー単体の離脱処理
    public void removePlayer(UUID uuid) {
        RaidSession session = activeSessions.get(uuid);
        if (session != null) {
            session.removeMember(uuid);
            activeSessions.remove(uuid);

            if (session.isEmpty()) {
                // 全員いなくなったらボスを消す等の処理が必要ならここに記述
            } else {
                broadcast(session, "§e仲間が1人脱落しました。残り人数で続行します。");
            }
        }
        // ロビーからも削除
        lobbies.values().forEach(set -> set.remove(uuid));
        invites.remove(uuid);
    }

    // セッション内メンバー全員へメッセージ
    private void broadcast(RaidSession session, String msg) {
        for (Player p : session.getOnlinePlayers()) {
            p.sendMessage(msg);
        }
    }

    public void endAllSessions() {
        activeSessions.clear();
    }

    private Location parseLoc(String str) {
        if (str == null) return null;
        String[] parts = str.split(",");
        if (parts.length < 4) return null;
        World w = Bukkit.getWorld(parts[0].trim());
        double x = Double.parseDouble(parts[1].trim());
        double y = Double.parseDouble(parts[2].trim());
        double z = Double.parseDouble(parts[3].trim());
        return new Location(w, x, y, z);
    }

    private static class StageData {
        final Location playerSpawn;
        final Location bossSpawn;
        final String mobName;
        StageData(Location p, Location b, String m) { this.playerSpawn = p; this.bossSpawn = b; this.mobName = m; }
    }
}