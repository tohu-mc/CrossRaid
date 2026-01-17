package mark.tofu.crossRaid;

import mark.tofu.crossRaid.bosses.BossDataManager;
import mark.tofu.crossRaid.commands.RaidCommand;
import mark.tofu.crossRaid.players.PlayerDataManager;
import mark.tofu.crossRaid.raids.RaidListener;
import mark.tofu.crossRaid.raids.RaidManager;
import mark.tofu.crossRaid.shops.ShopManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public final class CrossRaid extends JavaPlugin {

    private static CrossRaid instance;
    public PlayerDataManager playerDataManager;
    public BossDataManager bossDataManager;
    private ShopManager shopManager;
    private RaidManager raidManager;
    public static World CROSS_RAID_WORLD;

    @Override
    public void onEnable() {
        instance = this;
        // Plugin startup logic
        saveDefaultConfig();
        CROSS_RAID_WORLD = Bukkit.getWorld("CrossRaid");
        playerDataManager = new PlayerDataManager(this);
        bossDataManager = new BossDataManager(this);
        this.raidManager = new RaidManager(this);

        // 2. コマンド登録
        if (getCommand("crossraid") != null) {
            getCommand("crossraid").setExecutor(new RaidCommand(raidManager));
        }

        // 3. リスナー登録
        getServer().getPluginManager().registerEvents(new RaidListener(raidManager), this);
        this.shopManager = new ShopManager(this);
        bossDataManager.loadBosses();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
//        if (raidManager != null) {
//            raidManager.endAllSessions();
//        }
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
    public static CrossRaid getInstance() { return instance; }
    public RaidManager getRaidManager() { return raidManager; }
}
