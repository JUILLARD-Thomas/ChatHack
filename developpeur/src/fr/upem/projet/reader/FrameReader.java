package fr.upem.projet.reader;

import static fr.upem.projet.frame.IdFrame.ConnexionBddNo;
import static fr.upem.projet.frame.IdFrame.ConnexionBddYes;
import static fr.upem.projet.frame.IdFrame.ConnexionMdp;
import static fr.upem.projet.frame.IdFrame.ConnexionRepServ;
import static fr.upem.projet.frame.IdFrame.ConnexionSansMdp;
import static fr.upem.projet.frame.IdFrame.File;
import static fr.upem.projet.frame.IdFrame.PrivateAuth;
import static fr.upem.projet.frame.IdFrame.PrivateConnexCliToSrv;
import static fr.upem.projet.frame.IdFrame.PrivateConnexSrvToCli;
import static fr.upem.projet.frame.IdFrame.PrivateNoRepConnexCliToSrv;
import static fr.upem.projet.frame.IdFrame.PrivateNoRepConnexSrvToCli;
import static fr.upem.projet.frame.IdFrame.PrivateYesRepConnexCliToSrv;
import static fr.upem.projet.frame.IdFrame.PrivateYesRepConnexSrvToCli;
import static fr.upem.projet.frame.IdFrame.PublicClientToServ;
import static fr.upem.projet.frame.IdFrame.PublicServToClient;

import java.nio.ByteBuffer;

import fr.upem.projet.frame.Frame;
import fr.upem.projet.frame.IdFrame;

/**
 * @author CALONNE, JUILLARD Permet de récupérer une Frame depuis un ByteBuffer.
 *
 */
public class FrameReader implements Reader {

	private enum State {
		DONE, WAITINGOPCODE, WAITINGREADER, ERROR
	};

	private final ByteBuffer bb;
	private State state = State.WAITINGOPCODE;
	private Reader reader;
	private Frame frame;

	public FrameReader(ByteBuffer bb) {
		this.bb = bb;
	}

	private Reader chooseReader(byte opecode) {
		if (opecode == ConnexionMdp.getId()) {
			return new ConnexionMdpReader(bb);
		} else if (opecode == ConnexionRepServ.getId()) {
			return new ConnexionRepServReader(bb);
		} else if (opecode == ConnexionSansMdp.getId()) {
			return new ConnexionSansMdpReader(bb);
		} else if (opecode == PublicClientToServ.getId()) {
			return new MessageReader(bb);
		} else if (opecode == PublicServToClient.getId()) {
			return new PublicServToClientReader(bb);
		} else if (opecode == PrivateConnexCliToSrv.getId()) {
			return new PrivateConnexCliToSrvReader(bb);
		} else if (opecode == PrivateConnexSrvToCli.getId()) {
			return new PrivateConnexSrvToCliReader(bb);
		} else if (opecode == PrivateYesRepConnexCliToSrv.getId()) {
			return new PrivateYesRepConnexCliToSrvReader(bb);
		} else if (opecode == PrivateNoRepConnexCliToSrv.getId()) {
			return new PrivateNoRepConnexCliToSrvReader(bb);
		} else if (opecode == PrivateYesRepConnexSrvToCli.getId()) {
			return new PrivateYesRepConnexSrvToCliReader(bb);
		} else if (opecode == PrivateNoRepConnexSrvToCli.getId()) {
			return new PrivateNoRepConnexSrvToCliReader(bb);
		} else if (opecode == PrivateAuth.getId()) {
			return new PrivateAuthReader(bb);
		} else if (opecode == File.getId()) {
			return new FileReader(bb);
		} else if (opecode == ConnexionBddNo.getId()) {
			return new ConnexionBddNoReader(bb);
		} else if (opecode == ConnexionBddYes.getId()) {
			return new ConnexionBddYesReader(bb);
		} else if (opecode == IdFrame.PrivateError.getId()) {
			return new PrivateErrorReader(bb);
		} else {
			state = State.ERROR;
			throw new IllegalArgumentException("Unexpected value: " + opecode);
		}
	}

	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}

		for (;;) {
			switch (state) {
			case WAITINGOPCODE: {
				bb.flip();
				if (bb.remaining() >= Byte.BYTES) {

					var lim = bb.limit();
					bb.limit(bb.position() + 1);
					var opcode = bb.get();
					bb.limit(lim);
					bb.compact();
					reader = chooseReader(opcode);

					state = State.WAITINGREADER;
					continue;
				} else {
					bb.compact();
					return ProcessStatus.REFILL;
				}
			}

			case WAITINGREADER: {
				var process = reader.process();
				if (process == ProcessStatus.DONE) {
					frame = (Frame) reader.get();
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
	 * Renvoie une frame de ByteBuffer
	 */
	@Override
	public Frame get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return frame;
	}

	/**
	 * Permet de remettre l'état de state en mode initial
	 */
	@Override
	public void reset() {
		state = State.WAITINGOPCODE;
		reader.reset();
	}

}