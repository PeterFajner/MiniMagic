package ca.pfaj.minimagic;

/**
 * Extends Runnable to include initial delay and repeat delay.
 * @author peter
 *
 */
public interface DetailedRunnable extends Runnable
{

	@Override
	public void run();
	
	/**
	 * Returns the initial delay in ticks.
	 * @return
	 */
	public int getInitialDelay();
	
	/**
	 * Returns the repeating delay in ticks.
	 * @return
	 */
	public int getRepeatingDelay();

}
