package ca.pfaj.minimagic;

import java.util.logging.Level;

import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin
{

	public static boolean DEBUG = true;
	public static PluginLogger logger;
	
	FileConfiguration config = getConfig();	
	public Server server = getServer();
	
	public static void debug(String msg)
	{
		if (DEBUG) logger.log(Level.INFO, msg);
	}
	
	@Override
	public void onEnable()
	{
		if (logger == null) logger = new PluginLogger(this);
		createConfigDefaults();
		
		debug("Debug printing enabled.");
		
		Wand.init(this); // register wand object and recipes
		SpellFireball.init(this, config.getInt("fireball-cost")); // register fireball spell
	}
	
	@Override
	public void onDisable() {}
	
	void createConfigDefaults()
	{
		config.addDefault("fireball-cost", 15);
	    config.options().copyDefaults(true);
	    saveConfig();
	}
}
