package me.gotitim.guildscore.item;

import me.gotitim.guildscore.GuildsCore;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ItemBuilder implements Cloneable {
    private final ItemStack is;
    /**
     * Create a new ItemBuilder from scratch.
     * @param m The material to create the ItemBuilder with.
     */
    public ItemBuilder(Material m){
        this(m, 1);
    }
    /**
     * Create a new ItemBuilder over an existing itemstack.
     * @param is The itemstack to create the ItemBuilder over.
     */
    public ItemBuilder(ItemStack is){
        this.is=is;
    }
    /**
     * Create a new ItemBuilder from scratch.
     * @param m The material of the item.
     * @param amount The amount of the item.
     */
    public ItemBuilder(Material m, int amount){
        is= new ItemStack(m, amount);
    }
    /**
     * Clone the ItemBuilder into a new one.
     * @return The cloned instance.
     */
    public @NotNull ItemBuilder clone(){
        return new ItemBuilder(is);
    }
    /**
     * Change the durability of the item.
     * @param dur The durability to set it to.
     */
    public ItemBuilder setDurability(short dur){
        Damageable im = (Damageable) is.getItemMeta();
        im.setDamage(dur);
        is.setItemMeta(im);
        return this;
    }
    /**
     * Set the displayname of the item.
     * @param name The name to change it to.
     */
    public ItemBuilder setName(Component name){
        ItemMeta im = is.getItemMeta();
        im.displayName(name);
        is.setItemMeta(im);
        return this;
    }
    /**
     * Set the displayname of the item.
     * @param name The name to change it to.
     */
    public ItemBuilder setName(String name){
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(name);
        is.setItemMeta(im);
        return this;
    }
    /**
     * Add an unsafe enchantment.
     * @param ench The enchantment to add.
     * @param level The level to put the enchant on.
     */
    public ItemBuilder addUnsafeEnchantment(Enchantment ench, int level){
        is.addUnsafeEnchantment(ench, level);
        return this;
    }
    /**
     * Remove a certain enchant from the item.
     * @param ench The enchantment to remove
     */
    public ItemBuilder removeEnchantment(Enchantment ench){
        is.removeEnchantment(ench);
        return this;
    }
    /**
     * Set the skull owner for the item. Works on skulls only.
     * @param owner The name of the skull's owner.
     */
    public ItemBuilder setSkullOwner(OfflinePlayer owner){
        try{
            SkullMeta im = (SkullMeta)is.getItemMeta();
            im.setOwningPlayer(owner);
            is.setItemMeta(im);
        }catch(ClassCastException ignored){}
        return this;
    }
    /**
     * Add an enchant to the item.
     * @param ench The enchant to add
     * @param level The level
     */
    public ItemBuilder addEnchant(Enchantment ench, int level){
        ItemMeta im = is.getItemMeta();
        im.addEnchant(ench, level, true);
        is.setItemMeta(im);
        return this;
    }
    /**
     * Add multiple enchants at once.
     * @param enchantments The enchants to add.
     */
    public ItemBuilder addEnchantments(Map<Enchantment, Integer> enchantments){
        is.addEnchantments(enchantments);
        return this;
    }
    /**
     * Sets infinity durability on the item by setting the durability to Short.MAX_VALUE.
     */
    public ItemBuilder setUnbreakable(){
        ItemMeta meta = is.getItemMeta();
        meta.setUnbreakable(true);
        is.setItemMeta(meta);
        return this;
    }
    /**
     * Re-sets the lore.
     * @param lore The lore to set it to.
     */
    public ItemBuilder setLore(Component... lore){
        ItemMeta im = is.getItemMeta();
        im.lore(Arrays.asList(lore));
        is.setItemMeta(im);
        return this;
    }
    /**
     * Re-sets the lore.
     * @param lore The lore to set it to.
     */
    public ItemBuilder setLore(List<Component> lore) {
        ItemMeta im = is.getItemMeta();
        im.lore(lore);
        is.setItemMeta(im);
        return this;
    }
    /**
     * Remove a lore line.
     * @param line The line to remove.
     */
    public ItemBuilder removeLoreLine(Component line){
        ItemMeta im = is.getItemMeta();
        List<Component> lore = Optional.ofNullable(im.lore()).orElseGet(ArrayList::new);
        if(!lore.contains(line)) return this;
        lore.remove(line);
        im.lore(lore);
        is.setItemMeta(im);
        return this;
    }
    /**
     * Remove a lore line.
     * @param index The index of the lore line to remove.
     */
    public ItemBuilder removeLoreLine(int index){
        ItemMeta im = is.getItemMeta();
        List<Component> lore = Optional.ofNullable(im.lore()).orElseGet(ArrayList::new);
        if(index<0 || index>lore.size()) return this;
        lore.remove(index);
        im.lore(lore);
        is.setItemMeta(im);
        return this;
    }
    /**
     * Add a lore line.
     * @param line The lore line to add.
     */
    public ItemBuilder addLoreLine(Component line){
        ItemMeta im = is.getItemMeta();
        List<Component> lore = Optional.ofNullable(im.lore()).orElseGet(ArrayList::new);
        lore.add(line);
        im.lore(lore);
        is.setItemMeta(im);
        return this;
    }
    /**
     * Add a lore line.
     * @param line The lore line to add.
     * @param pos The index of where to put it.
     */
    public ItemBuilder addLoreLine(Component line, int pos){
        ItemMeta im = is.getItemMeta();
        List<Component> lore = Optional.ofNullable(im.lore()).orElseGet(ArrayList::new);
        lore.set(pos, line);
        im.lore(lore);
        is.setItemMeta(im);
        return this;
    }
    /**
     * Sets the dye color on an item.
     * <b>* Notice that this doesn't check for item type, sets the literal data of the dyecolor as durability.</b>
     * @param color The color to put.
     */
    @SuppressWarnings("deprecation")
    public ItemBuilder setDyeColor(DyeColor color){
        this.is.setDurability(color.getDyeData());
        return this;
    }
    /**
     * Sets the armor color of a leather armor piece. Works only on leather armor pieces.
     * @param color The color to set it to.
     */
    public ItemBuilder setLeatherArmorColor(Color color){
        try{
            LeatherArmorMeta im = (LeatherArmorMeta)is.getItemMeta();
            im.setColor(color);
            is.setItemMeta(im);
        }catch(ClassCastException ignored){}
        return this;
    }

    public ItemBuilder setLodestone(Location location) {
        try{
            CompassMeta im = (CompassMeta)is.getItemMeta();
            im.setLodestone(location);
            im.setLodestoneTracked(false);
            is.setItemMeta(im);
        }catch(ClassCastException ignored){}
        return this;
    }
    /**
     * Retrieves the itemstack from the ItemBuilder.
     * @return The itemstack created/modified by the ItemBuilder instance.
     */
    public ItemStack toItemStack(){
        return is;
    }

    public <T, Z> ItemBuilder setPersistentData(NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
        ItemMeta im = is.getItemMeta();
        im.getPersistentDataContainer().set(key, type, value);
        is.setItemMeta(im);
        return this;
    }

    public <T, Z> Z getPersistentData(NamespacedKey key, PersistentDataType<T, Z> type) {
        ItemMeta im = is.getItemMeta();
        return im.getPersistentDataContainer().get(key, type);
    }

    public void onClick(PlayerInteractEvent event) {}

    public static ItemBuilder get(GuildsCore core, ItemStack is) {
        switch (is.getItemMeta().getPersistentDataContainer().getOrDefault(core.itemIdKey, PersistentDataType.STRING, "")) {
            case "GUILD_COMPASS" -> {
                return new GuildCompass(core, is);
            }
            case "GUILD_HEART" -> {
                return new GuildHeartItem(core, is);
            }
            default -> {
                return new ItemBuilder(is);
            }
        }
    }
}
