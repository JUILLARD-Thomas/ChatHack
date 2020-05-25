package fr.upem.projet.visitor;

import java.net.InetAddress;
import java.net.UnknownHostException;

import fr.upem.projet.ClientChatHack;
import fr.upem.projet.Context;
import fr.upem.projet.frame.PrivateConnexSrvToCliFrame;
import fr.upem.projet.frame.PrivateErrorFrame;
import fr.upem.projet.frame.PrivateNoRepConnexSrvToCliFrame;
import fr.upem.projet.frame.PrivateYesRepConnexSrvToCliFrame;
import fr.upem.projet.frame.PublicServToClientFrame;

/**
 * @author Calonne, Juillard
 * 
 *         Utiliser cette classe lorsque le context d'un client pour converser
 *         avec un serveur.
 */
public class PublicClientVisitor extends AbstractVisitor{
	private final ClientChatHack clientCH;
	private final Context context;
	
	public PublicClientVisitor(ClientChatHack clientCH, Context context) {
		super(context);
		this.clientCH = clientCH;
		this.context = context;
	}

	@Override
	public void visit(PublicServToClientFrame frame) {
		System.out.println(frame.getExp() + ": " + frame.getMessage());
	}

	@Override
	public void visit(PrivateConnexSrvToCliFrame frame) {

		System.out.println(
				"Voulez-vous accepter la demande de connexion privée de: " + frame.getExp() + "  ? ['yes'/'no']");
		clientCH.demandConnexion.add(frame.getExp());
		clientCH.mapNameToken.putIfAbsent(frame.getExp(), frame.getToken());
		clientCH.mapTokenName.put(frame.getToken(), frame.getExp());
	}

	@Override
	public void visit(PrivateYesRepConnexSrvToCliFrame frame) {
		try {
			System.out.println(frame.getExp() + " a accepté votre demande de connexion");
			InetAddress adr = InetAddress.getByAddress(frame.getAdrIp());
			clientCH.addPrivateConnexion(frame.getExp(), adr, frame.getPortIp());
		} catch (UnknownHostException e) {
			context.silentlyClose();
		}
	}

	@Override
	public void visit(PrivateNoRepConnexSrvToCliFrame frame) {
		
		String name = frame.getExp();
		if(null != clientCH.mapNameToken.get(name) || null != clientCH.mapNameContextPri.get(name)) {
			System.out.println(
				name + " n'a pas accepté ton invitation ! Tes messages en attente pour lui vont être supprimés.");
			clientCH.mapNameToken.remove(name);
			clientCH.mapNameContextPri.remove(name);
		}		
	}
	
	@Override
    public void visit(PrivateErrorFrame frame) {
        if (frame.getWichError() == 1) {
            System.out.println("Aucune personne connectée ne possède ce nom : " + frame.getName());
        }
        else if (frame.getWichError() == 2) {
            System.out.println("Vous ne pouvez pas communiquer avec vous-même. ");
        }
        
        else if (frame.getWichError() == 3) {
            System.out.println(frame.getName() + " n'est plus connecté. Vous ne pouvez donc plus converser avec lui.");
        }
        
        clientCH.mapNameToken.remove(frame.getName());
		clientCH.mapNameContextPri.remove(frame.getName());
    }
}
