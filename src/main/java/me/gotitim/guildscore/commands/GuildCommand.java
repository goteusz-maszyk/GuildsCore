package me.gotitim.guildscore.commands;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.commands.guild.CreateSubcommand;
import me.gotitim.guildscore.commands.guild.JoinSubcommand;
import me.gotitim.guildscore.commands.guild.TeamDispaySubcommand;
import me.gotitim.guildscore.guilds.Guild;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static me.gotitim.guildscore.util.Components.getNoPermissionMessage;
import static me.gotitim.guildscore.util.Components.parseRaw;

public class GuildCommand extends Command {
    private final GuildsCore plugin;

    public GuildCommand(GuildsCore core) {
        super("guild");
        plugin = core;
        setDescription("Guild management command");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) { // args: 0: maincommand, 1, 2...: payload
        if(args.length == 0) {
            sender.sendMessage(parseRaw("guild_command.args_missing"));
            return false;
        }

        if(args[0].equalsIgnoreCase("save")) {
            if(!sender.hasPermission("guildscore.save")) {sender.sendMessage(getNoPermissionMessage());return false;}

            plugin.getGuildManager().getGuilds().values().forEach(g -> g.getConfig().save());
            plugin.saveConfig();
            sender.sendMessage(parseRaw("guild_command.data_saved"));
            return true;
        }

        if(args[0].equalsIgnoreCase("load")) {
            if(!sender.hasPermission("guildscore.load")) {sender.sendMessage(getNoPermissionMessage());return false;}

            plugin.getGuildManager().getGuilds().values().forEach(g -> g.getConfig().reload());
            plugin.reloadConfig();
            plugin.getMessages().reload();
            sender.sendMessage(parseRaw("guild_command.data_loaded"));
            return true;
        }

        if(!(sender instanceof Player player)) return false;
        switch (args[0].toLowerCase()) {
            case "create" ->
                CreateSubcommand.createGuild(player, args, plugin);
            case "invite" ->
                inviteToGuild(player, args);
            case "join" ->
                JoinSubcommand.joinGuild(player, args, plugin);
            case "leave" ->
                leaveGuild(player);
            case "delete" ->
                deleteGuild(player);

            case "prefix" ->
                TeamDispaySubcommand.presuffix(player, args, plugin, false);
            case "suffix" ->
                TeamDispaySubcommand.presuffix(player, args, plugin, true);
            case "color" ->
                TeamDispaySubcommand.color(player, args, plugin);
            case "name" ->
                TeamDispaySubcommand.name(player, args, plugin);
            case "icon" ->
                TeamDispaySubcommand.icon(player, args, plugin);
        }
        return true;
    }
    public static Guild guildCheck(GuildsCore plugin, Player player) {
        Guild guild = plugin.getGuildManager().getGuild(player);
        if(guild == null) {
            player.sendMessage(parseRaw("guild_command.no_guild"));
        }
        return guild;
    }

    private void deleteGuild(Player player) {
        Guild guild = guildCheck(plugin, player);
        if(guild == null) return;

        if(!player.hasPermission("guildscore.command.guild.delete")) {
            player.sendMessage(parseRaw("no_permission"));
            return;
        }

        guild.delete();
        player.sendMessage(parseRaw("guild_command.deleted"));
    }

    private void inviteToGuild(Player player, String[] args) {
        Guild guild = guildCheck(plugin, player);
        if(guild == null) return;

        if(args.length < 2) {
            player.sendMessage(parseRaw("guild_command.invite_usage"));
            return;
        }

        if(!player.hasPermission("guildscore.command.guild.invite")) {
            player.sendMessage(parseRaw("no_permission"));
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        guild.invitePlayer(target, player, true);
    }

    private void leaveGuild(Player player) {
        Guild guild = guildCheck(plugin, player);
        if(guild == null) return;

        if(!player.hasPermission("guildscore.command.guild.leave")) {
            player.sendMessage(parseRaw("no_permission"));
            return;
        }

        guild.removePlayer(player);
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        @NotNull List<String> results = new ArrayList<>();
        Guild guild = sender instanceof Player player ? plugin.getGuildManager().getGuild(player) : null;

        if(args.length == 1) {
            if(sender.hasPermission("guildscore.save")) results.add("save");
            if(sender.hasPermission("guildscore.load")) results.add("load");
            if(guild == null) {
                resultsAdd(sender, "create", results);
                resultsAdd(sender, "join", results);
            } else {
                resultsAdd(sender, "invite", results);
                resultsAdd(sender, "leave", results);
                resultsAdd(sender, "prefix", results);
                resultsAdd(sender, "suffix", results);
                resultsAdd(sender, "color", results);
                resultsAdd(sender, "name", results);
                resultsAdd(sender, "delete", results);
                resultsAdd(sender, "icon", results);
            }
        } else if (args.length >= 2) {
            switch (args[0]) {
                case "color" -> resultsAdd(sender, "color", () ->
                        results.addAll(NamedTextColor.NAMES.values().stream().map(NamedTextColor::toString).toList()));
                case "invite" -> resultsAdd(sender, "color", () ->
                        results.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList()));
                case "join" -> resultsAdd(sender, "color", () ->
                        results.addAll(plugin.getGuildManager().getGuilds().values().stream().map(Guild::getId).toList()));
                case "icon" -> resultsAdd(sender, "icon", () ->
                        results.addAll(Arrays.stream(Material.values()).map(Material::toString).toList()));
            }
        }

        results.sort(Comparator.naturalOrder());

        return StringUtil.copyPartialMatches(args[args.length-1], results, new ArrayList<>());
    }

    private void resultsAdd(CommandSender sender, String subcommand, List<String> results) {
        resultsAdd(sender, subcommand, () -> results.add(subcommand));
    }
    private void resultsAdd(CommandSender sender, String subcommand, Runnable run) {
        if(sender.hasPermission("guildscore.command.guild." + subcommand)) run.run();
    }
}
