package fr.upem.projet.reader;

import java.nio.ByteBuffer;


/**
 * @author CALONNE, JUILLARD
 * Permet de récupérer un Long depuis un ByteBuffer.
 *
 */
public class LongReader implements Reader {

	private enum State {
		DONE, WAITINGLONG, ERROR
	};

	private final ByteBuffer bb;
	private State state = State.WAITINGLONG;
	private long size;

	public LongReader(ByteBuffer bb) {
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
				case WAITINGLONG: {
					if (bb.remaining() >= Long.BYTES) {
						size = bb.getLong();
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
		state = State.WAITINGLONG;

	}

}
