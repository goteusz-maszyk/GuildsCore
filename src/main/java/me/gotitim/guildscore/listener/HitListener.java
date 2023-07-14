package me.gotitim.guildscore.listener;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.guilds.Guild;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public final class HitListener implements Listener {
    private final GuildsCore plugin;

    public HitListener(GuildsCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onHit(EntityExplodeEvent e){
        Guild guild = plugin.getGuildManager().getGuild(e.getEntity().getLocation());
        if(guild == null) return;
        if(e.getEntityType() != EntityType.ENDER_CRYSTAL) return;

        e.setCancelled(true);

        Player player = (Player) e.getEntity().getLastDamageCause().getEntity();
        if(guild.getPlayers().contains(player.getUniqueId())) {
            return;
        }
        Bukkit.broadcast(Component.text("Serce gildii ").color(NamedTextColor.DARK_GREEN)
                .append(Component.text(guild.getName()).color(NamedTextColor.GOLD))
                .append(Component.text(" zostało zniszczone przez ").color(NamedTextColor.DARK_GREEN))
                .append(player.name()).color(NamedTextColor.AQUA));

        e.getEntity().remove();
    }

    @EventHandler
    public void onCrystalDamage(EntityDamageByEntityEvent e) {
        Guild guild = plugin.getGuildManager().getGuild(e.getEntity().getLocation());
        if(guild == null) return;
        if(e.getEntityType() != EntityType.ENDER_CRYSTAL) return;

        if(e.getDamager() instanceof Player) return;
        e.setCancelled(false);
    }
}
