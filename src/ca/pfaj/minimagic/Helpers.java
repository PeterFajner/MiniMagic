package ca.pfaj.minimagic;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

/**
 * @author peter
 * This class contains helper methods that may be useful for multiple spells/effects.
 */
public class Helpers {
	
	public static void init(Main plugin)
	{
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		// register exp display event
		plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
			@Override
			public void run() 
			{
				for (Player p : plugin.getServer().getOnlinePlayers()) {
					// TODO: just add the scoreboard when the player joins
					List<MetadataValue> meta = p.getMetadata("scoreboard");
					if (meta == null || (meta.size() == 0)) {
						plugin.debug("Adding scoreboard...");
						Scoreboard b = manager.getNewScoreboard();
						b.registerNewTeam(p.getDisplayName());
						Objective obj = b.registerNewObjective("showEXP", "EXP");
						obj.setDisplaySlot(DisplaySlot.PLAYER_LIST);
						obj.setDisplayName("EXP");
						p.setScoreboard(b);
						p.setMetadata("scoreboard", new FixedMetadataValue(plugin, (Object) b));
					}
					Scoreboard board = (Scoreboard) p.getMetadata("scoreboard").get(0).value();
					board.getObjective("showEXP").getScore(p.getDisplayName()).setScore(Experience.getExp(p));
				} 
			}
		}, 0L, 10L);
	}
	
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
		int xp = Experience.getExp(player);
		if (xp >= amount) {
			Experience.changeExp(player, -amount);
			return true;
		}
		else {
			player.sendMessage("Insufficient EXP!");
			return false;
		}
	}
}
