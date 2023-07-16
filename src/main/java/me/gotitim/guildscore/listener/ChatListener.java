package me.gotitim.guildscore.listener;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.guilds.Guild;
import me.gotitim.guildscore.placeholders.Placeholders;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ChatListener implements Listener {
    private static final Set<UUID> guildChatEnabled = new HashSet<>();
    private final GuildsCore plugin;

    public ChatListener(GuildsCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("deprecation")
    public void onChat(AsyncPlayerChatEvent event) {
        event.getPlayer().displayName(event.getPlayer().playerListName());
        event.getPlayer().setDisplayName(event.getPlayer().getPlayerListName());

        if(guildChatEnabled(event.getPlayer())) {
            event.setCancelled(true);
            Guild guild = plugin.getGuildManager().getGuild(event.getPlayer());
            if(guild == null) {
                event.getPlayer().sendMessage("Nie jesteś w gildii, więc nie możesz pisać na czacie gildii.");
                return;
            }
            Placeholders ph = new Placeholders();
            ph.set("message", event.getMessage());
            ph.setPlayer(event.getPlayer());
            guild.broadcast(MiniMessage.miniMessage().deserialize(ph.apply(plugin.getConfig().getString("guild_chat_format"))), false);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("deprecation")
    public void onCommand(PlayerCommandPreprocessEvent event) {
        event.getPlayer().displayName(event.getPlayer().playerListName());
        event.getPlayer().setDisplayName(event.getPlayer().getPlayerListName());
    }

    public static boolean guildChatEnabled(Player player) {
        return guildChatEnabled.contains(player.getUniqueId());
    }

    /**
     * @param player Player to toggle guild chat
     * @return New value
     */
    public static boolean toggleGuildChat(Player player) {
        if (guildChatEnabled.contains(player.getUniqueId())) {
            guildChatEnabled.remove(player.getUniqueId());
            return false;
        } else {
            guildChatEnabled.add(player.getUniqueId());
            return true;
        }
    }

    public static void enableGuildChat(Player player) {
        guildChatEnabled.add(player.getUniqueId());
    }
}
