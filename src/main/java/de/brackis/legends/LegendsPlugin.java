package de.brackis.legends;

import de.brackis.legends.commands.*;
import de.brackis.legends.items.*;
import de.brackis.legends.minigame.AlleGegenAlle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.*;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.block.Action;
import java.util.*;

public class LegendsPlugin extends JavaPlugin implements Listener {

    public enum Rank {
        SPIELER(ChatColor.GRAY, 0, "Spieler"),
        CREATOR(ChatColor.RED, 1, "Creator"),
        VIP(ChatColor.LIGHT_PURPLE, 2, "VIP"),
        BUILDER(ChatColor.DARK_GREEN, 3, "Builder"),
        MOD(ChatColor.GREEN, 4, "Mod"),
        DEV(ChatColor.AQUA, 5, "Dev"),
        ADMIN(ChatColor.DARK_RED, 6, "Admin");

        public final ChatColor color;
        public final int level;
        public final String displayName;
        Rank(ChatColor color, int level, String displayName) {
            this.color = color;
            this.level = level;
            this.displayName = displayName;
        }
    }

    public static final Map<UUID, Rank> playerRanks = new HashMap<>();
    public static final Map<UUID, Boolean> playerGlowStatus = new HashMap<>();
    private final Map<UUID, Integer> candleColorState = new HashMap<>();
    private static final Map<UUID, Long> lastEventTime = new HashMap<>();
    private static final Set<UUID> blockedPlayers = new HashSet<>();
    private static final Map<UUID, Integer> playerEventCount = new HashMap<>();
    private static final Set<UUID> activeHosts = new HashSet<>();
    private static final Map<UUID, String> activeHostGamemode = new HashMap<>();
    public final Map<UUID, Boolean> playerOnlineStatus = new HashMap<>();
    public static final Map<String, UUID> nameToUUID = new HashMap<>();
    public static final Map<String, Location> minigameLobbys = new HashMap<>();
    public static Location spawnLocation = null;

    // WatchSystem
    public static final Map<UUID, Set<UUID>> watchedPlayers = new HashMap<>();
    public static final Set<UUID> watchAllAdmins = new HashSet<>();

    // ShowLastCommand history
    public static final Map<UUID, LinkedList<String>> playerCommandHistory = new HashMap<>();
    public static final Map<UUID, LinkedList<String>> playerChatHistory = new HashMap<>();
    public static final Map<UUID, LinkedList<String>> playerWhisperHistory = new HashMap<>();
    public static final int MAX_HISTORY_SIZE = 50;

    // LIVE SYSTEM
    private String liveStreamer = null;
    private String liveTitle = null;
    private ChatColor liveTitleColor = ChatColor.RED;

