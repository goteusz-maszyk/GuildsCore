package me.gotitim.guildscore.placeholders;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.guilds.Guild;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

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
    public @NotNull List<String> getAliases() {
        return List.of("newguild", "targetguild");
    }

    @Override
    public Object apply(Player player, @NotNull String alias, @NotNull String parametersString, @NotNull Map<String, Object> placeholderValues) {
        Guild guild;
        if (alias.equals("newguild") && placeholderValues.get("new_guild") instanceof Guild) {
            guild = (Guild) placeholderValues.get("new_guild");
        } else if(alias.equals("targetguild") && placeholderValues.get("targetguild") instanceof Guild) {
            guild = (Guild) placeholderValues.get("targetguild");
        } else {
            guild = core.getGuildManager().getGuild(player);
        }
        return switch (parametersString) {
            case "id" ->
                guild == null ? "Brak" : guild.getId();
            case "name" ->
                guild == null ? "Brak" : guild.getName();
            case "playercount" ->
                guild == null ? 0 : guild.getPlayers().size();
            case "bank" ->
                guild == null ? 0 : guild.getBank();
            case "color" ->
                guild == null ? "white" : guild.getColor().toString();
            case "icon_id" ->
                guild == null ? Material.COBBLESTONE.key().asString() : guild.getIcon().key().asString();
            case "icon" ->
                guild == null ? Material.COBBLESTONE : guild.getIcon();

            default -> parametersString;
        };
    }
}
