package fr.upem.projet.reader;

import java.nio.ByteBuffer;

import fr.upem.projet.frame.ConnexionBddYesFrame;

/**
 * @author CALONNE, JUILLARD Permet de lire une trame et de récupérer un ID
 *         depuis un ByteBuffer. Utilise un LongReader pour obtenir ces
 *         informations.
 *
 */
public class ConnexionBddYesReader implements Reader {

	private enum State {
		DONE, WAITINGID, ERROR
	};

	private Long id;
	private LongReader lr;
	private State state = State.WAITINGID;

	public ConnexionBddYesReader(ByteBuffer bb) {
		lr = new LongReader(bb);
	}

	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}

		for (;;) {
			switch (state) {
			case WAITINGID: {
				var process = lr.process();
				if (process == ProcessStatus.DONE) {
					id = (Long) lr.get();
					state = State.DONE;
				}
				return process;
			}
			default:
				throw new IllegalArgumentException("Unexpected value: " + state);
			}

		}
	}

	/**
	 * Renvoi un ConnexionBddYesFrame
	 */
	@Override
	public Object get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return new ConnexionBddYesFrame(id);
	}

	/**
	 * Permet de remettre l'état de state en mode initial
	 */
	@Override
	public void reset() {
		state = State.WAITINGID;
		lr.reset();

	}

}
