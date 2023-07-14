package me.gotitim.guildscore.item;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.guilds.Guild;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

//TODO: Guild Heart
public class GuildHeartItem extends ItemBuilder {
    private final Guild guild;
    public final NamespacedKey guildIdKey;
    private final GuildsCore core;

    public GuildHeartItem(Guild guild) {
        super(Material.END_CRYSTAL);
        core = guild.getGuildManager().getPlugin();
        guildIdKey = new NamespacedKey(core, "guildId");
        this.guild = guild;
        setName("§r§bGuild Heart");
        setPersistentData(core.itemIdKey, PersistentDataType.STRING, "GUILD_HEART");
    }

    public GuildHeartItem(GuildsCore core, ItemStack is) {
        super(is);
        this.core = core;
        guildIdKey = new NamespacedKey(core, "guildId");
        setName("§r§bGuild Heart");

        String guildId = getPersistentData(guildIdKey, PersistentDataType.STRING);
        guild = core.getGuildManager().getGuild(guildId);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        event.setCancelled(true);
    }
}
