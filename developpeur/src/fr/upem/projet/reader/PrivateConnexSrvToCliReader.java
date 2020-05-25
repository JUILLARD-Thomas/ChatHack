package fr.upem.projet.reader;

import java.nio.ByteBuffer;

import fr.upem.projet.frame.PrivateConnexSrvToCliFrame;

/**
 * @author CALONNE, JUILLARD Permet de lire une trame et de récupérer un nom et
 *         un token depuis un ByteBuffer. Utilise un StringReader et un
 *         LongReaderpour obtenir ces informations.
 *
 */
public class PrivateConnexSrvToCliReader implements Reader {

	private enum State {
		DONE, WAITINGNAME1, WAITINGTOKEN, ERROR
	}

	private State state = State.WAITINGNAME1;
	private String name1;
	private long token;
	private StringReader sr;
	private LongReader lr;

	public PrivateConnexSrvToCliReader(ByteBuffer bb) {
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

			case WAITINGNAME1: {
				var process = sr.process();
				if (process == ProcessStatus.DONE) {
					name1 = (String) sr.get();
					if (name1.length() <= 0) {
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
	 * Renvoie un PrivateConnexSrvToCliFrame
	 */
	@Override
	public Object get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return new PrivateConnexSrvToCliFrame(name1, token);
	}

	/**
	 * Permet de remettre l'état de state en mode initial
	 */
	@Override
	public void reset() {
		state = State.WAITINGNAME1;
		sr.reset();
		lr.reset();
	}

}
