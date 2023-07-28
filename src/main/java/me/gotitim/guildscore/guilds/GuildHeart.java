package me.gotitim.guildscore.guilds;

import me.gotitim.guildscore.util.Locations;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class GuildHeart {
    private final Guild guild;
    protected Location location;
    protected final Map<HeartUpgrade, Integer> upgrades = new HashMap<>();
    protected boolean placed = false;

    public GuildHeart(@NotNull Guild guild) {
        this.guild = guild;
        GuildConfiguration config = guild.getConfig();
        ConfigurationSection section = config.getConfigurationSection("heart.upgrades");
        if(section == null) {
            config.set("heart.upgrades." + HeartUpgrade.WORKING_RADIUS.name(), 1);
            return;
        }
        for (String upgrade : section.getKeys(false)) {
            upgrades.put(HeartUpgrade.valueOf(upgrade), config.getInt("heart.upgrades." + upgrade));
        }

        this.location = config.getLocation("heart.location", Bukkit.getWorld("world").getSpawnLocation());
        config.set("heart.location", location);
        this.placed = config.getBoolean("heart.placed");
    }

    public void place(Location location) {
        placed = true;
        this.location = location;
        guild.getConfig().set("heart.location", location);
        guild.getConfig().set("heart.placed", true);
    }

    public void pickup() {
        placed = false;
        this.location = Bukkit.getWorld("world").getSpawnLocation();
        guild.getConfig().set("heart.location", location);
        guild.getConfig().set("heart.placed", false);
    }

    public boolean isPlaced() {
        return placed;
    }

    public Location getLocation() {
        return location;
    }

    public void loadConfig() {
        this.location = guild.getConfig().getLocation("heart.location", Bukkit.getWorld("world").getSpawnLocation());
        this.placed = guild.getConfig().getBoolean("heart.placed");

        ConfigurationSection section = guild.getConfig().getConfigurationSection("heart.upgrades");
        if(section == null) {
            guild.getConfig().set("heart.upgrades." + HeartUpgrade.WORKING_RADIUS.name(), 1);
            return;
        }
        upgrades.clear();
        for (String upgrade : section.getKeys(false)) {
            upgrades.put(HeartUpgrade.valueOf(upgrade), guild.getConfig().getInt("heart.upgrades." + upgrade));
        }
    }

    public Boolean tryUpgradeLevel(HeartUpgrade upgrade) {
        int currentLevel = getUpgrade(upgrade);
        int upgradeCost = upgrade.getLevelPrice(currentLevel);
        if(upgradeCost == -1) return null;
        if(upgradeCost > guild.getBank()) return Boolean.FALSE;

        setUpgrade(upgrade, currentLevel+1);
        guild.bankWithdraw(upgradeCost);

        return Boolean.TRUE;
    }
    private void setUpgrade(HeartUpgrade upgrade, int level) {
        upgrades.put(upgrade, level);
        guild.getConfig().set("heart.upgrades." + upgrade.name(), level);
    }

    public int getUpgrade(HeartUpgrade upgrade) {
        return upgrades.getOrDefault(upgrade, 0);
    }

    public boolean affects(Location location) {
        return placed && Locations.distanceHorizontal(this.location, location) <= getUpgrade(HeartUpgrade.WORKING_RADIUS) * 16;
    }

    public boolean affects(Chunk chunk) {
        return affects(chunk.getBlock(0, 0, 0).getLocation()) ||
                affects(chunk.getBlock(15, 0, 0).getLocation()) ||
                affects(chunk.getBlock(0, 0, 15).getLocation()) ||
                affects(chunk.getBlock(15, 0, 15).getLocation());
    }
}
