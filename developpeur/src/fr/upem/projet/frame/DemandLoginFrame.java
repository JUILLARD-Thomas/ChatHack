package fr.upem.projet.frame;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class DemandLoginFrame{

	private final String login;
	private final Long idBdd;

	public DemandLoginFrame(String login, Long idBdd) {
		this.login = login;
		this.idBdd = idBdd;
	}
    /**
     * @return renvoie un String correspondant au login
     *
     */
	public String getLogin() {
		return login;
	}
    /**
     *@return renvoie un Bytebuffer contenant la trame correspondante à l'objet DemandLoginFrame
     *
     */
	public ByteBuffer asByteBuffer() {
		var logEncod = StandardCharsets.UTF_8.encode(login);
		var bb = ByteBuffer.allocate(Byte.BYTES + Long.BYTES + Integer.BYTES + logEncod.remaining());
		bb.put((byte) 2).putLong(idBdd).putInt(logEncod.remaining()).put(logEncod);
		return bb;
	}

	
	
}
