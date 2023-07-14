package me.gotitim.guildscore;

import me.gotitim.guildscore.commands.GuildCommand;
import me.gotitim.guildscore.commands.ShopCommand;
import me.gotitim.guildscore.guilds.GuildManager;
import me.gotitim.guildscore.listener.*;
import me.gotitim.guildscore.placeholders.GuildPlaceholders;
import me.gotitim.guildscore.placeholders.Placeholders;
import me.gotitim.guildscore.placeholders.PlayerPlaceholders;
import me.gotitim.guildscore.placeholders.ServerPlaceholders;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;


public final class GuildsCore extends JavaPlugin {
    public static final Component MESSAGE_DECORATOR = Component.text("-----------------------------------------------------").color(NamedTextColor.AQUA);
    private final GuildManager guildManager;
    public final NamespacedKey itemIdKey = new NamespacedKey(this, "customitem");

    public GuildsCore() {
        this.guildManager = new GuildManager(this);
    }

    @Override
    public void onEnable() {
        if(!getDataFolder().exists()) {
            try {
                Files.createDirectory(getDataFolder().toPath());
            } catch (IOException e) {
                getLogger().severe("Failed to create plugin data folder!");
                e.printStackTrace();
            }
        }
        this.guildManager.init();

        try { registerListeners(
                InteractListener.class,
                InventoryClickListener.class,
                PlayerJoinListener.class,
                ChatListener.class,
                HitListener.class); } catch (Exception ignored) {}

        new ServerPlaceholders().register();
        new PlayerPlaceholders().register();
        new GuildPlaceholders(this).register();

        new GuildCommand(this);
        new ShopCommand(this);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            new PlayerJoinListener(this).onJoin(new PlayerJoinEvent(onlinePlayer, Component.text("")));
        }

        getServer().getPluginManager().addPermission(new Permission("guildscore.save", PermissionDefault.OP));
        getServer().getPluginManager().addPermission(new Permission("guildscore.load", PermissionDefault.OP));
    }


    @Override
    public void onDisable() {
        Placeholders.clearPlugins();
        guildManager.saveAll();
    }

    public GuildManager getGuildManager() {
        return guildManager;
    }

    @SafeVarargs
    private void registerListeners(Class<? extends Listener>... listeners) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        PluginManager pm = getServer().getPluginManager();
        for (Class<? extends Listener> listenerClass : listeners) {
            pm.registerEvents(
                    listenerClass.getConstructor(GuildsCore.class).newInstance(this),
                    this
            );
        }
    }

    public Component getNoPermissionMessage() {
        return Component.text("Nie masz uprawnień, by to zrobić!").color(NamedTextColor.RED);
    }
}
