package me.gotitim.guildscore.guilds;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class GuildHeart {
    private final Guild guild;
    protected Location location;
    protected final Map<Upgrade, Integer> upgrades = new HashMap<>();
    protected boolean placed = false;

    public GuildHeart(@NotNull Guild guild) {
        this.guild = guild;
        GuildConfiguration config = guild.getConfig();
        ConfigurationSection section = config.getConfigurationSection("heart.upgrades");
        if(section == null) {
            config.set("heart.upgrades." + Upgrade.WORKING_RADIUS.name(), 1);
            return;
        }
        for (String upgrade : section.getKeys(false)) {
            upgrades.put(Upgrade.valueOf(upgrade), config.getInt("heart.upgrades." + upgrade));
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
            guild.getConfig().set("heart.upgrades." + Upgrade.WORKING_RADIUS.name(), 1);
            return;
        }
        for (String upgrade : section.getKeys(false)) {
            upgrades.put(Upgrade.valueOf(upgrade), guild.getConfig().getInt("heart.upgrades." + upgrade));
        }
    }

    public Boolean tryUpgradeLevel(Upgrade upgrade) {
        int currentLevel = getUpgrade(upgrade);
        int upgradeCost = upgrade.getLevelPrice(currentLevel);
        if(upgradeCost == -1) return null;
        if(upgradeCost > guild.getBank()) return Boolean.FALSE;

        setUpgrade(upgrade, currentLevel+1);
        guild.bankWithdraw(upgradeCost);

        return Boolean.TRUE;
    }
    private void setUpgrade(Upgrade upgrade, int level) {
        upgrades.put(upgrade, level);
        guild.getConfig().set("heart.upgrades." + upgrade.name(), level);
    }

    public int getUpgrade(Upgrade upgrade) {
        return upgrades.getOrDefault(upgrade, 0);
    }

    public enum Upgrade {
        WORKING_RADIUS("Working radius", "The heart main working radius, in multiples of 16", Material.SMITHING_TABLE, 19,
                0),
        WARNING_RADIUS("Warning radius", "The heart warning radius, in multiples of 16", Material.ENDER_EYE, 20),
        HEAL_RADIUS("Healing radius", "The heart healing radius, in multiples of 16", Material.AMETHYST_CLUSTER, 21),
        MINING_FATIGUE("Mining fatigue level", "The level of mining fatigue effect, applied in the working radius", Material.MINER_POTTERY_SHERD, 22),
        SLOWNESS("Slowness level", "The level of slowness effect, applied in the working radius", Material.MUD, 23),
        CHEST_LOCK("Chest locking", "Chest locking, applied in the working radius", Material.CHEST, 24);
        private final int shopSlot;
        private final int[] levelPrices;
        private final String friendlyName;
        private final String description;
        private final Material icon;

        /**
         * @param levelPrices Prices of each level of an upgrade
         */
        Upgrade(String name, String description, Material icon, int shopSlot, int... levelPrices) {
            this.friendlyName = name;
            this.description = description;
            this.icon = icon;
            this.shopSlot = shopSlot;
            this.levelPrices = levelPrices;
        }

        public int getLevelPrice(int level) {
            return levelPrices.length > level ? levelPrices[level] : -1;
        }

        public Material getIcon() {
            return icon;
        }

        public int getShopSlot() {
            return shopSlot;
        }

        public String toString() {
            return friendlyName;
        }

        public String getDescription() {
            return description;
        }
    }
}
