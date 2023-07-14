package me.gotitim.guildscore.guilds;

import me.gotitim.guildscore.GuildsCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GuildManager {
    private final GuildsCore plugin;
    private Scoreboard mainScoreboard;
    private final Map<String, Guild> guilds = new HashMap<>();
    private File guildsDir;

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
    }

    public @NotNull Team getOrCreateTeam(@NotNull String name) {
        return Optional.ofNullable(mainScoreboard.getTeam(name)).orElseGet(() -> mainScoreboard.registerNewTeam(name));
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
    public @Nullable Guild getGuild(Player player) {
        return getGuild((OfflinePlayer) player);
    }

    public Guild createGuild(String guildId, String name, Player player) throws IllegalStateException {
        if (guilds.containsKey(guildId)) throw new IllegalStateException("Guild ID already in use!");

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
}
