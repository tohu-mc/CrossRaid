package mark.tofu.crossRaid;

import mark.tofu.crossRaid.players.PlayerDataListener;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import static mark.tofu.crossRaid.CrossRaid.CROSS_RAID_WORLD;

public class Events implements Listener {
    private final CrossRaid plugin;

    public Events(CrossRaid plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        if (!event.getFrom().equals(CROSS_RAID_WORLD) && event.getPlayer().getWorld().equals(CROSS_RAID_WORLD)) {
            PlayerDataListener.playerLogin(event.getPlayer(),plugin);
        } else if (event.getFrom().equals(CROSS_RAID_WORLD) && !event.getPlayer().getWorld().equals(CROSS_RAID_WORLD)) {
            event.getPlayer().getAttribute(Attribute.MAX_HEALTH).setBaseValue(20.0);
        }
    }
}