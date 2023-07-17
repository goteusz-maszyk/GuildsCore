package me.gotitim.guildscore.commands.guild;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.guilds.Guild;
import me.gotitim.guildscore.placeholders.Placeholders;
import org.bukkit.entity.Player;

import static me.gotitim.guildscore.util.Components.parseRaw;

public class JoinSubcommand {
    public static void joinGuild(Player player, String[] args, GuildsCore plugin) {
        Placeholders ph = new Placeholders(player);
        if(args.length < 2) {
            player.sendMessage(parseRaw("join.id_missing", ph));
            return;
        }
        Guild guild = plugin.getGuildManager().getGuild(args[1]);
        Guild playerGuild = plugin.getGuildManager().getGuild(player);

        if(guild == null) {
            player.sendMessage(parseRaw("join.unknown_guild", ph));
            return;
        }

        if(playerGuild == guild) {
            player.sendMessage(parseRaw("join.already_joined", ph));
            return;
        }
        if(playerGuild != null) {
            player.sendMessage(parseRaw("join.already_in_guild", ph.setValue("new_guild", guild)));
            return;
        }
        guild.addPlayer(player);
    }
}
