package me.gotitim.guildscore.guilds;

import me.gotitim.guildscore.GuildsCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class GuildManager {
    private final GuildsCore plugin;
    private Scoreboard mainScoreboard;
    private final Map<String, Guild> guilds = new HashMap<>();
    private File guildsDir;
    private final List<UUID> awaitingUUIDs = new ArrayList<>();

    public GuildManager(GuildsCore gildieSokowy) {
        plugin = gildieSokowy;
    }

    public void init() {
        mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        Path dirPath = Path.of(plugin.getDataFolder().getPath(), "guilds");
        if(!new File(dirPath.toUri()).exists()) {
            try {
                Files.createDirectory(dirPath);
            } catch (IOException e) {
                plugin.getLogger().warning("Directory or file already exists in plugins/GuildsCore/guilds!");
                e.printStackTrace();
            }
        }

        guildsDir = new File(dirPath.toUri());
        for (File file : Optional.ofNullable(guildsDir.listFiles()).orElse(new File[]{})) {
            GuildConfiguration gc = GuildConfiguration.setup(file, null);
            Guild guild = new Guild(gc, this);
            guilds.put(gc.getString("id").split("\\.")[0], guild);
            gc.setGuild(guild);
        }

        for (String id : plugin.getConfig().getConfigurationSection("bank_materials").getKeys(false)) {
            Material material = Material.getMaterial(id);

            if (material == null) plugin.getLogger().warning("Found unknown material in bank_materials config: " + id);
            else Guild.BANK_MATERIALS.put(material, plugin.getConfig().getInt("bank_materials." + id));
        }
    }

    public @NotNull Team getOrCreateTeam(@NotNull String name) {
        return Objects.requireNonNullElseGet(mainScoreboard.getTeam(name), () -> mainScoreboard.registerNewTeam(name));
    }

    public Map<String, Guild> getGuilds() {
        return guilds;
    }

    public Guild getGuild(String guildId) {
        return guilds.get(guildId);
    }

    public @Nullable Guild getGuild(OfflinePlayer player) {
        for (Guild guild : guilds.values()) {
            if(guild.getPlayers().contains(player.getUniqueId())) return guild;
        }
        return null;
    }

    public Guild createGuild(@NotNull String name, @NotNull Player player) throws IllegalStateException {
        String guildId = name.toLowerCase().replaceAll(" ", "_");
        if (guilds.containsKey(guildId)) {
            guildId = guildId.replaceAll("_", "");
            if (guilds.containsKey(guildId)) throw new IllegalStateException("Couldn't create guild ID for name " + name);
        }

        Guild guild = new Guild(guildId, name, player, this);
        guilds.put(guildId, guild);
        return guild;
    }

    public void saveAll() {
        for (Guild guild : guilds.values()) {
            guild.getConfig().save();
        }
    }

    public GuildsCore getPlugin() {
        return plugin;
    }

    public File getGuildsDir() {
        return guildsDir;
    }

    public Guild getGuild(Location heartLocation) {
        for (Guild guild : guilds.values()) {
            if(guild.getHeart().getLocation().equals(heartLocation)) return guild;
        }
        return null;
    }

    public void addAwaitingUUID(UUID player) {
        awaitingUUIDs.add(player);
    }
    public void joinAwaitingUUID(Player player) {
        if(!awaitingUUIDs.remove(player.getUniqueId())) return;
        Guild guild = getGuild(player);
        if(guild != null) {
            guild.fixPlayer(player);
        }
    }

    /**
     * @param player Player to check
     * @param chunk Should whole chunk or just player location be checked?
     * @return guild
     */
    public @Nullable Guild negativeHeartAffect(Player player, boolean chunk) {
        for (Guild guild : guilds.values()) {
            if(guild.getPlayers().contains(player.getUniqueId())) continue;
            if (chunk && guild.getHeart().affects(player.getChunk()) || !chunk && guild.getHeart().affects(player.getLocation())) {
                return guild;
            }
        }
        return null;
    }
}
