package me.gotitim.guildscore.guilds;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.item.GuildHeartItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Guild {
    public static final Map<Material, Integer> BANK_MATERIALS = new HashMap<>();
    private final String id;
    private String name;
    private Component prefix = Component.empty();
    private Component suffix = Component.empty();
    private @NotNull Material icon = Material.COBBLESTONE;

    private final List<UUID> players = new ArrayList<>();
    private Team bukkitTeam;
    private final GuildHeart heart;

    private final GuildConfiguration config;
    private final GuildManager guildManager;
    private final List<UUID> invites = new ArrayList<>();
    private NamedTextColor color;
    private int bank;

    /**
     * Creates a new guild, used in a /guild create command
     *
     * @param id Permanent guild id
     * @param name Starting guild name
     * @param player Player that created the guild
     * @param guildManager Current guild manager instance
     * @throws IllegalArgumentException when an id is already in use
     */
    Guild(String id, String name, Player player, GuildManager guildManager) {
        this.guildManager = guildManager;
        this.id = id;
        this.config = GuildConfiguration.setup(this);
        this.name = name;
        players.add(player.getUniqueId());

        config.set("id", id);
        config.set("name", name);
        config.set("players", mapUUIDs());

        this.heart = new GuildHeart(this);

        player.getInventory().addItem(new GuildHeartItem(this).toItemStack());

        setupBukkitTeam();
    }

    /**
     * Loads a guild from provided guild config
     * @param config Guild config
     * @param guildManager Current guild manager instance
     */
    Guild(@NotNull GuildConfiguration config, GuildManager guildManager) {
        this.id = config.getString("id");
        this.config = config;
        this.guildManager = guildManager;
        this.heart = new GuildHeart(this);

        loadConfig();
        setupBukkitTeam();
    }

    public void setupBukkitTeam() {
        bukkitTeam = guildManager.getOrCreateTeam(id);
        players.forEach(player -> bukkitTeam.addPlayer(Bukkit.getOfflinePlayer(player)));

        bukkitTeam.prefix(prefix);
        bukkitTeam.suffix(suffix);
        bukkitTeam.color(color);

        bukkitTeam.displayName(Component.text(name));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        bukkitTeam.displayName(Component.text(name));
        config.set("name", name);
    }

    public GuildHeart getHeart() {
        return heart;
    }

    public Material getIcon() {
        return icon;
    }

    public void setIcon(Material icon) {
        this.icon = Objects.requireNonNullElse(icon, Material.COBBLESTONE);
        config.set("icon", Objects.requireNonNullElse(icon, Material.COBBLESTONE).name());
    }

    /**
     * @return A copy of guild player list
     */
    public List<UUID> getPlayers() {
        return new ArrayList<>(players);
    }

    public String getId() {
        return id;
    }
    public GuildManager getGuildManager() {
        return guildManager;
    }

    public GuildConfiguration getConfig() {
        return config;
    }

    /**
     * Requires invite
     *
     * @param player Player to add
     * @see Guild#invitePlayer(OfflinePlayer, OfflinePlayer, boolean)
     */
    public void addPlayer(Player player) {
        if (!invites.contains(player.getUniqueId()) && players.size() > 0) {
            player.sendMessage(
                    Component.text("Nie masz zaproszenia do gildii ").color(NamedTextColor.RED)
                            .append(Component.text(name).color(NamedTextColor.GOLD)));
            return;
        }

        invites.remove(player.getUniqueId());
        players.add(player.getUniqueId());

        broadcast(Component.text("Gracz ").color(NamedTextColor.GREEN)
                .append(Component.text(player.getName()).color(NamedTextColor.AQUA))
                .append(Component.text(" dołączył do gildii!")),
        true);
        bukkitTeam.addPlayer(player);
        config.set("players", mapUUIDs());
    }

    public void removePlayer(Player player) {
        broadcast(Component.text("Gracz ").color(NamedTextColor.GREEN)
                        .append(Component.text(player.getName()).color(NamedTextColor.AQUA))
                        .append(Component.text(" opuszcza gildię!")),
                true);

        players.remove(player.getUniqueId());
        bukkitTeam.removePlayer(player);

        config.set("players", mapUUIDs());
    }

    public void invitePlayer(OfflinePlayer target, @NotNull OfflinePlayer inviter, boolean notify) {
        invites.add(target.getUniqueId());
        if(notify && target.isOnline()) sendInviteNotification(target.getPlayer(), inviter);

        broadcast(Component.text(inviter.getName()).color(NamedTextColor.AQUA)
                .append(Component.text(" zaprosił gracza ").color(NamedTextColor.GREEN))
                .append(Component.text(target.getName()).color(NamedTextColor.AQUA))
                .append(Component.text(" do gildii!").color(NamedTextColor.GREEN)), true);
    }

    public void sendInviteNotification(Player target, @Nullable OfflinePlayer inviter) {
        if(!invites.contains(target.getUniqueId())) return;
        Component message = Component.text("Zostałeś zaproszony").color(NamedTextColor.GREEN);
        if(inviter != null) {
            message = message
                    .append(Component.text(" przez ").color(NamedTextColor.GREEN))
                    .append(Component.text(inviter.getName()).color(NamedTextColor.AQUA));
        }
        message = message
                .append(Component.text(" do gidii ").color(NamedTextColor.GREEN))
                .append(Component.text(name).color(NamedTextColor.GOLD))
                .append(Component.text(". KLIKNIJ TUTAJ BY DOŁĄCZYĆ").color(NamedTextColor.BLUE)
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/guild join " + id))
                        .hoverEvent(HoverEvent.showText(Component.text("/guild join " + id)))
                );

        target.sendMessage(message);
    }

    private List<String> mapUUIDs() {
        return players.stream().map(UUID::toString).toList();
    }

    public void loadConfig() {
        this.name = config.getString("name");
        this.heart.loadConfig();
        this.prefix = MiniMessage.miniMessage().deserialize(config.getString("prefix", ""));
        this.suffix = MiniMessage.miniMessage().deserialize(config.getString("suffix", ""));
        this.color = Optional.ofNullable(config.getString("color")).map(NamedTextColor.NAMES::value).orElse(null);
        this.icon = Objects.requireNonNullElse(Material.getMaterial(config.getString("icon")), Material.COBBLESTONE);
        this.bank = config.getInt("bank");

        this.players.clear();
        for (String uuid : config.getStringList("players")) {
            this.players.add(UUID.fromString(uuid));
        }
        setupBukkitTeam();
    }

    public void broadcast(Component message, boolean decorate) {
        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;

            if(decorate) player.sendMessage(GuildsCore.MESSAGE_DECORATOR);
            player.sendMessage(message);
            if(decorate) player.sendMessage(GuildsCore.MESSAGE_DECORATOR);
        }
    }

    public void broadcastTitle(Title message) {
        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;

            player.showTitle(message);
        }
    }

    public void setPrefix(Component prefix) {
        this.prefix = prefix;
        bukkitTeam.prefix(prefix);
        config.set("prefix", MiniMessage.miniMessage().serialize(prefix));
    }

    public void setSuffix(Component suffix) {
        this.suffix = suffix;
        bukkitTeam.suffix(suffix);
        config.set("suffix", MiniMessage.miniMessage().serialize(suffix));
    }

    public void setColor(@Nullable NamedTextColor color) {
        this.color = color;
        bukkitTeam.color(color);
        config.set("color", color == null ? null : color.toString());
    }

    public void delete() {
        this.bukkitTeam.unregister();
        this.config.getFile().delete();
        this.guildManager.getGuilds().remove(id);
    }

    public List<UUID> getInvites() {
        return invites;
    }

    public Component getNameComponent() {
        return Component.text(name).color(NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false);
    }

    public int getBank() {
        return bank;
    }

    public void bankWithdraw(int amount) {
        bank -= amount;
        config.set("bank", bank);
    }

    public void bankDeposit(int amount) {
        bank += amount;
        config.set("bank", bank);
    }

}
