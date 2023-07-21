package me.gotitim.guildscore.commands.tpa;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.commands.Command;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.gotitim.guildscore.util.Components.parseRaw;

public class TpdenyCommand extends Command {
    private final GuildsCore plugin;

    public TpdenyCommand(GuildsCore plugin) {
        super("tpdeny");
        this.plugin = plugin;
    }

    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        Player deniedPlayer;
        if (this.plugin.tpaStorage.tpaRequest.containsKey(player.getUniqueId())) {
            deniedPlayer = Bukkit.getPlayer(this.plugin.tpaStorage.tpaRequest.get(player.getUniqueId()));
            this.plugin.tpaStorage.tpaRequest.remove(player.getUniqueId());
            if (deniedPlayer != null) {
                deniedPlayer.sendMessage(parseRaw("tpa.rejected_sender"));
                player.sendMessage(parseRaw("tpa.rejected_player"));
            }
        } else {
            player.sendMessage(parseRaw("tpa.no_request"));
        }

        return true;
    }
}
