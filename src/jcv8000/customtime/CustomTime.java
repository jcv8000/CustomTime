package jcv8000.customtime;

import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class CustomTime extends JavaPlugin {
    
    public FileConfiguration config;
    public BukkitScheduler scheduler;
    public HashMap<String, CTWorldData> ctWorldDatas;
    public World mainWorld;
    public ChatColor mainColor;
    public ChatColor importantColor;
    public ChatColor errorColor;
    //boolean canSleep = true;
    
    @Override
    public void onEnable() {
        
        mainColor = ChatColor.GOLD;
        importantColor = ChatColor.LIGHT_PURPLE;
        errorColor = ChatColor.RED;
        
        scheduler = Bukkit.getServer().getScheduler();
        ctWorldDatas = new HashMap<String, CTWorldData>();
        
        // Config loading/setup
        try {
            
            config = getConfig();
            if (config.isConfigurationSection("worlds") == false) {
                config.createSection("worlds");
            }

            saveConfig();
            
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "Could not get CustomTime's config file correctly. Make sure it's in the correct format or let the plugin create a new one.");
        }
        
        //get main world
        File propFile = new File("server.properties");
        Properties prop = new Properties();
        try
        {
            FileInputStream in = new FileInputStream(propFile);
            prop.load(in);
            mainWorld = Bukkit.getServer().getWorld(prop.getProperty("level-name"));
        }
        catch (Exception ex)
        {
            Bukkit.getServer().broadcastMessage(errorColor + "ERROR: Could not find main world because the level-name in server.properties did not point to an existing world, or server.properties could not be loaded correctly.");
            getLogger().log(Level.SEVERE, "Could not find main world because the level-name in server.properties did not point to an existing world, or server.properties could not be loaded correctly.");
        }
        
        
        //Load worlds that are included in the config into the list
        for (String s : config.getConfigurationSection("worlds").getKeys(false)) {
            //worlds.add(Bukkit.getServer().getWorld(s));
            World w;
            try {
                w = Bukkit.getServer().getWorld(s);
                if (w.getEnvironment() != World.Environment.NORMAL) {
                    throw new Exception();
                }
                ctWorldDatas.put(s, new CTWorldData(w, config.getDouble("worlds." + w.getName() + ".day.multiplier"), config.getDouble("worlds." + w.getName() + ".night.multiplier")));
            }
            catch (Exception ex) {
                Bukkit.getServer().broadcastMessage(errorColor + "ERROR: Could not find world " + s);
                getLogger().log(Level.SEVERE, "Could not find world '" + s + "' or it's nether/end. It will not be used in CustomTime.");
            }
        }
        
        //print world list to console
        String worldlist = "";
        for (CTWorldData d : ctWorldDatas.values()) {
            worldlist += "\n" + d.world.getName();
        }
        getLogger().log(Level.INFO, "Worlds in effect of custom time scales:" + worldlist);
        
        
        //Check all loaded worlds and make sure doDaylightCycle is false
        for (CTWorldData d : ctWorldDatas.values()) {
            World w = d.world;
            if (w != null) {
                if (w.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE) == true) {
                    getLogger().log(Level.WARNING, "Gamerule \"doDaylightCycle\" is true in world '" + w.getName() + "'. This needs to be false for CustomTime to work. Setting to false.");
                    w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                } else {
                    getLogger().log(Level.INFO, "Gamerule \"doDaylightCycle\" is false in world '" + w.getName() + "'. Good!");
                }
            }
        }
        

        //Schedule the repeating task that moves time
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                
                //cache some reused variables
                long time;

                for (CTWorldData data : ctWorldDatas.values()) {
                    
                    if (data.world != null) {
                        
                        time = data.world.getTime();
                        
                        if (time <= 12000) { //DAY
                            if (data.dayMult > 0.0D && data.dayMult < 1.0D) { //slow down
                                if (data.tick > (1.0D / data.dayMult)) {
                                    data.tick = 0;
                                    data.world.setTime(time + 1);
                                }
                            }
                            else if (data.dayMult >= 1.0D) { //speed up or keep normal
                                data.world.setTime(data.world.getTime() + (long)data.dayMult);
                            }
                        }
                        else if (data.world.getTime() > 12000) { //NIGHT 
                            if (data.nightMult > 0.0D && data.nightMult < 1.0D) { //slow down
                                if (data.tick > (1.0D / data.nightMult)) {
                                    data.tick = 0;
                                    data.world.setTime(time + 1);
                                }
                            }
                            else if (data.nightMult >= 1.0D) { //speed up or keep normal
                                data.world.setTime(data.world.getTime() + (long)data.nightMult);
                            }
                        }
                        data.tick++;

                        if (data.world.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE) == true) {
                            getLogger().log(Level.WARNING, "Gamerule \"doDaylightCycle\" is true in world '" + data.world.getName() + "'. This needs to be false for CustomTime to work. Setting to false.");
                            data.world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                        }
                        
                        // SLEEP CHECKING
                        if (data.world.getTime() > 12000 || data.world.hasStorm()) {

                            List<Player> players = data.world.getPlayers();
                            List<Player> sleepers = new ArrayList<Player>();
                            if (players.size() > 0) {
        
                                for (Player p : players) {
                                    if (p.getSleepTicks() >= 100) {
                                        sleepers.add(p);
                                    }
                                }
        
                                int sleepersNeeded = (int)(((double)data.world.getGameRuleValue(GameRule.PLAYERS_SLEEPING_PERCENTAGE) / 100.0d) * players.size());
        
                                if (sleepers.size() >= sleepersNeeded) {

                                    for (Player p : sleepers) {
                                        p.wakeup(true);
                                    }

                                    getLogger().info(sleepers.size() + "/" + sleepersNeeded + " players needed to skip the night are sleeping. Skipping the night.");
                                    data.world.setTime(0);

                                    // After a storm ends there's clear weather for 0.5-7.5 days, or 12,000-180,000 ticks
                                    int clearTicks = (int)(Math.random() * (168000) + 12000);

                                    data.world.setClearWeatherDuration(clearTicks);
                                }
                            }

                        }
                        
                    }
                }
            }
        }, 0L, 1L);
        
        
        this.getCommand("ct").setExecutor(new CommandCustomTime(this));
        this.getCommand("ct").setTabCompleter(new CustomTimeTabCompleter(this));
    }
    
    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        ctWorldDatas.clear();
    }
}
