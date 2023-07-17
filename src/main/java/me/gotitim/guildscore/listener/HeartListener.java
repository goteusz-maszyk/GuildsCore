package me.gotitim.guildscore.listener;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.guilds.Guild;
import me.gotitim.guildscore.guilds.HeartUpgrade;
import me.gotitim.guildscore.placeholders.Placeholders;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static me.gotitim.guildscore.guilds.HeartUpgrade.*;
import static me.gotitim.guildscore.util.Components.parseRaw;

public final class HeartListener implements Listener {
    private final GuildsCore plugin;
    private static final Map<UUID, BukkitTask> fatigueTasks = new HashMap<>();
    private static final Map<UUID, BukkitTask> regenTasks = new HashMap<>();

    private static final Map<UUID, Guild> normalAffectedPlayers = new HashMap<>();
    private static final Map<UUID, Guild> warningPlayers = new HashMap<>();
    private static final Map<UUID, Guild> healedPlayers = new HashMap<>();

    public HeartListener(GuildsCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Guild guild = heartAffects(event.getBlock().getLocation(), event.getPlayer(), WORKING_RADIUS);
        if (guild== null) return;

        final Set<Material> containerTypes = EnumSet.of(
                Material.CHEST,
                Material.TRAPPED_CHEST
        );

        if(guild.getHeart().getUpgrade(HeartUpgrade.CHEST_LOCK) != 0 && containerTypes.contains(event.getBlock().getType())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(parseRaw("heart.chest_unbreakable"));
        }
    }
    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if(heartAffects(event.getBlock().getLocation(), event.getPlayer(), WORKING_RADIUS) != null) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(parseRaw("heart.cannot_place_blocks"));
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Guild guild = heartAffects(event.getFrom(), event.getPlayer(), WORKING_RADIUS);
        if (guild!= null) {
            int level = guild.getHeart().getUpgrade(SLOWNESS);
            if(level != 0) {
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, level-1, true, true, false));
            }
            int fatigueLevel = guild.getHeart().getUpgrade(MINING_FATIGUE);
            if(fatigueLevel != 0) {
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 20, fatigueLevel-1, true, true, false));
            }
        }

        Guild healGuild = heartAffects(event.getFrom(), event.getPlayer(), HeartUpgrade.HEAL_RADIUS);
        if(healGuild != null) try {
            Objects.requireNonNull(regenTasks.put(event.getPlayer().getUniqueId(), Bukkit.getScheduler().runTaskTimer(plugin,
                    () -> event.getPlayer().addPotionEffect(
                            new PotionEffect(PotionEffectType.REGENERATION, 60, 1, true, true, false)
                    ), 0, 19))).cancel();
        } catch (NullPointerException ignored) {}

        Guild warningGuild = heartAffects(event.getFrom(), event.getPlayer(), WARNING_RADIUS);
        if(warningGuild != null) {
            warningGuild.broadcastTitle(Title.title(parseRaw("heart.entered_warning", new Placeholders(event.getPlayer())), Component.empty()));
        }
    }

    @EventHandler
    public void onMining(BlockDamageEvent event) {
        Guild guild = heartAffects(event.getBlock().getLocation(), event.getPlayer(), WORKING_RADIUS);
        if (guild== null) return;

        int fatigueLevel = guild.getHeart().getUpgrade(HeartUpgrade.MINING_FATIGUE);
        if(fatigueLevel != 0) {
            try {
                Objects.requireNonNull(fatigueTasks.put(event.getPlayer().getUniqueId(), Bukkit.getScheduler().runTaskTimer(plugin,
                        () -> event.getPlayer().addPotionEffect(
                        new PotionEffect(PotionEffectType.SLOW_DIGGING, 20, fatigueLevel - 1, true, true, false)
                ), 0, 19))).cancel();
            } catch (NullPointerException ignored) {}
        }
    }

    @EventHandler
    public void onMiningStop(BlockDamageAbortEvent event) {
        try{
            fatigueTasks.remove(event.getPlayer().getUniqueId()).cancel();
        } catch (NullPointerException ignored) {}
    }

    @EventHandler
    public void onInteraction(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if(block == null) return;
        if(!event.getAction().isRightClick()) return;

        Guild guild = heartAffects(block.getLocation(), event.getPlayer(), WORKING_RADIUS);
        if (guild== null) return;
        if(guild.getHeart().getUpgrade(HeartUpgrade.CHEST_LOCK) == 0) return;

        final Set<Material> containerTypes = EnumSet.of(
                Material.CHEST,
                Material.DROPPER,
                Material.HOPPER,
                Material.DISPENSER,
                Material.TRAPPED_CHEST,
                Material.BREWING_STAND,
                Material.FURNACE,
                Material.BLAST_FURNACE,
                Material.SMOKER,
                Material.CHISELED_BOOKSHELF
        );

        if(containerTypes.contains(block.getType())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(parseRaw("heart.container_locked"));
        }
    }

    private Guild heartAffects(Location location, @NotNull Player player, HeartUpgrade upgrade) {
        Guild g = affectedAlready(player, upgrade);
        if(g!= null) return g;

        for (Guild guild : plugin.getGuildManager().getGuilds().values()) {
            boolean contains = guild.getPlayers().contains(player.getUniqueId());
            if(upgrade == HeartUpgrade.HEAL_RADIUS && !contains) continue;
            if(upgrade != HeartUpgrade.HEAL_RADIUS && contains) continue;

            int radius = guild.getHeart().getUpgrade(upgrade) * 16;
            if(!guild.getHeart().isPlaced()) continue;
            Location heart = guild.getHeart().getLocation();

            double distance = distanceHorizontal(heart, location);
            if(radius >= distance) {
                if(warningPlayers.containsKey(player.getUniqueId())) {
                    return null;
                } else {
                    warningPlayers.put(player.getUniqueId(), guild);
                }
                if(healedPlayers.containsKey(player.getUniqueId())) {
                    return null;
                } else {
                    healedPlayers.put(player.getUniqueId(), guild);
                }
                if (upgrade.equals(WORKING_RADIUS)) {
                    normalAffectedPlayers.put(player.getUniqueId(), guild);
                }
                return guild;
            }
        }
        return null;
    }

    private Guild affectedAlready(Player player, HeartUpgrade upgrade) {
        switch (upgrade.name()) {
            case "WORKING_RADIUS" -> {
                Guild g = normalAffectedPlayers.get(player.getUniqueId());
                if (g == null) return null;
                if (distanceHorizontal(g.getHeart().getLocation(), player.getLocation()) <= g.getHeart().getUpgrade(upgrade) * 16)
                    return g;
                else normalAffectedPlayers.remove(player.getUniqueId());
                return null;
            }
            case "HEAL_RADIUS" -> {
                Guild g = healedPlayers.get(player.getUniqueId());
                if (g == null) return null;
                if (!(distanceHorizontal(g.getHeart().getLocation(), player.getLocation()) <= g.getHeart().getUpgrade(upgrade) * 16)) {
                    healedPlayers.remove(player.getUniqueId());
                    try{regenTasks.remove(player.getUniqueId()).cancel();}catch (NullPointerException ignored) {}
                }
                return null;
            }
            case "WARNING_RADIUS" -> {
                Guild g = warningPlayers.get(player.getUniqueId());
                if (g == null) return null;
                if (!(distanceHorizontal(g.getHeart().getLocation(), player.getLocation()) <= g.getHeart().getUpgrade(upgrade) * 16)) {
                    warningPlayers.remove(player.getUniqueId());
                }
                return null;
            }
        }
        return null;
    }

    public static double distanceHorizontal(Location loc1, Location loc2) {
        if(!loc1.getWorld().equals(loc2.getWorld())) return Double.MAX_VALUE;
        double deltaX = Math.abs(loc2.getX() - loc1.getX());
        double deltaZ = Math.abs(loc2.getZ() - loc1.getZ());
        return Math.sqrt(deltaX*deltaX + deltaZ*deltaZ);
    }
    public static void resetTasks(Player player) {
        try{fatigueTasks.remove(player.getUniqueId()).cancel();} catch (NullPointerException ignored) {}
        try{regenTasks.remove(player.getUniqueId()).cancel();} catch (NullPointerException ignored) {}
        healedPlayers.remove(player.getUniqueId());
        warningPlayers.remove(player.getUniqueId());
        normalAffectedPlayers.remove(player.getUniqueId());
    }
}
