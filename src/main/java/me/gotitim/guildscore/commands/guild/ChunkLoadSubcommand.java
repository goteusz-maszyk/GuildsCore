package me.gotitim.guildscore.commands.guild;

import me.gotitim.guildscore.GuildsCore;
import me.gotitim.guildscore.guilds.Guild;
import me.gotitim.guildscore.placeholders.Placeholders;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import static me.gotitim.guildscore.commands.GuildCommand.guildCheck;
import static me.gotitim.guildscore.util.Components.parseRaw;

public class ChunkLoadSubcommand {
    public static void chunkLoad(Player player, GuildsCore plugin) {
        Guild guild = guildCheck(plugin, player);
        if(guild == null) return;

        if(!player.hasPermission("guildscore.command.guild.chunkload")) {
            player.sendMessage(parseRaw("no_permission"));
            return;
        }

        Chunk chunk = player.getChunk();
        Placeholders ph = new Placeholders(player).set("chunk_x", chunk.getX()).set("chunk_z", chunk.getZ());
        Guild affected = guild.getGuildManager().negativeHeartAffect(player, true);
        if(affected != null) {
            player.sendMessage(parseRaw("tpa.heart_affected"));
            return;
        }
        if(guild.getChunkLoader().isLoaded(chunk)) {
            if(guild.getChunkLoader().chunkLoad(chunk)) {
                guild.broadcast(parseRaw("guild.chunk_loaded", ph), true);
            } else {
                player.sendMessage(parseRaw("guild.chunk_limit_reached"));
            }
        } else {
            if(guild.getChunkLoader().chunkUnLoad(chunk)) {
                guild.broadcast(parseRaw("guild.chunk_unloaded", ph), true);
            } else {
                player.sendMessage(parseRaw("guild.chunk_not_loaded"));
            }
        }
    }
}
