package me.gotitim.guildscore.listener;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.item.ItemBuilder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public final class InteractListener implements Listener {
    private final GuildsCore plugin;

    public InteractListener(GuildsCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if(!event.getAction().isRightClick()) return;

        if (!meta.getPersistentDataContainer().has(plugin.itemIdKey, PersistentDataType.STRING)) return;

        ItemBuilder.get(plugin, event.getItem()).onClick(event);
    }
}
