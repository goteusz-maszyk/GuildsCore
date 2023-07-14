package me.gotitim.guildscore.listener;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.guilds.Guild;
import me.gotitim.guildscore.item.GuildCompass;
import me.gotitim.guildscore.placeholders.Placeholders;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public final class PlayerJoinListener implements Listener {
    private final GuildsCore plugin;
    private final Map<UUID, BukkitTask> tablistTasks = new HashMap<>();

    public PlayerJoinListener(GuildsCore core) {
        plugin = core;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        tablistTasks.put(player.getUniqueId(), Bukkit.getScheduler().runTaskTimer(plugin, () -> updatePlayerTablist(player), 20*30, 20*30));
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            updatePlayerTablist(onlinePlayer);
        }
        player.getInventory().addItem(new GuildCompass(plugin).toItemStack()); // TODO: TESTING ONLY

        for (Guild guild : plugin.getGuildManager().getGuilds().values()) {
            if(!guild.getInvites().contains(player.getUniqueId()))continue;
            guild.sendInviteNotification(player, null);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            updatePlayerTablist(onlinePlayer);
        }
        BukkitTask task = tablistTasks.remove(event.getPlayer().getUniqueId());
        if(task != null) task.cancel();
    }

    private void updatePlayerTablist(Player player) {
        Placeholders placeholders = new Placeholders();
        placeholders.setPlayer(player);

        List<ComponentLike> header = new ArrayList<>();
        for (String row : plugin.getConfig().getStringList("tablist.header")) {
            header.add( MiniMessage.miniMessage().deserialize( placeholders.apply(row) ) );
        }

        List<ComponentLike> footer = new ArrayList<>();
        for (String row : plugin.getConfig().getStringList("tablist.footer")) {
            footer.add( MiniMessage.miniMessage().deserialize( placeholders.apply(row) ) );
        }

        player.sendPlayerListHeaderAndFooter(
                Component.join(JoinConfiguration.newlines(), header),
                Component.join(JoinConfiguration.newlines(), footer)
        );
    }
}
