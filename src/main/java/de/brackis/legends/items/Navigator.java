package de.brackis.legends.items;

import de.brackis.legends.LegendsPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.inventory.meta.SkullMeta;
import java.util.*;

public class Navigator {
    public static void give(Player player) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Navigator");
        compass.setItemMeta(meta);
        player.getInventory().setItem(0, compass);
    }

    public static boolean isNavigator(ItemStack item) {
        return item != null
                && item.getType() == Material.COMPASS
                && item.hasItemMeta()
                && item.getItemMeta().hasDisplayName()
                && ChatColor.stripColor(item.getItemMeta().getDisplayName()).equals("Navigator");
    }

    public static void openNavigatorMenu(Player player, Set<UUID> activeHosts, Map<UUID, LegendsPlugin.Rank> playerRanks, Map<UUID, String> activeHostGamemode) {
        int size = Math.max(9, ((activeHosts.size() - 1) / 9 + 1) * 9);
        org.bukkit.inventory.Inventory inv = Bukkit.createInventory(player, size, ChatColor.YELLOW + "Aktive Spiele");

        int slot = 0;
        for (UUID uuid : activeHosts) {
            Player host = Bukkit.getPlayer(uuid);
            if (host == null) continue;
            LegendsPlugin.Rank rank = playerRanks.getOrDefault(uuid, LegendsPlugin.Rank.SPIELER);
            String gamemode = activeHostGamemode.getOrDefault(uuid, "?");
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setDisplayName(rank.color + host.getName() + ChatColor.WHITE + " [" + rank.displayName + "]" + ChatColor.AQUA + " - " + ChatColor.YELLOW + gamemode);
            meta.setOwningPlayer(host);
            skull.setItemMeta(meta);
            inv.setItem(slot, skull);
            slot++;
        }
        player.openInventory(inv);
    }
}