package fr.upem.projet.visitor;

import fr.upem.projet.Context;
import fr.upem.projet.ServerChatHack;
import fr.upem.projet.frame.MessageFrame;
import fr.upem.projet.frame.PrivateConnexCliToSrvFrame;
import fr.upem.projet.frame.PrivateConnexSrvToCliFrame;
import fr.upem.projet.frame.PrivateErrorFrame;
import fr.upem.projet.frame.PrivateNoRepConnexCliToSrvFrame;
import fr.upem.projet.frame.PrivateNoRepConnexSrvToCliFrame;
import fr.upem.projet.frame.PrivateYesRepConnexCliToSrvFrame;
import fr.upem.projet.frame.PrivateYesRepConnexSrvToCliFrame;
import fr.upem.projet.frame.PublicServToClientFrame;

/**
 * @author Calonne, Juillard
 * 
 *         Utiliser cette classe lorsque le context d'un serveur pour converser
 *         avec un client et que ce client est bien connecté.
 *
 */
public class PublicServerConnectedVisitor extends AbstractVisitor {

	private final ServerChatHack serverCH;
	private final Context context;

	public PublicServerConnectedVisitor(ServerChatHack serverCH, Context context) {
		super(context);
		this.serverCH = serverCH;
		this.context = context;
	}

	@Override
	public void visit(MessageFrame frame) {
		serverCH.broadcast(new PublicServToClientFrame(context.login, frame.getText()).asByteBuffer());

	}

	@Override
	public void visit(PrivateConnexCliToSrvFrame frame) {
		if (!serverCH.mapNameContext.containsKey(frame.getDest())) {
			serverCH.sendTo(context.login, new PrivateErrorFrame((byte) 1, frame.getDest()).asByteBuffer());
		} else if (frame.getDest().equals(context.login)) {
			serverCH.sendTo(context.login, new PrivateErrorFrame((byte) 2, frame.getDest()).asByteBuffer());
		} else {
			serverCH.sendTo(frame.getDest(),
					new PrivateConnexSrvToCliFrame(context.login, frame.getToken()).asByteBuffer());
		}

	}

	@Override
	public void visit(PrivateYesRepConnexCliToSrvFrame frame) {
			if (serverCH.mapNameContext.containsKey(frame.getDest())) {
				serverCH.sendTo(frame.getDest(), new PrivateYesRepConnexSrvToCliFrame(context.login,
						context.address.length, context.address, frame.getPort()).asByteBuffer());
			} else {
				serverCH.sendTo(context.login, new PrivateErrorFrame((byte) 3, frame.getDest()).asByteBuffer());
			}		
	}

	@Override
	public void visit(PrivateNoRepConnexCliToSrvFrame frame) {
		if (serverCH.mapNameContext.containsKey(frame.getDest())) {
			serverCH.sendTo(frame.getDest(), new PrivateNoRepConnexSrvToCliFrame(context.login).asByteBuffer());
		} else {
			
		}
	}
}
