package me.gotitim.guildscore.listener;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.guilds.Guild;
import me.gotitim.guildscore.item.GuildHeartItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

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

        if(e.getDamager() instanceof Player player) {
            e.setCancelled(true);
            e.getEntity().remove();
            guild.getHeart().pickup();

            if(guild.getPlayers().contains(player.getUniqueId())) {
                player.getInventory().addItem(new GuildHeartItem(guild).toItemStack());
                guild.broadcast(Component.text("Gracz ").color(NamedTextColor.GREEN)
                                .append(Component.text(player.getName()).color(NamedTextColor.AQUA))
                                .append(Component.text(" podniósł serce gildii!")),
                        true);
                return;
            }
            Bukkit.broadcast(Component.text("Serce gildii ").color(NamedTextColor.DARK_GREEN)
                    .append(Component.text(guild.getName()).color(NamedTextColor.GOLD))
                    .append(Component.text(" zostało zniszczone przez ").color(NamedTextColor.DARK_GREEN))
                    .append(player.name()).color(NamedTextColor.AQUA));

        }
        e.setCancelled(true);
    }
}
