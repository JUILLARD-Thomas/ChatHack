package fr.upem.projet.reader;

import java.nio.ByteBuffer;

import fr.upem.projet.frame.PrivateYesRepConnexCliToSrvFrame;


/**
 * @author CALONNE, JUILLARD
 * Permet de lire une trame et de récupérer deux noms et une adresse IP depuis un ByteBuffer. Utilise un StringReader et un IpReader pour obtenir ces informations.
 *
 */
public class PrivateYesRepConnexCliToSrvReader implements Reader {

	private enum State {
		DONE, WAITINGDEST, WAITINGPORT, ERROR
	};

	private State state = State.WAITINGDEST;
	private String dest;
	private int port;
	private StringReader sr;
	private IntReader ir;

	public PrivateYesRepConnexCliToSrvReader(ByteBuffer bb) {
		sr = new StringReader(bb);
		ir = new IntReader(bb);
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
					state = State.WAITINGPORT;
					continue;
				} else {
					return process;
				}
			}			
			case WAITINGPORT : {
				var process = ir.process();
				if (process == ProcessStatus.DONE) {
					port = (Integer) ir.get();
					if (port <= 0) {
						return ProcessStatus.ERROR;
					}
					ir.reset();
					state = State.DONE;
				}return process;
			}
			default:
				throw new IllegalArgumentException("Unexpected value: " + state);

			}
		}

	}

	
	/**
	 * Renvoie un PrivateYesRepConnexCliToSrvFrame
	 */
	@Override
	public Object get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return new PrivateYesRepConnexCliToSrvFrame(dest,port);
	}

	
	/**
	 * Permet de remettre l'état de state en mode initial
	 */
	@Override
	public void reset() {
		state = State.WAITINGDEST;
		sr.reset();
		ir.reset();

	}

}