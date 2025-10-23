package de.brackis.legends.items;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;

public class Spielerkerze {
    public static final Material[] CANDLE_COLORS = {
            Material.GREEN_CANDLE,    // "Alle anzeigen"
            Material.PURPLE_CANDLE,    // "Nur VIPs anzeigen"
            Material.RED_CANDLE        // "Unsichtbar"
    };

    // Gibt dem Spieler die Kerze (immer gelb zum Start)
    public static void give(Player player) {
        ItemStack candle = new ItemStack(CANDLE_COLORS[0]);
        ItemMeta meta = candle.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Alle anzeigen");
        candle.setItemMeta(meta);
        player.getInventory().setItem(1, candle); // z.B. Slot 1
    }

    // Optional: Methode, um die Kerze-Farbe zu aktualisieren
    public static void updateColor(Player player, int colorIndex) {
        ItemStack candle = new ItemStack(CANDLE_COLORS[colorIndex]);
        String name = colorIndex == 0 ? ChatColor.GREEN + "Alle anzeigen"
                : colorIndex == 1 ? ChatColor.LIGHT_PURPLE + "Nur VIPs anzeigen"
                : ChatColor.RED + "Alle Unsichtbar";
        ItemMeta meta = candle.getItemMeta();
        meta.setDisplayName(name);
        candle.setItemMeta(meta);
        player.getInventory().setItem(1, candle);
    }

    // Sichtbarkeit wie zuvor besprochen:
    public static void updateVisibility(Player player, int colorIndex, de.brackis.legends.LegendsPlugin plugin) {
        for (Player target : player.getServer().getOnlinePlayers()) {
            if (target.equals(player)) continue;
            if (colorIndex == 0) {
                player.showPlayer(plugin, target);
            } else if (colorIndex == 1) {
                de.brackis.legends.LegendsPlugin.Rank rank =
                        de.brackis.legends.LegendsPlugin.playerRanks.getOrDefault(target.getUniqueId(),
                                de.brackis.legends.LegendsPlugin.Rank.SPIELER);
                if (rank == de.brackis.legends.LegendsPlugin.Rank.VIP ||
                        rank == de.brackis.legends.LegendsPlugin.Rank.BUILDER ||
                        rank == de.brackis.legends.LegendsPlugin.Rank.MOD ||
                        rank == de.brackis.legends.LegendsPlugin.Rank.DEV ||
                        rank == de.brackis.legends.LegendsPlugin.Rank.ADMIN) {
                    player.showPlayer(plugin, target);
                } else {
                    player.hidePlayer(plugin, target);
                }
            } else if (colorIndex == 2) {
                player.hidePlayer(plugin, target);
            }
        }
    }
}