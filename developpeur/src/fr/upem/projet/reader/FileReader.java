package fr.upem.projet.reader;

import java.nio.ByteBuffer;

import fr.upem.projet.frame.FileFrame;

/**
 * @author CALONNE, JUILLARD Permet de récupérer un nom encodé en UTF-8 et des
 *         octets depuis un ByteBuffer. Un stringReader et un OctetsReader
 *         seront utilisés pour collecter les informations
 *
 */
public class FileReader implements Reader {

	private enum State {
		DONE, WAITINGNAME, WAITINGOCTETS, ERROR
	}

	private State state = State.WAITINGNAME;
	private String name;
	private ByteBuffer[] bbs;
	private StringReader sr;
	private OctetsReader or;

	public FileReader(ByteBuffer bb) {
		sr = new StringReader(bb);
		or = new OctetsReader(bb);
	}

	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}
		for (;;) {
			switch (state) {

			case WAITINGNAME: {
				var process = sr.process();
				if (process == ProcessStatus.DONE) {
					name = (String) sr.get();
					if (name.length() <= 0) {
						return ProcessStatus.ERROR;
					}
					sr.reset();
					state = State.WAITINGOCTETS;
					continue;

				} else {
					return process;
				}

			}
			case WAITINGOCTETS: {
				var process = or.process();
				if (process == ProcessStatus.DONE) {
					bbs = (ByteBuffer[]) or.get();
					or.reset();
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
	 * Renvoie un FileFrame
	 */
	@Override
	public Object get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		long size = 0;
		for (var bb : bbs) {
			size += bb.position();
		}
		return new FileFrame(name, bbs, size);
	}

	/**
	 * Permet de remettre l'état de state en mode initial
	 */
	@Override
	public void reset() {
		state = State.WAITINGNAME;
		sr.reset();
		or.reset();
	}

}
