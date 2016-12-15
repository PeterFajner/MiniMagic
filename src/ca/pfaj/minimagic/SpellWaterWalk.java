package ca.pfaj.minimagic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * A wand that allows a nonhuman LivingEntity to walk on water when rightclicked, and disables this effect when leftclicked.
 * @author peter
 *
 */
public class SpellWaterWalk
{
	// make spell castable on LivingEntity except humans
	// repeating event every tick or so, freeze nearby blocks
	// rightclick to enable, leftclick to disable
	
	// on start:
	//		open waterwalk file, iterate over all entities and enable waterwalking for those with relevant IDs
	// on cast:
	//		enable sync event
	//		write entity id to file
	// on discast:
	//		disable syncevent
	//		remove entity id from file
	// sync event:
	//		particle effect
	
	public static ItemStack waterWalkWand;
	public static Main plugin;
	public static final String IDENTIFIER = "WaterWalk";
	public static final String METADATA_KEY = "WaterWalk";
	public static final long EVENT_REPEAT_DELAY = 2L; // delay between particle effects and block checks, in ticks
	public static File waterwalkers_file;
	public static int disableCost;
	public static int enableCost;
	public static int radius;
	public static List<Integer> waterWalkingEntities;
	static long ENTITY_LIST_FILE_SAVE_DELAY = 60L; // the list of entities is saved every this many ticks  
	
	public static void init(Main plugin, int enableCost, int disableCost, int radius)
	{
		Server server = plugin.getServer();
		SpellWaterWalk.enableCost = enableCost;
		SpellWaterWalk.disableCost = disableCost;
		SpellWaterWalk.radius = radius;
		SpellWaterWalk.plugin = plugin;
		
		createIDList(); // create file to hold IDs of waterwalk-enabled entities
		
		// create waterwalk wand item
		waterWalkWand = Wand.wand_1.clone();
		ArrayList<String> lore = new ArrayList<String>();
		lore.add(IDENTIFIER);
		lore.add("Cost: " + enableCost + ", " + disableCost);
		ItemMeta wandMeta = waterWalkWand.getItemMeta();
		wandMeta.setLore(lore);
		wandMeta.setDisplayName(IDENTIFIER);
		waterWalkWand.setItemMeta(wandMeta);
		
		// create waterwalk wand recipe
		// the recipe is by default a generic wand, the listener converts it into a fireball wand
		ShapelessRecipe recipe = new ShapelessRecipe(Wand.wand_1);
		recipe.addIngredient(Material.STICK);
		recipe.addIngredient(3, Material.ICE);
		server.addRecipe(recipe);
		
		// add listener
		server.getPluginManager().registerEvents(new WaterWalkListener(plugin), plugin);
	}
	
	public static void close()
	{
		writeIdsToFile();
	}
		
	
	/**
	 * Loads the list of waterwalking entities from file and creates a task to periodically save them to the file.
	 */
	static void createIDList()
	{
		// make sure the file exists
		SpellWaterWalk.waterwalkers_file = new File(plugin.getDataFolder(), "waterwalkers.yml");
		if (!waterwalkers_file.exists()) {
			waterwalkers_file.getParentFile().mkdirs();
			try {
				waterwalkers_file.createNewFile();
			} catch (IOException e) { e.printStackTrace(); }
		}
		
		// make sure the ID list is empty
		waterWalkingEntities = new ArrayList<Integer>();
		
		// load IDs
		try {
			BufferedReader br = new BufferedReader(new FileReader(waterwalkers_file));  
			String line = null;  
			while ((line = br.readLine()) != null)  
			{  
				waterWalkingEntities.add(Integer.parseInt(line));
			} 
			br.close();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		
		// task to write IDs to the file
		plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
			@Override
			public void run() 
			{
				writeIdsToFile();
			}
		}, 0L, ENTITY_LIST_FILE_SAVE_DELAY);
		
