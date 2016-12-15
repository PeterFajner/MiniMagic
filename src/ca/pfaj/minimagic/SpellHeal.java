package ca.pfaj.minimagic;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.Server;
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
import org.bukkit.attribute.Attribute;

/*
 * Heal wand, heals the player by heal-ammount for heal-cost
 * TODO:
 *   - Add projectile version (while shift is being pressed)'
 *   - add AOE?
 *   - add check of undead
 * 
 */

/**
 * A wand that casts a ghast heal towards the cursor.
 * @author peter
 *
 */
public class SpellHeal
{
	
	public static ItemStack healWand;
	public static String IDENTIFIER = "Heal";
	public static int COST;
    public static int AMT;
	
	public static void init(Main plugin, int cost, int ammount)
	{
		Server server = plugin.getServer();
		SpellHeal.COST = cost;
                SpellHeal.AMT = ammount;
		
		// create heal wand item
		healWand = Wand.wand_1.clone();
		ArrayList<String> lore = new ArrayList<String>();
		lore.add(IDENTIFIER);
		lore.add("Cost: " + COST);
		ItemMeta healWandMeta = healWand.getItemMeta();
		healWandMeta.setLore(lore);
		healWandMeta.setDisplayName(IDENTIFIER);
		healWand.setItemMeta(healWandMeta);
		
		// create heal wand recipe
		// the recipe is by default a stick, the listener converts it into a heal wand
		ShapelessRecipe recipe = new ShapelessRecipe(Wand.wand_1); // create a generic wand as the default
		recipe.addIngredient(Material.STICK); // the listener will ensure this is a wand
		recipe.addIngredient(Material.FERMENTED_SPIDER_EYE);
        recipe.addIngredient(Material.WHEAT);
        recipe.addIngredient(Material.MILK_BUCKET);
		server.addRecipe(recipe);
		
		// add listener
		server.getPluginManager().registerEvents(new HealListener(plugin), plugin);
	}
}

class HealListener implements Listener
{	
	
	Main plugin;
	
	HealListener(Main plugin)
	{
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onItemCrafted(PrepareItemCraftEvent event)
	/**
	 * If there is a recipe that creates a wand, and the ingredients are those for a heal wand, change the result to a heal wand.
	 */
	{
		plugin.debug("Crafting started...");
		CraftingInventory inventory = event.getInventory();
		ItemStack result = inventory.getResult();
		if (Helpers.loreContains(result, Wand.IDENTIFIER_MAIN)) { // if a wand is being crafted
			plugin.debug("Crafting wand...");
			boolean wand_in_ingredients = false; // check if a wand is one of the ingredients
                        //options to check rest of ingredients
			boolean has_f_spider_eye = false;
                        boolean has_wheat = false;
                        boolean has_milk = false;
                        
                        //check all ingredients are as the should be
			for (ItemStack ingredient : inventory.getMatrix()) {
				if (Helpers.loreContains(ingredient, Wand.IDENTIFIER_MAIN)) {
					wand_in_ingredients = true;
					plugin.debug("Wand in ingredients.");
				}
				switch (ingredient.getData().getItemType()) {
                                        case FERMENTED_SPIDER_EYE:
                                                plugin.debug("Has f_spider_eye");
                                                has_f_spider_eye = true;
                                                break;
                                        case WHEAT:
                                                plugin.debug("Has wheat");
                                                has_wheat = true;
                                                break;
                                        case MILK_BUCKET:
                                                plugin.debug("Has milk");
                                                has_milk = true;
                                                break;
                                        default:
                                                break;
				}
			}
			if (wand_in_ingredients && has_f_spider_eye && has_wheat && has_milk) {
				inventory.setResult(SpellHeal.healWand);
			}
		}
	}
	
	@EventHandler
	/**
	 * Handles firing the heal when clicking with the wand.
	 */
	public void ballFiring(PlayerInteractEvent e) {
	    Player p = e.getPlayer();
	    Action a = e.getAction();
	    ItemStack item = e.getItem();
	    if (a != Action.PHYSICAL) { // the action is clicking something rather than stepping somewhere
	    	if (Helpers.loreContains(item, SpellHeal.IDENTIFIER)) { // the item held is a heal wand
	    		if (Helpers.deductEXP(p, SpellHeal.COST)) {
	    			plugin.debug("Pew!");
		           //effect of the spell
                           
                           //ammount that needs to be added back
                           double missing_health = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() - p.getHealth();
                           //add up heal-amount back to the player
                           p.setHealth(p.getHealth() + (missing_health > SpellHeal.AMT ? SpellHeal.AMT : missing_health));
	    		}
	    	}
	    }
	}
}
