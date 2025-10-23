package de.brackis.legends.commands;

import de.brackis.legends.LegendsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RangCommand implements CommandExecutor, TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        boolean isConsole = !(sender instanceof Player);
        Player giver = isConsole ? null : (Player) sender;

        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Benutzung: /rang <spieler> <rang>");
            sender.sendMessage(ChatColor.GRAY + "Verfügbare Ränge: " + getAllRanksString());
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Spieler nicht gefunden!");
            return true;
        }

        LegendsPlugin.Rank rank = null;
        for (LegendsPlugin.Rank r : LegendsPlugin.Rank.values()) {
            if (r.name().equalsIgnoreCase(args[1]) || r.displayName.equalsIgnoreCase(args[1])) {
                rank = r;
                break;
            }
        }
        if (rank == null) {
            sender.sendMessage(ChatColor.RED + "Ungültiger Rang!");
            sender.sendMessage(ChatColor.GRAY + "Verfügbare Ränge: " + getAllRanksString());
            return true;
        }

        // Permission-Check: Konsole darf immer, Spieler nur Dev/Admin
        if (!isConsole) {
            LegendsPlugin.Rank senderRank = LegendsPlugin.playerRanks.getOrDefault(giver.getUniqueId(), LegendsPlugin.Rank.SPIELER);
            if (senderRank != LegendsPlugin.Rank.DEV && senderRank != LegendsPlugin.Rank.ADMIN) {
                giver.sendMessage(ChatColor.RED + "Du hast keine Berechtigung, Ränge zu vergeben!");
                return true;
            }
        }

        LegendsPlugin.playerRanks.put(target.getUniqueId(), rank);
        LegendsPlugin.setTablistName(target, rank);

        // Hotbar-Update für Spielhost (Barriere/Feuerwerk)
        if (rank.level >= LegendsPlugin.Rank.CREATOR.level) {
            ItemStack rocket = new ItemStack(Material.FIREWORK_ROCKET);
            ItemMeta rocketMeta = rocket.getItemMeta();
            rocketMeta.setDisplayName(ChatColor.GOLD + "Spiel erstellen");
            rocket.setItemMeta(rocketMeta);
            target.getInventory().setItem(8, rocket);
        } else {
            ItemStack barrier = new ItemStack(Material.BARRIER);
            ItemMeta barMeta = barrier.getItemMeta();
            barMeta.setDisplayName(ChatColor.GOLD + "Spiel erstellen");
            barrier.setItemMeta(barMeta);
            target.getInventory().setItem(8, barrier);
        }

        // Nachricht für Empfänger: Rang aktualisiert (ohne Anleitung)
        target.sendMessage(ChatColor.GREEN + "Dein Rang wurde auf " + rank.displayName + " aktualisiert.");

        // Nachricht für Admins, Mods, Devs
        for (Player p : Bukkit.getOnlinePlayers()) {
            LegendsPlugin.Rank r = LegendsPlugin.playerRanks.getOrDefault(p.getUniqueId(), LegendsPlugin.Rank.SPIELER);
            if (r == LegendsPlugin.Rank.ADMIN || r == LegendsPlugin.Rank.MOD || r == LegendsPlugin.Rank.DEV) {
                String von = isConsole ? "Konsole" : giver.getName();
                p.sendMessage(ChatColor.GREEN + "Der Spieler " + target.getName() + " hat von " + von + " den Rang " + rank.displayName + " erhalten.");
            }
        }
        sender.sendMessage(ChatColor.GREEN + "Rang für " + target.getName() + " wurde erfolgreich vergeben: " + rank.displayName);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("rang")) {
            if (args.length == 1) {
                List<String> names = new ArrayList<>();
                for (Player p : Bukkit.getOnlinePlayers()) names.add(p.getName());
                return names;
            }
            if (args.length == 2) {
                List<String> rangNamen = new ArrayList<>();
                for (LegendsPlugin.Rank r : LegendsPlugin.Rank.values()) rangNamen.add(r.displayName);
                return rangNamen;
            }
        }
        return Collections.emptyList();
    }

    private String getAllRanksString() {
        List<String> list = new ArrayList<>();
        for (LegendsPlugin.Rank r : LegendsPlugin.Rank.values()) {
            list.add(r.displayName);
        }
        return String.join(", ", list);
    }
}