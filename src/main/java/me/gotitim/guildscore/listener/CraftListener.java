package me.gotitim.guildscore.listener;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.guilds.Guild;
import me.gotitim.guildscore.item.GuildCompass;
import me.gotitim.guildscore.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class CraftListener implements Listener {
    private final GuildsCore plugin;

    public CraftListener(GuildsCore core) {
        this.plugin = core;
    }

    @EventHandler
    public void onSmithing(PrepareAnvilEvent event) {
        ItemStack inputComp = event.getInventory().getFirstItem();
        ItemStack inputHead = event.getInventory().getSecondItem();
        if(inputComp == null) return;
        if(inputHead == null) return;
        if (!ItemBuilder.get(plugin, inputComp).getClass().equals(GuildCompass.class)
                || inputHead.getType() != Material.PLAYER_HEAD) {
            return;
        }
        OfflinePlayer player = ((SkullMeta) inputHead.getItemMeta()).getOwningPlayer();
        if(player == null) return;

        Guild guild = plugin.getGuildManager().getGuild(player);
        if(guild == null) return;

        ItemStack item = ((GuildCompass) ItemBuilder.get(plugin, "GUILD_COMPASS")).setGuild(guild).toItemStack();
        event.setResult(item);
        event.getInventory().setRepairCost(1);
    }
}
