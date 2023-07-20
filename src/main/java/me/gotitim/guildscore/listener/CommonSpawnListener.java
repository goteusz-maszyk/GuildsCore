package me.gotitim.guildscore.listener;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.util.region.StretchedCuboidRegion;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;

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
        event.setCancelled(true);
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
    public void onHit(EntityDamageByEntityEvent event) {
        if(!spawn.contains(event.getEntity().getLocation())) return;
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
