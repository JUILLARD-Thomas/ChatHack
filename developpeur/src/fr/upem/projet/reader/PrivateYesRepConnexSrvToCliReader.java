package fr.upem.projet.reader;

import java.nio.ByteBuffer;

import fr.upem.projet.frame.PrivateYesRepConnexSrvToCliFrame;

/**
 * @author CALONNE, JUILLARD
 * Permet de lire une trame et de récupérer un nom et une adresse IP depuis un ByteBuffer. Utilise un StringReader et un IpReader pour obtenir ces informations.
 *
 */
public class PrivateYesRepConnexSrvToCliReader implements Reader {

	private enum State {
		DONE, WAITINGNAME2, WAITINGADRIP, ERROR
	};

	private State state = State.WAITINGNAME2;
	private String name2;
	private int sizeIp; 
	private byte[] adrIp;
	private int portIp;

	private StringReader sr;
	private IpReader ir;

	public PrivateYesRepConnexSrvToCliReader(ByteBuffer bb) {
		sr = new StringReader(bb);
		ir = new IpReader(bb);
	}

	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}
		for (;;) {
			switch (state) {
			case WAITINGNAME2: {
				var process = sr.process();
				if(process == ProcessStatus.DONE) {
					name2 = (String) sr.get();
					if (name2.length() <= 0) {
						return ProcessStatus.ERROR;
					}
					sr.reset();
					state = State.WAITINGADRIP;
					continue;
				}else {
					return process;
				}
				
			}
			
			case WAITINGADRIP : {
				var process = ir.process();
				if (process == ProcessStatus.DONE) {
					var ipData = ir.get();
					sizeIp = (int) ipData.getSize();
					if (sizeIp <= 0) {
						return ProcessStatus.ERROR;
					}
					adrIp = (byte[]) ipData.getAdrIp();
					if (adrIp.length <= 0) {
						return ProcessStatus.ERROR;
					}
					portIp = (int) ipData.getPort();
					if (portIp <= 0) {
						return ProcessStatus.ERROR;
					}
					ir.reset();
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
	 * Renvoie un PrivateYesRepConnexSrvToCliFrame
	 */
	@Override
	public Object get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return new PrivateYesRepConnexSrvToCliFrame(name2, sizeIp, adrIp, portIp) ;/// --------
	}

	
	/**
	 * Permet de remettre l'état de state en mode initial
	 */
	@Override
	public void reset() {
		state = State.WAITINGNAME2;
		sr.reset();
		ir.reset();

	}

}