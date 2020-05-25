package fr.upem.projet.frame;

import fr.upem.projet.visitor.AbstractVisitor;

public class ConnexionBddYesFrame implements Frame {
	private Long id;
	
	public ConnexionBddYesFrame(Long id) {
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
}
