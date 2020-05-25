package fr.upem.projet.reader;

import java.nio.ByteBuffer;

import fr.upem.projet.frame.Frame;
import fr.upem.projet.frame.MessageFrame;

/**
 * @author CALONNE, JUILLARD Permet de lire une trame et de récupérer un login
 *         et un text depuis un ByteBuffer. Utilise des StringReader pour
 *         obtenir ces informations.
 *
 */
public class MessageReader implements Reader {

	private enum State {
		DONE, WAITINGMSG, ERROR
	};

	private State state = State.WAITINGMSG;
	private String message;
	private StringReader sr;

	public MessageReader(ByteBuffer bb) {
		sr = new StringReader(bb);
	}

	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}

		for (;;) {
			switch (state) {

			case WAITINGMSG: {
				var process = sr.process();
				if (process == ProcessStatus.DONE) {
					message = (String) sr.get();
					if (message.length() < 0) {
						return ProcessStatus.ERROR;
					}
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
	 * Renvoie un PublicClientToServFrame
	 */
	@Override
	public Frame get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return new MessageFrame(message);
	}

	/**
	 * Permet de remettre l'état de state en mode initial
	 */
	@Override
	public void reset() {
		state = State.WAITINGMSG;
		sr.reset();

	}

}
