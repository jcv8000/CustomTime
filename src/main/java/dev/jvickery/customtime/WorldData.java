package dev.jvickery.customtime;

import org.bukkit.World;

public final class WorldData {
    public World world;

    public double dayMultiplier;

    public String dayDescription;

    public double nightMultiplier;

    public String nightDescription;

    public long tick;

    public WorldData(World w, double day, String dayDesc, double night, String nightDesc) {
        world = w;
        dayMultiplier = day;
        dayDescription = dayDesc;
        nightMultiplier = night;
        nightDescription = nightDesc;
        tick = 0;
    }
}
