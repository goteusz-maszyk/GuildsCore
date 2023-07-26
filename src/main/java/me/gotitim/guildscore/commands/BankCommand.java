package me.gotitim.guildscore.commands;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.guilds.Guild;
import me.gotitim.guildscore.item.CoreInventoryHolder;
import me.gotitim.guildscore.item.ItemBuilder;
import me.gotitim.guildscore.placeholders.Placeholders;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

import static me.gotitim.guildscore.util.Components.*;

public class BankCommand extends Command {
    private final GuildsCore plugin;
    private final NamespacedKey sellableKey;

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
            player.sendMessage(loreComponentRaw("bank.no_guild"));
            return false;
        }
        if(!sender.hasPermission("guildscore.command.bank")) {
            player.sendMessage(parseRaw("no_permission"));
            return false;
        }
        createInventory(player, guild);
        return true;
    }

    private void createInventory(Player player, Guild guild) {
        CoreInventoryHolder holder = new CoreInventoryHolder();
        holder.createInventory(6, parseRaw("bank.title"));
        holder.setClickAction(this::onMenuClick);

        Inventory inv = holder.getInventory();

        fill(inv, guild, player);

        player.openInventory(inv);
    }

    private void fill(Inventory inv, Guild guild, Player player) {
        inv.setItem(4, guild.getAsIcon());

        inv.setItem(49, bankTooltip(player));

        AtomicInteger index = new AtomicInteger(19);
        Guild.BANK_MATERIALS.forEach((material, value) -> {
            ItemBuilder item = new ItemBuilder(material)
                    .addLoreLine(loreComponentRaw("bank.tooltip_value", new Placeholders(player).set("value", value)))
                    .addLoreLine(loreComponentRaw("bank.tooltip_lmb"))
                    .addLoreLine(loreComponentRaw("bank.tooltip_rmb"))
                    .setPersistentData(sellableKey, PersistentDataType.BOOLEAN, true);

            inv.setItem(index.incrementAndGet(), item.toItemStack());
        });
    }

    private void onMenuClick(InventoryClickEvent event) {
        event.setCancelled(true);

        if(event.isShiftClick()) return;
        if(event.getClick().isKeyboardClick()) return;
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

        int oneValue = Guild.BANK_MATERIALS.get(material);
        int itemAmount = 0;
        if(!player.getInventory().contains(material, 1)) {
            player.sendMessage(loreComponentRaw("bank.material_absent"));
            return;
        }
        if(event.isRightClick()) {
            for (ItemStack stack : player.getInventory().getContents()) {
                if (stack != null && stack.getType() == material) {
                    itemAmount += stack.getAmount();
                    stack.setAmount(0);
                }
            }
        } else if(event.isLeftClick()) {
            player.getInventory().removeItem(new ItemStack(material, 1));
            itemAmount = 1;
        } else return;
        guild.broadcast(parseRaw("bank.deposit", new Placeholders(player).set("amount", itemAmount * oneValue)), true);

        guild.bankDeposit(itemAmount * oneValue);
        fill(event.getInventory(), guild, player);
    }
}
