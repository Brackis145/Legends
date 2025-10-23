package de.brackis.legends.commands;

import de.brackis.legends.LegendsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LiveCommand implements CommandExecutor {

    private final LegendsPlugin plugin;

    public LiveCommand(LegendsPlugin plugin) {
        this.plugin = plugin;
    }

    private void sendLiveOptions(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "Live-Command Optionen:");
        sender.sendMessage(ChatColor.YELLOW + "/live setstreamer <Streamer>");
        sender.sendMessage(ChatColor.GRAY   + "  → Setzt den Streamer, dessen Name angezeigt wird.");
        sender.sendMessage(ChatColor.YELLOW + "/live settitel <Titel>");
        sender.sendMessage(ChatColor.GRAY   + "  → Setzt den Titel, der allen angezeigt wird. Standard: '<Streamer> ist Live'");
        sender.sendMessage(ChatColor.YELLOW + "/live settitelcolor <Farbe>");
        sender.sendMessage(ChatColor.GRAY   + "  → Setzt die Farbe des Live-Titels. Beispiel: red, green, aqua, dark_red, etc.");
        sender.sendMessage(ChatColor.YELLOW + "/live allert");
        sender.sendMessage(ChatColor.GRAY   + "  → Zeigt allen Spielern den Live-Titel als roten Schriftzug an.");
        sender.sendMessage(ChatColor.YELLOW + "/live help");
        sender.sendMessage(ChatColor.GRAY   + "  → Zeigt diese Übersicht aller Live-Befehle und Erklärungen.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Nur Admins dürfen diesen Command!
        if (!(sender instanceof Player) || LegendsPlugin.playerRanks.getOrDefault(((Player)sender).getUniqueId(), LegendsPlugin.Rank.SPIELER) != LegendsPlugin.Rank.ADMIN) {
            sender.sendMessage(ChatColor.RED + "Nur Admins dürfen diesen Befehl nutzen!");
            return true;
        }

        if (args.length < 1 || args[0].equalsIgnoreCase("help")) {
            sendLiveOptions(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "setstreamer":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Benutzung: /live setstreamer <Streamer>");
                    return true;
                }
                plugin.setLiveStreamer(args[1]);
                sender.sendMessage(ChatColor.GREEN + "Streamer gesetzt: " + args[1]);
                break;
            case "settitel":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Benutzung: /live settitel <Titel>");
                    return true;
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    sb.append(args[i]).append(" ");
                }
                plugin.setLiveTitle(sb.toString().trim());
                sender.sendMessage(ChatColor.GREEN + "Live Titel gesetzt!");
                break;
            case "settitelcolor":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Benutzung: /live settitelcolor <Color>");
                    return true;
                }
                ChatColor color = LegendsPlugin.getColorFromString(args[1]);
                if (color == null) {
                    sender.sendMessage(ChatColor.RED + "Unbekannte Farbe! (Erlaubt: red, green, yellow, blue, aqua, dark_red, white, etc.)");
                    return true;
                }
                plugin.setLiveTitleColor(color);
                sender.sendMessage(ChatColor.GREEN + "Live Titel Farbe gesetzt!");
                break;
            case "allert":
                plugin.sendLiveAlert();
                sender.sendMessage(ChatColor.GREEN + "Live-Alert gesendet.");
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unbekannter Subcommand.");
                sendLiveOptions(sender);
                break;
        }
        return true;
    }
}