package fr.upem.projet.frame;

import static fr.upem.projet.frame.IdFrame.PublicServToClient;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import fr.upem.projet.visitor.AbstractVisitor;

public class PublicServToClientFrame implements Frame {
	private final Charset charset = StandardCharsets.UTF_8;
	private final String exp;
	private final String message;

	public PublicServToClientFrame(String login, String text) {
		this.exp = login;
		this.message = text;
	}

	@Override
	public void accept(AbstractVisitor visitor) {
		visitor.visit(this);
	}
    /**
     * @return renvoi un String correspondant à l'expéditeur
     *
     */
	public String getExp() {
		return exp;
	}
    /**
     * @return renvoi un String correspondant au message
     *
     */
	public String getMessage() {
		return message;
	}
    /**
     *@return renvoi un Bytebuffer contenant la trame correspondante à l'objet PublicServToClientFrame
     *
     */
	public ByteBuffer asByteBuffer() {
		var expEncod = charset.encode(exp);
		var messageEncod = charset.encode(message);
		var bb = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES * 2 + expEncod.remaining() + messageEncod.remaining());
		bb.put(PublicServToClient.getId()).putInt(expEncod.remaining()).put(expEncod).putInt(messageEncod.remaining()).put(messageEncod);
		return bb;
	}
}
