package fr.upem.projet.frame;

import static fr.upem.projet.frame.IdFrame.PrivateNoRepConnexCliToSrv;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import fr.upem.projet.visitor.AbstractVisitor;

public class PrivateNoRepConnexCliToSrvFrame implements Frame {
	private final String dest;
	private final Charset charset = StandardCharsets.UTF_8;

	
	public PrivateNoRepConnexCliToSrvFrame(String destinataire) {
		this.dest = destinataire;
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
     *@return renvoie un Bytebuffer contenant la trame correspondante � l'objet PrivateNoRepConnexCliToSrvFrame
     *
     */
	public ByteBuffer asByteBuffer() {
		var destEnco = charset.encode(dest);
		var bb = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + destEnco.remaining());
		bb.put(PrivateNoRepConnexCliToSrv.getId()).putInt(destEnco.remaining()).put(destEnco);
		return bb;
	}
	

}
