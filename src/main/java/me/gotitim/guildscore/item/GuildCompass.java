package me.gotitim.guildscore.item;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.guilds.Guild;
import me.gotitim.guildscore.placeholders.Placeholders;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import static me.gotitim.guildscore.util.Components.loreComponentRaw;
import static me.gotitim.guildscore.util.Components.parseRaw;

public final class GuildCompass extends ItemBuilder {
    private final GuildsCore core;

    public GuildCompass(GuildsCore core) {
        super(Material.COMPASS);
        this.core = core;
        setName(loreComponentRaw("compass.name"));
        buildLore(null);
        setPersistentData(core.itemIdKey, PersistentDataType.STRING, "GUILD_COMPASS");
    }

    public GuildCompass(GuildsCore core, ItemStack itemStack) {
        super(itemStack);
        this.core = core;
        String guildId = getPersistentData(core.guildIdKey, PersistentDataType.STRING);
    }

    public void setGuild(Guild guild) {
        buildLore(guild);

        setLodestone(guild.getHeart().getLocation()).setPersistentData(core.guildIdKey, PersistentDataType.STRING, guild.getId());
    }

    private void buildLore(Guild guild) {
        String guildName = guild == null ? "None" : guild.getName();
        Placeholders ph = new Placeholders().set("guild", guildName);
        setLore(
                loreComponentRaw("compass.tooltip_1", ph),
                loreComponentRaw("compass.tooltip_2", ph)
        );
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        CoreInventoryHolder holder = new CoreInventoryHolder();
        Inventory inventory = holder.createInventory(2, parseRaw("compass.title"));
        holder.setClickAction(this::onMenuClick);

        core.getGuildManager().getGuilds().values().stream().map(Guild::getAsIcon).forEach(inventory::addItem);
        player.openInventory(inventory);
    }

    public void onMenuClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if(event.isShiftClick()) return;
        ItemStack item = event.getCurrentItem();
        if(item == null) return;
        String guildId = item.getItemMeta().getPersistentDataContainer().get(core.guildIdKey, PersistentDataType.STRING);
        Guild guild = core.getGuildManager().getGuild(guildId);

        setGuild(guild);
        Placeholders ph = new Placeholders().set("guild", guild.getName());
        event.getWhoClicked().sendMessage(loreComponentRaw("compass.set_guild", ph));
        event.getInventory().close();
    }
}
