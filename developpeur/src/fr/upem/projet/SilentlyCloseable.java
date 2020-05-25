package fr.upem.projet;

import java.nio.channels.SelectionKey;

/**
 * @author Calonne Juillard
 *
 */
public interface SilentlyCloseable {
	/**
	 * @param key SelectionKey qui va être fermée.
	 */
	public void silentlyClose(SelectionKey key);
}
