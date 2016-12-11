package ca.pfaj.minimagic;

import java.util.logging.Level;

import org.bukkit.Server;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin
{

	public static boolean DEBUG = true;
	public static PluginLogger logger;
	
	public Server server;
	
	@Override
	public void onEnable()
	{
		this.server = getServer();
		if (logger == null) logger = new PluginLogger(this);
		
		debug("Debug printing enabled.");
		
		Wand.init(this); // register wand object and recipes
		Fireball.init(this); // register fireball spell
	}
	
	@Override
	public void onDisable() {}
	
	public static void debug(String msg)
	{
		if (DEBUG) logger.log(Level.INFO, msg);
	}
}
