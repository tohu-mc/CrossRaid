package mark.tofu.crossRaid.bosses;

import mark.tofu.crossRaid.CrossRaid;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class BossDataManager {
    private final CrossRaid plugin;
    private final Map<String, BossData> bossMap = new HashMap<>();

    public BossDataManager(CrossRaid plugin) {
        this.plugin = plugin;
    }

    public void loadBosses() {
        bossMap.clear();
        File file = new File(plugin.getDataFolder(), "bosses.yaml");

        if (!file.exists()) {
            plugin.saveResource("bosses.yaml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) continue;

            // boss_001 等のIDをキーにデータを取得
            String name = section.getString("name", "Unknown Boss");
            long gold = section.getLong("drop_gold", 0);
            int exp = section.getInt("drop_exp", 0);

            BossData boss = new BossData(id, name, gold, exp);
            bossMap.put(id, boss);
        }

        plugin.getLogger().info("[BossManager] " + bossMap.size() + " 体のボスデータをロードしました。");
    }

    public BossData getBoss(String id) {
        return bossMap.get(id);
    }
}
