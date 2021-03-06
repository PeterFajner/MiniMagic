package ca.pfaj.minimagic;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;

/*
 * Fireball wand, casts a ghast fireball towards the target
 * TODO:
 * V create wand and recipe
 * - create fireball wand and recipe
 * - - create spell effect
 * - - register action
 * - - deduct XP
 * - - deduct durability
 * - create fireball spell
 */

/**
 * A wand that casts a ghast fireball towards the cursor.
 * @author peter
 *
 */
public class SpellFireball
{
	
	public static ItemStack fireballWand;
	public static String IDENTIFIER = "Fireball";
	public static int COST;
	
	public static void init(Main plugin, int cost)
	{
		Server server = plugin.getServer();
		SpellFireball.COST = cost;
		
		// create fireball wand item
		fireballWand = Wand.wand_1.clone();
		ArrayList<String> lore = new ArrayList<String>();
		lore.add(IDENTIFIER);
		lore.add("Cost: " + COST);
		ItemMeta fireballWandMeta = fireballWand.getItemMeta();
		fireballWandMeta.setLore(lore);
		fireballWandMeta.setDisplayName(IDENTIFIER);
		fireballWand.setItemMeta(fireballWandMeta);
		
		// create fireball wand recipe
		// the recipe is by default a stick, the listener converts it into a fireball wand
		ShapelessRecipe recipe = new ShapelessRecipe(Wand.wand_1); // create a generic wand as the default
		recipe.addIngredient(Material.STICK); // the listener will ensure this is a wand
		recipe.addIngredient(3, Material.FIREBALL);
		server.addRecipe(recipe);
		
		// add listener
		server.getPluginManager().registerEvents(new FireballListener(plugin), plugin);
	}
}

class FireballListener implements Listener
{	
	
	Main plugin;

	FireballListener(Main plugin)
	{
		super();
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onItemCrafted(PrepareItemCraftEvent event)
	/**
	 * If there is a recipe that creates a wand, and the ingredients are those for a fireball wand, change the result to a fireball wand.
	 */
	{
		plugin.debug("Crafting started...");
		CraftingInventory inventory = event.getInventory();
		ItemStack result = inventory.getResult();
		if (Helpers.loreContains(result, Wand.IDENTIFIER_MAIN)) { // if a wand is being crafted
			plugin.debug("Crafting wand...");
			boolean wand_in_ingredients = false; // check if a wand is one of the ingredients
			int number_of_fire_charges = 0; // check if three fire charges are in the ingredients
			for (ItemStack ingredient : inventory.getMatrix()) {
				if (Helpers.loreContains(ingredient, Wand.IDENTIFIER_MAIN)) {
					wand_in_ingredients = true;
					plugin.debug("Wand in ingredients.");
				}
				if (ingredient.getData().getItemType() == Material.FIREBALL) {
					plugin.debug("Fire charge in ingredients.");
					number_of_fire_charges++;
				}
			}
			if (wand_in_ingredients && number_of_fire_charges == 3) {
				inventory.setResult(SpellFireball.fireballWand);
			}
		}
	}
	
	@EventHandler
	/**
	 * Handles firing the fireball when clicking with the wand.
	 */
	public void ballFiring(PlayerInteractEvent e) {
	    Player p = e.getPlayer();
	    Action a = e.getAction();
	    ItemStack item = e.getItem();
	    if (a != Action.PHYSICAL) { // the action is clicking something rather than stepping somewhere
	    	if (Helpers.loreContains(item, SpellFireball.IDENTIFIER)) { // the item held is a fireball wand
	    		if (Helpers.deductEXP(p, SpellFireball.COST)) {
	    			plugin.debug("Pew!");
		            p.launchProjectile(Fireball.class);
	    		}
	    	}
	    }
	}
}