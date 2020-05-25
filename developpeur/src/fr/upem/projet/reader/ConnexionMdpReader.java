package fr.upem.projet.reader;

import java.nio.ByteBuffer;

import fr.upem.projet.frame.ConnexionMdpFrame;
import fr.upem.projet.frame.Frame;

/**
 * @author CALONNE, JUILLARD Permet de lire une trame et de récupérer les logins
 *         et les mots de passe depuis un ByteBuffer. Utilise des StringReader
 *         pour obtenir ces informations.
 *
 */
public class ConnexionMdpReader implements Reader {

	private enum State {
		DONE, WAITINGLOGIN, WAITINGPWD, ERROR
	};

	private State state = State.WAITINGLOGIN;
	private String login;
	private String password;
	private StringReader sr;

	public ConnexionMdpReader(ByteBuffer bb) {
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
					state = State.WAITINGPWD;
					continue;
				} else {
					return process;
				}
			}

			case WAITINGPWD: {
				var process = sr.process();
				if (process == ProcessStatus.DONE) {
					password = (String) sr.get();
					if (password.length() <= 0) {
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
	 * Renvoie un ConnexionMdpFrame
	 */
	@Override
	public Frame get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return new ConnexionMdpFrame(login, password);
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