		// task to cause the waterwalking effect
		plugin.getServer().getScheduler().runTaskTimer(plugin, new WaterWalkEvent(plugin), 100L, SpellWaterWalk.EVENT_REPEAT_DELAY);
	}
	
	static void writeIdsToFile()
	{
		try {
			FileOutputStream fos = new FileOutputStream(waterwalkers_file, false);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			for (Integer id : waterWalkingEntities) {
				bw.write(Integer.toString(id));
				bw.newLine();
			} 
			bw.close();
			fos.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Checks if an entity is marked as waterwalking.
	 * @param e The entity to check
	 * @return
	 */
	static boolean isWaterWalking(Entity e)
	{
		int id = e.getEntityId();
		for (Integer i : waterWalkingEntities) {
			if (i.intValue() == id) return true;
		}
		return false;
	}
	
	static void setWaterWalking(Entity e, boolean waterWalking)
	{
		if (waterWalking == true) {
			waterWalkingEntities.add(new Integer(e.getEntityId()));
		}
		else {
			waterWalkingEntities.remove(new Integer(e.getEntityId()));
		}
	}
}


class WaterWalkListener implements Listener
{
	
	Main plugin;
	
	WaterWalkListener(Main plugin)
	{
		super();
		this.plugin = plugin;
	}
	
	/**
	 * If there is a recipe that creates a wand, and the ingredients are those for a waterwalk wand, change the result to a waterwalk wand.
	 */
	@EventHandler
	public void onItemCrafted(PrepareItemCraftEvent e)
	{
		CraftingInventory inventory = e.getInventory();
		ItemStack result = inventory.getResult();
		if (Helpers.loreContains(result, Wand.IDENTIFIER_MAIN)) {
			boolean wand_in_ingredients = false;
			int number_of_ice_blocks = 0;
			for (ItemStack ingredient : inventory.getMatrix()) {
				if (Helpers.loreContains(ingredient, Wand.IDENTIFIER_MAIN)) {
					wand_in_ingredients = true;
				}
				if (ingredient.getData().getItemType() == Material.ICE) {
					number_of_ice_blocks++;
				}
			}
			if (wand_in_ingredients && number_of_ice_blocks == 3) {
				inventory.setResult(SpellWaterWalk.waterWalkWand);
			}
		}
	}
	
	/**
	 * Handles checking when a player clicks an entity with the waterwalk wand.
	 */
	@EventHandler
	public void onAction(PlayerInteractEntityEvent e)
	{
		Player p = e.getPlayer();
		Entity ent = e.getRightClicked();
		if (e.getHand().equals(EquipmentSlot.OFF_HAND)) return; // when clicking on a horse the off hand event isn't fired; at other times both events are fired, so we ignore the off hand event 
	    ItemStack mainHand = p.getInventory().getItemInMainHand();
	    ItemStack offHand = p.getInventory().getItemInOffHand();
		if (Helpers.loreContains(mainHand, SpellWaterWalk.IDENTIFIER) || Helpers.loreContains(offHand, SpellWaterWalk.IDENTIFIER)) { // holding the waterwalk wand
			plugin.debug("Casting WaterWalk...");
			if (!(ent instanceof Player)) { // can't be cast on players
				plugin.debug("Entity valid...");
				if (SpellWaterWalk.isWaterWalking(ent)) { // currently enabled
					plugin.debug("Disabling WaterWalk...");
			        SpellWaterWalk.setWaterWalking(ent, false);
				}
				else { // currently enabled
					plugin.debug("Enabling WaterWalk...");
					SpellWaterWalk.setWaterWalking(ent, true);
				}
			}
		}
			
	}
}


/**
 * Fires several times per second. Freezes nearby water and creates a water effect.
 * @author peter
 *
 */
class WaterWalkEvent implements Runnable
{
	Main plugin;
	int r = SpellWaterWalk.radius;
	
	WaterWalkEvent(Main plugin)
	{
		this.plugin = plugin;
	}
	
	void createEffect(Entity e)
	{
		// convert nearby water to frosted ice
				Location loc = e.getLocation();
				World world = loc.getWorld();
				int x = loc.getBlockX();
				int y = loc.getBlockY();
				int z = loc.getBlockZ();
				for (int u=x-r; u<=x+r; u++) {
					for (int v=y-r; v<=y+r; v++) {
						for (int w=z-r; w<=z+r; w++) {
							Block block = world.getBlockAt(u,v,w);
							if (block.getType() == Material.STATIONARY_WATER) {
								block.setType(Material.FROSTED_ICE);
							}
						}
					}
				}
				
				// spawn particle
				world.spawnParticle(Particle.WATER_SPLASH, x, y, z, 5);
	}

	@Override
	public void run() {
		for (World w : plugin.server.getWorlds()) {
			for (Entity entity : w.getEntities()) {
				int entityId = entity.getEntityId();
				for (Integer id : SpellWaterWalk.waterWalkingEntities) {
					if (id.intValue() == entityId) {
						createEffect(entity);
					}
				}
			}
		}
		
	}
}