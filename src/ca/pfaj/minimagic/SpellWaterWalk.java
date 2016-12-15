package ca.pfaj.minimagic;

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
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

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
	//		check all entities for waterwalk metadata and enable repeating event (which is set in their metadata?)
	// on cast:
	//		enable sync event (set a reference to it in metadata)
	// on discast:
	//		disable syncevent and remove it from metadata
	// sync event:
	//		particle effect
	//		nearby blocks turn into Frosted Ice
	
	public static ItemStack waterWalkWand;
	public static final String IDENTIFIER = "WaterWalk";
	public static final String METADATA_KEY = "WaterWalk";
	public static int disableCost;
	public static int enableCost;
	public static int radius;
	public static final long EVENT_REPEAT_DELAY = 2L; // delay between particle effects and block checks, in ticks
	
	public static void init(Main plugin, int enableCost, int disableCost, int radius)
	{
		Server server = plugin.getServer();
		SpellWaterWalk.enableCost = enableCost;
		SpellWaterWalk.disableCost = disableCost;
		SpellWaterWalk.radius = radius;
		
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
	
		// sort through all entities and enable the sync event on those with correct metadata
		List<World> worlds = plugin.getServer().getWorlds();
		for (World w : worlds) {
			List<Entity> entities = w.getEntities();
			for (Entity e : entities) {
				List<MetadataValue> meta = e.getMetadata(SpellWaterWalk.METADATA_KEY);
				if (meta == null) { // current not enabled
					BukkitScheduler scheduler = plugin.getServer().getScheduler();
			        BukkitTask task = scheduler.runTaskTimer(plugin, new WaterWalkEvent(plugin, e), 0L, EVENT_REPEAT_DELAY);
			        e.setMetadata(SpellWaterWalk.METADATA_KEY, (MetadataValue) task);
				}
			}
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
				List<MetadataValue> meta = ent.getMetadata(SpellWaterWalk.METADATA_KEY);
				if (meta == null || (meta != null && meta.size() == 0)) { // current not enabled
					plugin.debug("Enabling WaterWalk...");
					BukkitScheduler scheduler = plugin.getServer().getScheduler();
			        BukkitTask task = scheduler.runTaskTimer(plugin, new WaterWalkEvent(plugin, ent), 0L, SpellWaterWalk.EVENT_REPEAT_DELAY);
			        FixedMetadataValue val = new FixedMetadataValue(plugin, task);
			        ent.setMetadata(SpellWaterWalk.METADATA_KEY, val);
				}
				else { // currently enabled
					plugin.debug("Disabling WaterWalk...");
					BukkitTask task = (BukkitTask) meta.get(0).value();
					task.cancel();
					ent.removeMetadata(SpellWaterWalk.METADATA_KEY, plugin);
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
	Entity e;
	int r = SpellWaterWalk.radius;
	
	WaterWalkEvent(Main plugin, Entity e)
	{
		this.plugin = plugin;
		this.e = e;
	}

	@Override
	public void run() {
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
}