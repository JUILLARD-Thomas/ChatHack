package fr.upem.projet.reader;

public interface Reader {
	/**
	 * @author CALONNE, JUILLARD Cette interface contient toutes les méthodes que
	 *         devront avoir tous les autres Reader via une implémentation
	 *
	 */

	public static enum ProcessStatus {
		DONE, REFILL, ERROR
	};

	public ProcessStatus process();

	public Object get();

	public void reset();

}
