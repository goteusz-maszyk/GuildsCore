package me.gotitim.guildscore.commands.guild;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.placeholders.Placeholders;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Arrays;

import static me.gotitim.guildscore.util.Components.parseRaw;

public class CreateSubcommand {
    public static void createGuild(Player player, String[] args, GuildsCore plugin) {
        Placeholders ph = new Placeholders(player);
        if(plugin.getGuildManager().getGuild(player) != null) {
            player.sendMessage(parseRaw("create_in_guild"));
            return;
        }
        if(args.length < 3) {
            player.sendMessage(parseRaw("create_args_missing"));
            return;
        }

        try {
            plugin.getGuildManager().createGuild(args[1], String.join(" ", (Arrays.copyOfRange(args, 2, args.length))), player);
        } catch(IllegalStateException e) {
            player.sendMessage(parseRaw("id_in_use", ph));
            return;
        }
        Component message = parseRaw("guild_command.created", ph);
        player.sendMessage(message);
    }
}
