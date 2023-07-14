package me.gotitim.guildscore.placeholders;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ServerPlaceholders extends Placeholders.PlaceholderPlugin {
    @Override
    public @NotNull String getId() {
        return "server";
    }

    @Override
    public Object apply(Player player, String parametersString) {
        return switch (parametersString) {
            case "onlinecount" ->
                    Bukkit.getOnlinePlayers().size();
            case "max_players" ->
                    Bukkit.getMaxPlayers();
            case "tps" ->
                    Bukkit.getServer().getTPS()[0];

            case "ram_used" ->
                    ramEfficientUnit(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
            case "ram_free" ->
                    ramEfficientUnit(Runtime.getRuntime().freeMemory());
            case "ram_total" ->
                    ramEfficientUnit(Runtime.getRuntime().totalMemory());
            case "ram_max" ->
                    ramEfficientUnit(Runtime.getRuntime().maxMemory());
            default -> parametersString;
        };
    }

    private String ramEfficientUnit(Long memoryBytes) {
        final int kB = (int) Math.pow(1024, 1);
        final int MB = (int) Math.pow(1024, 2);
        final int GB = (int) Math.pow(1024, 3);

        if(memoryBytes > GB) return (memoryBytes / GB) + "GB";
        if(memoryBytes > MB) return (memoryBytes / MB) + "MB";
        if(memoryBytes > kB) return (memoryBytes / kB) + "kB";
        return memoryBytes + "B";
    }
}
