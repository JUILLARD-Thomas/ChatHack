package fr.upem.projet.reader;

import java.nio.ByteBuffer;

import fr.upem.projet.frame.PrivateConnexCliToSrvFrame;

/**
 * @author CALONNE, JUILLARD Permet de lire une trame et de récupérer deux noms
 *         et un token depuis un ByteBuffer. Utilise des StringReader et un
 *         LongReaderpour obtenir ces informations.
 *
 */
public class PrivateConnexCliToSrvReader implements Reader {
	private enum State {
		DONE, WAITINGDEST, WAITINGTOKEN, ERROR
	};

	private State state = State.WAITINGDEST;
	private String dest;
	private Long token;
	private StringReader sr;
	private LongReader lr;

	public PrivateConnexCliToSrvReader(ByteBuffer bb) {
		sr = new StringReader(bb);
		lr = new LongReader(bb);
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
					state = State.WAITINGTOKEN;
					continue;
				} else {
					return process;
				}

			}

			case WAITINGTOKEN: {
				var process = lr.process();
				if (process == ProcessStatus.DONE) {
					token = (Long) lr.get();
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
	 * Renvoie un PrivateConnexCliToSrvFrame
	 */
	@Override
	public Object get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return new PrivateConnexCliToSrvFrame(dest, token);
	}

	/**
	 * Permet de remettre l'état de state en mode initial
	 */
	@Override
	public void reset() {
		state = State.WAITINGDEST;
		sr.reset();
		lr.reset();

	}

}
