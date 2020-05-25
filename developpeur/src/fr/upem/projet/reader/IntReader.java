package fr.upem.projet.reader;

import java.nio.ByteBuffer;

/**
 * @author CALONNE, JUILLARD
 * 
 * Permet de récupérer un INT depuis un ByteBuffer.
 *
 */
public class IntReader implements Reader {

	private enum State {
		DONE, WAITINGINT, ERROR
	};

	private final ByteBuffer bb;
	private State state = State.WAITINGINT;
	private int size;

	public IntReader(ByteBuffer bb) {
		this.bb = bb;
	}

	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}
		bb.flip();
		try {
			for (;;) {
				switch (state) {
				case WAITINGINT: {
					if (bb.remaining() >= Integer.BYTES) {
						size = bb.getInt();
						if (size < 0) {
							return ProcessStatus.ERROR;
						}
						state = State.DONE;
						return ProcessStatus.DONE;
					} else {
						return ProcessStatus.REFILL;
					}
				}
				default:
					throw new IllegalArgumentException("Unexpected value: " + state);
				}
			}
		} finally {
			bb.compact();
		}
	}

	@Override
	public Object get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return size;
	}

	/**
	 * Permet de remettre l'état de state en mode initial
	 */
	@Override
	public void reset() {
		state = State.WAITINGINT;

	}

}
