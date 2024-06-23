package dev.jvickery.customtime;

import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.dependency.SoftDependency;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.LogPrefix;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.Website;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion.Target;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

@Plugin(name="CustomTime", version="3.0.0")
@ApiVersion(Target.v1_17)
@Description("Plugin to control the speed of the day/night cycle")
@Author("jcv8000")
@Website("https://github.com/jcv8000/CustomTime")
@LogPrefix("CustomTime")
@SoftDependency("Multiverse-Core")

public class CustomTime extends JavaPlugin
{
    static CustomTime inst = null;
    WorldDataMap worldDataMap = null;

    public CustomTime() {
        super();
        CustomTime.inst = this;
    }

    @Override
    public void onEnable() {
        var logger = getLogger();

        worldDataMap = WorldDataMap.loadFromConfig(this);
        if (worldDataMap == null) {
            logger.severe("Error loading data from config file.");
            logger.severe("Plugin will not run.");
            logger.severe("Check the config.yml for errors, or delete it and reset your worlds' time speeds using the /ct command.");
            return;
        }
        logger.info("Successfully loaded data from config file");
        logger.info("Affected worlds: " + worldDataMap.keySet().toString());

        // Disable doDaylightCycle for all affected worlds
        for (var data : worldDataMap.values()) {
            data.world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            logger.info("Disabling doDaylightCycle in world \"" + data.world.getName() + "\".");
        }

        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                for (var data : worldDataMap.values()) {
                    World world = data.world;

                    // Make sure doDaylightCycle is still off
                    if (world.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE)) {
                        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                    }

                    long time = world.getTime();
                    double multiplier = 1.0d;

                    // Choose either day or night multiplier
                    if (time <= 12000) {
                        multiplier = data.dayMultiplier;
                    }
                    else {
                        multiplier = data.nightMultiplier;
                    }

                    // No negative multipliers
                    if (multiplier <= 0.0d) continue;
                    
                    if (multiplier < 1.0d) {
                        // Slow down time
                        double ticksNeeded = 1.0d / multiplier;

                        if (data.tick > ticksNeeded) {
                            world.setTime(time + 1);
                            data.tick = 0;
                        }
                        else data.tick++;
                    }
                    else if (multiplier >= 1.0d) {
                        // Speed up or keep normal
                        world.setTime(time + Math.round(multiplier));
                    }
                }
            }
        }, 0, 1);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);

        // Re-enable doDaylightCycle for all affected worlds
        for (var data : worldDataMap.values()) {
            data.world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
            getLogger().info("Re-enabling doDaylightCycle in world \"" + data.world.getName() + "\".");
        }

        if (worldDataMap != null) {
            WorldDataMap.writeToConfig(this, worldDataMap);
            worldDataMap = null;
        }
    }

    public void updateEntry(String worldName, WorldData data) {
        if (worldDataMap == null) return; // error

        worldDataMap.put(worldName, data);
    }
}
