package fr.upem.projet.frame;

import static fr.upem.projet.frame.IdFrame.ConnexionSansMdp;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import fr.upem.projet.visitor.AbstractVisitor;

public class ConnexionSansMdpFrame implements Frame {
    private final String login;
    private final Charset charset = StandardCharsets.UTF_8;


    public ConnexionSansMdpFrame(String login) {
        this.login = login;
    }

    @Override
    public void accept(AbstractVisitor visitor) {
        visitor.visit(this);
    }
    /**
     *@return renvoie un Bytebuffer contenant la trame correspondante à l'objet ConnexionSansMdpFrame
     *
     */
    public ByteBuffer asByteBuffer() {
        var log = charset.encode(login);
        var bb = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + log.remaining());
        bb.put(ConnexionSansMdp.getId()).putInt(log.remaining()).put(log);
        return bb;
    }
    /**
     * @return renvoie un String correspondant au login
     *
     */
    public String getLogin() {
        return login;
    }

}