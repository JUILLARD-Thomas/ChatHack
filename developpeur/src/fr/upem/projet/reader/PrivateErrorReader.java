package fr.upem.projet.reader;

import java.nio.ByteBuffer;

import fr.upem.projet.frame.PrivateErrorFrame;

/**
 * @author CALONNE, JUILLARD Permet de lire une trame et de récupérer un byte et
 *         un nom depuis un ByteBuffer. Utilise un StringReader et un ByteReader
 *         pour obtenir ces informations.
 *
 */
public class PrivateErrorReader implements Reader {
	private enum State {
		DONE, WAITINGWICHERROR, WAITINGNAME, ERROR
	}

	private State state = State.WAITINGWICHERROR;
	private byte wichError;
	private ByteReader br;
	private StringReader sr;
	private String name;

	public PrivateErrorReader(ByteBuffer bb) {
		br = new ByteReader(bb);
		sr = new StringReader(bb);
	}

	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}

		for (;;) {
			switch (state) {

			case WAITINGWICHERROR: {
				var process = br.process();

				if (process == ProcessStatus.DONE) {
					wichError = (byte) br.get();
					if (wichError < 0) {
						return ProcessStatus.ERROR;
					}

					br.reset();
					state = State.WAITINGNAME;
					continue;
				} else {
					return process;
				}
			}
			case WAITINGNAME: {
				var process = sr.process();
				if (process == ProcessStatus.DONE) {
					name = sr.get();
					if (name.length() <= 0) {
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
	 * Renvoie un PrivateErrorFrame
	 */
	@Override
	public Object get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return new PrivateErrorFrame(wichError, name);
	}

	/**
	 * Permet de remettre l'état de state en mode initial
	 */
	@Override
	public void reset() {
		state = State.WAITINGWICHERROR;
		br.reset();

	}

}
