package de.brackis.legends.commands;

import de.brackis.legends.LegendsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class WatchCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Nur Spieler können diesen Befehl nutzen.");
            return true;
        }
        Player admin = (Player) sender;
        LegendsPlugin.Rank rank = LegendsPlugin.playerRanks.getOrDefault(admin.getUniqueId(), LegendsPlugin.Rank.SPIELER);
        if (rank != LegendsPlugin.Rank.ADMIN) {
            admin.sendMessage(ChatColor.RED + "Du hast keine Rechte für diesen Befehl!");
            return true;
        }
        if (args.length != 1) {
            admin.sendMessage(ChatColor.RED + "Benutzung: /watch <Spieler/@a>");
            return true;
        }
        String target = args[0];
        if (target.equalsIgnoreCase("@a")) {
            LegendsPlugin.watchAllAdmins.add(admin.getUniqueId());
            admin.sendMessage(ChatColor.WHITE + "Das Watchsystem für " + ChatColor.GREEN + "@a"
                    + ChatColor.WHITE + " wurde gestartet" + ChatColor.GREEN + "!");
            return true;
        }
        Player watched = Bukkit.getPlayer(target);
        if (watched == null) {
            admin.sendMessage(ChatColor.RED + "Spieler '" + target + "' ist nicht online.");
            return true;
        }
        LegendsPlugin.watchedPlayers.computeIfAbsent(admin.getUniqueId(), k -> new HashSet<>()).add(watched.getUniqueId());
        admin.sendMessage(ChatColor.WHITE + "Das Watchsystem für " + ChatColor.GREEN + watched.getName()
                + ChatColor.WHITE + " wurde gestartet" + ChatColor.GREEN + "!");
        return true;
    }
}