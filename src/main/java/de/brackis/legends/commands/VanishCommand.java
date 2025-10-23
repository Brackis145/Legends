package de.brackis.legends.commands;

import de.brackis.legends.LegendsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VanishCommand implements CommandExecutor {

    private final LegendsPlugin plugin;
    public static final Set<UUID> vanishedPlayers = new HashSet<>();

    public VanishCommand(LegendsPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean canVanish(LegendsPlugin.Rank rank) {
        return rank == LegendsPlugin.Rank.ADMIN ||
                rank == LegendsPlugin.Rank.MOD ||
                rank == LegendsPlugin.Rank.BUILDER ||
                rank == LegendsPlugin.Rank.DEV;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Nur Spieler können diesen Befehl ausführen.");
            return true;
        }

        Player player = (Player) sender;
        LegendsPlugin.Rank rank = LegendsPlugin.playerRanks.getOrDefault(player.getUniqueId(), LegendsPlugin.Rank.SPIELER);

        if (!canVanish(rank)) {
            player.sendMessage(ChatColor.RED + "Du hast keine Berechtigung, diesen Befehl zu nutzen.");
            return true;
        }

        if (args.length != 1 || !(args[0].equalsIgnoreCase("true") || args[0].equalsIgnoreCase("false"))) {
            player.sendMessage(ChatColor.RED + "Benutzung: /vanish true|false");
            return true;
        }

        boolean enable = args[0].equalsIgnoreCase("true");

        if (enable) {
            vanishedPlayers.add(player.getUniqueId());
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.equals(player)) continue;
                // vanished sehen sich gegenseitig!
                if (vanishedPlayers.contains(p.getUniqueId())) {
                    player.showPlayer(plugin, p);
                    p.showPlayer(plugin, player);
                } else {
                    p.hidePlayer(plugin, player);
                }
            }
            player.sendMessage(ChatColor.GREEN + "Du bist jetzt unsichtbar.");
        } else {
            vanishedPlayers.remove(player.getUniqueId());
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.equals(player)) continue;
                p.showPlayer(plugin, player);
            }
            player.sendMessage(ChatColor.YELLOW + "Du bist jetzt wieder sichtbar.");
        }

        return true;
    }
}