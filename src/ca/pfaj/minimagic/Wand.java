package ca.pfaj.minimagic;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class Wand
/**
 * Contains info about the wand item.
 */
// TODO: make enchanted?
{
	public static ItemStack wand_1; // level 1 wand
	public static final String IDENTIFIER_MAIN = "Wand "; // an item containing this in its lore is a wand
	public static final String IDENTIFIER_LEVEL_1 = IDENTIFIER_MAIN + "Level I"; // an item containing this in its lore is a level 1 wand
	
	
	public static void init(Plugin plugin)
	{
		Server server = plugin.getServer();
		
		// create wand item with lore
		wand_1 = new ItemStack(Material.STICK);
		List<String> lore = new ArrayList<String>();
		lore.add(IDENTIFIER_LEVEL_1);
		ItemMeta wandMeta = wand_1.getItemMeta();
		wandMeta.setLore(lore);
		wandMeta.setDisplayName("Wand");
		wand_1.setItemMeta(wandMeta);
		
		// create wand recipe
		ShapelessRecipe wandRecipe = new ShapelessRecipe(wand_1);
		wandRecipe.addIngredient(Material.STICK);
		wandRecipe.addIngredient(Material.DIAMOND);
		wandRecipe.addIngredient(2, Material.EXP_BOTTLE);
		server.addRecipe(wandRecipe);
	}
}
