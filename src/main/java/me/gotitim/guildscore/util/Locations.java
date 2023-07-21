package me.gotitim.guildscore.util;

import org.bukkit.Location;

public class Locations {
    public static double distanceHorizontal(Location loc1, Location loc2) {
        if(!loc1.getWorld().equals(loc2.getWorld())) return Double.MAX_VALUE;
        double deltaX = Math.abs(loc2.getX() - loc1.getX());
        double deltaZ = Math.abs(loc2.getZ() - loc1.getZ());
        return Math.sqrt(deltaX*deltaX + deltaZ*deltaZ);
    }
}
