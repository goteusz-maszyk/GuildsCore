package me.gotitim.guildscore.placeholders;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.guilds.Guild;
import me.gotitim.guildscore.guilds.HeartUpgrade;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;

public class GuildUpgradePlaceholders extends Placeholders.PlaceholderPlugin {
    private final GuildsCore plugin;

    public GuildUpgradePlaceholders(GuildsCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getId() {
        return "upgrade";
    }

    @Override
    public Object apply(Player player, @NotNull String alias, @NotNull String parametersString, @NotNull Map<String, Object> placeholderValues) {
        String[] split = parametersString.split("_");
        String function = split[0];
        String upgradeName = String.join("_", Arrays.copyOfRange(split, 1, split.length));
        if(HeartUpgrade.valueOf(upgradeName) == null) return null;
        if(function.equals("name")) {
            return HeartUpgrade.valueOf(upgradeName).toString();
        } else if (function.equals("level")) {
            Guild guild = plugin.getGuildManager().getGuild(player);
            if(guild == null) return null;
            return guild.getHeart().getUpgrade(HeartUpgrade.valueOf(upgradeName));
        } else if (function.equals("cost")) {
            Guild guild = plugin.getGuildManager().getGuild(player);
            if(guild == null) return null;
            HeartUpgrade upgrade = HeartUpgrade.valueOf(upgradeName);
            return upgrade.getLevelPrice(guild.getHeart().getUpgrade(upgrade));
        }
        return null;
    }
}