    public static boolean eventsEnabled = true;
    private static long eventCooldown = 12 * 60 * 60 * 1000; // 12h

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("rang").setExecutor(new RangCommand());
        getCommand("glow").setExecutor(new GlowCommand(this));
        getCommand("live").setExecutor(new LiveCommand(this));
        getCommand("vanish").setExecutor(new VanishCommand(this));
        getCommand("fly").setExecutor(new FlyCommand(this));
        getCommand("checkrang").setExecutor(new CheckRangCommand());
        getCommand("watch").setExecutor(new WatchCommand());
        getCommand("unwatch").setExecutor(new UnwatchCommand());
        getCommand("showlast").setExecutor(new ShowLastCommand());
        getCommand("setlobby").setExecutor(this);
        getCommand("spawn").setExecutor(new SpawnCommand());
        getCommand("hub").setExecutor(new SpawnCommand());
        getCommand("setspawn").setExecutor(this);
        getCommand("pov").setExecutor(new PovCommand());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("setlobby")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Nur Spieler können diesen Befehl nutzen.");
                return true;
            }
            Player p = (Player) sender;
            Rank rank = playerRanks.getOrDefault(p.getUniqueId(), Rank.SPIELER);
            if (rank != Rank.ADMIN) {
                p.sendMessage("§cDu hast keine Rechte für diesen Befehl!");
                return true;
            }
            if (args.length != 1) {
                p.sendMessage("§cBenutzung: /setlobby <Minigame>");
                return true;
            }
            String name = args[0].toLowerCase();
            minigameLobbys.put(name, p.getLocation());
            p.sendMessage("§aDie Lobby für §e" + name + " §awurde erfolgreich auf deine aktuelle Position gesetzt!");
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("setspawn")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Nur Spieler können diesen Befehl nutzen.");
                return true;
            }
            Player p = (Player) sender;
            Rank rank = playerRanks.getOrDefault(p.getUniqueId(), Rank.SPIELER);
            if (rank != Rank.ADMIN) {
                p.sendMessage("§cDu hast keine Rechte für diesen Befehl!");
                return true;
            }
            spawnLocation = p.getLocation();
            p.sendMessage("§aDer Spawn wurde erfolgreich auf deine aktuelle Position gesetzt!");
            return true;
        }
        return false;
    }

    public static Location getLobbyLocation(String name) {
        return minigameLobbys.get(name.toLowerCase());
    }

    // --- LIVE SYSTEM API ---
    public void setLiveStreamer(String name) {
        this.liveStreamer = name;
        if (liveTitle == null || liveTitle.isEmpty()) {
            this.liveTitle = name + " ist Live";
        }
    }
    public void setLiveTitle(String title) {
        this.liveTitle = title;
    }
    public void setLiveTitleColor(ChatColor color) {
        this.liveTitleColor = color;
    }
    public String getLiveStreamer() {
        return this.liveStreamer;
    }
    public String getLiveTitle() {
        if (liveTitle == null || liveTitle.isEmpty()) {
            if (liveStreamer == null || liveStreamer.isEmpty())
                return null;
            return liveStreamer + " ist Live";
        }
        return liveTitle;
    }
    public ChatColor getLiveTitleColor() {
        return liveTitleColor == null ? ChatColor.RED : liveTitleColor;
    }
    public static ChatColor getColorFromString(String str) {
        switch (str.toLowerCase()) {
            case "red": return ChatColor.RED;
            case "green": return ChatColor.GREEN;
            case "yellow": return ChatColor.YELLOW;
            case "blue": return ChatColor.BLUE;
            case "aqua": return ChatColor.AQUA;
            case "dark_red": return ChatColor.DARK_RED;
            case "dark_green": return ChatColor.DARK_GREEN;
            case "white": return ChatColor.WHITE;
            case "light_purple": return ChatColor.LIGHT_PURPLE;
            case "dark_blue": return ChatColor.DARK_BLUE;
            case "gold": return ChatColor.GOLD;
            default: return null;
        }
    }
    public void sendLiveAlert() {
        String title = getLiveTitle();
        if (title == null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(ChatColor.RED + "Kein Streamer festgelegt! /live setstreamer <Name>");
            }
            return;
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle("", getLiveTitleColor() + title, 10, 70, 20);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        playerRanks.putIfAbsent(player.getUniqueId(), Rank.SPIELER);
        playerOnlineStatus.put(player.getUniqueId(), true);
        nameToUUID.put(player.getName().toLowerCase(), player.getUniqueId());

        Spielerkerze.give(player);
        CommandblockEinstellungen.give(player);
        Navigator.give(player);

        Rank rank = playerRanks.get(player.getUniqueId());
        Spielhost.give(player, rank.level >= Rank.CREATOR.level);

        updateEventBlock(player);
        giveEventDiamondOrHostSkull(player);

        updateTablistAndVisibility(player);

        if (spawnLocation != null) {
            player.teleport(spawnLocation);
        }

        boolean wantsGlow = playerGlowStatus.getOrDefault(player.getUniqueId(), false);
        GlowCommand.removeGlowTeam(player);
        if (wantsGlow && GlowCommand.hasGlowRank(rank)) {
            GlowCommand.setGlowTeam(player, rank);
        }

        for (UUID vanishedUUID : de.brackis.legends.commands.VanishCommand.vanishedPlayers) {
            Player vanished = Bukkit.getPlayer(vanishedUUID);
            if (vanished != null && !vanished.equals(player)) {
                boolean isJoiningVanished = de.brackis.legends.commands.VanishCommand.vanishedPlayers.contains(player.getUniqueId());
                if (!isJoiningVanished) {
                    player.hidePlayer(this, vanished);
                } else {
                    player.showPlayer(this, vanished);
                    vanished.showPlayer(this, player);
                }
            }
        }

        getLogger().info(player.getName() + " ist dem Server beigetreten.");
        event.setJoinMessage(null);
        candleColorState.put(player.getUniqueId(), 0);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player quitting = event.getPlayer();
        getLogger().info(quitting.getName() + " hat den Server verlassen.");
        event.setQuitMessage(null);
        activeHosts.remove(quitting.getUniqueId());
        activeHostGamemode.remove(quitting.getUniqueId());
        playerOnlineStatus.remove(quitting.getUniqueId());
        GlowCommand.removeGlowTeam(quitting);
        de.brackis.legends.commands.VanishCommand.vanishedPlayers.remove(quitting.getUniqueId());
    }

    private void giveEventDiamondOrHostSkull(Player player) {
        Rank rank = playerRanks.getOrDefault(player.getUniqueId(), Rank.SPIELER);
        if (activeHosts.contains(player.getUniqueId())) {
            ItemStack skull = new ItemStack(Material.SKELETON_SKULL);
            ItemMeta meta = skull.getItemMeta();
            meta.setDisplayName(ChatColor.RED + "Spiel beenden");
            skull.setItemMeta(meta);
            player.getInventory().setItem(8, skull);
        } else if (rank.level >= Rank.CREATOR.level) {
            ItemStack diamond = new ItemStack(Material.DIAMOND);
            ItemMeta meta = diamond.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + "Spielauswahl");
            diamond.setItemMeta(meta);
            player.getInventory().setItem(8, diamond);
        } else {
            ItemStack current = player.getInventory().getItem(8);
            if (current != null && (current.getType() == Material.DIAMOND || current.getType() == Material.SKELETON_SKULL)) {
                player.getInventory().setItem(8, null);
            }
        }
    }

    private void removeEventDiamondOrSkull(Player player) {
        ItemStack current = player.getInventory().getItem(8);
        if (current != null && (current.getType() == Material.DIAMOND || current.getType() == Material.SKELETON_SKULL)) {
            player.getInventory().setItem(8, null);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null) return;

        if (item.getType() == Material.COMMAND_BLOCK && item.hasItemMeta() &&
                (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            event.setCancelled(true);
            CommandblockEinstellungen.openStatusMenu(player);
            return;
        }

        if (item.getType() == Material.BARRIER && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Du hast keine Berechtigungen ein Spiel zu erstellen");
            return;
        }

        if (Navigator.isNavigator(item) && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            event.setCancelled(true);
            Navigator.openNavigatorMenu(player, activeHosts, playerRanks, activeHostGamemode);
            return;
        }

        if (item.getType() == Material.DIAMOND && item.hasItemMeta()
                && ChatColor.stripColor(item.getItemMeta().getDisplayName()).equals("Spielauswahl")
                && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            event.setCancelled(true);
            openEventMenu(player);
            return;
        }

        if (item.getType() == Material.SKELETON_SKULL && item.hasItemMeta()
                && ChatColor.stripColor(item.getItemMeta().getDisplayName()).equals("Spiel beenden")
                && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            event.setCancelled(true);
            endHostedGame(player);
            return;
        }

        if (Arrays.asList(Spielerkerze.CANDLE_COLORS).contains(item.getType())) {
            int current = candleColorState.getOrDefault(player.getUniqueId(), 0);
            int next = (current + 1) % Spielerkerze.CANDLE_COLORS.length;
            candleColorState.put(player.getUniqueId(), next);
            Spielerkerze.updateColor(player, next);
            Spielerkerze.updateVisibility(player, next, this);
            event.setCancelled(true);
            return;
        }

        if (isEventItem(item) && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            event.setCancelled(true);
            Rank rank = playerRanks.getOrDefault(player.getUniqueId(), Rank.SPIELER);
            if (blockedPlayers.contains(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "Du bist für Events gesperrt!");
                return;
            }
            if (!eventsEnabled) {
                player.sendMessage(ChatColor.RED + "Events sind aktuell deaktiviert!");
                return;
            }
            if (rank.level >= Rank.VIP.level) {
                boolean hasCooldown = !(rank == Rank.ADMIN || rank == Rank.DEV);
                long now = System.currentTimeMillis();
                long last = lastEventTime.getOrDefault(player.getUniqueId(), 0L);
                if (hasCooldown && (now - last < eventCooldown)) {
                    long restMillis = eventCooldown - (now - last);
                    player.sendMessage(ChatColor.RED + "Du kannst erst in " + formatMillis(restMillis) + " wieder ein Event veranstalten.");
                } else {
                    String coloredName = rank.color + "[" + rank.displayName + "] " + ChatColor.WHITE + player.getName() + ChatColor.GOLD;
                    Bukkit.broadcastMessage(ChatColor.GOLD + "Der Spieler " + coloredName + ChatColor.GOLD + " veranstaltet ein EVENT!");
                    if (hasCooldown) {
                        lastEventTime.put(player.getUniqueId(), now);
                    }
                    playerEventCount.put(player.getUniqueId(),
                            playerEventCount.getOrDefault(player.getUniqueId(), 0) + 1);
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                    }
                }
            } else {
                player.sendMessage(ChatColor.RED + "Du hast keine Berechtigungen um EVENTS zu veranstalten.");
            }
            return;
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = null;
        if (event.getWhoClicked() instanceof Player) {
            player = (Player) event.getWhoClicked();
            Rank rank = playerRanks.getOrDefault(player.getUniqueId(), Rank.SPIELER);

            String title = event.getView().getTitle();
            if (title.equals(ChatColor.GOLD + "Status") || title.equals(ChatColor.YELLOW + "Spielmodus-Auswahl")) {
                event.setCancelled(true);
                return;
            }

            if (rank == Rank.ADMIN || rank == Rank.DEV || rank == Rank.BUILDER) {
                return;
            }
        }
        CommandblockEinstellungen.handleStatusMenuClick(event, playerOnlineStatus, this);

        if (event.getWhoClicked() instanceof Player) {
            ItemStack item = event.getCurrentItem();
            if (isEventItem(item)) {
                event.setCancelled(true);
                return;
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Player player = null;
        if (event.getWhoClicked() instanceof Player) {
            player = (Player) event.getWhoClicked();
            Rank rank = playerRanks.getOrDefault(player.getUniqueId(), Rank.SPIELER);

            String title = event.getView().getTitle();
            if (title.equals(ChatColor.GOLD + "Status") || title.equals(ChatColor.YELLOW + "Spielmodus-Auswahl")) {
                event.setCancelled(true);
                return;
            }

            if (rank == Rank.ADMIN || rank == Rank.DEV || rank == Rank.BUILDER) {
                return;
            }
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Rank rank = playerRanks.getOrDefault(player.getUniqueId(), Rank.SPIELER);

        UUID uuid = player.getUniqueId();
        String msg = event.getMessage();
        playerChatHistory.computeIfAbsent(uuid, k -> new LinkedList<>());
        LinkedList<String> history = playerChatHistory.get(uuid);
        history.add(msg);
        if (history.size() > MAX_HISTORY_SIZE) history.removeFirst();

        event.setFormat(rank.color + "[" + rank.displayName + "] " + ChatColor.WHITE + player.getName() + ": " + event.getMessage());
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player sender = event.getPlayer();
        UUID senderUUID = sender.getUniqueId();

        String command = event.getMessage();
        playerCommandHistory.computeIfAbsent(senderUUID, k -> new LinkedList<>());
        LinkedList<String> history = playerCommandHistory.get(senderUUID);
        history.add(command);
        if (history.size() > MAX_HISTORY_SIZE) history.removeFirst();

        String cmd = command.toLowerCase();
        if (cmd.startsWith("/msg ") || cmd.startsWith("/w ") || cmd.startsWith("/whisper ") || cmd.startsWith("/tell ")) {
            playerWhisperHistory.computeIfAbsent(senderUUID, k -> new LinkedList<>());
            LinkedList<String> whisperHistory = playerWhisperHistory.get(senderUUID);
            whisperHistory.add(command);
            if (whisperHistory.size() > MAX_HISTORY_SIZE) whisperHistory.removeFirst();
        }

        for (UUID adminUUID : watchAllAdmins) {
            Player admin = Bukkit.getPlayer(adminUUID);
            if (admin != null && admin.isOnline()) {
                admin.sendMessage(ChatColor.DARK_RED + "[WatchSystem] "
                        + ChatColor.GOLD + "Der Spieler "
                        + ChatColor.WHITE + sender.getName()
                        + ChatColor.GOLD + " verwendet den Befehl: "
                        + ChatColor.GOLD + event.getMessage()
                        + ChatColor.GREEN + ".");
            }
        }
        for (Map.Entry<UUID, Set<UUID>> entry : watchedPlayers.entrySet()) {
            UUID adminUUID = entry.getKey();
            Set<UUID> watchedSet = entry.getValue();
            if (watchedSet.contains(senderUUID)) {
                Player admin = Bukkit.getPlayer(adminUUID);
                if (admin != null && admin.isOnline()) {
                    admin.sendMessage(ChatColor.DARK_RED + "[WatchSystem] "
                            + ChatColor.GOLD + "Der Spieler "
                            + ChatColor.WHITE + sender.getName()
                            + ChatColor.GOLD + " verwendet den Befehl: "
                            + ChatColor.GOLD + event.getMessage()
                            + ChatColor.GREEN + ".");
                }
            }
        }
    }

    public static void setTablistName(Player player, Rank rank) {
        player.setPlayerListName(rank.color + "[" + rank.displayName + "] " + ChatColor.WHITE + player.getName());
    }

    public static void setPlayerRank(Player player, Rank rank) {
        playerRanks.put(player.getUniqueId(), rank);
        setTablistName(player, rank);
        updateEventBlock(player);

        boolean wantsGlow = playerGlowStatus.getOrDefault(player.getUniqueId(), false);
        GlowCommand.removeGlowTeam(player);
        if (wantsGlow && GlowCommand.hasGlowRank(rank)) {
            GlowCommand.setGlowTeam(player, rank);
        }
    }

    public static void updateEventBlock(Player player) {
        Rank rank = playerRanks.getOrDefault(player.getUniqueId(), Rank.SPIELER);
        ItemStack current = player.getInventory().getItem(4);
        boolean hasEventBlock = current != null && isEventItem(current);

        for (int i = 0; i < 9; i++) {
            ItemStack slotItem = player.getInventory().getItem(i);
            if (slotItem != null && slotItem.getType() == Material.FIREWORK_ROCKET
                    && slotItem.hasItemMeta()
                    && ChatColor.stripColor(slotItem.getItemMeta().getDisplayName()).equals("Event erstellen")) {
                player.getInventory().setItem(i, null);
            }
        }

        if (rank.level >= Rank.VIP.level) {
            if (!hasEventBlock) {
                Event.give(player);
            }
        } else if (rank == Rank.CREATOR || rank == Rank.SPIELER) {
            if (hasEventBlock) {
                player.getInventory().setItem(4, null);
            }
        }
    }

    private static boolean isEventItem(ItemStack item) {
        return item != null
                && item.getType() == Material.GOLD_BLOCK
                && item.hasItemMeta()
                && item.getItemMeta().hasDisplayName()
                && ChatColor.stripColor(item.getItemMeta().getDisplayName()).equals("EVENT veranstalten");
    }

    public void openEventMenu(Player player) {
        org.bukkit.inventory.Inventory inv = Bukkit.createInventory(player, 9, ChatColor.YELLOW + "Spielmodus-Auswahl");
        ItemStack apple = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta metaApple = apple.getItemMeta();
        metaApple.setDisplayName(ChatColor.GREEN + "Alle gegen Alle");
        apple.setItemMeta(metaApple);
        inv.setItem(0, apple);

        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta metaSword = sword.getItemMeta();
        metaSword.setDisplayName(ChatColor.GREEN + "Alle gegen Einen");
        sword.setItemMeta(metaSword);
        inv.setItem(1, sword);

        player.openInventory(inv);
    }

    @EventHandler
    public void onEventMenuClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.YELLOW + "Spielmodus-Auswahl")) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            Rank rank = playerRanks.getOrDefault(player.getUniqueId(), Rank.SPIELER);
            String gamemode = null;
            if (event.getSlot() == 0) gamemode = "Alle gegen Alle";
            if (event.getSlot() == 1) gamemode = "Alle gegen Einen";
            if (gamemode != null) {
                player.closeInventory();
                sendEventStartMessage(player, rank, gamemode);
                activeHosts.add(player.getUniqueId());
                activeHostGamemode.put(player.getUniqueId(), gamemode);
                giveEventDiamondOrHostSkull(player);

                if (gamemode.equalsIgnoreCase("Alle gegen Alle")) {
                    AlleGegenAlle.teleportToLobby(player);
                }
            }
        }
    }

    public void endHostedGame(Player player) {
        if (activeHosts.contains(player.getUniqueId())) {
            String gamemode = activeHostGamemode.getOrDefault(player.getUniqueId(), "?");
            Rank rank = playerRanks.getOrDefault(player.getUniqueId(), Rank.SPIELER);
            Bukkit.broadcastMessage(ChatColor.RED + "Das Spiel '" + ChatColor.YELLOW + gamemode + ChatColor.RED + "' von " + rank.color + "[" + rank.displayName + "] " + ChatColor.WHITE + player.getName() + ChatColor.RED + " wurde beendet!");
            activeHosts.remove(player.getUniqueId());
            activeHostGamemode.remove(player.getUniqueId());
            giveEventDiamondOrHostSkull(player);
        }
    }

    public void sendEventStartMessage(Player host, Rank rank, String gamemodeName) {
        String hostPart = rank.color + "[" + rank.displayName + "] " + ChatColor.WHITE + host.getName() + ChatColor.RESET;
        String gamePart = ChatColor.YELLOW + gamemodeName;
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(ChatColor.GOLD + "Der Spieler " + hostPart + ChatColor.GOLD + " spielt " + gamePart);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();
        Rank rank = playerRanks.getOrDefault(player.getUniqueId(), Rank.SPIELER);

        if (rank == Rank.ADMIN || rank == Rank.DEV || rank == Rank.BUILDER) {
            return;
        }
        if (item != null && (item.getType() == Material.DIAMOND || item.getType() == Material.SKELETON_SKULL)) {
            event.setCancelled(true);
            return;
        }
        if (isBlockedItem(item)) event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Rank rank = playerRanks.getOrDefault(player.getUniqueId(), Rank.SPIELER);

        if (rank == Rank.ADMIN || rank == Rank.DEV || rank == Rank.BUILDER) {
            return;
        }
        event.setCancelled(true);
    }

    private String formatMillis(long millis) {
        long sec = millis / 1000;
        long min = sec / 60;
        long h = min / 60;
        long m = min % 60;
        return h + " Stunden " + m + " Minuten";
    }

    private boolean isBlockedItem(ItemStack item) {
        if (item == null) return false;
        Material type = item.getType();
        return type == Material.COMPASS ||
                Arrays.asList(Spielerkerze.CANDLE_COLORS).contains(type) ||
                type == Material.COMMAND_BLOCK ||
                type == Material.BARRIER;
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if (isEventItem(item)) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
    }

    public void updateTablistAndVisibility(Player player) {
        boolean isOnline = playerOnlineStatus.getOrDefault(player.getUniqueId(), true);
        Rank rank = playerRanks.getOrDefault(player.getUniqueId(), Rank.SPIELER);

        if (isOnline) {
            setTablistName(player, rank);
        } else {
            player.setPlayerListName("");
        }
    }

    public static Player getPlayerByName(String name) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().equalsIgnoreCase(name)) return p;
        }
        return null;
    }

    public static UUID getUUIDByName(String name) {
        if (name == null) return null;
        return nameToUUID.get(name.toLowerCase());
    }
}