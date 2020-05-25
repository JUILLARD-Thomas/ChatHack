package fr.upem.projet.reader;

import java.nio.ByteBuffer;

import fr.upem.projet.frame.ConnexionSansMdpFrame;
import fr.upem.projet.frame.Frame;

/**
 * @author CALONNE, JUILLARD Permet de lire une trame et de récupérer un login
 *         depuis un ByteBuffer. Utilise un StringReader pour obtenir ces
 *         informations.
 *
 */
public class ConnexionSansMdpReader implements Reader {

	private enum State {
		DONE, WAITINGLOGIN, ERROR
	};

	private State state = State.WAITINGLOGIN;
	private String login;
	private StringReader sr;

	public ConnexionSansMdpReader(ByteBuffer bb) {
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
					state = State.DONE;
					return ProcessStatus.DONE;
				} else {
					return process;
				}
			}

			default:
				throw new IllegalArgumentException("Unexpected value: " + state);
			}
		}

	}

	/**
	 * Renvoie un ConnexionSansMdpFrame
	 */
	@Override
	public Frame get() {
		if (state != State.DONE) {
			throw new IllegalArgumentException();
		}
		return new ConnexionSansMdpFrame(login);
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
