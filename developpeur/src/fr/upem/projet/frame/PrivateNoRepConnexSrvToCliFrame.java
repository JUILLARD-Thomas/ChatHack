package fr.upem.projet.frame;

import static fr.upem.projet.frame.IdFrame.PrivateNoRepConnexSrvToCli;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import fr.upem.projet.visitor.AbstractVisitor;

public class PrivateNoRepConnexSrvToCliFrame implements Frame {
	private final String exp;
	private final Charset charset = StandardCharsets.UTF_8;

	public PrivateNoRepConnexSrvToCliFrame(String name2) {
		this.exp = name2;
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
     *@return renvoi un Bytebuffer contenant la trame correspondante à l'objet PrivateNoRepConnexSrvToCliFrame
     *
     */
	
	public ByteBuffer asByteBuffer() {
		var expEncod = charset.encode(exp);
		var bb = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + expEncod.remaining());
		bb.put(PrivateNoRepConnexSrvToCli.getId()).putInt(expEncod.remaining()).put(expEncod);
		return bb;
	}
}
