package mark.tofu.crossRaid.shops;


import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class ShopListener implements Listener {

    private final ShopManager shopManager;

    public ShopListener(ShopManager shopManager) {
        this.shopManager = shopManager;
    }

    // NPC右クリック
    @EventHandler
    public void onNpcClick(PlayerInteractEntityEvent e) {
        if (e.getRightClicked().getCustomName() != null &&
                e.getRightClicked().getCustomName().equals(shopManager.getNpcName())) {

            e.setCancelled(true);
            shopManager.openShop(e.getPlayer());
        }
    }

    // インベントリ操作
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        // タイトルで判定（簡易的ですが実用的です）
        // shop.ymlのタイトルと完全に一致させる必要があります
        // ※厳密にやるならInventoryHolderを使う手法もありますが、yml設定型ならタイトル判定が楽です
        if (e.getView().getTitle().equals("§0§lRaid Shop")) {
            e.setCancelled(true); // アイテムを持ち出せないようにキャンセル

            if (e.getCurrentItem() == null) return;
            if (!(e.getWhoClicked() instanceof Player)) return;

            Player player = (Player) e.getWhoClicked();

            // 自分のインベントリではなく、ショップ側のインベントリをクリックしたか判定
            if (e.getClickedInventory() == e.getView().getTopInventory()) {
                shopManager.buyItem(player, e.getSlot());
            }
        }
    }
}