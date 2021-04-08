package jcv8000.customtime;

import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
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
    boolean canSleep = true;
    
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
                    getLogger().log(Level.WARNING, "Gamerule \"doDaylightCycle\" is false in world '" + w.getName() + "'. Good!");
                }
            }
        }
        
        
        
        //Schedule the repeating task that moves time
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                
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

                                long projectedTime = time + (long)data.dayMult;

                                if (projectedTime > 12000)
                                    data.world.setTime(12000);
                                else
                                    data.world.setTime(time + (long)data.dayMult);
                            }
                        }
                        else { //NIGHT 
                            if (data.nightMult > 0.0D && data.nightMult < 1.0D) { //slow down
                                if (data.tick > (1.0D / data.nightMult)) {
                                    data.tick = 0;
                                    data.world.setTime(time + 1);
                                }
                            }
                            else if (data.nightMult >= 1.0D) { //speed up or keep normal

                                long projectedTime = time + (long)data.nightMult;

                                if (projectedTime >= 24000)
                                    data.world.setTime(0);
                                else
                                    data.world.setTime(time + (long)data.nightMult);
                            }
                        }
                        data.tick++;
                    }
                }
            }
        }, 0L, 1L);
        
        //Check to see if all players are sleeping every second, and make sure gamerule is false
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                
                //cache
                List<Player> players;
                
                for (CTWorldData data : ctWorldDatas.values()) {
                    if (data.world.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE) == true) {
                        getLogger().log(Level.WARNING, "Gamerule \"doDaylightCycle\" is true in world '" + data.world.getName() + "'. This needs to be false for CustomTime to work. Setting to false.");
                        data.world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                    }
                    
                    players = data.world.getPlayers();
                    if (players.size() > 0) {
                        int peopleSleeping = 0;
                        for (Player p : players) {
                            if (p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE) {
                                if (p.isSleeping() == true) {
                                    peopleSleeping = peopleSleeping + 1;
                                }
                            }
                        }

                        if (peopleSleeping == data.world.getPlayers().size()) {
                            
                            if (canSleep) {
                                sleep(data.world);
                                canSleep = false;
                            }
                            
                        }
                    }
                }
                
            }
        }, 0L, 20L);
        
        
        this.getCommand("ct").setExecutor(new CommandCustomTime(this));
        this.getCommand("ct").setTabCompleter(new CustomTimeTabCompleter(this));
    }
    
    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        ctWorldDatas.clear();
    }
    
    void sleep(World w) {
        scheduler.runTaskLater(this, new Runnable() {
            @Override
            public void run() {
                w.setTime(0);
                canSleep = true;
            }
        }, 100);
    }
}
