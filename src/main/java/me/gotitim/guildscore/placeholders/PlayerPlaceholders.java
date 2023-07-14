package me.gotitim.guildscore.placeholders;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerPlaceholders extends Placeholders.PlaceholderPlugin {
    @Override
    public @NotNull String getId() {
        return "player";
    }

    @Override
    public Object apply(Player player, String parametersString) {
        return switch (parametersString) {
            case "name" ->
                player.getName();
            case "uuid" ->
                player.getUniqueId();

            case "x" ->
                player.getLocation().getX();
            case "y" ->
                player.getLocation().getY();
            case "z" ->
                player.getLocation().getZ();

            case "ping" ->
                player.getPing();
            default -> parametersString;
        };
    }
}
