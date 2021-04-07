package jcv8000.customtime;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class CustomTimeTabCompleter implements TabCompleter {

    CustomTime main;
    public CustomTimeTabCompleter(CustomTime inst) {
        main = inst;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("ct")) {
            
            if (args.length == 1) {
                
                ArrayList<String> list = new ArrayList<String>();
                list.add("day");
                list.add("night");
                
                return list;
            } else if (args.length == 3) {
                
                ArrayList<String> list = new ArrayList<String>();
                for (World w : main.getServer().getWorlds()) {
                    if (w.getEnvironment() == World.Environment.NORMAL) {
                        list.add(w.getName());
                    }
                }
                
                return list;
            }
            return new ArrayList<String>();
        }
        
        return new ArrayList<String>();
    }
    
}
