package me.gotitim.guildscore.listener;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.guilds.Guild;
import me.gotitim.guildscore.placeholders.Placeholders;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import static me.gotitim.guildscore.util.Components.parseRaw;

public class TpaListener implements Listener {
    private final GuildsCore plugin;

    public TpaListener(GuildsCore core) {
        plugin = core;
    }

    @EventHandler
    public void onDeathEvent(PlayerDeathEvent e) {
        Player p = e.getEntity();
        for (Guild g : plugin.getGuildManager().getGuilds().values()) {
            if(g.getPlayers().contains(p.getUniqueId())) continue;
            if(g.getHeart().affects(p.getLocation())) {
                plugin.tpaStorage.backCommandLocation.remove(p.getUniqueId());
                return;
            }
        }
        plugin.tpaStorage.backCommandLocation.put(p.getUniqueId(), p.getLocation());
        p.sendMessage(parseRaw("tpa.death", new Placeholders(p)));
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (this.plugin.tpaStorage.locationPlayers.containsKey(player.getUniqueId())
                && this.plugin.tpaStorage.locationPlayers.get(player.getUniqueId()).distance(player.getLocation()) > 2.0) {
            this.plugin.tpaStorage.locationPlayers.remove(player.getUniqueId());
            player.sendMessage(parseRaw("tpa.moved"));
        }

        if (this.plugin.tpaStorage.locationBackPlayers.containsKey(player.getUniqueId())
                && this.plugin.tpaStorage.locationBackPlayers.get(player.getUniqueId()).distance(player.getLocation()) > 2.0) {
            this.plugin.tpaStorage.locationBackPlayers.remove(player.getUniqueId());
            player.sendMessage(parseRaw("tpa.moved"));
        }

    }
}
