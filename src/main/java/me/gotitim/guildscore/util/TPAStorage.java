package me.gotitim.guildscore.util;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.commands.tpa.*;
import me.gotitim.guildscore.guilds.Guild;
import me.gotitim.guildscore.placeholders.Placeholders;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static me.gotitim.guildscore.util.Components.parseRaw;

public class TPAStorage {
    public final Map<UUID, Location> locationPlayers = new HashMap<>();
    GuildsCore plugin;
    public Map<UUID, Location> backCommandLocation = new HashMap<>();
    public Map<UUID, Location> locationBackPlayers = new HashMap<>();
    public Map<UUID, UUID> tpaHereRequest = new HashMap<>();
    public Map<UUID, UUID> tpaRequest = new HashMap<>();

    public TPAStorage(GuildsCore plugin) {
        this.plugin = plugin;
    }

    public void cancelRequest(Player p) {
        if (tpaRequest.containsKey(p.getUniqueId())) {
            Player player = Bukkit.getPlayer(tpaRequest.get(p.getUniqueId()));
            tpaRequest.remove(p.getUniqueId());

            if (player != null) player.sendMessage(parseRaw("tpa.timeout"));
        }
    }

    public void sendRequest(Player sender, Player receiver) {
        Guild guild = plugin.getGuildManager().getGuild(sender);
        if(guild == null) {
            sender.sendMessage(parseRaw("tpa.guild_required"));
            return;
        }
        if(guild.getBank() < plugin.getConfig().getInt("tpa_cost")) {
            sender.sendMessage(parseRaw("tpa.cannot_afford"));
            return;
        }
        sender.sendMessage(parseRaw("tpa.sending_tp_request", new Placeholders(sender).setValue("targetplayer", receiver)));

        receiver.sendMessage(parseRaw("tpa.received_tp_request", new Placeholders(sender).setValue("targetplayer", receiver)));
        tpaRequest.put(receiver.getUniqueId(), sender.getUniqueId());
    }

    public void loadCommands() {
        new TpaCommand(plugin);
        new TpacceptCommand(plugin);
        new TpcancelCommand(plugin);
        new TpdenyCommand(plugin);
        new BackCommand(plugin);
    }
}
