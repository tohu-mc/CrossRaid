package mark.tofu.crossRaid.bosses;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import mark.tofu.crossRaid.CrossRaid;
import mark.tofu.crossRaid.players.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class BossDeathListener implements Listener {

    private final CrossRaid plugin;
    private final BossManager bossManager;

    public BossDeathListener(CrossRaid plugin, BossManager bossManager) {
        this.plugin = plugin;
        this.bossManager = bossManager;
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent e) {
        // 1. MythicMobかどうか確認
        if (!MythicBukkit.inst().getMobManager().isMythicMob(e.getEntity())) {
            return;
        }

        // 2. Mob情報を取得
        ActiveMob activeMob = MythicBukkit.inst().getMobManager().getMythicMobInstance(e.getEntity());
        if (activeMob == null) return;

        // 3. 内部IDを使って BossManager からデータ取得
        String internalName = activeMob.getType().getInternalName();
        BossData bossData = bossManager.getBossData(internalName);

        // 管理外のボスなら無視
        if (bossData == null) return;

        // 4. 報酬の付与
        // ※レイドシステムの場合、「誰に報酬を与えるか」が重要です。
        // ここではシンプルに「とどめを刺したプレイヤー」に与える例を書きますが、
        // RaidSessionと連携する場合は「参加者全員」にループ処理を変更してください。

        Player killer = e.getEntity().getKiller();
        if (killer != null) {
            giveReward(killer, bossData);
        }
    }

    private void giveReward(Player player, BossData data) {
        // PlayerDataを取得
        PlayerData pData = plugin.getPlayerDataManager().getData(player.getUniqueId());
        if (pData == null) return;

        // --- ゴールド付与 ---
        long gold = data.getRewardGold();
        if (gold > 0) {
            pData.addGold(gold); // PlayerDataの実装に合わせてメソッド名を変更してください (setGold, addMoney等)
            player.sendMessage("§e[報酬] §6" + gold + "G §eを獲得しました！");
        }

        // --- 経験値付与 ---
        double exp = data.getRewardExp();
        if (exp > 0) {
            pData.addExp(exp); // PlayerDataの実装に合わせて変更してください
            player.sendMessage("§e[報酬] §b" + exp + " EXP §eを獲得しました！");
        }

        // データ保存（必要であれば）
        // plugin.getPlayerDataManager().savePlayerData(player.getUniqueId());
    }
}