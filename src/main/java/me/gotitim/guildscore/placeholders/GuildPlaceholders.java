package me.gotitim.guildscore.placeholders;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.guilds.Guild;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GuildPlaceholders extends Placeholders.PlaceholderPlugin {
    private final GuildsCore core;

    public GuildPlaceholders(GuildsCore core) {
        this.core = core;
    }
    @Override
    public @NotNull String getId() {
        return "guild";
    }

    @Override
    public Object apply(Player player, String parametersString) {
        Guild guild = core.getGuildManager().getGuild(player);
        return switch (parametersString) {
            case "name" ->
                    guild == null ? "Brak" : guild.getName();
            case "playercount" ->
                    guild == null ? 0 : guild.getPlayers().size();
            case "bank" ->
                    guild == null ? 0 : guild.getBank();
            default -> parametersString;
        };
    }
}
