package fr.upem.projet.frame;

import static fr.upem.projet.frame.IdFrame.PrivateYesRepConnexSrvToCli;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import fr.upem.projet.visitor.AbstractVisitor;

public class PrivateYesRepConnexSrvToCliFrame implements Frame {
	private final Charset charset = StandardCharsets.UTF_8;
	private final String exp;
	private int sizeIp;
	private byte[] adrIp;
	private int portIp;

	public PrivateYesRepConnexSrvToCliFrame(String name2, int sizeIp, byte[] adrIp2, int portIp) {
		this.exp = name2;
		this.sizeIp = sizeIp;
		this.adrIp = adrIp2;
		this.portIp = portIp;
	}

	@Override
	public void accept(AbstractVisitor visitor) {
		visitor.visit(this);
	}
    /**
     * @return renvoie un tableau de byte correspondant à l'adresse IP 
     *
     */
	public byte[] getAdrIp() {
		return adrIp;
	}
    /**
     * @return renvoie un String correspondant à l'exp�diteur
     *
     */
	public String getExp() {
		return exp;
	}
    /**
     * @return renvoie un int correspondant numéro de port
     *
     */
	public int getPortIp() {
		return portIp;
	}
    /**
     * @return renvoie un int correspondant à la taille de l'IP 
     *
     */
	public int getSizeIp() {
		return sizeIp;
	}
    /**
     *@return renvoie un Bytebuffer contenant la trame correspondante à l'objet PrivateYesRepConnexSrvToCliFrame
     *
     */
	public ByteBuffer asByteBuffer() {
		var expEncod = charset.encode(exp);
		var bb = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES * 3 + adrIp.length + expEncod.remaining());
		bb.put(PrivateYesRepConnexSrvToCli.getId()).putInt(expEncod.remaining()).put(expEncod).putInt(adrIp.length)
				.put(adrIp).putInt(portIp);
		return bb;
	}
}
