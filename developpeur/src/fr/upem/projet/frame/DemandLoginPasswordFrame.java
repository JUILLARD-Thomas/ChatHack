package fr.upem.projet.frame;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class DemandLoginPasswordFrame {
	
	private final String login; 
	private final String password;
	private final Long idBdd;
	
	
	public DemandLoginPasswordFrame( Long idBdd , String login, String password) {
		this.login = login;
		this.password = password;
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
     * @return renvoie un String correspondant au mot de passe
     *
     */
	public String getPassword() {
		return password;
	}
    /**
     *@return renvoie un Bytebuffer contenant la trame correspondante � l'objet DemandLoginPasswordFrame
     *
     */
	
	public ByteBuffer asByteBuffer() {
		var logEncod = StandardCharsets.UTF_8.encode(login);
		var pwdEncod = StandardCharsets.UTF_8.encode(password);
		var bb = ByteBuffer.allocate(Byte.BYTES + Long.BYTES + Integer.BYTES * 2 + logEncod.remaining() + pwdEncod.remaining());
		bb.put((byte) 1).putLong(idBdd).putInt(logEncod.remaining()).put(logEncod).putInt(pwdEncod.remaining()).put(pwdEncod);
		return bb;
	}
}
