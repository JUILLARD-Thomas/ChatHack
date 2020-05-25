package fr.upem.projet.reader;

import java.nio.ByteBuffer;

/**
 * @author CALONNE, JUILLARD
 * 
 * Permet de récupérer un byte depuis un ByteBuffer.
 */
public class ByteReader implements Reader {
	private enum State {
		DONE, WAITINGBYTES, ERROR
	}

	private final ByteBuffer bb;
	private State state = State.WAITINGBYTES;
	private byte bit;

	public ByteReader(ByteBuffer bb) {
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
				case WAITINGBYTES: {
					if (bb.remaining() >= Byte.BYTES) {
						var lim = bb.limit();
						bb.limit(bb.position() + 1);
						bit = bb.get();
						bb.limit(lim);
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

	/**
	 * Renvoie un byte
	 */
	@Override
	public Object get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return bit;
	}

	/**
	 * Permet de remettre l'état de state en mode initial
	 */
	@Override
	public void reset() {
		state = State.WAITINGBYTES;

	}

}
