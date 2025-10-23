package de.brackis.legends.commands;

import de.brackis.legends.LegendsPlugin;
import de.brackis.legends.LegendsPlugin.Rank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PovCommand implements CommandExecutor {

    // Speicher für Originaldaten, damit man zurückwechseln kann (optional)
    private static final Map<UUID, ItemStack[]> savedInventories = new HashMap<>();
    private static final Map<UUID, Double> savedHealth = new HashMap<>();
    private static final Map<UUID, Integer> savedFood = new HashMap<>();
    private static final Map<UUID, Float> savedSaturation = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Nur Spieler können diesen Befehl nutzen.");
            return true;
        }

        Player admin = (Player) sender;
        Rank rank = LegendsPlugin.playerRanks.getOrDefault(admin.getUniqueId(), Rank.SPIELER);
        if (!(rank == Rank.ADMIN || rank == Rank.DEV || rank == Rank.MOD)) {
            admin.sendMessage(ChatColor.RED + "Du hast keine Rechte für diesen Befehl!");
            return true;
        }

        if (args.length != 1) {
            admin.sendMessage(ChatColor.RED + "Benutzung: /pov <Spieler>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            admin.sendMessage(ChatColor.RED + "Spieler '" + args[0] + "' wurde nicht gefunden oder ist nicht online!");
            return true;
        }

        // Sicherung der eigenen Werte (optional, für Rückkehr)
        savedInventories.put(admin.getUniqueId(), admin.getInventory().getContents());
        savedHealth.put(admin.getUniqueId(), admin.getHealth());
        savedFood.put(admin.getUniqueId(), admin.getFoodLevel());
        savedSaturation.put(admin.getUniqueId(), admin.getSaturation());

        // Inventar, Herzen, Hunger und Sättigung kopieren
        admin.getInventory().setContents(target.getInventory().getContents());
        admin.setHealth(target.getHealth());
        admin.setFoodLevel(target.getFoodLevel());
        admin.setSaturation(target.getSaturation());

        // Skin-Übernahme: In Vanilla nur mit externen Plugins/Mods möglich!
        // admin.setSkin(target.getSkin()); // <--- Pseudocode!

        // TP zur Position des Spielers (optional)
        admin.teleport(target.getLocation());

        admin.sendMessage(ChatColor.AQUA + "Du bist nun in der Perspektive von " + ChatColor.GOLD + target.getName() + ChatColor.AQUA + "!");
        target.sendMessage(ChatColor.YELLOW + admin.getName() + " schaut nun durch deine POV.");

        return true;
    }

    // Optional: Rückkehr zu eigenen Werten
    public static void resetPov(Player admin) {
        UUID id = admin.getUniqueId();
        if (savedInventories.containsKey(id)) {
            admin.getInventory().setContents(savedInventories.get(id));
            savedInventories.remove(id);
        }
        if (savedHealth.containsKey(id)) {
            admin.setHealth(savedHealth.get(id));
            savedHealth.remove(id);
        }
        if (savedFood.containsKey(id)) {
            admin.setFoodLevel(savedFood.get(id));
            savedFood.remove(id);
        }
        if (savedSaturation.containsKey(id)) {
            admin.setSaturation(savedSaturation.get(id));
            savedSaturation.remove(id);
        }
        admin.sendMessage(ChatColor.GREEN + "Du bist wieder in deiner eigenen POV.");
    }
}