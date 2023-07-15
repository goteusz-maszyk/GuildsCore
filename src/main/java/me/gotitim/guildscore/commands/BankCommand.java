package me.gotitim.guildscore.commands;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.guilds.Guild;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static me.gotitim.guildscore.GuildsCore.loreComponent;

public class BankCommand extends Command {
    private final GuildsCore plugin;
    private NamespacedKey sellableKey;

    public BankCommand(GuildsCore plugin) {
        super("bank");
        this.plugin = plugin;
        sellableKey = new NamespacedKey(plugin, "sellable");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if(!(sender instanceof Player player)) return false;

        Guild guild = plugin.getGuildManager().getGuild(player);
        if(guild == null) {
            player.sendMessage(Component.text("Nie ma gildii - nie ma banku.").color(NamedTextColor.RED));
            return false;
        }
        createInventory(player, guild);
        return true;
    }

    private void createInventory(Player player, Guild guild) {
        CoreInventoryHolder holder = new CoreInventoryHolder();
        holder.createInventory(6, Component.text("Guild bank"));
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

        ItemBuilder bankDisplay = new ItemBuilder(Material.GOLD_INGOT)
                .setName(loreComponent("Bank gildii: ").color(NamedTextColor.WHITE)
                        .append(loreComponent(guild.getBank()).color(NamedTextColor.GOLD)));
        inv.setItem(49, bankDisplay.toItemStack());

        AtomicInteger index = new AtomicInteger(19);
        Guild.BANK_MATERIALS.forEach((material, value) -> {
            ItemBuilder item = new ItemBuilder(material)
                    .addLoreLine(loreComponent("Value: ").color(NamedTextColor.WHITE)
                            .append(loreComponent(value).color(NamedTextColor.GOLD)))
                    .addLoreLine(loreComponent("Left-Click to sell one").color(NamedTextColor.GREEN))
                    .addLoreLine(loreComponent("Right-Click to sell all").color(NamedTextColor.GREEN))
                    .setPersistentData(sellableKey, PersistentDataType.BOOLEAN, true);

            inv.setItem(index.incrementAndGet(), item.toItemStack());

        });
    }

    private void onMenuClick(InventoryClickEvent event) {
        event.setCancelled(true);

        if(event.isShiftClick()) return;
        if(event.getClickedInventory() == null) return;
        ItemBuilder item = new ItemBuilder(event.getCurrentItem());
        if(item.toItemStack() == null) return;
        if(item.getPersistentData(sellableKey, PersistentDataType.BOOLEAN) == null) return;

        Player player = (Player) event.getWhoClicked();
        Guild guild = plugin.getGuildManager().getGuild(player);

        if (guild == null) {
            event.getClickedInventory().close();
            return;
        }
        Material material = event.getCurrentItem().getType();
        int oneAmount = Guild.BANK_MATERIALS.get(material);
        int amount = 0;
        if(event.isRightClick()) {
            if(!player.getInventory().contains(material, 1)) {
                player.sendMessage(Component.text("Nie masz tego materiału.").color(NamedTextColor.RED));
                return;
            }
            int itemAmount = 0;
            for (ItemStack stack : player.getInventory().getContents()) {
                if (stack != null && stack.getType() == material) {
                    itemAmount += stack.getAmount();
                    stack.setAmount(0);
                }
            }
            amount = oneAmount*itemAmount;
        } else if(event.isLeftClick()) {
            if(!player.getInventory().contains(material, 1)) {
                player.sendMessage(Component.text("Nie masz tego materiału.").color(NamedTextColor.RED));
                return;
            }
            player.getInventory().removeItem(new ItemStack(material, 1));
            amount = oneAmount;
        } else return;
        guild.broadcast(Component.text(player.getName()).color(NamedTextColor.AQUA)
                .append(Component.text(" wpłacił do banku ").color(NamedTextColor.GREEN))
                .append(Component.text(amount).color(NamedTextColor.GOLD)), true);
        guild.bankDeposit(amount);
        fill(event.getInventory(), guild);
    }
}
