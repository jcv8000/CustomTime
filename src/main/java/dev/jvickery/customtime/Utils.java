package dev.jvickery.customtime;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.bukkit.Server;
import org.bukkit.World;

public class Utils {
    public static World getDefaultWorld(Server server) {
        Properties properties = new Properties();
        try {
            FileInputStream in = new FileInputStream(new File("server.properties"));
            properties.load(in);
            
            return server.getWorld(properties.getProperty("level-name"));
        }
        catch (Exception ex) {
            // error: could not load/parse server.properties
            return null;
        }
    }
}
