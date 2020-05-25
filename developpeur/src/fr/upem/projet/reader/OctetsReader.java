package fr.upem.projet.reader;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author CALONNE, JUILLARD Permet de récupérer des octets depuis des tableaux
 *         de ByteBuffer. Il est précédé de la taille du text en Long.
 *
 */
public class OctetsReader implements Reader {

	private enum State {
		DONE, WAITINGLONG, WAITINGFILE, ERROR
	};

	private final ByteBuffer bb;
	private State state = State.WAITINGLONG;
	private List<ByteBuffer> bbs = new ArrayList<ByteBuffer>();
	private Long size;

	public OctetsReader(ByteBuffer bb) {
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
					if (bb.remaining() >= Integer.BYTES) {
						size = bb.getLong();
						if (size <= 0) {
							return ProcessStatus.ERROR;
						}
						state = State.WAITINGFILE;
						continue;
					} else {
						return ProcessStatus.REFILL;
					}
				}

				case WAITINGFILE: {
					if (bb.remaining() >= size) {
						var lim = bb.limit();
						bb.limit((int) (bb.position() + size));
						var buffTMP = ByteBuffer.allocate(bb.remaining());
						buffTMP.put(bb);
						bbs.add(buffTMP);
						state = State.DONE;
						bb.limit(lim);
						return ProcessStatus.DONE;
					} else if (bb.hasRemaining()) {
						size -= bb.remaining();
						var buffTMP = ByteBuffer.allocate(bb.remaining());
						buffTMP.put(bb);
						bbs.add(buffTMP);
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
	 * Renvoie un tableau de ByteBuffer
	 */
	@Override
	public Object get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		var bbsArray = new ByteBuffer[bbs.size()];
		for (int i = 0; i < bbs.size(); i++) {
			bbsArray[i] = bbs.get(i);
		}
		return bbsArray;
	}

	/**
	 * Permet de remettre l'état de state en mode initial
	 */
	@Override
	public void reset() {
		state = State.WAITINGLONG;
		bbs = new ArrayList<ByteBuffer>();
	}

}