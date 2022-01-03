package io.github.jcv8000.CustomTime;

import org.bukkit.World;

public class CTWorldData {
    World world;
    double dayMult;
    double nightMult;
    long tick;

    public CTWorldData(World w, double day, double night) {
        world = w;
        dayMult = day;
        nightMult = night;
        tick = 0;
    }
}