package fr.upem.projet.visitor;

import static fr.upem.projet.frame.IdFrame.ConnexionRepServ;

import java.nio.ByteBuffer;

import fr.upem.projet.Context;
import fr.upem.projet.ServerChatHack;
import fr.upem.projet.frame.ConnexionMdpFrame;
import fr.upem.projet.frame.ConnexionSansMdpFrame;

/**
 * @author Calonne, Juillard
 * 
 *         Utiliser cette classe lorsque le context d'un serveur pour converser
 *         avec un client et que ce client n'est pas encore connecté.
 *
 */
public class PublicServerAuthVisitor extends AbstractVisitor {

	private final ServerChatHack serverCH;
	private final Context context;

	public PublicServerAuthVisitor(ServerChatHack serverCH, Context context) {
		super(context);
		this.serverCH = serverCH;
		this.context = context;
	}

	@Override
	public void visit(ConnexionMdpFrame frame) {
		if (null == serverCH.mapNameContext.putIfAbsent(frame.getLogin(), context)) {
			context.login = frame.getLogin();
			context.password = frame.getPassword();
			serverCH.demandLoginPassword(context, context.login, context.password);
			context.deconnected = true;
		} else {
			var bb = ByteBuffer.allocate(Byte.BYTES * 2);
			bb.put(ConnexionRepServ.getId()).put((byte) 1);
			context.queueMessage(bb);
		}
	}

	@Override
	public void visit(ConnexionSansMdpFrame frame) {
		var login = frame.getLogin();

		if (null == serverCH.mapNameContext.putIfAbsent(login, context)) {
			context.login = login;
			serverCH.demandLogin(context, login);
			context.deconnected = true;
		} else {
			var bb = ByteBuffer.allocate(Byte.BYTES * 2);
			bb.put(ConnexionRepServ.getId()).put((byte) 1);
			context.queueMessage(bb);
		}
	}
}
