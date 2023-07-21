package me.gotitim.guildscore.item;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.guilds.Guild;
import me.gotitim.guildscore.placeholders.Placeholders;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import static me.gotitim.guildscore.util.Components.loreComponentRaw;

public final class GuildCompass extends ItemBuilder {
    private final GuildsCore core;
    private String guildId;

    public GuildCompass(GuildsCore core) {
        super(Material.COMPASS);
        this.core = core;
        setName(loreComponentRaw("compass.name"));
        buildLore(null);
        setPersistentData(core.itemIdKey, PersistentDataType.STRING, "GUILD_COMPASS");
        guildId = null;
    }

    public GuildCompass(GuildsCore core, ItemStack itemStack) {
        super(itemStack);
        this.core = core;
        guildId = getPersistentData(core.guildIdKey, PersistentDataType.STRING);
    }

    public GuildCompass setGuild(Guild guild) {
        buildLore(guild);
        guildId = guild.getId();

        setLodestone(guild.getHeart().getLocation()).setPersistentData(core.guildIdKey, PersistentDataType.STRING, guild.getId());
        return this;
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
        setGuild(core.getGuildManager().getGuild(guildId));
    }
}
