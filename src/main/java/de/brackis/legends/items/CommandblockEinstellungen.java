package de.brackis.legends.items;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import java.util.Map;
import java.util.UUID;
import de.brackis.legends.LegendsPlugin;

public class CommandblockEinstellungen {

    // Gibt den Commandblock auf Slot 7 (links neben Dia)
    public static void give(Player player) {
        ItemStack block = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta meta = block.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Einstellungen");
        block.setItemMeta(meta);
        player.getInventory().setItem(7, block); // Hier liegt er auf Slot 7!
    }

    // Öffnet das Statusmenü (wird von LegendsPlugin aufgerufen)
    public static void openStatusMenu(Player player) {
        Inventory inv = Bukkit.createInventory(player, 9, ChatColor.GOLD + "Status");

        // Slot 3: Online (grün)
        ItemStack online = new ItemStack(Material.LIME_DYE);
        ItemMeta metaOnline = online.getItemMeta();
        metaOnline.setDisplayName(ChatColor.GREEN + "Online");
        online.setItemMeta(metaOnline);
        inv.setItem(3, online);

        // Slot 5: Offline (rot)
        ItemStack offline = new ItemStack(Material.RED_DYE);
        ItemMeta metaOffline = offline.getItemMeta();
        metaOffline.setDisplayName(ChatColor.RED + "Offline");
        offline.setItemMeta(metaOffline);
        inv.setItem(5, offline);

        player.openInventory(inv);
    }

    // Diese Methode kann von LegendsPlugin im InventoryClickEvent aufgerufen werden!
    public static void handleStatusMenuClick(InventoryClickEvent event, Map<UUID, Boolean> playerOnlineStatus, LegendsPlugin plugin) {
        if (!event.getView().getTitle().equals(ChatColor.GOLD + "Status")) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        if (event.getSlot() == 3) { // Online
            playerOnlineStatus.put(player.getUniqueId(), true);
            player.closeInventory();
            player.sendMessage(ChatColor.GREEN + "Du bist jetzt Online!");
            plugin.updateTablistAndVisibility(player);
        }
        if (event.getSlot() == 5) { // Offline
            playerOnlineStatus.put(player.getUniqueId(), false);
            player.closeInventory();
            player.sendMessage(ChatColor.RED + "Du bist jetzt Offline! Du bist jetzt nicht mehr in der Tabliste.");
            plugin.updateTablistAndVisibility(player);
        }
    }
}