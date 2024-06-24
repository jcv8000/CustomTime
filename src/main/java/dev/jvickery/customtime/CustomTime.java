package dev.jvickery.customtime;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.dependency.SoftDependency;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.permission.Permissions;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.LogPrefix;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.Website;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion.Target;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

import dev.jvickery.customtime.commands.CustomTimeCommand;
import dev.jvickery.customtime.commands.CustomTimeTabCompleter;

@Plugin(name = "CustomTime", version = "3.0.0")
@Description("Plugin to control the speed of the day/night cycle")
@Author("jcv8000")
@Website("https://github.com/jcv8000/CustomTime")
@LogPrefix("CustomTime")

@ApiVersion(Target.v1_17)
@SoftDependency("Multiverse-Core")

@Commands(@Command(name = "customtime", desc = "Command to control day/night length", aliases = {
        "ct" }, permission = "customtime.*", usage = "/customtime - Displays info about affected worlds.\n"
                + "/customtime help - Help command.\n" + "/customtime set (day|night) <value>(s|m|h|d|x) [world]\n"
                + "You can also use the alias /ct"))
@Permissions(@Permission(name = "customtime.*", desc = "Allows /customtime command", defaultValue = PermissionDefault.OP))

public class CustomTime extends JavaPlugin {
    public static CustomTime inst = null;

    public WorldDataMap worldDataMap = null;

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
            logger.severe(
                    "Check the config.yml for errors, or delete it and reset your worlds' time speeds using the /ct command.");
            return;
        }
        logger.info("Successfully loaded data from config file");
        logger.info("Affected worlds: " + worldDataMap.keySet().toString());

        // Disable doDaylightCycle for all affected worlds
        for (var data : worldDataMap.values()) {
            data.world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            logger.info("Disabling doDaylightCycle in world \"" + data.world.getName() + "\".");
        }

        getCommand("customtime").setExecutor(new CustomTimeCommand());
        getCommand("customtime").setTabCompleter(new CustomTimeTabCompleter());
        getCommand("customtime").setAliases(List.of("ct"));
        getCommand("customtime").setPermission("customtime.*");

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
                    double multiplier = 1.0;

                    // Choose either day or night multiplier
                    if (time <= 12000) {
                        multiplier = data.dayMultiplier;
                    }
                    else {
                        multiplier = data.nightMultiplier;
                    }

                    // No negative multipliers
                    if (multiplier <= 0.0)
                        continue;

                    if (multiplier < 1.0) {
                        // Slow down time
                        double ticksNeeded = 1.0 / multiplier;

                        if (data.tick > ticksNeeded) {
                            world.setTime(time + 1);
                            data.tick = 0;
                        }
                        else
                            data.tick++;
                    }
                    else if (multiplier >= 1.0) {
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
            worldDataMap.writeToConfig(this);
            worldDataMap = null;
        }
    }

    public World getDefaultWorld() {
        Properties properties = new Properties();
        try {
            FileInputStream in = new FileInputStream(new File("server.properties"));
            properties.load(in);

            return getServer().getWorld(properties.getProperty("level-name"));
        }
        catch (Exception ex) {
            getLogger().severe("Couldn't load property \"level-name\" from server.properties");
            return null;
        }
    }
}
