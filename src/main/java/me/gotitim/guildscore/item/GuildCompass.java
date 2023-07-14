package me.gotitim.guildscore.item;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.guilds.Guild;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class GuildCompass extends ItemBuilder {
    private final GuildsCore core;
    public final NamespacedKey guildIdKey;

    public GuildCompass(GuildsCore core) {
        super(Material.COMPASS);
        guildIdKey = new NamespacedKey(core, "guildId");
        this.core = core;
        setName("§r§bGuild Compass");
        buildLore(null);
        setPersistentData(core.itemIdKey, PersistentDataType.STRING, "GUILD_COMPASS");
    }

    public GuildCompass(GuildsCore core, ItemStack itemStack) {
        super(itemStack);
        guildIdKey = new NamespacedKey(core, "guildId");
        this.core = core;
        String guildId = getPersistentData(guildIdKey, PersistentDataType.STRING);

        buildLore(core.getGuildManager().getGuild(guildId));
    }

    public void setGuild(Guild guild) {
        buildLore(guild);

        setLodestone(guild.getHeart().getLocation()).setPersistentData(guildIdKey, PersistentDataType.STRING, guild.getId());
    }

    private void buildLore(Guild guild) {
        Component guildName = Component.text(guild == null ? "None" : guild.getName());

        setLore(
                Component.translatable("lore.guilds.guild_compass.line_1", "Always points towards selected guild's heart")
                        .color(TextColor.color(Color.GREEN.asRGB())),
                Component.translatable("lore.guilds.guild_compass.line_2", "Selected guild: %s")
                        .args(guildName)
        );
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        CoreInventoryHolder holder = new CoreInventoryHolder();
        Inventory inventory = holder.createInventory(2, Component.translatable("container.guild_compass", "Select guild to track:"));
        holder.setClickAction(this::onMenuClick);

        for (Guild guild : core.getGuildManager().getGuilds().values()) {
            ItemStack is = new ItemStack(guild.getIcon());
            ItemMeta meta = is.getItemMeta();

            meta.displayName(Component.text(guild.getName()).color(NamedTextColor.GOLD));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(" "));
            lore.add(Component.text("Members:").color(NamedTextColor.GREEN));
            for (UUID guildPlayer : guild.getPlayers()) {
                String playerName = Optional.ofNullable(Bukkit.getOfflinePlayer(guildPlayer).getName()).orElse("(Unknown Player)");
                lore.add(Component.text(playerName).color(NamedTextColor.AQUA));
            }

            meta.lore(lore);
            meta.getPersistentDataContainer().set(guildIdKey, PersistentDataType.STRING, guild.getId());

            is.setItemMeta(meta);
            inventory.addItem(is);
        }
        player.openInventory(inventory);
    }

    public void onMenuClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if(event.isShiftClick()) return;

        String guildId = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(guildIdKey, PersistentDataType.STRING);
        Guild guild = core.getGuildManager().getGuild(guildId);

        setGuild(guild);
        event.getWhoClicked().sendMessage(Component.text("Ustawiono gildię na ").color(NamedTextColor.GREEN)
                .append(Component.text(guild.getName()).color(NamedTextColor.GOLD)));
        event.getInventory().close();
    }
}
