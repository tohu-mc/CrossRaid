package mark.tofu.crossRaid.shops;


import mark.tofu.crossRaid.CrossRaid;
import mark.tofu.crossRaid.players.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ShopManager {

    private final CrossRaid plugin;
    // ShopItemクラスは内部または別ファイルで定義
    private final Map<Integer, ShopItem> shopItems = new HashMap<>();
    private String guiTitle;
    private String npcName;

    public ShopManager(CrossRaid plugin) {
        this.plugin = plugin;
        loadShopConfig();
    }

    public void loadShopConfig() {
        File file = new File(plugin.getDataFolder(), "shop.yml");
        if (!file.exists()) plugin.saveResource("shop.yml", false);

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        this.guiTitle = config.getString("shop-settings.gui-title", "Shop");
        this.npcName = config.getString("shop-settings.npc-name", "Shopkeeper");

        shopItems.clear();
        ConfigurationSection itemsSec = config.getConfigurationSection("items");
        if (itemsSec != null) {
            for (String key : itemsSec.getKeys(false)) {
                try {
                    int slot = itemsSec.getInt(key + ".slot");
                    Material mat = Material.matchMaterial(itemsSec.getString(key + ".material", "STONE"));
                    String name = itemsSec.getString(key + ".name");
                    List<String> lore = itemsSec.getStringList(key + ".lore");
                    int price = itemsSec.getInt(key + ".price");
                    String command = itemsSec.getString(key + ".command", null);

                    shopItems.put(slot, new ShopItem(mat, name, lore, price, command));
                } catch (Exception e) {
                    plugin.getLogger().warning("Shop item load error: " + key);
                }
            }
        }
    }

    public String getNpcName() {
        return npcName;
    }

    public void openShop(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, guiTitle);

        for (Map.Entry<Integer, ShopItem> entry : shopItems.entrySet()) {
            ShopItem item = entry.getValue();
            ItemStack icon = new ItemStack(item.material);
            ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(item.name);
                meta.setLore(item.lore);
                icon.setItemMeta(meta);
            }
            inv.setItem(entry.getKey(), icon);
        }
        player.openInventory(inv);
    }

    // 購入処理
    public void buyItem(Player player, int slot) {
        ShopItem item = shopItems.get(slot);
        if (item == null) return;

        // --- 既存のPlayerData連携部分 ---
        // あなたのプラグインの構造に合わせて取得方法を調整してください
        // 例: plugin.getPlayerDataManager().getPlayerData(player.getUniqueId())
        PlayerData data = plugin.getPlayerDataManager().getData(player.getUniqueId());

        if (data == null) return;

        // 所持金チェック (PlayerDataに getGold() があると仮定)
        if (data.getGold() >= item.price) {

            // お金の引き落とし (PlayerDataに setGold/removeGold があると仮定)
            data.setGold(data.getGold() - item.price);

            // アイテム配布
            if (item.command != null && !item.command.isEmpty()) {
                // コマンド実行型の場合
                String cmd = item.command.replace("%player%", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            } else {
                // 通常アイテムの場合
                ItemStack give = new ItemStack(item.material);
                player.getInventory().addItem(give);
            }

            player.sendMessage("§a購入しました！ §7(残高: " + data.getGold() + "G)");

            // 必要ならデータ保存などを呼び出す
            // plugin.getPlayerDataManager().savePlayer(player.getUniqueId());

        } else {
            player.sendMessage("§cお金が足りません！ (必要: " + item.price + "G)");
        }
    }

    // データ保持用レコード
    private static class ShopItem {
        final Material material;
        final String name;
        final List<String> lore;
        final int price;
        final String command;

        public ShopItem(Material material, String name, List<String> lore, int price, String command) {
            this.material = material;
            this.name = name;
            this.lore = lore;
            this.price = price;
            this.command = command;
        }
    }
}