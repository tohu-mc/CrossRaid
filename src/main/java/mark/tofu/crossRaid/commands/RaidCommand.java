package mark.tofu.crossRaid.commands;

import mark.tofu.crossRaid.raids.RaidManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RaidCommand implements CommandExecutor {

    private final RaidManager manager;

    public RaidCommand(RaidManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (args.length == 0) return false;

        switch (args[0].toLowerCase()) {
            case "start":
                // 自分がリーダーのパーティ、またはソロで開始
                manager.startRaid(player);
                return true;

            case "invite":
                if (args.length < 2) {
                    player.sendMessage("§cプレイヤー名を指定してください。 /raidspire invite <player>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage("§cプレイヤーが見つかりません。");
                    return true;
                }
                manager.invitePlayer(player, target);
                return true;

            case "join":
                manager.joinParty(player);
                return true;

            case "quit":
            case "leave":
                manager.removePlayer(player.getUniqueId());
                player.sendMessage("§eレイド/パーティから離脱しました。");
                return true;

            case "reload":
                if (player.hasPermission("raidspire.admin")) {
                    manager.loadConfig();
                    player.sendMessage("§aConfig reloaded.");
                    return true;
                }
                break;
        }
        return false;
    }
}