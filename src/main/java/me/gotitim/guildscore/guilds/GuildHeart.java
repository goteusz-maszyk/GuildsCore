package me.gotitim.guildscore.guilds;

import com.google.protobuf.ExperimentalApi;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
//TODO: Guild Heart
public class GuildHeart {
    private final Guild guild;
    protected Location location;
    protected final Map<Upgrade, Integer> upgrades = new HashMap<>();
    protected boolean placed = false;

    public GuildHeart(@NotNull Guild guild) {
        this.guild = guild;
        GuildConfiguration config = guild.getConfig();
        ConfigurationSection section = config.getConfigurationSection("heart_upgrades");
        if(section == null) {
            config.set("heart_upgrades." + Upgrade.WORKING_RADIUS, 0);
            return;
        }
        for (String upgrade : section.getKeys(false)) {
            upgrades.put(Upgrade.valueOf(upgrade), config.getInt("heart_upgrades." + upgrade));
        }

        this.location = config.getLocation("location", Bukkit.getWorld("world").getSpawnLocation());
        config.set("location", location);
    }

    public void place(Location location) {
        placed = true;
        this.location = location;
        guild.getConfig().set("location", location);
    }

    public void pickup() {
        placed = false;
        this.location = Bukkit.getWorld("world").getSpawnLocation();
        guild.getConfig().set("location", location);
    }

    public boolean isPlaced() {
        return placed;
    }

    public Location getLocation() {
        return location;
    }

    public void loadConfig() {
        this.location = guild.getConfig().getLocation("location", Bukkit.getWorld("world").getSpawnLocation());
    }

    public enum Upgrade {
        WORKING_RADIUS, WARNING_RADIUS, HEAL_RADIUS, SLOWNESS_TRAP, CHEST_LOCK;
        private final int[] levelPrices;

        /**
         * @param levelPrices Prices of each level of an upgrade
         */
        Upgrade(int... levelPrices) {
            this.levelPrices = levelPrices;
        }

        public int getLevelPrice(int level) {
            return levelPrices[level];
        }
    }
}
