package fr.upem.projet.frame;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import fr.upem.projet.visitor.AbstractVisitor;

public class PrivateConnexCliToSrvFrame implements Frame {
    private final String dest;
    private final long token;
    private final Charset charset = StandardCharsets.UTF_8;

    public PrivateConnexCliToSrvFrame(String dest, long token) {
        this.dest = dest;
        this.token = token;
    }

    @Override
    public void accept(AbstractVisitor visitor) {
        visitor.visit(this);
    }

    /*
     * @return renvoie un String correspondant au destinataire
     *
     */
    public String getDest() {
        return dest;
    }
    /**
     * @return renvoie un Long correspondant au Token
     *
     */
    public long getToken() {
        return token;
    }
    /**
     *@return renvoie un Bytebuffer contenant la trame correspondante � l'objet PrivateConnexCliToSrvFrame
     *
     */
    public ByteBuffer asByteBuffer() {
        var destEncode = charset.encode(dest);

        var bb = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES  + destEncode.remaining() + Long.BYTES);
        ((ByteBuffer) bb).put(IdFrame.PrivateConnexCliToSrv.getId()).putInt(destEncode.remaining()).put(destEncode).putLong(token);
        return bb;
    }
}