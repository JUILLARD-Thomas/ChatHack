package fr.upem.projet.reader;

import java.nio.ByteBuffer;

import fr.upem.projet.frame.Frame;
import fr.upem.projet.frame.PublicServToClientFrame;

/**
 * @author CALONNE, JUILLARD Permet de lire une trame et de récupérer un login
 *         et un text depuis un ByteBuffer. Utilise des StringReader pour
 *         obtenir ces informations.
 *
 */
public class PublicServToClientReader implements Reader {

	private enum State {
		DONE, WAITINGLOGIN, TEXT, ERROR
	};

	private State state = State.WAITINGLOGIN;
	private String login;
	private String text;
	private StringReader sr;

	public PublicServToClientReader(ByteBuffer bb) {
		sr = new StringReader(bb);
	}

	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}

		for (;;) {
			switch (state) {
			case WAITINGLOGIN: {
				var process = sr.process();
				if (process == ProcessStatus.DONE) {
					login = (String) sr.get();
					if (login.length() <= 0) {
						return ProcessStatus.ERROR;
					}
					sr.reset();
					state = State.TEXT;
					continue;
				} else {
					return process;
				}
			}

			case TEXT: {
				var process = sr.process();
				if (process == ProcessStatus.DONE) {
					text = (String) sr.get();
					if (text.length() < 0) {
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
	 * Renvoie un PublicServToClientFrame
	 */
	@Override
	public Frame get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return new PublicServToClientFrame(login, text);
	}

	/**
	 * Permet de remettre l'état de state en mode initial
	 */
	@Override
	public void reset() {
		state = State.WAITINGLOGIN;
		sr.reset();

	}

}
