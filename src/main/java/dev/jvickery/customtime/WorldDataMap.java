package dev.jvickery.customtime;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class WorldDataMap extends HashMap<String, WorldData> {

    public WorldDataMap() {
        super();
    }

    public static WorldDataMap loadFromConfig(JavaPlugin plugin) {
        var map = new WorldDataMap();
        FileConfiguration config = null;
        var logger = plugin.getLogger();

        try {
            config = plugin.getConfig();

            if (!config.isConfigurationSection("worlds")) {
                config.createSection("worlds");
                plugin.saveConfig();
            }
        }
        catch (Exception ex) {
            logger.severe("Error loading CustomTime's config.yml");
            logger.severe(ex.getMessage());
            return null;
        }

        Set<String> worlds = config.getConfigurationSection("worlds").getKeys(false);
        for (String s : worlds) {
            World world = plugin.getServer().getWorld(s);

            if (world == null) {
                logger.warning("Could not find world with name \"" + s + "\", skipping.");
                continue;
            }
            if (world.getEnvironment() == World.Environment.NETHER || world.getEnvironment() == World.Environment.THE_END) {
                logger.warning("World \"" + s + "\" is NETHER or END dimension, skipping.");
                continue;
            }

            double dayMultiplier = config.getDouble("worlds." + s + ".day.multiplier");
            String dayDescription = config.getString("worlds." + s + ".day.desc");

            double nightMultiplier = config.getDouble("worlds." + s + ".night.multiplier");
            String nightDescription = config.getString("worlds." + s + ".night.desc");

            WorldData data = new WorldData(world, dayMultiplier, dayDescription, nightMultiplier, nightDescription);
            map.put(s, data);
        }

        return map;
    }

    public static void writeToConfig(JavaPlugin plugin, WorldDataMap dataMap) {
        var config = plugin.getConfig();
        var logger = plugin.getLogger();

        config.set("worlds", null);
        config.createSection("worlds");

        for (var data : dataMap.values()) {
            String worldName = data.world.getName();
            config.set("worlds." + worldName + ".day.multiplier", data.dayMultiplier);
            config.set("worlds." + worldName + ".day.desc", data.dayMultiplier);

            config.set("worlds." + worldName + ".night.multiplier", data.nightMultiplier);
            config.set("worlds." + worldName + ".night.desc", data.nightDescription);
        }
        plugin.saveConfig();
        
        logger.info("Successfully wrote data to config file.");
    }
}

final class WorldData {
    World world;

    double dayMultiplier;
    String dayDescription;
    
    double nightMultiplier;
    String nightDescription;

    long tick;

    public WorldData(World w, double day, String dayDesc, double night, String nightDesc) {
        world = w;
        dayMultiplier = day;
        dayDescription = dayDesc;
        nightMultiplier = night;
        nightDescription = nightDesc;
        tick = 0;
    }
}