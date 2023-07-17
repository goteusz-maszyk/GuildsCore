package me.gotitim.guildscore.guilds;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.item.GuildHeartItem;
import me.gotitim.guildscore.item.ItemBuilder;
import me.gotitim.guildscore.placeholders.Placeholders;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static me.gotitim.guildscore.listener.HeartListener.resetTasks;
import static me.gotitim.guildscore.util.Components.*;

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

        player.getInventory().addItem(new GuildHeartItem(getGuildManager().getPlugin()).toItemStack());

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

    public @NotNull Material getIcon() {
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
        Placeholders ph = new Placeholders(player);
        if (!invites.contains(player.getUniqueId()) && players.size() > 0) {
            player.sendMessage(parseRaw("join.no_invite", ph.setValue("new_guild", this)));
            return;
        }
        resetTasks(player);

        invites.remove(player.getUniqueId());
        players.add(player.getUniqueId());
        bukkitTeam.addPlayer(player);
        config.set("players", mapUUIDs());

        broadcast(parseRaw("join.notification", ph), true);
    }

    public void removePlayer(Player player) {
        broadcast(parseRaw("guild_command.leave_notification", new Placeholders(player)), true);
        resetTasks(player);

        players.remove(player.getUniqueId());
        bukkitTeam.removePlayer(player);

        config.set("players", mapUUIDs());
    }

    public void invitePlayer(OfflinePlayer target, @NotNull OfflinePlayer inviter, boolean notify) {
        invites.add(target.getUniqueId());
        Player targetPlayer = target.getPlayer();
        if (!notify || targetPlayer == null) return;

        sendInviteNotification(targetPlayer, inviter);

        broadcast(parseRaw("join.invite_notification", new Placeholders(targetPlayer).setValue("inviter", inviter)), true);
    }

    public void sendInviteNotification(Player target, @Nullable OfflinePlayer inviter) {
        if(!invites.contains(target.getUniqueId())) return;
        Placeholders ph = new Placeholders(target).setValue("inviter", inviter).setValue("new_guild", this);

        if(inviter != null) target.sendMessage(parseRaw("join.invited_inviter", ph));
        else target.sendMessage(parseRaw("join.invited_no_inviter", ph));
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
        Bukkit.getConsoleSender().sendMessage("[Guild " + id + "] >> " + legacyColors(message));
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
        return loreComponent(name).color(NamedTextColor.GOLD);
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

    public ItemStack getAsIcon() {
        ItemBuilder ic = new ItemBuilder(icon)
                .setName(getNameComponent())
                .addLoreLine(loreComponentRaw("guild.tooltip_members"))
                .setPersistentData(guildManager.getPlugin().guildIdKey, PersistentDataType.STRING, id);

        for (UUID uuid : players) {
            ic.addLoreLine(loreComponent(Bukkit.getOfflinePlayer(uuid).getName()).color(NamedTextColor.AQUA));
        }
        return ic.toItemStack();
    }

    public NamedTextColor getColor() {
        return color;
    }

    public List<Player> getOnlinePlayers() {
        List<Player> online = new ArrayList<>();
        for (UUID player : players) {
            if(Bukkit.getPlayer(player) != null) online.add(Bukkit.getPlayer(player));
        }
        return online;
    }
}
