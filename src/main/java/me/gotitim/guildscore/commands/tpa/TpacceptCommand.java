package me.gotitim.guildscore.commands.tpa;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.commands.Command;
import me.gotitim.guildscore.guilds.Guild;
import me.gotitim.guildscore.guilds.HeartUpgrade;
import me.gotitim.guildscore.placeholders.Placeholders;
import me.gotitim.guildscore.util.Locations;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.gotitim.guildscore.util.Components.parseRaw;

public class TpacceptCommand extends Command {
    private final GuildsCore plugin;

    public TpacceptCommand(GuildsCore core) {
        super("tpaccept");
        plugin = core;
    }

    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true; // TODO: check if they can afford ?

        Player theSender;
        int delay;
        if (this.plugin.tpaStorage.tpaRequest.containsKey(player.getUniqueId())) {
            theSender = Bukkit.getPlayer(this.plugin.tpaStorage.tpaRequest.get(player.getUniqueId()));
            if (theSender == null) {
                this.plugin.tpaStorage.tpaRequest.remove(player.getUniqueId());
                return true;
            }

            delay = this.plugin.getConfig().getInt("tpa_delay");

            Placeholders ph = new Placeholders(player);
            if (delay != 0) theSender.sendMessage(parseRaw("tpa.tp_start", ph));

            theSender.sendMessage(parseRaw("tpa.accepted_sender", ph));
            player.sendMessage(parseRaw("tpa.accepted_player", ph));
            this.plugin.tpaStorage.locationPlayers.put(theSender.getUniqueId(), theSender.getLocation());
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
                if (this.plugin.tpaStorage.locationPlayers.containsKey(theSender.getUniqueId()) && this.plugin.tpaStorage.tpaRequest.containsKey(player.getUniqueId())) {
                    Guild guild = plugin.getGuildManager().getGuild(theSender);
                    if(guild == null) {
                        theSender.sendMessage(parseRaw("tpa.guild_required"));
                        return;
                    }
                    int tpaCost = plugin.getConfig().getInt("tpa_cost");
                    if(guild.getBank() < tpaCost) {
                        theSender.sendMessage(parseRaw("tpa.cannot_afford"));
                        return;
                    }
                    for (Guild g : plugin.getGuildManager().getGuilds().values()) {
                        if(g.getPlayers().contains(theSender.getUniqueId())) continue;
                        if(Locations.distanceHorizontal(g.getHeart().getLocation(), theSender.getLocation()) <= g.getHeart().getUpgrade(HeartUpgrade.WORKING_RADIUS) * 16)
                            return;
                    }
                    guild.bankWithdraw(tpaCost);
                    guild.broadcast(parseRaw("tpa.broadcast", new Placeholders(theSender).set("cost", tpaCost)), false);

                    this.plugin.tpaStorage.backCommandLocation.put(theSender.getUniqueId(), theSender.getLocation());

                    theSender.teleport(player);
                }
                this.plugin.tpaStorage.locationPlayers.remove(theSender.getUniqueId());
                this.plugin.tpaStorage.tpaRequest.remove(player.getUniqueId());

            }, delay * 20L);
        }
        return true;
    }
}
