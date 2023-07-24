package me.gotitim.guildscore.util;

import me.gotitim.guildscore.GuildsCore;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

public class CustomConfig extends YamlConfiguration {
    private final File file;
    private final boolean allowSave;
    private boolean loaded;

    public CustomConfig(File file, boolean allowSave) {
        this.file = file;
        this.allowSave = allowSave;
        this.loaded = false;
    }

    public static CustomConfig setup(GuildsCore core, String name, boolean allowSave) {
        File file = new File(core.getDataFolder(), name + ".yml");
        CustomConfig config = new CustomConfig(file, allowSave);
        if(!file.exists()) {
            core.getLogger().info("Copying messages!");
            core.saveResource(name + ".yml", false);
        }
        config.setup();
        return config;
    }

    protected void setup() {
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Bukkit.getLogger().severe("Failed to create " + file.getName() + " config file!");
                e.printStackTrace();
            }
        }
        reload();
    }

    public void save() {
        if(!loaded) return;
        if(!allowSave) {
            throw new IllegalStateException("[GuildsCore] tried to save " + file.getName() + ", while it's a non-saving configuration!");
        }
        try {
            save(file);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to save " + file.getName() + " config file!");
            e.printStackTrace();
        }
    }

    public void reload() {
        try {
            loaded = false;
            load(file);
            loaded = true;
        } catch (IOException | InvalidConfigurationException e) {
            Bukkit.getLogger().severe("Failed to load " + file.getName() + " config file!");
            e.printStackTrace();
        }
    }

    @Override
    public void set(@NotNull String path, @Nullable Object value) {
        super.set(path, value);
        save();
    }

    public File getFile() {
        return file;
    }
}
