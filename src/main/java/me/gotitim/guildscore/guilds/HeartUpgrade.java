package me.gotitim.guildscore.guilds;

import me.gotitim.guildscore.GuildsCore;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static me.gotitim.guildscore.GuildsCore.loreComponent;

public class HeartUpgrade {
    public static final Map<String, HeartUpgrade> VALUES = new HashMap<>();
    public static final HeartUpgrade WORKING_RADIUS = new HeartUpgrade("WORKING_RADIUS");
    public static final HeartUpgrade WARNING_RADIUS = new HeartUpgrade("WARNING_RADIUS");
    public static final HeartUpgrade HEAL_RADIUS = new HeartUpgrade("HEAL_RADIUS");
    public static final HeartUpgrade MINING_FATIGUE = new HeartUpgrade("MINING_FATIGUE");
    public static final HeartUpgrade SLOWNESS = new HeartUpgrade("SLOWNESS");
    public static final HeartUpgrade CHEST_LOCK = new HeartUpgrade("CHEST_LOCK");


    private final String internal;
    private String friendlyName;
    private String description;
    private Material icon;
    private int shopSlot;
    private @NotNull List<Integer> levelPrices = new ArrayList<>();

    public HeartUpgrade(String internal) {
        this.internal = internal;
        VALUES.put(internal, this);
    }

    public static void loadConfig(GuildsCore core) {
        core.getLogger().info("Loading upgrades...");
        VALUES.forEach((key, upgrade) -> {
            core.getLogger().info("Loading upgrade " + key);
            try{
                upgrade.friendlyName = core.getConfig().getString("upgrades." + key + ".name");
                upgrade.description = core.getConfig().getString("upgrades." + key + ".description");
                upgrade.shopSlot = core.getConfig().getInt("upgrades." + key + ".slot");
                upgrade.levelPrices = core.getConfig().getIntegerList("upgrades." + key + ".level_prices");
                upgrade.icon = Material.getMaterial(Objects.requireNonNull(core.getConfig().getString("upgrades." + key + ".icon")));
            } catch (NullPointerException e) {core.getLogger().severe("Found null material in upgrade config!");}
            if(upgrade.icon == null) upgrade.icon = Material.BEDROCK;
        });
    }

    public static HeartUpgrade valueOf(String name) {
        return VALUES.get(name);
    }

    public static Collection<HeartUpgrade> values() {
        return VALUES.values();
    }

    public int getLevelPrice(int level) {
        return levelPrices.size() > level ? levelPrices.get(level) : -1;
    }

    public @NotNull Material getIcon() {
        return icon;
    }

    public int getShopSlot() {
        return shopSlot;
    }

    public String toString() {
        return friendlyName;
    }

    public @NotNull Component getDescription() {
        return loreComponent(description);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof HeartUpgrade u && Objects.equals(u.internal, internal);
    }
    public String name() {
        return internal;
    }
}