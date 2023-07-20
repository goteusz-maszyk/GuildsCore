package me.gotitim.guildscore.listener;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.util.region.StretchedCuboidRegion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public class CommonSpawnListener implements Listener {
    private final GuildsCore plugin;
    private final StretchedCuboidRegion spawn;

    public CommonSpawnListener(GuildsCore core) {
        this.plugin = core;
        spawn = StretchedCuboidRegion.from(core.getConfig().getConfigurationSection("spawn"));
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if(!spawn.contains(event.getBlock().getLocation())) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if(!spawn.contains(event.getBlock().getLocation())) return;
        if(replantCrop(event)) return;
        if(replantTree(event)) return;
        event.setCancelled(true);
    }
    @EventHandler
    public void onDecay(LeavesDecayEvent event) {
        if(!spawn.contains(event.getBlock().getLocation())) return;
        event.setCancelled(true);
        event.getBlock().setType(Material.AIR);
    }


    private boolean replantTree(BlockBreakEvent event) {
        final Set<Material> wood = EnumSet.of(
                Material.OAK_LOG,
                Material.OAK_LEAVES,
                Material.BIRCH_LOG,
                Material.BIRCH_LEAVES,
                Material.SPRUCE_LOG,
                Material.SPRUCE_LEAVES,
                Material.DARK_OAK_LOG,
                Material.DARK_OAK_LEAVES,
                Material.JUNGLE_LOG,
                Material.JUNGLE_LEAVES,
                Material.ACACIA_LOG,
                Material.ACACIA_LEAVES,
                Material.CHERRY_LOG,
                Material.CHERRY_LEAVES
        );
        @NotNull Material type = event.getBlock().getType();
        if(type.name().endsWith("_LEAVES")) {
            event.setDropItems(false);
        }
        if(wood.contains(type)) {
            if(event.getBlock().getRelative(BlockFace.DOWN).getType() == Material.DIRT) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    String woodType = String.join("_", Arrays.copyOfRange(type.name().split("_"), 0, type.toString().split("_").length-1));
                    event.getBlock().setType(Objects.requireNonNull(Material.getMaterial(woodType + "_SAPLING")));
                });
            }
            return true;
        }
        return false;
    }

    private boolean replantCrop(BlockBreakEvent event) {
        final Set<Material> crops = EnumSet.of(
                Material.WHEAT,
                Material.CARROTS,
                Material.POTATOES,
                Material.BEETROOTS
        );
        @NotNull Material type = event.getBlock().getType();
        if(crops.contains(type) && event.getBlock().getRelative(BlockFace.DOWN).getType() == Material.FARMLAND) {
            if(((Ageable) event.getBlock().getBlockData()).getMaximumAge() != ((Ageable) event.getBlock().getBlockData()).getAge()) return false;
            Bukkit.getScheduler().runTask(plugin, () -> event.getBlock().setType(type));
            return true;
        }
        return type == Material.PUMPKIN || type == Material.MELON;
    }
    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        if(!spawn.contains(event.getBlock().getLocation())) return;
        event.setCancelled(true);
    }
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if(!spawn.contains(event.getLocation())) return;
        event.setCancelled(true);
    }
    @EventHandler
    public void onDestroy(BlockDestroyEvent event) {
        if(!spawn.contains(event.getBlock().getLocation())) return;
        event.setCancelled(true);
    }
    @EventHandler
    public void onSpawn(EntitySpawnEvent event) {
        if(!spawn.contains(event.getLocation())) return;
        if(event.getEntity().getEntitySpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) return;
        event.setCancelled(true);
    }
    @EventHandler
    public void onHit(PrePlayerAttackEntityEvent event) {
        if(!spawn.contains(event.getAttacked().getLocation())) return;
        event.setCancelled(true);
    }
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if(event.getClickedBlock() == null) return;
        if(!spawn.contains(event.getClickedBlock().getLocation())) return;
        if(event.getItem() == null || event.getItem().getType() != Material.MINECART) return;
        event.setCancelled(true);
    }
    @EventHandler
    public void onPush(VehicleEntityCollisionEvent event) {
        if(!spawn.contains(event.getEntity().getLocation())) return;
        event.setCancelled(true);
    }
}
