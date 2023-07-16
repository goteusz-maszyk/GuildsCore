package me.gotitim.guildscore.item;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.guilds.Guild;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

import static me.gotitim.guildscore.listener.HeartListener.distanceHorizontal;

public class GuildHeartItem extends ItemBuilder {
    private final GuildsCore core;

    public GuildHeartItem(GuildsCore core) {
        super(Material.END_CRYSTAL);
        this.core = core;
        setName("§r§bGuild Heart");
        setPersistentData(core.itemIdKey, PersistentDataType.STRING, "GUILD_HEART");
    }

    public GuildHeartItem(GuildsCore core, ItemStack is) {
        super(is);
        this.core = core;
        setName("§r§bGuild Heart");
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        if(!event.getAction().isRightClick()) return;
        if(event.getClickedBlock() == null) return;
        if(event.getClickedBlock().getType() != Material.OBSIDIAN && event.getClickedBlock().getType() != Material.BEDROCK) return;
        if(event.getItem().getType() != Material.END_CRYSTAL) return;
        Guild guild = core.getGuildManager().getGuild(event.getPlayer());
        if(guild == null) {
            event.getPlayer().sendMessage("Nie jesteś w gildii");
            return;
        }
        if(guild.getHeart().isPlaced()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Component.text("Nie możesz postawić serca, gdy już jest jakieś!").color(NamedTextColor.RED));
            return;
        }
        Location loc = event.getClickedBlock().getLocation();
        for (Guild g : core.getGuildManager().getGuilds().values()) {
            Location heart = g.getHeart().getLocation();
            if(g.getHeart().isPlaced() && distanceHorizontal(loc, heart) < 16*24) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(Component.text("Nie możesz postawić serca, gdy w promieniu 24 chunków jest inna gildia!").color(NamedTextColor.RED));
                return;
            }
        }

        Bukkit.getScheduler().runTask(core, () -> {
            List<Entity> entities = event.getPlayer().getNearbyEntities(5, 5, 5);

            for (Entity entity : entities) {
                if (EntityType.ENDER_CRYSTAL != entity.getType()) continue;

                EnderCrystal crystal = (EnderCrystal) entity;
                Block belowCrystal = crystal.getLocation().getBlock().getRelative(BlockFace.DOWN);

                if (!event.getClickedBlock().equals(belowCrystal)) continue;

                guild.broadcast(Component.text("Gracz ").color(NamedTextColor.GREEN)
                                .append(Component.text(event.getPlayer().getName()).color(NamedTextColor.AQUA))
                                .append(Component.text(" postawił serce gildii!")),
                        true);
                guild.getHeart().place(crystal.getLocation());

                break;
            }
        });

    }
}
