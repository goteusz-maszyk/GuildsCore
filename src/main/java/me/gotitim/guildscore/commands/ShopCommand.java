package me.gotitim.guildscore.commands;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.guilds.Guild;
import me.gotitim.guildscore.guilds.GuildHeart;
import me.gotitim.guildscore.item.CoreInventoryHolder;
import me.gotitim.guildscore.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static me.gotitim.guildscore.GuildsCore.loreComponent;

public class ShopCommand extends Command {

    private final GuildsCore plugin;
    private NamespacedKey upgradeKey;

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
            player.sendMessage(Component.text("Nie ma gildii - nie ma sklepu.").color(NamedTextColor.RED));
            return false;
        }
        createInventory(player, guild);
        return true;
    }

    private void createInventory(Player player, Guild guild) {
        CoreInventoryHolder holder = new CoreInventoryHolder();
        holder.createInventory(6, Component.text("Guild shop"));
        holder.setClickAction(this::onMenuClick);

        Inventory inv = holder.getInventory();

        fill(inv, guild);

        player.openInventory(inv);
    }

    private void fill(Inventory inv, Guild guild) {
        ItemBuilder guildIcon = new ItemBuilder(guild.getIcon())
                .setName(guild.getNameComponent())
                .addLoreLine(loreComponent("Members: ").color(NamedTextColor.GREEN));
        for (UUID uuid : guild.getPlayers()) {
            guildIcon.addLoreLine(loreComponent(Bukkit.getOfflinePlayer(uuid).getName()).color(NamedTextColor.AQUA));
        }
        inv.setItem(4, guildIcon.toItemStack());
        for (GuildHeart.Upgrade upgrade : GuildHeart.Upgrade.values()) {
            ItemBuilder item = new ItemBuilder(upgrade.getIcon())
                    .setName(loreComponent(upgrade + " " + guild.getHeart().getUpgrade(upgrade)).color(NamedTextColor.BLUE))
                    .addLoreLine(loreComponent(upgrade.getDescription()).color(NamedTextColor.WHITE))
                    .setPersistentData(upgradeKey, PersistentDataType.STRING, upgrade.name());
            inv.setItem(upgrade.getShopSlot(), item.toItemStack()); //TODO: Add cost
        }

        ItemBuilder bankDisplay = new ItemBuilder(Material.GOLD_INGOT)
                .setName(loreComponent("Bank gildii: ").color(NamedTextColor.WHITE)
                        .append(loreComponent(guild.getBank()).color(NamedTextColor.GOLD)));
        inv.setItem(49, bankDisplay.toItemStack());
    }


    private void onMenuClick(InventoryClickEvent event) {
        event.setCancelled(true);

        if(event.isShiftClick()) return;
        if(event.getClickedInventory() == null) return;
        ItemBuilder item = new ItemBuilder(event.getCurrentItem());
        if(item.toItemStack() == null) return;
        if(item.getPersistentData(upgradeKey, PersistentDataType.STRING) == null) return;

        GuildHeart.Upgrade upgrade = GuildHeart.Upgrade.valueOf(item.getPersistentData(upgradeKey, PersistentDataType.STRING));
        Guild guild = plugin.getGuildManager().getGuild((Player) event.getWhoClicked());

        if (guild == null) {
            event.getClickedInventory().close();
            return;
        }

        Boolean result = guild.getHeart().tryUpgradeLevel(upgrade);
        if(result == Boolean.TRUE) {
            guild.broadcast(Component.text(event.getWhoClicked().getName() + " ulepszył " + upgrade + " do poziomu " + guild.getHeart().getUpgrade(upgrade)), true);
        } else if(result == null) {
            event.getWhoClicked().sendMessage(Component.text("Osiągnąłeś maksymalny poziom ulepszenia.").color(NamedTextColor.YELLOW));
        } else {
            event.getWhoClicked().sendMessage(Component.text("Nie stać cię na to.").color(NamedTextColor.RED));
        }
        fill(event.getInventory(), guild);
    }
}
