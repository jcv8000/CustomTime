package dev.jvickery.customtime.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import dev.jvickery.customtime.CustomTime;

public class CustomTimeTabCompleter implements TabCompleter {

    // /ct set (day|night) <value>(s|m|h|d|x) [world]
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            // /ct __
            return Arrays.asList("set", "help");
        }

        if (args.length > 1 && args[0].equals("set")) {
            if (args.length == 2) {
                // /ct set __
                return Arrays.asList("day", "night");
            }

            if (args.length == 3) {
                // /ct set (day|night) __
                try {
                    double v = Double.parseDouble(args[2]);
                    if (v > 0.0)
                        return Arrays.asList(args[2] + "s", args[2] + "m", args[2] + "h", args[2] + "d", args[2] + "x");
                }
                catch (Exception ex) {
                    return new ArrayList<String>();
                }
            }

            if (args.length == 4) {
                // /ct set (day|night) <value>(s|m|h|d|x) __
                var list = new ArrayList<String>();
                var worldList = CustomTime.inst.getServer().getWorlds();

                for (World world : worldList) {
                    if (world.getEnvironment() == Environment.NORMAL) {
                        list.add(world.getName());
                    }
                }

                return list;
            }
        }

        return new ArrayList<String>();
    }

}
