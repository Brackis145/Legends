package de.brackis.legends.commands;

import de.brackis.legends.LegendsPlugin;
import de.brackis.legends.LegendsPlugin.Rank;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.UUID;

public class CheckRangCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Argument-Check
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Benutzung: /checkrang <Spieler>");
            return true;
        }

        // Spieler-Rang prüfen (Konsole immer erlaubt)
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Rank senderRank = LegendsPlugin.playerRanks.getOrDefault(player.getUniqueId(), Rank.SPIELER);
            if (!(senderRank == Rank.ADMIN || senderRank == Rank.DEV || senderRank == Rank.MOD)) {
                player.sendMessage(ChatColor.RED + "Du hast keine Berechtigung für diesen Befehl!");
                return true;
            }
        }

        String targetName = args[0];
        UUID targetUUID = LegendsPlugin.nameToUUID.get(targetName.toLowerCase());

        if (targetUUID == null) {
            sender.sendMessage(ChatColor.RED + "Spieler '" + targetName + "' wurde nicht gefunden!");
            return true;
        }

        Rank rank = LegendsPlugin.playerRanks.getOrDefault(targetUUID, Rank.SPIELER);

        sender.sendMessage(ChatColor.GOLD + "Der Spieler " + ChatColor.WHITE + targetName +
                ChatColor.GOLD + " trägt zurzeit den " + rank.color + rank.displayName + ChatColor.GOLD + " Rang.");
        return true;
    }
}