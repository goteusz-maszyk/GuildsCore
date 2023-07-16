package me.gotitim.guildscore.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.List;

public abstract class Command extends BukkitCommand {
    public Command(@NotNull String name, String... aliases) {
        super(name);
        setAliases(List.of(aliases));

        try {
            Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            CommandMap commandMap = (CommandMap) field.get(Bukkit.getServer());
            commandMap.register("guildscore", this);
        } catch (Exception e) {
            Bukkit.getLogger().warning("Failed to load command!");
        }
    }
}
