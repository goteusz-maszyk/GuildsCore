package me.gotitim.guildscore.item;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.guilds.Guild;
import me.gotitim.guildscore.placeholders.Placeholders;
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
import static me.gotitim.guildscore.util.Components.loreComponentRaw;
import static me.gotitim.guildscore.util.Components.parseRaw;

public class GuildHeartItem extends ItemBuilder {
    private final GuildsCore core;

    public GuildHeartItem(GuildsCore core) {
        super(Material.END_CRYSTAL);
        this.core = core;
        setName(loreComponentRaw("heart.item_name"));
        setPersistentData(core.itemIdKey, PersistentDataType.STRING, "GUILD_HEART");
    }

    public GuildHeartItem(GuildsCore core, ItemStack is) {
        super(is);
        this.core = core;
        setName(loreComponentRaw("heart.item_name"));
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        if(!event.getAction().isRightClick()) return;
        if(event.getClickedBlock() == null) return;
        if(event.getClickedBlock().getType() != Material.OBSIDIAN && event.getClickedBlock().getType() != Material.BEDROCK) return;
        if(event.getItem().getType() != Material.END_CRYSTAL) return;
        Guild guild = core.getGuildManager().getGuild(event.getPlayer());
        if(guild == null) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(parseRaw("guild_command.no_guild"));
            return;
        }
        if(guild.getHeart().isPlaced()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(parseRaw("heart.already_placed"));
            return;
        }
        Location loc = event.getClickedBlock().getLocation();
        for (Guild g : core.getGuildManager().getGuilds().values()) {
            Location heart = g.getHeart().getLocation();
            if(g.getHeart().isPlaced() && distanceHorizontal(loc, heart) < core.getConfig().getInt("heart_place_range", 16*24)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(parseRaw("heart.placed_too_near"));
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

                guild.broadcast(parseRaw("heart.place", new Placeholders(event.getPlayer())), true);
                guild.getHeart().place(crystal.getLocation());

                break;
            }
        });

    }
}
