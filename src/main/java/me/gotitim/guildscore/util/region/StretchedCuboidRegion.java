package me.gotitim.guildscore.util.region;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class StretchedCuboidRegion {
    private @NotNull World world;
    private double x1;
    private double x2;
    private double z1;
    private double z2;

    public StretchedCuboidRegion(double x1, double z1, double x2, double z2, @NotNull World world) {
        this.world = world;
        this.x1 = Math.min(x1, x2);
        this.x2 = Math.max(x1, x2);
        this.z1 = Math.min(z1, z2);
        this.z2 = Math.max(z1, z2);
    }

    public static StretchedCuboidRegion from(ConfigurationSection section) {
        return new StretchedCuboidRegion(
                section.getDouble("x1"),
                section.getDouble("z1"),
                section.getDouble("x2"),
                section.getDouble("z2"),
                Objects.requireNonNullElse(Bukkit.getWorld(section.getString("world", "world")), Bukkit.getWorlds().get(0)));
    }

    public boolean contains(double x, double z) {
        return x >= this.x1 && x <= this.x2 && z >= this.z1 && z <= this.z2;
    }

    public boolean contains(Location location) {
        return contains(location.getX(), location.getZ());
    }
}
