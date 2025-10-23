package de.brackis.legends.commands;

import de.brackis.legends.LegendsPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class GlowCommand implements CommandExecutor {

    private final LegendsPlugin plugin;

    public GlowCommand(LegendsPlugin plugin) {
        this.plugin = plugin;
    }

    public static boolean hasGlowRank(LegendsPlugin.Rank rank) {
        return rank == LegendsPlugin.Rank.VIP
                || rank == LegendsPlugin.Rank.BUILDER
                || rank == LegendsPlugin.Rank.MOD
                || rank == LegendsPlugin.Rank.DEV
                || rank == LegendsPlugin.Rank.ADMIN;
    }

    // Setzt Glow-Farbe per Team, OHNE Prefix/Suffix/Teamfarbe im Namen!
    public static void setGlowTeam(Player player, LegendsPlugin.Rank rank) {
        removeGlowTeam(player); // Erst alte Teams entfernen
        if (!hasGlowRank(rank)) return;
        Scoreboard sb = player.getScoreboard();
        String teamName = "glow_" + player.getName();
        Team team = sb.getTeam(teamName);
        if (team == null) team = sb.registerNewTeam(teamName);
        team.addEntry(player.getName());
        team.setColor(getGlowColor(rank));
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER); // NameTag NICHT beeinflussen!
        team.setPrefix(""); // KEIN Prefix!
        team.setSuffix(""); // KEIN Suffix!
        player.setGlowing(true);
    }

    public static void removeGlowTeam(Player player) {
        Scoreboard sb = player.getScoreboard();
        String teamName = "glow_" + player.getName();
        Team team = sb.getTeam(teamName);
        if (team != null) {
            team.removeEntry(player.getName());
            team.unregister();
        }
        player.setGlowing(false);
    }

    private static ChatColor getGlowColor(LegendsPlugin.Rank rank) {
        switch (rank) {
            case VIP:     return ChatColor.LIGHT_PURPLE;
            case BUILDER: return ChatColor.DARK_GREEN;
            case MOD:     return ChatColor.GREEN;
            case DEV:     return ChatColor.AQUA;
            case ADMIN:   return ChatColor.DARK_RED;
            default:      return ChatColor.WHITE;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Nur Spieler können diesen Befehl ausführen.");
            return true;
        }

        Player player = (Player) sender;
        LegendsPlugin.Rank rank = LegendsPlugin.playerRanks.getOrDefault(player.getUniqueId(), LegendsPlugin.Rank.SPIELER);

        if (args.length != 1 || !(args[0].equalsIgnoreCase("true") || args[0].equalsIgnoreCase("false"))) {
            player.sendMessage(ChatColor.RED + "Benutzung: /glow true|false");
            return true;
        }

        boolean enable = args[0].equalsIgnoreCase("true");

        if (enable) {
            if (hasGlowRank(rank)) {
                setGlowTeam(player, rank);
                player.sendMessage(ChatColor.GRAY + "Dein Glow ist aktiviert!");
                LegendsPlugin.playerGlowStatus.put(player.getUniqueId(), true); // Glow als aktiv merken
            } else {
                player.sendMessage(ChatColor.RED + "Du hast keinen Rang mit Glow-Farbe!");
            }
        } else {
            removeGlowTeam(player);
            player.sendMessage(ChatColor.GRAY + "Dein Glow wurde deaktiviert.");
            LegendsPlugin.playerGlowStatus.put(player.getUniqueId(), false); // Glow als deaktiviert merken
        }

        return true;
    }
}