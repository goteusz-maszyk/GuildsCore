package me.gotitim.guildscore.commands.guild;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.guilds.Guild;
import me.gotitim.guildscore.placeholders.Placeholders;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

import static me.gotitim.guildscore.commands.GuildCommand.guildCheck;
import static me.gotitim.guildscore.util.Components.legacyColors;
import static me.gotitim.guildscore.util.Components.parseRaw;

public class TeamDispaySubcommand {
    public static void presuffix(Player player, String[] args, GuildsCore plugin, boolean isSuffix) {
        Guild guild = guildCheck(plugin, player);
        if(guild == null) return;

        if(isSuffix && !player.hasPermission("guildscore.command.guild.suffix")) {
            player.sendMessage(parseRaw("no_permission"));
            return;
        } else if(!isSuffix && !player.hasPermission("guildscore.command.guild.prefix")) {
            player.sendMessage(parseRaw("no_permission"));
            return;
        }

        String raw = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        Component comp = Component.text(isSuffix ? (" " + raw) : (raw + " "));
        if(raw.equals("")) {
            comp = Component.empty();
        }
        if (isSuffix) guild.setSuffix(comp);
        else guild.setPrefix(comp);
        player.sendMessage(parseRaw("guild_command.presuffix_set", new Placeholders(player)
                .set("value", legacyColors(comp)).set("presuffix", (isSuffix ? "Suffix" : "Prefix"))
        ));
    }

    public static void color(Player player, String[] args, GuildsCore plugin) {
        Guild guild = guildCheck(plugin, player);
        if(guild == null) return;

        if(!player.hasPermission("guildscore.command.guild.color")) {
            player.sendMessage(parseRaw("no_permission"));
            return;
        }

        if(args.length == 2) {
            NamedTextColor ntc = NamedTextColor.NAMES.value(args[1]);
            if(ntc == null) {
                player.sendMessage(parseRaw("guild_command.unknown_color", new Placeholders(player).set("color", args[1])));
                return;
            }
            guild.setColor(ntc);
            player.sendMessage(parseRaw("guild_command.color_set", new Placeholders(player)));
        } else {
            guild.setColor(null);
            player.sendMessage(parseRaw("guild_command.color_reset"));
        }
    }

    public static void name(Player player, String[] args, GuildsCore plugin) {
        Guild guild = guildCheck(plugin, player);
        if(guild == null) return;

        if(!player.hasPermission("guildscore.command.guild.name")) {
            player.sendMessage(parseRaw("no_permission"));
            return;
        }

        String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        guild.setName(name);
        player.sendMessage(parseRaw("guild_command.name_set", new Placeholders(player)));
    }

    public static void icon(Player player, String[] args, GuildsCore plugin) {
        Guild guild = guildCheck(plugin, player);
        if(guild == null) return;

        if(!player.hasPermission("guildscore.command.guild.icon")) {
            player.sendMessage(parseRaw("no_permission"));
            return;
        }

        if(args.length < 2) {
            guild.setIcon(null);
            player.sendMessage(parseRaw("guild_command.icon_reset"));
            return;
        }
        Material material = Material.getMaterial(args[1]);
        if(material == null) {
            player.sendMessage(parseRaw("guild_command.unknown_material"));
            return;
        }
        guild.setIcon(material);
        player.sendMessage(parseRaw("guild_command.icon_set", new Placeholders(player)));
    }
}
