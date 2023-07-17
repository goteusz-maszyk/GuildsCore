package me.gotitim.guildscore.commands;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.guilds.Guild;
import me.gotitim.guildscore.guilds.HeartUpgrade;
import me.gotitim.guildscore.item.CoreInventoryHolder;
import me.gotitim.guildscore.item.ItemBuilder;
import me.gotitim.guildscore.placeholders.Placeholders;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import static me.gotitim.guildscore.util.Components.*;

public class ShopCommand extends Command {

    private final GuildsCore plugin;
    private final NamespacedKey upgradeKey;

    public ShopCommand(@NotNull GuildsCore core) {
        super("shop");
        setDescription("Opens guild shop");
        plugin = core;
        upgradeKey = new NamespacedKey(core, "upgrade");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if(!(sender instanceof Player player)) return false;

        Guild guild = plugin.getGuildManager().getGuild(player);
        if(guild == null) {
            player.sendMessage(parseRaw("shop.no_guild"));
            return false;
        }
        createInventory(player, guild);
        return true;
    }

    private void createInventory(Player player, Guild guild) {
        CoreInventoryHolder holder = new CoreInventoryHolder();
        holder.createInventory(6, parseRaw("shop.title"));
        holder.setClickAction(this::onMenuClick);

        Inventory inv = holder.getInventory();

        fill(inv, guild, player);

        player.openInventory(inv);
    }

    private void fill(Inventory inv, Guild guild, Player player) {
        inv.setItem(4, guild.getAsIcon());

        for (HeartUpgrade upgrade : HeartUpgrade.values()) inv.setItem(upgrade.getShopSlot(), upgrade.getIcon(player, upgradeKey));

        Placeholders ph = new Placeholders().setPlayer(player);
        for (String key : plugin.getConfig().getConfigurationSection("items").getKeys(false)) {
            int cost = plugin.getConfig().getInt("items." + key + ".cost");
            ph.set("upgrade_cost", cost);
            ItemBuilder item = ItemBuilder.get(plugin, key)
                    .addLoreLine(loreComponentRaw("shop.cost", ph))
                    .setPersistentData(upgradeKey, PersistentDataType.STRING, key);
            inv.setItem(plugin.getConfig().getInt("items." + key + ".slot"), item.toItemStack());
        }

        inv.setItem(49, bankTooltip(player));
    }


    private void onMenuClick(InventoryClickEvent event) {
        event.setCancelled(true);

        if(event.isShiftClick()) return;
        if(event.getAction() != InventoryAction.PICKUP_ALL) return;
        if(event.getClickedInventory() == null) return;
        ItemBuilder item = ItemBuilder.get(plugin, event.getCurrentItem());
        if(item.toItemStack() == null) return;
        if(item.getPersistentData(upgradeKey, PersistentDataType.STRING) == null) return;

        HeartUpgrade upgrade = HeartUpgrade.valueOf(item.getPersistentData(upgradeKey, PersistentDataType.STRING));
        Guild guild = plugin.getGuildManager().getGuild((Player) event.getWhoClicked());

        if (guild == null) {
            event.getClickedInventory().close();
            return;
        }
        if(upgrade == null) {
            String itemId = item.getPersistentData(upgradeKey, PersistentDataType.STRING);
            if(itemId == null) return;
            int cost = plugin.getConfig().getInt("items." + itemId + ".cost");
            if(cost > guild.getBank()) {
                event.getWhoClicked().sendMessage(parseRaw("shop.cannot_afford"));
                return;
            }
            guild.bankWithdraw(cost);
            for (ItemStack is : event.getWhoClicked().getInventory().addItem(ItemBuilder.get(plugin, itemId).toItemStack()).values()) {
                event.getWhoClicked().getWorld().dropItem(event.getWhoClicked().getLocation(), is);
            }
            Component hover = item.getName().hoverEvent(
                    HoverEvent.showItem(
                            HoverEvent.ShowItem.showItem(item.toItemStack().getType().key(), 1,
                                    BinaryTagHolder.binaryTagHolder(item.toItemStack().getItemMeta().getAsString()))
                    )
            );
            guild.broadcast(parseRaw("shop.bought", new Placeholders(((Player) event.getWhoClicked()))
                    .set("hover_item", MiniMessage.miniMessage().serialize(hover))), true);
            return;
        }
        Boolean result = guild.getHeart().tryUpgradeLevel(upgrade);
        if(result == Boolean.TRUE) {
            guild.broadcast(parseRaw("shop.upgraded", new Placeholders(((Player) event.getWhoClicked()))
                    .set("upgrade_name", "%upgrade_name_" + upgrade.name() + "%")
                    .set("upgrade_level", "%upgrade_level_" + upgrade.name() + "%")), true);
        } else if(result == null) {
            event.getWhoClicked().sendMessage(parseRaw("shop.max_level"));
        } else {
            event.getWhoClicked().sendMessage(parseRaw("shop.cannot_afford"));
        }
        fill(event.getInventory(), guild, (Player) event.getWhoClicked());
    }
}
