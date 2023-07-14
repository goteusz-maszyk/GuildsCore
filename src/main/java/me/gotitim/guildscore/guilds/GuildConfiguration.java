package me.gotitim.guildscore.guilds;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

public class GuildConfiguration extends YamlConfiguration {
    private final File file;
    private Guild guild = null;

    private GuildConfiguration(File file) {
        this.file = file;
    }

    @Override
    public void set(@NotNull String path, @Nullable Object value) {
        super.set(path, value);
        save();
    }

    public static GuildConfiguration setup(File file, Guild guild) {
        GuildConfiguration guildConfiguration = new GuildConfiguration(file);
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Bukkit.getLogger().severe("Failed to create guild config file!");
            }
        }
        try {
            guildConfiguration.load(file);
        } catch (Exception e) {
            Bukkit.getLogger().severe("Failed to load guild config file!");
            e.printStackTrace();
        }
        guildConfiguration.setGuild(guild);
        return guildConfiguration;
    }

    public static GuildConfiguration setup(Guild guild) {
        File file = new File(guild.getGuildManager().getGuildsDir(), guild.getId() + ".yml");
        return setup(file, guild);
    }

    public void save() {
        try {
            save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void reload() {
        try {
            load(file);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
        this.guild.loadConfig();
    }

    public void setGuild(Guild guild) {
        this.guild = guild;
    }

    public File getFile() {
        return file;
    }
}
