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
import org.bukkit.scheduler.BukkitScheduler;

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
    
    BukkitScheduler scheduler;
    CTConfig config = null;

    @Override
    public void onEnable() {
        inst = this;
        scheduler = getServer().getScheduler();

        config = CTConfig.loadConfig(this);
        if (config == null) {
            // config error
            return;
        }

        // Disable doDaylightCycle for all affected worlds
        for (var data : config.dataList) {
            data.world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        }

        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                for (var data : config.dataList) {
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
        config.writeConfig(this);
        config = null;
    }
}
