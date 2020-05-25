package fr.upem.projet.reader;

import java.nio.ByteBuffer;

import fr.upem.projet.frame.PrivateNoRepConnexCliToSrvFrame;

/**
 * @author CALONNE, JUILLARD
 * 
 *         Permet de lire une trame et de récupérer deux noms depuis un
 *         ByteBuffer. Utilise des StringReader pour obtenir ces informations.
 *
 */
public class PrivateNoRepConnexCliToSrvReader implements Reader {

	private enum State {
		DONE, WAITINGDEST, ERROR
	};

	private State state = State.WAITINGDEST;
	private String dest;
	private StringReader sr;

	public PrivateNoRepConnexCliToSrvReader(ByteBuffer bb) {
		sr = new StringReader(bb);

	}

	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();

		}
		for (;;) {
			switch (state) {
			case WAITINGDEST: {
				var process = sr.process();
				if (process == ProcessStatus.DONE) {
					dest = (String) sr.get();
					if (dest.length() <= 0) {
						return ProcessStatus.ERROR;
					}
					sr.reset();
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
	 * Renvoie un PrivateNoRepConnexCliToSrvFrame
	 */
	@Override
	public Object get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return new PrivateNoRepConnexCliToSrvFrame(dest);

	}

	/**
	 * Permet de remettre l'état de state en mode initial
	 */
	@Override
	public void reset() {
		state = State.WAITINGDEST;
		sr.reset();

	}

}
