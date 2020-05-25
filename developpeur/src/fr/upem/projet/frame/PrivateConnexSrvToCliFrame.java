package fr.upem.projet.frame;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import fr.upem.projet.visitor.AbstractVisitor;

public class PrivateConnexSrvToCliFrame implements Frame {

    private final String exp;
    private final long token;
    private final Charset charset = StandardCharsets.UTF_8;

    public PrivateConnexSrvToCliFrame(String exp, long token) {
        this.exp = exp;
        this.token = token;
    }
    @Override
    public void accept(AbstractVisitor visitor) {
        visitor.visit(this);

    }
    /**
     * @return renvoie un String correspondant à l'expéditeur
     *
     */
    public String getExp() {
        return exp;
    }
    /**
     * @return renvoie un Long correspondant au Token
     *
     */

    public long getToken() {
        return token;
    }
    /**
     *@return renvoie un Bytebuffer contenant la trame correspondante à l'objet PrivateConnexSrvToCliFrame
     *
     */
    public ByteBuffer asByteBuffer() {
        var expEncode = charset.encode(exp);
        var bb = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + expEncode.remaining() + Long.BYTES);
        bb.put(IdFrame.PrivateConnexSrvToCli.getId()).putInt(expEncode.remaining()).put(expEncode).putLong(token);
        return bb;
    }
}