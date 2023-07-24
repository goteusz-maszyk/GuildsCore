package me.gotitim.guildscore.guilds;

import me.gotitim.guildscore.util.CustomConfig;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;

public class GuildConfiguration extends CustomConfig {
    private @MonotonicNonNull Guild guild = null;

    private GuildConfiguration(File file) {
        super(file, true);
    }

    public static GuildConfiguration setup(File file, Guild guild) {
        GuildConfiguration guildConfiguration = new GuildConfiguration(file);
        guildConfiguration.setGuild(guild);
        guildConfiguration.setup();
        return guildConfiguration;
    }

    public static GuildConfiguration setup(Guild guild) {
        File file = new File(guild.getGuildManager().getGuildsDir(), guild.getId() + ".yml");
        return setup(file, guild);
    }
    public void reload() {
        super.reload();
        if(this.guild != null) {
            this.guild.loadConfig();
        }
    }

    public void setGuild(@NonNull Guild guild) {
        this.guild = guild;
    }
}
