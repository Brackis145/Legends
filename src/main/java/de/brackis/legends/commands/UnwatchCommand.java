package de.brackis.legends.commands;

import de.brackis.legends.LegendsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Set;

public class UnwatchCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Nur Spieler können diesen Befehl nutzen.");
            return true;
        }
        Player admin = (Player) sender;

        // Nur für Admin-Ränge
        LegendsPlugin.Rank rank = LegendsPlugin.playerRanks.getOrDefault(admin.getUniqueId(), LegendsPlugin.Rank.SPIELER);
        if (rank != LegendsPlugin.Rank.ADMIN) {
            admin.sendMessage(ChatColor.RED + "Du hast keine Rechte für diesen Befehl!");
            return true;
        }
        if (args.length != 1) {
            admin.sendMessage(ChatColor.RED + "Benutzung: /unwatch <Spieler/@a>");
            return true;
        }

        String target = args[0];
        if (target.equalsIgnoreCase("@a")) {
            LegendsPlugin.watchAllAdmins.remove(admin.getUniqueId());
            admin.sendMessage(ChatColor.RED + "Das Watchsystem für " + ChatColor.RED + "@a"
                    + ChatColor.RED + " wurde beendet!" + ChatColor.RED);
            return true;
        }

        Set<java.util.UUID> watchedSet = LegendsPlugin.watchedPlayers.get(admin.getUniqueId());
        if (watchedSet != null) {
            Player watched = Bukkit.getPlayer(target);
            if (watched != null) {
                watchedSet.remove(watched.getUniqueId());
                admin.sendMessage(ChatColor.RED + "Das Watchsystem für " + ChatColor.RED + watched.getName()
                        + ChatColor.RED + " wurde beendet!" + ChatColor.RED);
            } else {
                admin.sendMessage(ChatColor.RED + "Spieler '" + target + "' ist nicht online.");
            }
        } else {
            admin.sendMessage(ChatColor.RED + "Du beobachtest diesen Spieler nicht.");
        }
        return true;
    }
}