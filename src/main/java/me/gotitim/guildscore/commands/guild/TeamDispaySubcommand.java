package me.gotitim.guildscore.commands.guild;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.guilds.Guild;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class TeamDispaySubcommand {
    public static void presuffix(Player player, String[] args, GuildsCore plugin, boolean isSuffix) {
        Guild guild = guildCheck(plugin, player);
        if(guild == null) return;

        String raw = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        Component comp = Component.text(isSuffix ? (" " + raw) : (raw + " "));
        if(raw.equals("")) {
            comp = Component.empty();
        }
        if (isSuffix) guild.setSuffix(comp);
        else guild.setPrefix(comp);
        player.sendMessage(Component.text((isSuffix ? "Suffix" : "Prefix") + " gildii ustawiony na ").color(NamedTextColor.GREEN)
                .append(comp.color(NamedTextColor.DARK_AQUA)));
    }

    public static void color(Player player, String[] args, GuildsCore plugin) {
        Guild guild = guildCheck(plugin, player);
        if(guild == null) return;

        if(args.length == 2) {
            NamedTextColor ntc = NamedTextColor.NAMES.value(args[1]);
            if(ntc == null) {
                player.sendMessage(Component.text("Podany kolor nie jest prawidłowy.").color(NamedTextColor.RED));
                return;
            }
            guild.setColor(ntc);
            player.sendMessage(Component.text("Kolor gildii ustawiony na ").color(NamedTextColor.GREEN)
                    .append(Component.text(ntc.toString()).color(ntc)));
        } else {
            guild.setColor(null);
            player.sendMessage(Component.text("Kolor gildii zresetowany").color(NamedTextColor.GREEN));
        }
    }

    private static Guild guildCheck(GuildsCore plugin, Player player) {
        Guild guild = plugin.getGuildManager().getGuild(player);
        if (guild == null) {
            player.sendMessage(Component.text("Nic tu nie zdziałasz, bo w gildii nawet nie jesteś.").color(NamedTextColor.RED));
        }
        return guild;
    }

    public static void name(Player player, String[] args, GuildsCore plugin) {
        Guild guild = guildCheck(plugin, player);
        if(guild == null) return;

        String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        guild.setName(name);
        player.sendMessage(Component.text("Ustawiono nazwę gildii na ").color(NamedTextColor.GREEN)
                .append(Component.text(guild.getName()).color(NamedTextColor.GOLD)));
    }
}
