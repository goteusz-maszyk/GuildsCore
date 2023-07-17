package me.gotitim.guildscore.util;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.item.ItemBuilder;
import me.gotitim.guildscore.placeholders.Placeholders;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Components {
    private static GuildsCore core;

    public static void setCore(GuildsCore core) {
        Components.core = core;
    }

    public static Component getNoPermissionMessage() {
        return parseMiniMessage(getRaw("no_permission"));
    }

    public static Component loreComponent(String line) {
        return parseMiniMessage(line).decoration(TextDecoration.ITALIC, false);
    }

    public static Component loreComponentRaw(String key) {
        return loreComponent(getRaw(key));
    }

    public static Component loreComponentRaw(String key, Placeholders ph) {
        return loreComponent(ph.apply(getRaw(key)));
    }

    public static Component parseMiniMessage(String text) {
        return MiniMessage.miniMessage().deserialize(text);
    }

    public static String getRaw(String key) {
        return core.getConfig().getString("messages." + key);
    }

    public static Component parseRaw(String key) {
        return parseMiniMessage(getRaw(key));
    }
    public static Component parseRaw(String key, Placeholders ph) {
        return parseMiniMessage(ph.apply(getRaw(key)));
    }

    public static String legacyColors(Component component) {
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    public static ItemStack bankTooltip(Player player) {
        return new ItemBuilder(Material.GOLD_INGOT)
                .setName(loreComponentRaw("guild.tooltip_bank", new Placeholders(player))).toItemStack();
    }
}
