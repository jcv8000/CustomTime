package jcv8000.customtime;

import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandCustomTime implements CommandExecutor{

    public CustomTime main;
    public CommandCustomTime(CustomTime inst) {
        main = inst;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = null;
        World world = null;

        if (sender instanceof Player) {
            player = (Player) sender;
            world = player.getWorld();
            
            if (!sender.hasPermission("customtime.*")) {
                return false;
            }
        } else {
            world = main.mainWorld;
        }
        
        if (cmd.getName().equalsIgnoreCase("ct")) {
            
            if (args.length == 0) { //  /customtime
                sender.sendMessage(main.mainColor + "-------------------------------------------");
                sender.sendMessage(main.mainColor + "| " + ChatColor.RED + "CustomTime" + main.mainColor + " version 2.0 by " + ChatColor.GREEN + "jcv8000/Crafterguy3x3");
                sender.sendMessage(main.mainColor + "| World day/night cycle info:");
                for (CTWorldData data : main.ctWorldDatas.values()) {
                    String infoMessage = "";
                    if (main.config.contains("worlds." + data.world.getName() + ".day.desc")) {
                        infoMessage += " Day = " + main.config.getString("worlds." + data.world.getName() + ".day.desc") + ".";
                    }
                    if (main.config.contains("worlds." + data.world.getName() + ".night.desc")) {
                        infoMessage += " Night = " + main.config.getString("worlds." + data.world.getName() + ".night.desc") + ".";
                    }
                    sender.sendMessage(main.mainColor + "| - " + main.importantColor + data.world.getName() + main.mainColor + ":" + infoMessage);
                }
                sender.sendMessage(main.mainColor + "-------------------------------------------");
                
                return true;
            }
            else if (args.length == 2 || args.length == 3) { //  /customtime [day/night] [value] ?[world]
                
                if (args.length == 3) { // used [world] parameter
                    try {
                        world = main.getServer().getWorld(args[2]);
                        if (world.getEnvironment() != World.Environment.NORMAL) {
                            throw new Exception();
                        }
                    }
                    catch (Exception ex) {
                        sender.sendMessage(main.errorColor + "ERROR: Make sure you entered a valid world that isn't nether/end.");
                        return false;
                    }
                }
                
                if (args[0].equalsIgnoreCase("day") || args[0].equalsIgnoreCase("night")) {

                    String daynight = args[0];
                    String value = args[1];

                    if (value.endsWith("s")) { //used seconds
                        try {
                            double num = Double.parseDouble(value.replace("s", "")); //just to make sure the number before the 's' is valid
                            if (num <= 0.0D) {
                                throw new Exception();
                            }
                            double mult = (10.0D / (num / 60.0D));
                            
                            main.config.set("worlds." + world.getName() + "." + daynight + ".multiplier", mult);
                            main.config.set("worlds." + world.getName() + "." + daynight + ".desc", num + " seconds");
                            main.saveConfig();
                            
                            //ADD THIS WORLD DATA TO THE HASHMAP
                            if (main.ctWorldDatas.containsKey(world.getName())) {
                                if (daynight.equalsIgnoreCase("day")) {
                                    main.ctWorldDatas.get(world.getName()).dayMult = mult;
                                }
                                else if (daynight.equalsIgnoreCase("night")) {
                                    main.ctWorldDatas.get(world.getName()).nightMult = mult;
                                }
                            }
                            else {
                                if (daynight.equalsIgnoreCase("day")) {
                                    main.ctWorldDatas.put(world.getName(), new CTWorldData(world, mult, 1.0D));
                                }
                                else if (daynight.equalsIgnoreCase("night")) {
                                    main.ctWorldDatas.put(world.getName(), new CTWorldData(world, 1.0D, mult));
                                }
                            }
                            //ADD THIS WORLD DATA TO THE HASHMAP
                            
                            sender.sendMessage(main.mainColor + "Set world " + main.importantColor + world.getName() + " " + main.mainColor + daynight + " to last " + num + " seconds.");
                            main.getLogger().log(Level.INFO, "Set world " + world.getName() + " " + daynight + " to last " + num + " seconds.");
                            return true;
                        }
                        catch (Exception ex) {
                            sender.sendMessage(main.errorColor + "ERROR: Make sure you entered a valid positive number.");
                            return false;
                        }
                    }
                    else if (value.endsWith("m")) { //used minutes
                        try {
                            double num = Double.parseDouble(value.replace("m", "")); //just to make sure the number before the 'm' is valid
                            if (num <= 0.0D) {
                                throw new Exception();
                            }
                            double mult = (10.0D / num);
                            main.config.set("worlds." + world.getName() + "." + daynight + ".multiplier", mult);
                            main.config.set("worlds." + world.getName() + "." + daynight + ".desc", num + " minutes");
                            main.saveConfig();
                            
                            //ADD THIS WORLD DATA TO THE HASHMAP
                            if (main.ctWorldDatas.containsKey(world.getName())) {
                                if (daynight.equalsIgnoreCase("day")) {
                                    main.ctWorldDatas.get(world.getName()).dayMult = mult;
                                }
                                else if (daynight.equalsIgnoreCase("night")) {
                                    main.ctWorldDatas.get(world.getName()).nightMult = mult;
                                }
                            }
                            else {
                                if (daynight.equalsIgnoreCase("day")) {
                                    main.ctWorldDatas.put(world.getName(), new CTWorldData(world, mult, 1.0D));
                                }
                                else if (daynight.equalsIgnoreCase("night")) {
                                    main.ctWorldDatas.put(world.getName(), new CTWorldData(world, 1.0D, mult));
                                }
                            }
                            //ADD THIS WORLD DATA TO THE HASHMAP
                            
                            sender.sendMessage(main.mainColor + "Set world " + main.importantColor + world.getName() + " " + main.mainColor + daynight + " to last " + num + " minutes.");
                            main.getLogger().log(Level.INFO, "Set world " + world.getName() + " " + daynight + " to last " + num + " minutes.");
                            return true;
                        }
                        catch (Exception ex) {
                            sender.sendMessage(main.errorColor + "ERROR: Make sure you entered a valid positive number.");
                            return false;
                        }
                    }
                    else if (value.endsWith("h")) { //used hours
                        try {
                            double num = Double.parseDouble(value.replace("h", "")); //just to make sure the number before the 'h' is valid
                            if (num <= 0.0D) {
                                throw new Exception();
                            }
                            double mult = (10.0D / (num * 60.0D));
                            main.config.set("worlds." + world.getName() + "." + daynight + ".multiplier", mult);
                            main.config.set("worlds." + world.getName() + "." + daynight + ".desc", num + " hours");
                            main.saveConfig();
                            
                            //ADD THIS WORLD DATA TO THE HASHMAP
                            if (main.ctWorldDatas.containsKey(world.getName())) {
                                if (daynight.equalsIgnoreCase("day")) {
                                    main.ctWorldDatas.get(world.getName()).dayMult = mult;
                                }
                                else if (daynight.equalsIgnoreCase("night")) {
                                    main.ctWorldDatas.get(world.getName()).nightMult = mult;
                                }
                            }
                            else {
                                if (daynight.equalsIgnoreCase("day")) {
                                    main.ctWorldDatas.put(world.getName(), new CTWorldData(world, mult, 1.0D));
                                }
                                else if (daynight.equalsIgnoreCase("night")) {
                                    main.ctWorldDatas.put(world.getName(), new CTWorldData(world, 1.0D, mult));
                                }
                            }
                            //ADD THIS WORLD DATA TO THE HASHMAP
                            
                            sender.sendMessage(main.mainColor + "Set world " + main.importantColor + world.getName() + " " + main.mainColor + daynight + " to last " + num + " hours.");
                            main.getLogger().log(Level.INFO, "Set world " + world.getName() + " " + daynight + " to last " + num + " hours.");
                            return true;
                        }
                        catch (Exception ex) {
                            sender.sendMessage(main.errorColor + "ERROR: Make sure you entered a valid positive number.");
                            return false;
                        }
                    }
                    else if (value.endsWith("d")) { //used days
                        try {
                            double num = Double.parseDouble(value.replace("d", "")); //just to make sure the number before the 'd' is valid
                            if (num <= 0.0D) {
                                throw new Exception();
                            }
                            double mult = (10.0D / (num * 1440.0D));
                            main.config.set("worlds." + world.getName() + "." + daynight + ".multiplier", mult);
                            main.config.set("worlds." + world.getName() + "." + daynight + ".desc", num + " days");
                            main.saveConfig();
                            
                            //ADD THIS WORLD DATA TO THE HASHMAP
                            if (main.ctWorldDatas.containsKey(world.getName())) {
                                if (daynight.equalsIgnoreCase("day")) {
                                    main.ctWorldDatas.get(world.getName()).dayMult = mult;
                                }
                                else if (daynight.equalsIgnoreCase("night")) {
                                    main.ctWorldDatas.get(world.getName()).nightMult = mult;
                                }
                            }
                            else {
                                if (daynight.equalsIgnoreCase("day")) {
                                    main.ctWorldDatas.put(world.getName(), new CTWorldData(world, mult, 1.0D));
                                }
                                else if (daynight.equalsIgnoreCase("night")) {
                                    main.ctWorldDatas.put(world.getName(), new CTWorldData(world, 1.0D, mult));
                                }
                            }
                            //ADD THIS WORLD DATA TO THE HASHMAP
                            
                            sender.sendMessage(main.mainColor + "Set world " + main.importantColor + world.getName() + " " + main.mainColor + daynight + " to last " + num + " days.");
                            main.getLogger().log(Level.INFO, "Set world " + world.getName() + " " + daynight + " to last " + num + " days.");
                            return true;
                        }
                        catch (Exception ex) {
                            sender.sendMessage(main.errorColor + "ERROR: Make sure you entered a valid positive number.");
                            return false;
                        }
                    }
                    else if (value.endsWith("x")) { //multiplier value
                        try {
                            double mult = Double.parseDouble(value.replace("x", ""));
                            if (mult < 0.0D) {
                                throw new Exception();
                            }
                            main.config.set("worlds." + world.getName() + "." + daynight + ".multiplier", mult);
                            main.config.set("worlds." + world.getName() + "." + daynight + ".desc", mult + "x speed");
                            main.saveConfig();
                            
                            //ADD THIS WORLD DATA TO THE HASHMAP
                            if (main.ctWorldDatas.containsKey(world.getName())) {
                                if (daynight.equalsIgnoreCase("day")) {
                                    main.ctWorldDatas.get(world.getName()).dayMult = mult;
                                }
                                else if (daynight.equalsIgnoreCase("night")) {
                                    main.ctWorldDatas.get(world.getName()).nightMult = mult;
                                }
                            }
                            else {
                                if (daynight.equalsIgnoreCase("day")) {
                                    main.ctWorldDatas.put(world.getName(), new CTWorldData(world, mult, 1.0D));
                                }
                                else if (daynight.equalsIgnoreCase("night")) {
                                    main.ctWorldDatas.put(world.getName(), new CTWorldData(world, 1.0D, mult));
                                }
                            }
                            //ADD THIS WORLD DATA TO THE HASHMAP
                            
                            sender.sendMessage(main.mainColor + "Set world " + main.importantColor + world.getName() + " " + main.mainColor + daynight + " to go at " + mult + "x speed.");
                            main.getLogger().log(Level.INFO, "Set world " + world.getName() + " " + daynight + " to last " + mult + "x speed.");
                            return true;
                        }
                        catch (Exception ex) {
                            sender.sendMessage(main.errorColor + "ERROR: Make sure you entered a valid positive number.");
                            return false;
                        }
                    }
                    else {
                        sender.sendMessage(main.errorColor + "ERROR: Value must end in s (sec), m (min), h (hours), d (days), or x (multiplier)");
                        return false;
                    }
                }
            }
        }
        return false;
    }
}
