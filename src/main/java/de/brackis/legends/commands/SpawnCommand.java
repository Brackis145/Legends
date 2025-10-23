package de.brackis.legends.commands;

import de.brackis.legends.LegendsPlugin;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Nur Spieler können diesen Befehl nutzen.");
            return true;
        }
        Player player = (Player) sender;
        Location loc = LegendsPlugin.spawnLocation;
        if (loc != null) {
            player.teleport(loc);
            player.sendMessage("§aDu wurdest zum Spawn teleportiert!");
        } else {
            player.sendMessage("§cDer Spawn wurde noch nicht gesetzt! Ein Admin kann ihn mit /setspawn festlegen.");
        }
        return true;
    }
}