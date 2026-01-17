package mark.tofu.crossRaid.bosses;

import mark.tofu.crossRaid.CrossRaid;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class BossManager {

    private final CrossRaid plugin;
    // MythicMobsID -> BossData のマップ
    private final Map<String, BossData> bossMap = new HashMap<>();

    public BossManager(CrossRaid plugin) {
        this.plugin = plugin;
        loadBosses();
    }

    public void loadBosses() {
        bossMap.clear();
        File file = new File(plugin.getDataFolder(), "bosses.yml");
        if (!file.exists()) {
            plugin.saveResource("bosses.yml", false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("bosses");

        if (section == null) return;

        for (String mobId : section.getKeys(false)) {
            try {
                String path = mobId + ".";
                String name = section.getString(path + "display-name", mobId);

                // 報酬セクションの読み込み
                long gold = section.getLong(path + "rewards.gold", 0);
                double exp = section.getDouble(path + "rewards.exp", 0.0);

                BossData data = new BossData(mobId, name, gold, exp);
                bossMap.put(mobId, data);

            } catch (Exception e) {
                plugin.getLogger().warning("ボスデータの読み込み失敗: " + mobId);
                e.printStackTrace();
            }
        }
        plugin.getLogger().info("ボスデータを " + bossMap.size() + " 件ロードしました。");
    }

    /**
     * MythicMobsのInternalNameからボスデータを取得
     */
    public BossData getBossData(String mythicMobId) {
        return bossMap.get(mythicMobId);
    }
}