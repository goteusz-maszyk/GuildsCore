package me.gotitim.guildscore.commands.tpa;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.commands.Command;
import me.gotitim.guildscore.guilds.Guild;
import me.gotitim.guildscore.placeholders.Placeholders;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.gotitim.guildscore.util.Components.parseRaw;

public class BackCommand extends Command {
    private final GuildsCore plugin;

    public BackCommand(GuildsCore core) {
        super("back");
        this.plugin = core;
    }

    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (this.plugin.tpaStorage.backCommandLocation.containsKey(player.getUniqueId())) {
            World world = this.plugin.tpaStorage.backCommandLocation.get(player.getUniqueId()).getWorld();
            if (Bukkit.getWorld(world.getName()) == null) {
                return true;
            } else {
                int delay = this.plugin.getConfig().getInt("tpa_delay");
                if (delay != 0) player.sendMessage(parseRaw("tpa.tp_start", new Placeholders(player)));

                Guild guild = plugin.getGuildManager().getGuild(player);
                if(guild == null) {
                    player.sendMessage(parseRaw("tpa.guild_required"));
                    return true;
                }
                int backCost = plugin.getConfig().getInt("back_cost");
                if(guild.getBank() < backCost) {
                    player.sendMessage(parseRaw("tpa.cannot_afford"));
                    return true;
                }
                if (delay == 0) {
                    guild.bankWithdraw(backCost);
                    guild.broadcast(parseRaw("tpa.broadcast", new Placeholders(player).set("cost", backCost)), false);

                    Location location = player.getLocation();

                    player.teleport(this.plugin.tpaStorage.backCommandLocation.get(player.getUniqueId()));
                    this.plugin.tpaStorage.backCommandLocation.put(player.getUniqueId(), location);
                    this.plugin.tpaStorage.backCommandLocation.remove(player.getUniqueId());
                } else {
                    this.plugin.tpaStorage.locationBackPlayers.put(player.getUniqueId(), player.getLocation());
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
                        if (!this.plugin.tpaStorage.locationBackPlayers.containsKey(player.getUniqueId())) return;

                        Guild nowGuild = plugin.getGuildManager().getGuild(player);
                        if(nowGuild == null) {
                            player.sendMessage(parseRaw("tpa.guild_required"));
                            return;
                        }
                        if(nowGuild.getBank() < backCost) {
                            player.sendMessage(parseRaw("tpa.cannot_afford"));
                            return;
                        }

                        guild.bankWithdraw(backCost);
                        guild.broadcast(parseRaw("tpa.broadcast", new Placeholders(player).set("cost", backCost)), false);

                        Location location = player.getLocation();
                        player.teleport(this.plugin.tpaStorage.backCommandLocation.get(player.getUniqueId()));
                        this.plugin.tpaStorage.backCommandLocation.remove(player.getUniqueId());
                        this.plugin.tpaStorage.backCommandLocation.put(player.getUniqueId(), location);
                        this.plugin.tpaStorage.locationBackPlayers.remove(player.getUniqueId());

                    }, (long)delay * 20L);
                }
            }
        } else {
            player.sendMessage(parseRaw("tpa.no_back"));
        }

        return true;
    }
}
