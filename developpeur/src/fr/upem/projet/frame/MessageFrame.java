package fr.upem.projet.frame;

import static fr.upem.projet.frame.IdFrame.PublicClientToServ;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import fr.upem.projet.visitor.AbstractVisitor;


public class MessageFrame implements Frame {
    private final String text;
    private final Charset charset = StandardCharsets.UTF_8;


    public MessageFrame(String text) {
        this.text = text;
    }

    @Override
    public void accept(AbstractVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * @return renvoi un String correspondant au text
     *
     */
    public String getText() {
        return text;
    }
    
    /**
     *@return renvoi un Bytebuffer contenant la trame correspondante à l'objet PublicClientToServFrame
     *
     */
    public ByteBuffer asByteBuffer() {
        var textEncod = charset.encode(text);
        var bb = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES  + ((ByteBuffer) textEncod).remaining());
        bb.put(PublicClientToServ.getId()).putInt(textEncod.remaining()).put(textEncod);

        return bb;
    }
}