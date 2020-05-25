package fr.upem.projet.frame;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import fr.upem.projet.visitor.AbstractVisitor;

public class PrivateErrorFrame implements Frame {

    private final byte wichError;
    private final Charset charset = StandardCharsets.UTF_8;
    private final String name; 

    public PrivateErrorFrame(byte wichError, String name) {
        this.wichError = wichError;
        this.name = name;
    }

    @Override
    public void accept(AbstractVisitor visitor) {
        visitor.visit(this);

    }
    /**
     *@return renvoie un Bytebuffer contenant la trame correspondante à l'objet PrivateErrorFrame
     *
     */
    public ByteBuffer asByteBuffer() {
        var bbName = charset.encode(name);
        var bb = ByteBuffer.allocate(Byte.BYTES * 2 + Integer.BYTES + bbName.remaining()); 
        bb.put(IdFrame.PrivateError.getId()).put(wichError).putInt(bbName.remaining()).put(bbName);
        return bb;
    } 
    /**
     * @return renvoie un byte correspondant au type d'erreur
     *
     */
    public byte getWichError() {
        return wichError;
    }
    /**
     * @return renvoie un String correspondant au Nom
     *
     */
    public String getName() {
        return name;
    }


}