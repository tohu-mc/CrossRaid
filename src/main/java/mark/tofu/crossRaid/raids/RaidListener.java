package mark.tofu.crossRaid.raids;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class RaidListener implements Listener {

    private final RaidManager manager;

    public RaidListener(RaidManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent e) {
        manager.onBossDeath(e.getEntity());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        // ログアウトしたらパーティ/レイドから抜ける
        manager.removePlayer(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        // 死んだらレイドから抜ける扱いにする（または観戦モードにする等の拡張が可能）
        manager.removePlayer(e.getEntity().getUniqueId());
        e.getEntity().sendMessage("§cレイドから脱落しました。");
    }
}