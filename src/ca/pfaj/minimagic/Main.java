package ca.pfaj.minimagic;

import java.util.logging.Level;

import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin
{

	public static boolean DEBUG;
	public static PluginLogger logger;
	
	FileConfiguration config = getConfig();	
	public Server server = getServer();
	
	public void debug(String msg)
	{
		if (config.getBoolean("debug")) logger.log(Level.INFO, msg);
	}
	
	@Override
	public void onEnable()
	{
		if (logger == null) logger = new PluginLogger(this);
		createConfigDefaults();
		
		debug("Debug printing enabled.");
		
		Wand.init(this); // register wand object and recipes
		SpellFireball.init(this, config.getInt("fireball-cost")); // register fireball spell
		SpellWaterWalk.init(this, config.getInt("waterwalk-enablecost"), config.getInt("waterwalk-disablecost"), config.getInt("waterwalk-radius"));
        SpellHeal.init(this, config.getInt("heal-cost"), config.getInt("heal-amount"));
	}
	
	@Override
	public void onDisable() {
		SpellWaterWalk.close();
	}
	
	void createConfigDefaults()
	{
		config.addDefault("debug", true);
		config.addDefault("fireball-cost", 15);
		config.addDefault("waterwalk-enablecost", 30);
		config.addDefault("waterwalk-disablecost", 0);
		config.addDefault("waterwalk-radius", 3);
        config.addDefault("heal-cost", 50);
        config.addDefault("heal-amount", 3);
	    config.options().copyDefaults(true);
	    saveConfig();
	}
}
