package fr.upem.projet.frame;


import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import fr.upem.projet.visitor.AbstractVisitor;

public class ConnexionMdpFrame implements Frame {
    private final String login;
    private final String password;
    private final Charset charset = StandardCharsets.UTF_8;


    public ConnexionMdpFrame(String login, String password) {
        this.login = login;
        this.password = password;
    }

    @Override
    public void accept(AbstractVisitor visitor) {
        visitor.visit(this);
    }
    /**
     * @return renvoi un String correspondant au login
     *
     */
    public String getLogin() {
		return login;
	}
    /**
     * @return renvoi un String correspondant au mot de passe
     *
     */
    public String getPassword() {
		return password;
	}
    /**
     *@return renvoie un Bytebuffer contenant la trame correspondante à l'objet ConnexionMdpFrame
     *
     */
    public ByteBuffer asByteBuffer() {
        var log = charset.encode(login);
        var pwd = charset.encode(password);
        var bb = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES * 2 + ((Buffer) log).remaining() + ((Buffer) pwd).remaining());
        bb.put(IdFrame.ConnexionMdp.getId()).putInt(log.remaining()).put(log).putInt(pwd.remaining()).put(pwd);
        return bb;
    }
}