package dev.jvickery.customtime.commands;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.jvickery.customtime.CustomTime;
import dev.jvickery.customtime.WorldData;

enum Time {
    DAY, NIGHT
}

enum TimeMeasurement {
    S, M, H, D, X
}

public class CustomTimeCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        World assumedWorld = CustomTime.inst.getDefaultWorld();

        if (sender instanceof Player) {
            Player player = (Player) sender;
            assumedWorld = player.getWorld();

            // TODO see if checking permissions is necessary
            // if (!sender.hasPermission("customtime.*")) return false;
        }
        else if (sender instanceof BlockCommandSender) {
            BlockCommandSender b = (BlockCommandSender) sender;
            assumedWorld = b.getBlock().getWorld();
        }

        if (args.length == 0) {
            InfoCommand();
            return true;
        }
        else if (args.length == 1 && args[0].equals("help")) {
            HelpCommand();
            return true;
        }

        // /ct set (day|night) <value>(s|m|h|d|x) [world]
        // Returning true if there's an error just so I can send a
        // custom error message and not show the "Usage" message
        if ((args.length == 3 || args.length == 4) && args[0].equals("set")) {
            Time daynight = null;
            double value = 0.0;
            double actualMultiplier = 0.0;
            TimeMeasurement measurement;
            World world = null;
            String description = null;

            // Get day/night
            if (args[1].equals("day"))
                daynight = Time.DAY;
            else if (args[1].equals("night"))
                daynight = Time.NIGHT;
            else {
                sender.sendMessage(ChatColor.RED + "Error: Expected \"day\" or \"night\", got \"" + args[1] + "\"");
                return true;
            }

            // Get duration/multiplier value
            try {
                String substring = args[2].substring(0, args[2].length() - 1);
                double v = Double.parseDouble(substring);

                if (v <= 0.0) {
                    sender.sendMessage(ChatColor.RED + "Error: value must be greater than 0");
                    return true;
                }

                value = v;
            }
            catch (Exception ex) {
                sender.sendMessage(ChatColor.RED + "Error parsing <value>(s|m|h|d|x) argument");
                return true;
            }

            // Get time measurement
            if (args[2].endsWith("s")) {
                measurement = TimeMeasurement.S;
                description = value + " " + (value == 1.0 ? "second" : "seconds");
                actualMultiplier = 10.0 / (value / 60.0);
            }
            else if (args[2].endsWith("m")) {
                measurement = TimeMeasurement.M;
                description = value + " " + (value == 1.0 ? "minute" : "minutes");
                actualMultiplier = 10.0 / value;
            }
            else if (args[2].endsWith("h")) {
                measurement = TimeMeasurement.H;
                description = value + " " + (value == 1.0 ? "hour" : "hours");
                actualMultiplier = 10.0 / (value * 60.0);
            }
            else if (args[2].endsWith("d")) {
                measurement = TimeMeasurement.D;
                description = value + " " + (value == 1.0 ? "day" : "days");
                actualMultiplier = 10.0 / (value * 1440.0);
            }
            else if (args[2].endsWith("x")) {
                measurement = TimeMeasurement.X;
                description = value + "x speed";
                actualMultiplier = value;
            }
            else {
                sender.sendMessage(ChatColor.RED + "Error: Value suffix must be s, m, d, h, or x");
                return true;
            }

            // Get world
            if (args.length == 4) {
                World w = sender.getServer().getWorld(args[3]);
                if (w == null) {
                    sender.sendMessage(ChatColor.RED + "Error: No world found with name \"" + args[3] + "\"");
                    return true;
                }
                if (w.getEnvironment() != Environment.NORMAL) {
                    sender.sendMessage(ChatColor.RED + "Error: World cannot be Nether, End, or Custom dimension.");
                    return true;
                }
                world = w;
            }
            else
                world = assumedWorld;

            // Make change
            String worldName = world.getName();
            WorldData entry;

            if (CustomTime.inst.worldDataMap.containsKey(worldName)) {
                entry = CustomTime.inst.worldDataMap.get(worldName);
                entry.tick = 0;
            }
            else {
                entry = new WorldData(world, 1.0, "1.0x speed", 1.0, "1.0x speed");
                CustomTime.inst.worldDataMap.put(worldName, entry);
            }

            if (daynight == Time.DAY) {
                entry.dayMultiplier = actualMultiplier;
                entry.dayDescription = description;
            }
            else {
                entry.nightMultiplier = actualMultiplier;
                entry.nightDescription = description;
            }

            CustomTime.inst.worldDataMap.writeToConfig(CustomTime.inst);

            String message = sender.getName() + " set " + worldName + (daynight == Time.DAY ? " day" : " night")
                    + (measurement == TimeMeasurement.X ? " to run at " : " to last ") + description;
            sender.getServer().broadcast(message, "customtime.*");

            return true;
        }

        return false;
    }

    void InfoCommand() {
        // TODO Info Command
    }

    void HelpCommand() {
        // TODO Help command
    }
}
