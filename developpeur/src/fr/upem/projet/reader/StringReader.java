package fr.upem.projet.reader;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * @author CALONNE, JUILLARD Permet de récupérer une chaîne de caractères encodé
 *         en UTF-8 depuis un ByteBuffer. Il est procède de la taille de la
 *         représentation en octets sur un INT.
 *
 */
public class StringReader implements Reader {

	private enum State {
		DONE, WAITINGINT, WAITINGTEXT, ERROR
	};

	private final ByteBuffer bb;
	private State state = State.WAITINGINT;
	private String value;
	private int size;

	public StringReader(ByteBuffer bb) {
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
						state = State.WAITINGTEXT;
						continue;
					} else {
						return ProcessStatus.REFILL;
					}
				}

				case WAITINGTEXT: {
					if (bb.remaining() >= size) {
						var lim = bb.limit();
						bb.limit(bb.position() + size);
						value = StandardCharsets.UTF_8.decode(bb).toString();
						state = State.DONE;
						bb.limit(lim);
						return ProcessStatus.DONE;
					}
					return ProcessStatus.REFILL;
				}
				default:
					throw new IllegalArgumentException("Unexpected value: ");
				}
			}
		} finally {
			bb.compact();
		}
	}

	/**
	 * Renvoie une String de ByteBuffer
	 */
	@Override
	public String get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return value;
	}

	/**
	 * Permet de remettre l'état de state en mode initial
	 */
	@Override
	public void reset() {
		state = State.WAITINGINT;
	}

}