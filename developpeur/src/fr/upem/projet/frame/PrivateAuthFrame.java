package fr.upem.projet.frame;

import java.nio.ByteBuffer;

import fr.upem.projet.visitor.AbstractVisitor;

public class PrivateAuthFrame implements Frame {
	
	private final long token;
	
	
	
	public PrivateAuthFrame(long token) {
		this.token = token;
	}

	@Override
	public void accept(AbstractVisitor visitor) {
		visitor.visit(this);
		
	}

    /**
     * @return renvoie un Long correspondant au Token
     *
     */
	public Long getToken() {
		return token;
	}
	
    /**
     *@return renvoie un Bytebuffer contenant la trame correspondante à l'objet PrivateAuthFrame
     *
     */

	public ByteBuffer asByteBuffer() {
		var bb = ByteBuffer.allocate(Byte.BYTES + Long.BYTES);
		bb.put(IdFrame.PrivateAuth.getId()).putLong(token);
		return bb;
	}
}
