package mark.tofu.crossRaid;

import mark.tofu.crossRaid.bosses.BossDataManager;
import mark.tofu.crossRaid.players.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public final class CrossRaid extends JavaPlugin {

    public PlayerDataManager playerDataManager;
    public BossDataManager bossDataManager;
    public static World CROSS_RAID_WORLD;

    @Override
    public void onEnable() {
        // Plugin startup logic
        CROSS_RAID_WORLD = Bukkit.getWorld("CrossRaid");
        playerDataManager = new PlayerDataManager(this);
        bossDataManager = new BossDataManager(this);
        bossDataManager.loadBosses();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
