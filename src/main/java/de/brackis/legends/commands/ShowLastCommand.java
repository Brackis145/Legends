package de.brackis.legends.commands;

import de.brackis.legends.LegendsPlugin;
import de.brackis.legends.LegendsPlugin.Rank;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.*;

public class ShowLastCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // /showlast help darf jeder nutzen!
        if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(ChatColor.DARK_GREEN + "Benutzung: "
                    + ChatColor.WHITE + "/showlast <" + ChatColor.GOLD + "command"
                    + ChatColor.WHITE + "/" + ChatColor.GOLD + "chatmsg"
                    + ChatColor.WHITE + "/" + ChatColor.GOLD + "whispermsg"
                    + ChatColor.WHITE + "> <" + ChatColor.YELLOW + "Spieler"
                    + ChatColor.WHITE + "> <" + ChatColor.YELLOW + "Anzahl"
                    + ChatColor.WHITE + ">");
            sender.sendMessage(ChatColor.DARK_GREEN + "Möglichkeiten:");
            sender.sendMessage(ChatColor.GOLD + "command" + ChatColor.WHITE + ": Zeigt die letzten Commands des Spielers");
            sender.sendMessage(ChatColor.GOLD + "chatmsg" + ChatColor.WHITE + ": Zeigt die letzten Chatnachrichten des Spielers");
            sender.sendMessage(ChatColor.GOLD + "whispermsg" + ChatColor.WHITE + ": Zeigt die letzten Flüsternachrichten des Spielers");
            return true;
        }

        // Ab hier: Nur Admin, Dev, Mod dürfen die History abfragen
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Nur Spieler können diesen Befehl nutzen.");
            return true;
        }
        Player admin = (Player) sender;
        Rank rank = LegendsPlugin.playerRanks.getOrDefault(admin.getUniqueId(), Rank.SPIELER);
        if (!(rank == Rank.ADMIN || rank == Rank.DEV || rank == Rank.MOD)) {
            admin.sendMessage(ChatColor.RED + "Du hast keine Rechte für diesen Befehl!");
            return true;
        }

        if (args.length < 3) {
            admin.sendMessage(ChatColor.RED + "Benutzung: /showlast <command|chatmsg|whispermsg> <Spieler> <Anzahl>");
            admin.sendMessage(ChatColor.YELLOW + "Nutze /showlast help für alle Möglichkeiten");
            return true;
        }

        String type = args[0].toLowerCase();
        String targetName = args[1];
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
            if (amount < 1) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            admin.sendMessage(ChatColor.RED + "Ungültige Anzahl!");
            return true;
        }
        UUID targetUUID = LegendsPlugin.getUUIDByName(targetName);
        if (targetUUID == null) {
            admin.sendMessage(ChatColor.RED + "Spieler '" + targetName + "' wurde nicht gefunden!");
            return true;
        }
        LinkedList<String> history = null;
        String what = "";
        if (type.equals("command")) {
            history = LegendsPlugin.playerCommandHistory.getOrDefault(targetUUID, new LinkedList<>());
            what = "Commands";
        } else if (type.equals("chatmsg")) {
            history = LegendsPlugin.playerChatHistory.getOrDefault(targetUUID, new LinkedList<>());
            what = "Chatnachrichten";
        } else if (type.equals("whispermsg")) {
            history = LegendsPlugin.playerWhisperHistory.getOrDefault(targetUUID, new LinkedList<>());
            what = "Flüsternachrichten";
        } else {
            admin.sendMessage(ChatColor.RED + "Ungültige Auswahl! Wähle: command, chatmsg, whispermsg");
            admin.sendMessage(ChatColor.YELLOW + "Nutze /showlast help für alle Möglichkeiten");
            return true;
        }
        admin.sendMessage(ChatColor.AQUA + "Letzte " + amount + " " + what + " von " + ChatColor.GOLD + targetName + ChatColor.AQUA + ":");
        int start = Math.max(0, history.size() - amount);
        List<String> sub = history.subList(start, history.size());
        if (sub.isEmpty()) {
            admin.sendMessage(ChatColor.GRAY + "Keine Daten vorhanden.");
        } else {
            for (String msg : sub) {
                admin.sendMessage(ChatColor.GRAY + msg);
            }
        }
        return true;
    }
}