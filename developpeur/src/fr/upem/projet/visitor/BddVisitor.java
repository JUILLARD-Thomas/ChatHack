package fr.upem.projet.visitor;

import fr.upem.projet.Context;
import fr.upem.projet.ServerChatHack;
import fr.upem.projet.frame.ConnexionBddNoFrame;
import fr.upem.projet.frame.ConnexionBddYesFrame;

/**
 * @author Calonne, Juillard
 * 
 *         Utiliser cette classe lorsque le context d'un serveur pour converser
 *         avec la base de donnée.
 *
 */
public class BddVisitor extends AbstractVisitor {

	private final ServerChatHack serverCH;

	public BddVisitor(Context context, ServerChatHack serverCH) {
		super(context);
		this.serverCH = serverCH;
	}
	
	@Override
	public void visit(ConnexionBddNoFrame frame) {
		serverCH.bddAnswer(frame.getId(), false);
	}

	@Override
	public void visit(ConnexionBddYesFrame frame) {
		serverCH.bddAnswer(frame.getId(), true);
	}
}
