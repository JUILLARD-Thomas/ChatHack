package fr.upem.projet.frame;

import static fr.upem.projet.frame.IdFrame.PrivateYesRepConnexCliToSrv;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import fr.upem.projet.visitor.AbstractVisitor;

public class PrivateYesRepConnexCliToSrvFrame implements Frame {
	private final String dest;
	private final int port;
	private final Charset charset = StandardCharsets.UTF_8;

	public PrivateYesRepConnexCliToSrvFrame(String destinataire, int port) {
		this.dest = destinataire;
		this.port = port;
	}

	@Override
	public void accept(AbstractVisitor visitor) {
		visitor.visit(this);
	}
 
    /**
     * @return renvoie un String correspondant au destinataire
     *
     */
	public String getDest() {
		return dest;
	}

    /**
     * @return renvoie un int correspondant au num�ro du port
     *
     */
	public int getPort() {
		return port;
	}
    /**
     *@return renvoie un Bytebuffer contenant la trame correspondante � l'objet PrivateYesRepConnexCliToSrvFrame
     *
     */
	public ByteBuffer asByteBuffer() {
		var destEncod = charset.encode(dest);
		var bb = ByteBuffer
				.allocate(Byte.BYTES + Integer.BYTES * 2 + destEncod.remaining() );
		bb.put(PrivateYesRepConnexCliToSrv.getId()).putInt(destEncod.remaining()).put(destEncod).putInt(port);;
		return bb;
	}
	
	
}
