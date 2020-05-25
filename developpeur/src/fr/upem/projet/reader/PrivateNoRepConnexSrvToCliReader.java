package fr.upem.projet.reader;

import java.nio.ByteBuffer;

import fr.upem.projet.frame.PrivateNoRepConnexSrvToCliFrame;

/**
 * @author CALONNE, JUILLARD Permet de lire une trame et de récupérer un nom
 *         depuis un ByteBuffer. Utilise un StringReader pour obtenir cette
 *         information.
 *
 */
public class PrivateNoRepConnexSrvToCliReader implements Reader {

	private enum State {
		DONE, WAITINGNAME2, ERROR
	};

	private State state = State.WAITINGNAME2;
	private String name2;
	private StringReader sr;

	public PrivateNoRepConnexSrvToCliReader(ByteBuffer bb) {
		sr = new StringReader(bb);

	}

	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();

		}
		for (;;) {
			switch (state) {
			case WAITINGNAME2: {
				var process = sr.process();
				if (process == ProcessStatus.DONE) {
					name2 = (String) sr.get();
					if (name2.length() <= 0) {
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
	 * Renvoie un PrivateNoRepConnexSrvToCliFrame
	 */
	@Override
	public Object get() {
		if (state != State.DONE) {
			throw new IllegalStateException();

		}
		return new PrivateNoRepConnexSrvToCliFrame(name2);
	}

	/**
	 * Permet de remettre l'état de state en mode initial
	 */
	@Override
	public void reset() {
		state = State.WAITINGNAME2;
		sr.reset();

	}

}
