package fr.upem.projet.frame;

import fr.upem.projet.visitor.AbstractVisitor;

public interface Frame {
	void accept(AbstractVisitor visitor);
	
//	ByteBuffer asByteBuffer();

}
