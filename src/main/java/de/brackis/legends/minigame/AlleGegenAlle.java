package de.brackis.legends.minigame;

import de.brackis.legends.LegendsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class AlleGegenAlle {

    // Set für alle Spieler in der Lobby
    private static final Set<UUID> lobbyPlayers = new HashSet<>();
    // Map für gespeichertes Inventar
    private static final Map<UUID, ItemStack[]> savedInventories = new HashMap<>();

    // Spieler tritt dem Spiel bei (z.B. Navigator-Kopf-Klick)
    public static void handleNavigatorJoin(Player player) {
        // Inventar nur speichern, wenn noch nicht gespeichert!
        if (!savedInventories.containsKey(player.getUniqueId())) {
            savedInventories.put(player.getUniqueId(), player.getInventory().getContents());
        }
        teleportToLobby(player);
        joinGame(player);
    }

    // Spieler verlässt das Spiel (z.B. Spiel beenden Totenkopf oder Logout)
    public static void handleNavigatorLeave(Player player) {
        leaveGame(player);
        restoreInventoryAndTeleport(player);
    }

    // Spiel beenden für alle (z.B. durch Admin)
    public static void endGame() {
        for (UUID uuid : new HashSet<>(lobbyPlayers)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                leaveGame(player);
                restoreInventoryAndTeleport(player);
            }
        }
        lobbyPlayers.clear();
        savedInventories.clear();
    }

    // Teleport zur Lobby und Inventar leeren außer Slot 8 (Totenkopf)
    public static void teleportToLobby(Player player) {
        Location loc = LegendsPlugin.getLobbyLocation("allegegenalle");
        if (loc != null) {
            ItemStack slot8 = player.getInventory().getItem(8);
            player.getInventory().clear();
            player.getInventory().setItem(8, slot8);
            player.teleport(loc);
        } else {
            player.sendMessage(ChatColor.RED + "Es wurde noch keine Lobby für Alle-gegen-Alle gesetzt. Nutze /setlobby allegegenalle");
        }
    }

    // Spieler der Lobby hinzufügen und Chat-Nachricht
    private static void joinGame(Player player) {
        if (lobbyPlayers.add(player.getUniqueId())) {
            for (UUID uuid : lobbyPlayers) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) {
                    p.sendMessage(ChatColor.GREEN + player.getName() + " ist dem Spiel beigetreten!");
                }
            }
        }
    }

    // Spieler aus Lobby entfernen und Chat-Nachricht
    private static void leaveGame(Player player) {
        if (lobbyPlayers.remove(player.getUniqueId())) {
            for (UUID uuid : lobbyPlayers) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) {
                    p.sendMessage(ChatColor.RED + player.getName() + " hat das Spiel verlassen!");
                }
            }
        }
    }

    // Inventar zurückgeben und zu Spawn teleportieren
    private static void restoreInventoryAndTeleport(Player player) {
        ItemStack[] items = savedInventories.remove(player.getUniqueId());
        if (items != null) {
            player.getInventory().setContents(items);
        }
        Location spawn = LegendsPlugin.spawnLocation;
        if (spawn != null) {
            player.teleport(spawn);
        } else {
            player.sendMessage(ChatColor.RED + "Spawn wurde noch nicht gesetzt!");
        }
    }

    // Getter für die Spieler in der Lobby (optional z.B. für Scoreboard)
    public static Set<UUID> getLobbyPlayers() {
        return new HashSet<>(lobbyPlayers);
    }
}