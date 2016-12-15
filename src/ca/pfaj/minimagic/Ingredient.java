package ca.pfaj.minimagic;

import org.bukkit.Material;

/**
 * Represents an ingredient to be added to a ShapelessRecipe
 * @author peter
 *
 */
public class Ingredient
{
	public int amount;
	public Material material;
	
	public Ingredient(int amount, Material material)
	{
		this.amount = amount;
		this.material = material;
	}
	
	public Ingredient(Material material)
	{
		this.amount = 1;
		this.material = material;
	}
}