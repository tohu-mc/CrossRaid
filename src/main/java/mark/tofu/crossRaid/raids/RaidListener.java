package mark.tofu.crossRaid.raids;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class RaidListener implements Listener {

    private final RaidManager raidManager;

    public RaidListener(RaidManager raidManager) {
        this.raidManager = raidManager;
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent e) {
        // 全てのEntity死亡をマネージャーに通知し、レイドボスか判定させる
        raidManager.handleBossDeath(e.getEntity());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        // 途中抜け処理
        raidManager.quitPlayer(e.getPlayer().getUniqueId());
    }
}