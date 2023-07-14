package me.gotitim.guildscore.item;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class CoreInventoryHolder implements InventoryHolder {
    private Inventory inventory = null;
    private Consumer<InventoryClickEvent> clickAction = (e) -> {};

    public CoreInventoryHolder setInventory(Inventory inventory) {
        this.inventory = inventory;
        return this;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void setClickAction(Consumer<InventoryClickEvent> action) {
        this.clickAction = action;
    }

    public void onClick(InventoryClickEvent event) {
        clickAction.accept(event);
    }

    public Inventory createInventory(int rows, Component title) {
        inventory = Bukkit.createInventory(this, rows*9, title);
        return inventory;
    }
}
