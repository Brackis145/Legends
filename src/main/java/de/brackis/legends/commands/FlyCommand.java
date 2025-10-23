package de.brackis.legends.commands;

import de.brackis.legends.LegendsPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FlyCommand implements CommandExecutor {

    private final LegendsPlugin plugin;

    public FlyCommand(LegendsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Nur Spieler können diesen Befehl ausführen.");
            return true;
        }

        Player player = (Player) sender;
        LegendsPlugin.Rank rank = LegendsPlugin.playerRanks.getOrDefault(player.getUniqueId(), LegendsPlugin.Rank.SPIELER);

        if (rank.level < LegendsPlugin.Rank.VIP.level) {
            player.sendMessage(ChatColor.RED + "Du benötigst mindestens den VIP-Rang, um fliegen zu können.");
            return true;
        }

        if (args.length != 1 || !(args[0].equalsIgnoreCase("true") || args[0].equalsIgnoreCase("false"))) {
            player.sendMessage(ChatColor.RED + "Benutzung: /fly true|false");
            return true;
        }

        boolean enable = args[0].equalsIgnoreCase("true");

        player.setAllowFlight(enable);
        player.setFlying(enable);

        if (enable) {
            player.sendMessage(ChatColor.GREEN + "Flugmodus aktiviert!");
        } else {
            player.sendMessage(ChatColor.YELLOW + "Flugmodus deaktiviert!");
        }
        return true;
    }
}