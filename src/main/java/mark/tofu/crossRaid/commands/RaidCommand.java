package mark.tofu.crossRaid.commands;

import mark.tofu.crossRaid.raids.RaidManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RaidCommand implements CommandExecutor {

    private final RaidManager raidManager;

    public RaidCommand(RaidManager raidManager) {
        this.raidManager = raidManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("start")) {
                raidManager.startRaid(player);
                return true;
            }
            if (args[0].equalsIgnoreCase("reload")) {
                if (player.hasPermission("crossraid.admin")) {
                    raidManager.loadStages();
                    player.sendMessage("§aConfig reloaded.");
                    return true;
                }
            }
        }
        return false;
    }
}