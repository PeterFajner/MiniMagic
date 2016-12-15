package ca.pfaj.minimagic;

import java.util.List;

/*
 * Unique spell properties:
 * - wand recipe materials (Material and number), not including the wand
 * - methods that run every several ticks
 * - leftclick, rightclick, leftclickblock, rightclickblock, rightclickentity methods
 * - special actions on init
 * - special actions on disable
 * 
 * Properties passed to a SpellProperties from Main (can be different for each spell):
 * - cost
 * - radius, etc
 */

/**
 * A generic class for a spell's properties. Spells should override needed methods and the ingredients object.
 * @author peter
 *
 */
public interface SpellProperties
{
	/**
	 * The non-wand ingredients in the spell wand's recipe.
	 * @return
	 */
	public List<Ingredient> getIngredients();
	
	/**
	 * Events that are run every fixed amount of time.
	 * @return
	 */
	public List<DetailedRunnable> getRunnables();
	
	public void onLeftClickAir();
	
	public void onRightClickAir();
	
	public void onLeftClickBlock();
	
	public void onRightClickBlock();
	
	public void onRightClickEntity();
	
	public void onEnable();
	
	public void onDisable();
	
}
