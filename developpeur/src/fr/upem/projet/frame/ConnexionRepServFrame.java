package fr.upem.projet.frame;

import static fr.upem.projet.frame.IdFrame.ConnexionRepServ;

import java.nio.ByteBuffer;

import fr.upem.projet.visitor.AbstractVisitor;

public class ConnexionRepServFrame implements Frame  {
	private final int ack;
	

	
	public ConnexionRepServFrame(int ack) {
		this.ack = ack;
	}
	
	public ConnexionRepServFrame(boolean isConnected) {

		this(isConnected ? 0 : 1);
	}

	@Override
	public void accept(AbstractVisitor visitor) {
		visitor.visit(this);	
	}
    /**
     * @return renvoie un Integer correspondant à l'ACK
     *
     */
	public Integer getAck() {
		return ack;
	}

	
    /**
     *@return renvoie un Bytebuffer contenant la trame correspondante à l'objet ConnexionRepServFrame
     *
     */
	public ByteBuffer asByteBuffer() {
		var bb = ByteBuffer.allocate(Byte.BYTES * 2);
		bb.put(ConnexionRepServ.getId()).put((byte) ack);
		return bb;
	}
}
