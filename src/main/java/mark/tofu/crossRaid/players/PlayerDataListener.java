package mark.tofu.crossRaid.players;

import mark.tofu.crossRaid.CrossRaid;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerDataListener {
    public static void playerLogin(Player player, CrossRaid plugin) {
        UUID uuid = player.getUniqueId();
        String name = player.getName();

        if (plugin.playerDataManager.hasData(uuid)) {
            plugin.playerDataManager.load(uuid,name);
            PlayerData data = plugin.playerDataManager.getData(uuid);

            player.sendMessage("§a[MINE] §fおかえりなさい、Lv." + data.getLevel() + " の " + name + " さん！");
        } else {
            plugin.playerDataManager.load(uuid, name);

        }
    }
}
