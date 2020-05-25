package fr.upem.projet.reader;

import java.nio.ByteBuffer;

import fr.upem.projet.frame.PrivateAuthFrame;

/**
 * @author CALONNE, JUILLARD Permet de lire une trame et de récupérer un token
 *         depuis un ByteBuffer. Utilise un LongReader.
 *
 */
public class PrivateAuthReader implements Reader {
	private enum State {
		DONE, WAITINGTOKEN, ERROR
	};

	private State state = State.WAITINGTOKEN;
	private long token;
	private final LongReader lr;

	public PrivateAuthReader(ByteBuffer bb) {
		lr = new LongReader(bb);
	}

	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();

		}
		for (;;) {
			switch (state) {
			case WAITINGTOKEN: {
				var process = lr.process();
				if (process == ProcessStatus.DONE) {
					token = (long) lr.get();
					lr.reset();
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
	 * Renvoie un PrivateAuthFrame
	 */
	@Override
	public Object get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return new PrivateAuthFrame(token);
	}

	/**
	 * Permet de remettre l'état de state en mode initial
	 */
	@Override
	public void reset() {
		state = State.WAITINGTOKEN;
		lr.reset();

	}
}
