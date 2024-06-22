package dev.jvickery.customtime;

import java.util.ArrayList;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class CTConfig {
    public ArrayList<WorldData> dataList;

    public static CTConfig loadConfig(JavaPlugin plugin) {
        var list = new ArrayList<WorldData>();
        FileConfiguration config = null;

        try {
            config = plugin.getConfig();

            if (!config.isConfigurationSection("worlds")) {
                config.createSection("worlds");
            }

            plugin.saveConfig();
        }
        catch (Exception ex) {
            // error: could not load config
            return null;
        }

        Set<String> worlds = config.getConfigurationSection("worlds").getKeys(false);
        for (String s : worlds) {
            World world = plugin.getServer().getWorld(s);

            if (world == null) {
                // error: world not found
                continue;
            }
            if (world.getEnvironment() != World.Environment.NORMAL) {
                // error: not normal world
                continue;
            }

            double dayMultiplier = config.getDouble("worlds." + s + ".day.multiplier");
            String dayDescription = config.getString("worlds." + s + ".day.desc");

            double nightMultiplier = config.getDouble("worlds." + s + ".night.multiplier");
            String nightDescription = config.getString("worlds." + s + ".night.desc");

            WorldData data = new WorldData(world, dayMultiplier, dayDescription, nightMultiplier, nightDescription);
            list.add(data);
        }

        CTConfig c = new CTConfig();
        c.dataList = list;
        return c;
    }

    public void writeConfig(JavaPlugin plugin) {
        var config = plugin.getConfig();
        config.set("worlds", null);
        config.createSection("worlds");

        for (var data : dataList) {
            String worldName = data.world.getName();
            config.set("worlds." + worldName + ".day.multiplier", data.dayMultiplier);
            config.set("worlds." + worldName + ".day.desc", data.dayMultiplier);

            config.set("worlds." + worldName + ".night.multiplier", data.nightMultiplier);
            config.set("worlds." + worldName + ".night.desc", data.nightDescription);
        }
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