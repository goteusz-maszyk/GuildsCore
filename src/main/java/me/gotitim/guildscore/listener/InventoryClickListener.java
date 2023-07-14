package me.gotitim.guildscore.listener;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.item.CoreInventoryHolder;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;

public final class InventoryClickListener implements Listener {
    private final GuildsCore plugin;
    private final List<HumanEntity> openInventories = new ArrayList<>();

    public InventoryClickListener(GuildsCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if(!openInventories.contains(event.getWhoClicked())) return;

        InventoryHolder holder = event.getInventory().getHolder();
        if(holder instanceof CoreInventoryHolder coreHolder) coreHolder.onClick(event);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if(event.getInventory().getHolder() instanceof CoreInventoryHolder) openInventories.add(event.getPlayer());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        openInventories.remove(event.getPlayer());
    }
}
