package de.brackis.legends.items;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;

public class Spielhost {

    public static void give(Player player, boolean isCreatorOrAbove) {
        if (isCreatorOrAbove) {
            ItemStack rocket = new ItemStack(Material.FIREWORK_ROCKET);
            ItemMeta meta = rocket.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + "Spiel erstellen");
            rocket.setItemMeta(meta);
            player.getInventory().setItem(8, rocket);
        } else {
            ItemStack barrier = new ItemStack(Material.BARRIER);
            ItemMeta meta = barrier.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + "Spiel erstellen");
            barrier.setItemMeta(meta);
            player.getInventory().setItem(8, barrier);
        }
    }
}