package dev.jvickery.customtime;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.dependency.SoftDependency;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.LogPrefix;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.Website;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion.Target;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

@Plugin(name="CustomTime", version="3.0")
@ApiVersion(Target.v1_17)
@Description("Plugin to control the speed of the day/night cycle")
@Author("jcv8000")
@Website("https://github.com/jcv8000/CustomTime")
@LogPrefix("CustomTime")
@SoftDependency("Multiverse-Core")
public class CustomTime extends JavaPlugin
{
    @Override
    public void onEnable() {
        getLogger().info("CUSTOMTIME!");
    }
}
