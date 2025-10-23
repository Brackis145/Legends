package de.brackis.legends.items;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;

public class Event {
    public static void give(Player player) {
        ItemStack eventBlock = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta meta = eventBlock.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "EVENT veranstalten");
        eventBlock.setItemMeta(meta);
        player.getInventory().setItem(4, eventBlock); // Slot 4 ist mittig!
    }
}