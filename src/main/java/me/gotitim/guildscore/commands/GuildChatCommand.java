package me.gotitim.guildscore.commands;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.guilds.Guild;
import me.gotitim.guildscore.placeholders.Placeholders;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static me.gotitim.guildscore.listener.ChatListener.*;

public class GuildChatCommand extends Command {
    private final GuildsCore plugin;

    public GuildChatCommand(GuildsCore core) {
        super("guildchat", "gc", "gchat");
        plugin = core;
        setDescription("Guild chat toggle/message");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if(!(sender instanceof Player player)) return false;

        if(args.length == 0) {
            if(toggleGuildChat(player))
                player.sendMessage(Component.text("Włączono czat gildii", NamedTextColor.GREEN));
            else player.sendMessage(Component.text("Wyłączono czat gildii", NamedTextColor.RED));
            return true;
        }
        Guild guild = plugin.getGuildManager().getGuild(player);
        if(guild == null) {
            player.sendMessage("Nie jesteś w gildii, więc nie możesz pisać na czacie gildii.");
            return true;
        }
        String message = String.join(" ", Arrays.copyOfRange(args, 0, args.length));
        Placeholders ph = new Placeholders();
        ph.set("message", message);
        ph.setPlayer(player);
        guild.broadcast(MiniMessage.miniMessage().deserialize(ph.apply(plugin.getConfig().getString("guild_chat_format"))), false);
        if(!guildChatEnabled(player)) player.sendMessage(Component.text("Włączono czat gildii", NamedTextColor.GREEN));
        enableGuildChat(player);
        return true;
    }
}
