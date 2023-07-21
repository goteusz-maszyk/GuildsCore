package me.gotitim.guildscore.listener;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.guilds.Guild;
import me.gotitim.guildscore.item.GuildHeartItem;
import me.gotitim.guildscore.placeholders.Placeholders;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import static me.gotitim.guildscore.util.Components.parseRaw;

public final class HitListener implements Listener {
    private final GuildsCore plugin;

    public HitListener(GuildsCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCrystalDamage(EntityDamageByEntityEvent e) {
        Guild guild = plugin.getGuildManager().getGuild(e.getEntity().getLocation());
        if(guild == null) return;
        if(e.getEntityType() != EntityType.ENDER_CRYSTAL) return;

        e.setCancelled(true);
        if (!(e.getDamager() instanceof Player player)) return;

        if(guild.getPlayers().contains(player.getUniqueId())) {
            e.getEntity().remove();
            guild.getHeart().pickup();
            player.getInventory().addItem(new GuildHeartItem(plugin).toItemStack());
            guild.broadcast(parseRaw("heart.pickup", new Placeholders(player)),
                    true);
            return;
        }
        if(!plugin.getConfig().getBoolean("allow_heart_destroy")) {
            player.sendMessage(parseRaw("heart.destroy_disabled"));
            return;
        }
        if(guild.getOnlinePlayers().size() > 0) {
            player.getWorld().strikeLightningEffect(e.getEntity().getLocation());
            e.getEntity().remove();
            guild.getHeart().pickup();
            Bukkit.broadcast(parseRaw("heart.destroy", new Placeholders(player).setValue("targetguild", guild)));
        } else {
            player.sendMessage(parseRaw("heart.nobody_online", new Placeholders(player)));
        }
    }
}
