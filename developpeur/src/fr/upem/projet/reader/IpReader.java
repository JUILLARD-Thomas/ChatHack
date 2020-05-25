package fr.upem.projet.reader;

import java.nio.ByteBuffer;

import fr.upem.projet.frame.IpFrame;

/**
 * @author CALONNE, JUILLARD Permet de récupérer une liste depuis un ByteBuffer.
 *         Cette liste contient une adresse IP et un port.
 *
 */
public class IpReader implements Reader {

	private enum State {
		DONE, WAITINGWICHIP, WAITINGIPADRCLI2, WAITINGPORTCLI2, ERROR
	};

	private final ByteBuffer bb;
	private State state = State.WAITINGWICHIP;
	private int sizeIP;
	private byte[] valueIP;
	private int port;

	public IpReader(ByteBuffer bb) {
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
				case WAITINGWICHIP: {
					if (bb.remaining() >= Integer.BYTES) {
						// champs renvoie ou 4 ou 16 faire la vérification de l'adresse IP
						sizeIP = bb.getInt();
						if (sizeIP != 4 && sizeIP != 16) {
							return ProcessStatus.ERROR;
						}
						state = State.WAITINGIPADRCLI2;
						continue;

					} else {
						return ProcessStatus.REFILL;
					}
				}
				case WAITINGIPADRCLI2: {
					if (bb.remaining() >= Byte.BYTES * sizeIP) {
						var lim = bb.limit();
						bb.limit(bb.position() + sizeIP);
						var tmpBuff = ByteBuffer.allocate(bb.remaining());
						tmpBuff.put(bb);
						valueIP = tmpBuff.array();
						bb.limit(lim);
						state = State.WAITINGPORTCLI2;
						continue;
					} else {
						return ProcessStatus.REFILL;
					}
				}
				case WAITINGPORTCLI2: {
					if (bb.remaining() >= Integer.BYTES) {

						port = bb.getInt();
						if (port <= 0) {
							return ProcessStatus.ERROR;
						}
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
	 * Renvoie un objet Ip
	 */
	@Override
	public IpFrame get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}

		return new IpFrame(port, valueIP, port);

	}

	/**
	 * Permet de remettre l'état de state en mode initial
	 */
	@Override
	public void reset() {
		state = State.WAITINGWICHIP;
	}

}
