package me.gotitim.guildscore.commands.tpa;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.commands.Command;
import me.gotitim.guildscore.placeholders.Placeholders;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.gotitim.guildscore.util.Components.parseRaw;

public class TpaCommand extends Command {
    private final GuildsCore plugin;

    public TpaCommand(GuildsCore core) {
        super("tpa");
        plugin = core;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        Player player = (Player)sender;
        long keepAliveRequest;

        if (args.length == 0) {
            player.sendMessage(parseRaw("tpa.usage", new Placeholders(player).set("command", "tpa")));
            return false;
        }
        Player target = Bukkit.getServer().getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(parseRaw("tpa.no_target"));
            return true;
        }

        if (target == player) {
            player.sendMessage(parseRaw("tpa.target_yourself"));
            return false;
        }

        if (this.plugin.tpaStorage.tpaRequest.containsKey(player.getUniqueId()) || this.plugin.tpaStorage.tpaRequest.containsKey(target.getUniqueId())) {
            player.sendMessage(parseRaw("tpa.already_requested", new Placeholders(player).setValue("target", target)));
            return true;
        }

        keepAliveRequest = 60 * 20L;
        this.plugin.tpaStorage.sendRequest(player, target);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> this.plugin.tpaStorage.cancelRequest(player), keepAliveRequest);

        return true;
    }
}
