package me.gotitim.guildscore.commands;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.guilds.Guild;
import me.gotitim.guildscore.placeholders.Placeholders;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static me.gotitim.guildscore.listener.ChatListener.*;
import static me.gotitim.guildscore.util.Components.parseMiniMessage;
import static me.gotitim.guildscore.util.Components.parseRaw;

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
                player.sendMessage(parseRaw("guild.chat_enabled"));
            else player.sendMessage(parseRaw("guild.chat_disabled"));
            return true;
        }
        Guild guild = plugin.getGuildManager().getGuild(player);
        if(guild == null) {
            player.sendMessage(parseRaw("guild.chat_unavailable"));
            return true;
        }
        String message = String.join(" ", Arrays.copyOfRange(args, 0, args.length));
        Placeholders ph = new Placeholders();
        ph.set("message", message);
        ph.setPlayer(player);
        guild.broadcast(parseMiniMessage(ph.apply(plugin.getConfig().getString("guild_chat_format"))), false);
        if(!guildChatEnabled(player)) player.sendMessage(parseRaw("guild.chat_enabled"));
        enableGuildChat(player);
        return true;
    }
}
