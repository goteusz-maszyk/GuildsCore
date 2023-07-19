package me.gotitim.guildscore.commands.tpa;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.commands.Command;
import me.gotitim.guildscore.placeholders.Placeholders;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.gotitim.guildscore.util.Components.parseRaw;

public class TpcancelCommand extends Command {
    private final GuildsCore plugin;

    public TpcancelCommand(GuildsCore core) {
        super("tpcancel");
        plugin = core;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length == 0) {
            player.sendMessage(parseRaw("tpa.usage", new Placeholders(player).set("command", "tpcancel")));
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(parseRaw("tpa.no_target"));
            return true;
        }
        if (target == player) {
            player.sendMessage(parseRaw("tpa.target_yourself"));
            return false;
        }

        if (plugin.tpaStorage.tpaRequest.containsKey(target.getUniqueId()) && plugin.tpaStorage.tpaRequest.get(target.getUniqueId()).equals(player.getUniqueId())) {
            player.sendMessage(parseRaw("tpa.cancelled"));
            this.plugin.tpaStorage.tpaRequest.remove(target.getUniqueId());
        } else if (this.plugin.tpaStorage.tpaHereRequest.containsKey(target.getUniqueId()) && this.plugin.tpaStorage.tpaHereRequest.get(target.getUniqueId()).equals(player.getUniqueId())) {
            player.sendMessage(parseRaw("tpa.cancelled"));
            this.plugin.tpaStorage.tpaHereRequest.remove(target.getUniqueId());
        } else {
            player.sendMessage(parseRaw("tpa.no_request"));
        }

        return true;
    }
}
