package me.gotitim.guildscore.placeholders;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class PlayerPlaceholders extends Placeholders.PlaceholderPlugin {
    @Override
    public @NotNull String getId() {
        return "player";
    }

    @Override
    public @NotNull List<String> getAliases() {
        return List.of("inviter", "targetplayer");
    }

    @Override
    public Object apply(Player p, @NotNull String alias, @NotNull String parametersString, @NotNull Map<String, Object> placeholderValues) {
        OfflinePlayer offlinePlayer;
        if (alias.equals("inviter") && placeholderValues.get("inviter") instanceof OfflinePlayer ip) {
            offlinePlayer = ip;
        } else if (alias.equals("targetplayer") && placeholderValues.get("targetplayer") instanceof OfflinePlayer tp) {
            offlinePlayer = tp;
        } else {
            offlinePlayer = p;
        }
        Player player = offlinePlayer.getPlayer();
        return switch (parametersString) {
            case "name" ->
                offlinePlayer.getName();
            case "uuid" ->
                offlinePlayer.getUniqueId();

            case "x" ->
                player == null ? null : (int) player.getLocation().getX();
            case "y" ->
                    player == null ? null : (int) player.getLocation().getY();
            case "z" ->
                    player == null ? null : (int) player.getLocation().getZ();

            case "ping" ->
                    player == null ? null : player.getPing();
            default -> parametersString;
        };
    }
}
