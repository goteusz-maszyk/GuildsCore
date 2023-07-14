package me.gotitim.guildscore.commands;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.commands.guild.CreateSubcommand;
import me.gotitim.guildscore.commands.guild.JoinSubcommand;
import me.gotitim.guildscore.commands.guild.TeamDispaySubcommand;
import me.gotitim.guildscore.guilds.Guild;
import me.gotitim.guildscore.item.GuildHeartItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class GuildCommand extends Command {
    private final GuildsCore plugin;

    public GuildCommand(GuildsCore core) {
        super("guild");
        plugin = core;
        setDescription("Guild management command");
        setAliases(List.of("gildia"));
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) { // args: 0: maincommand, 1, 2...: payload
        if(args.length == 0) {
            sender.sendMessage("Not enough arguments.");
            return false;
        }

        if(args[0].equalsIgnoreCase("save")) {
            if(!sender.hasPermission("guildscore.save")) {sender.sendMessage(plugin.getNoPermissionMessage());return false;}

            plugin.getGuildManager().getGuilds().values().forEach(g -> g.getConfig().save());
            sender.sendMessage(Component.text("Zapisano dane gildii.").color(NamedTextColor.GREEN));
            return true;
        }

        if(args[0].equalsIgnoreCase("load")) {
            if(!sender.hasPermission("guildscore.load")) {sender.sendMessage(plugin.getNoPermissionMessage());return false;}

            plugin.getGuildManager().getGuilds().values().forEach(g -> g.getConfig().reload());
            sender.sendMessage(Component.text("Załadowano dane gildii.").color(NamedTextColor.GREEN));
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

            case "giveheart" -> //TODO: Testing only
                giveHeart(player);
        }
        return true;
    }

    private void giveHeart(Player player) {
        Guild guild = plugin.getGuildManager().getGuild(player);
        if(guild == null) {
            player.sendMessage(Component.text("W gildii nie jesteś").color(NamedTextColor.RED));
            return;
        }

        player.getInventory().addItem(new GuildHeartItem(guild).toItemStack());
    }

    private void deleteGuild(Player player) {
        Guild guild = plugin.getGuildManager().getGuild(player);
        if(guild == null) {
            player.sendMessage(Component.text("A Co ty w ogóle usuwać chcesz?").color(NamedTextColor.RED));
            return;
        }
        guild.delete();
        player.sendMessage(Component.text("Gildia usunięta.").color(NamedTextColor.RED));
    }

    private void inviteToGuild(Player player, String[] args) {
        Guild guild = plugin.getGuildManager().getGuild(player);
        if(guild == null) {
            player.sendMessage(Component.text("A do kąd ty chcesz zapraszać jak w gildii nawet nie jesteś?").color(NamedTextColor.RED));
            return;
        }
        if(args.length < 1) {
            player.sendMessage(Component.text("Użycie: /guild invite <gracz>").color(NamedTextColor.YELLOW));
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        guild.invitePlayer(target, player, true);
    }

    private void leaveGuild(Player player) {
        Guild guild = plugin.getGuildManager().getGuild(player);
        if(guild == null) {
            player.sendMessage(Component.text("A Co ty w ogóle opuszczać chcesz?").color(NamedTextColor.RED));
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
                results.add("create");
                results.add("join");
            } else {
                results.add("invite");
                results.add("leave");
                results.add("prefix");
                results.add("suffix");
                results.add("color");
                results.add("name");
                results.add("delete");
                results.add("giveheart");
                results.add("icon");
            }
        } else if (args.length >= 2) {
            switch (args[0]) {
                case "color" -> results.addAll(NamedTextColor.NAMES.values().stream().map(NamedTextColor::toString).toList());
                case "invite" -> results.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
                case "join" -> results.addAll(plugin.getGuildManager().getGuilds().values().stream().map(Guild::getId).toList());
                case "icon" -> results.addAll(Arrays.stream(Material.values()).map(Material::toString).toList());
            }
        }

        results.sort(Comparator.naturalOrder());

        return StringUtil.copyPartialMatches(args[args.length-1], results, new ArrayList<>());
    }
}
