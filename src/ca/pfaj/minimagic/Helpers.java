package ca.pfaj.minimagic;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author peter
 * This class contains helper methods that may be useful for multiple spells/effects.
 */
public class Helpers {
	/**
	 * Check if an item's lore contains a string.
	 * @param item The item whose lore you want to check.
	 * @param str The method checks if this string is in the lore.
	 * @param ignoreCase Whether case should be ignored, default false.
	 * @return Whether the string was found in item's lore.
	 */
	public static boolean loreContains(ItemStack item, String str, boolean ignoreCase)
	{
		if (item == null) return false;
		if (!item.hasItemMeta()) return false;
		if (item.getItemMeta().getLore() == null) return false;
		List<String> lore = item.getItemMeta().getLore();
		for (String line : lore) {
			if (ignoreCase) {
				if (line.toLowerCase().contains(str.toLowerCase())) {
					return true;
				}
			}
			else {
				if (line.contains(str)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Check if an item's lore contains a string. Case matters.
	 * @param item The item whose lore you want to check.
	 * @param str The method checks if this string is in the lore.
	 * @return Whether the string was found in item's lore.
	 */
	public static boolean loreContains(ItemStack item, String str)
	{
		return loreContains(item, str, false);
	}
	
	/**
	 * Attempts to deduct a positive amount of EXP from a player.
	 * @param player The player to deduct from.
	 * @param amount The amount of EXP to deduct.
	 * @return True if amount successfully deducted, false if amount not deducted because it exceed's the player's total EXP.
	 */
	public static boolean deductEXP(Player player, int amount)
	{
		if (amount < 0) throw new IllegalArgumentException("amount cannot be negative");
		int xp = player.getLevel();
		if (xp >= amount) {
			player.setLevel(xp - amount);
			return true;
		}
		else {
			player.sendMessage("Insufficient EXP!");
			return false;
		}
	}
}
