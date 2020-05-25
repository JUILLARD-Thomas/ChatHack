package fr.upem.projet.visitor;

import fr.upem.projet.ClientChatHack;
import fr.upem.projet.Context;
import fr.upem.projet.frame.PrivateAuthFrame;

/**
 * @author Calonne, Juillard
 * 
 *         Utiliser cette classe lorsque le context d'un client pour converser
 *         avec un client et que ce client n'est pas encore connecté.
 *
 */
public class PrivateClientAuthVisitor extends AbstractVisitor {
	
	private final ClientChatHack clientCH;
	private final Context context ;
	
	
	public PrivateClientAuthVisitor(ClientChatHack clientCH, Context context) {
		super(context);
		this.clientCH = clientCH; 
		this.context = context;
		
	}

	@Override
    public void visit(PrivateAuthFrame frame) {
        var token = frame.getToken();
        var name = clientCH.mapTokenName.get(token);
        var tokenRight = clientCH.mapNameToken.get(name);
        if (null != name && null == context.login && null != tokenRight && tokenRight.compareTo(token) == 0) {
            context.login = name;
            clientCH.mapNameContextPri.put(name, context);
            clientCH.mapNameToken.remove(name);
            context.setVisitorHandler(new PrivateClientConnectedVisitor(clientCH, context));
        } else {
            context.silentlyClose();
        }
    }
}
