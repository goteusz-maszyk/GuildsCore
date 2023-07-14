package me.gotitim.guildscore.commands.guild;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.guilds.Guild;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class CreateSubcommand {
    public static void createGuild(Player player, String[] args, GuildsCore plugin) {
        if(plugin.getGuildManager().getGuild(player) != null) {
            player.sendMessage(Component.text("Nie możesz utworzyć gildii, gdy jesteś już w jakiejś!").color(NamedTextColor.YELLOW));
            return;
        }
        if(args.length < 3) {
            player.sendMessage(Component.text("Podaj unikalne ID i nazwę dla twojej gildii!").color(NamedTextColor.RED));
            return;
        }
        Guild guild;
        try {
            guild = plugin.getGuildManager().createGuild(args[1], String.join(" ", (Arrays.copyOfRange(args, 2, args.length))), player);
        } catch(IllegalStateException e) {
            player.sendMessage(Component.text("Gildia o podanym ID już istnieje!").color(NamedTextColor.RED));
            return;
        }
        Component message = Component.text("Utworzyłeś gildię ").color(NamedTextColor.GREEN);
        message.append(Component.text(guild.getId() + " (" + guild.getName() + ")").color(NamedTextColor.AQUA));
        player.sendMessage(message);
    }
}
