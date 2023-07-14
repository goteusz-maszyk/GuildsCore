package me.gotitim.guildscore.commands.guild;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.guilds.Guild;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class JoinSubcommand {
    public static void joinGuild(Player player, String[] args, GuildsCore plugin) {
        if(args.length < 2) {
            player.sendMessage(Component.text("Podaj ID gildii, do której chcesz dołączyć!").color(NamedTextColor.RED));
            return;
        }
        Guild guild = plugin.getGuildManager().getGuild(args[1]);
        Guild playerGuild = plugin.getGuildManager().getGuild(player);

        if(guild == null) {
            player.sendMessage(Component.text("Nie znaleziono gildii.").color(NamedTextColor.RED));
            return;
        }

        if(playerGuild == guild) {
            player.sendMessage(Component.text("Jesteś już członkiem gidii ").color(NamedTextColor.YELLOW)
                    .append(Component.text(playerGuild.getName()).color(NamedTextColor.GOLD)));
            return;
        }
        if(playerGuild != null) {
            player.sendMessage(
                    Component.text("Jesteś już członkiem gidii ").color(NamedTextColor.YELLOW)
                            .append(Component.text(playerGuild.getName()).color(NamedTextColor.GOLD))
                            .append(Component.text(". By dołączyć do ").color(NamedTextColor.YELLOW))
                            .append(Component.text(guild.getName()).color(NamedTextColor.GOLD))
                            .append(Component.text(" musisz najpierw opuścić obecną. (/guild leave)").color(NamedTextColor.YELLOW))
            );
            return;
        }
        guild.addPlayer(player);
    }
}
