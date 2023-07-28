package me.gotitim.guildscore.guilds;

import org.bukkit.Chunk;

import java.util.ArrayList;
import java.util.List;

public class GuildChunkLoader {
    private final Guild guild;
    private final List<Chunk> chunks = new ArrayList<>();
    private final int maxChunks;

    GuildChunkLoader(Guild guild) {
        this.guild = guild;
        this.maxChunks = guild.getGuildManager().getPlugin().getConfig().getInt("forceload_limit");
    }

    public boolean chunkLoad(Chunk chunk) {
        if(this.maxChunks >= chunks.size()) return false;
        // TODO: check if another guild chunkloads this
        chunks.add(chunk);
        chunk.setForceLoaded(true);
        return true;
    }

    public boolean isLoaded(Chunk chunk) {
        return chunks.contains(chunk);
    }
    public boolean chunkUnLoad(Chunk chunk) {
        chunk.setForceLoaded(false);
        return chunks.remove(chunk);
    }
}
