package fr.upem.projet.reader;

import java.nio.ByteBuffer;

import fr.upem.projet.frame.ConnexionRepServFrame;
import fr.upem.projet.frame.Frame;

/**
 * @author CALONNE, JUILLARD Permet de lire une trame et de récupérer un ACK
 *         depuis un ByteBuffer.
 *
 */
public class ConnexionRepServReader implements Reader {

	private enum State {
		DONE, WAITINGACK, ERROR
	};

	private final ByteBuffer bb;
	private State state = State.WAITINGACK;
	private int ack;

	public ConnexionRepServReader(ByteBuffer bb) {
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
				case WAITINGACK: {

					if (bb.remaining() >= Byte.BYTES) {
						ack = bb.get();
						state = State.DONE;
						return ProcessStatus.DONE;
					}
					return ProcessStatus.REFILL;
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
	 * Renvoie un ConnexionRepServFrame
	 */
	@Override
	public Frame get() {
		if (state != State.DONE) {
			throw new IllegalArgumentException();
		}
		return new ConnexionRepServFrame(ack);
	}

	/**
	 * Permet de remettre l'état de state en mode initial
	 */
	@Override
	public void reset() {
		state = State.WAITINGACK;
	}

}
