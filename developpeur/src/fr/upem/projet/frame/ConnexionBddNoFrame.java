package fr.upem.projet.frame;

import java.nio.ByteBuffer;

import fr.upem.projet.visitor.AbstractVisitor;

public class ConnexionBddNoFrame implements Frame {
    private Long id;


    public ConnexionBddNoFrame(Long id) {
        this.id = id;
    }

    @Override
    public void accept(AbstractVisitor visitor) {
        visitor.visit(this);

    }
    /**
     * @return renvoie un long correspondant à l'identifiant
     *
     */

    public Long getId() {
        return id;
    }

    /**
     *@return renvoie un Bytebuffer contenant la trame correspondante à l'objet ConnexionBddNoFrame
     *
     */

    public ByteBuffer asByteBuffer() {
        throw new IllegalStateException();
    }

}