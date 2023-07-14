package me.gotitim.guildscore.listener;

import me.gotitim.guildscore.GuildsCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public final class ChatListener implements Listener {

    private final GuildsCore plugin;

    public ChatListener(GuildsCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("deprecation")
    public void onChat(AsyncPlayerChatEvent event) {
        event.getPlayer().displayName(event.getPlayer().playerListName());
        event.getPlayer().setDisplayName(event.getPlayer().getPlayerListName());
    }
    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("deprecation")
    public void onCommand(PlayerCommandPreprocessEvent event) {
        event.getPlayer().displayName(event.getPlayer().playerListName());
        event.getPlayer().setDisplayName(event.getPlayer().getPlayerListName());
    }
}
