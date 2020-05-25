package fr.upem.projet.visitor;

import fr.upem.projet.Context;
import fr.upem.projet.frame.ConnexionBddNoFrame;
import fr.upem.projet.frame.ConnexionBddYesFrame;
import fr.upem.projet.frame.ConnexionMdpFrame;
import fr.upem.projet.frame.ConnexionRepServFrame;
import fr.upem.projet.frame.ConnexionSansMdpFrame;
import fr.upem.projet.frame.FileFrame;
import fr.upem.projet.frame.PrivateAuthFrame;
import fr.upem.projet.frame.PrivateConnexCliToSrvFrame;
import fr.upem.projet.frame.PrivateConnexSrvToCliFrame;
import fr.upem.projet.frame.PrivateErrorFrame;
import fr.upem.projet.frame.PrivateNoRepConnexCliToSrvFrame;
import fr.upem.projet.frame.PrivateNoRepConnexSrvToCliFrame;
import fr.upem.projet.frame.PrivateYesRepConnexCliToSrvFrame;
import fr.upem.projet.frame.PrivateYesRepConnexSrvToCliFrame;
import fr.upem.projet.frame.MessageFrame;
import fr.upem.projet.frame.PublicServToClientFrame;

/**
 * @author Calonne, Juillard
 *
 */
public abstract class AbstractVisitor {

	private final Context context;

	public AbstractVisitor(Context context) {
		this.context = context;
	}

	/**
	 * Cette trame correspond a la trame qui est envoyé pour la demande de connexion
	 * d'un client au serveur en mode avec mot de passe.
	 * 
	 * @param frame frame utilisé pour le pattern visitor.
	 */
	public void visit(ConnexionMdpFrame frame) {
		context.silentlyClose();
	}

	/**
	 * Cette trame correspond à la trame qui est envoyé pour la demande de connexion
	 * d'un client au serveur en mode sans mot de passe.
	 * 
	 * @param frame frame utilisé pour le pattern visitor.
	 */
	public void visit(ConnexionSansMdpFrame frame) {
		context.silentlyClose();
	}

	/**
	 * Cette trame correspond à la trame qui est envoyé pour la réponse de connexion
	 * d'un serveur au client.
	 * 
	 * @param frame frame utilisé pour le pattern visitor.
	 */
	public void visit(ConnexionRepServFrame frame) {
		context.silentlyClose();
	}

	/**
	 * Cette trame correspond à la trame qui est envoyé lorsqu'un client envoi un
	 * message public au serveur.
	 * 
	 * @param frame frame utilisé pour le pattern visitor.
	 */
	public void visit(PublicServToClientFrame frame) {
		context.silentlyClose();
	}

	/**
	 * Cette trame correspond à la trame qui est envoyé lorsque le serveur envoi un
	 * message public à un client.
	 * 
	 * @param frame frame utilisé pour le pattern visitor.
	 */
	public void visit(MessageFrame frame) {
		context.silentlyClose();
	}

	/**
	 * Cette trame correspond à la trame qui est envoyé lorsqu'un client fait une
	 * demande de connexion privée a un autre client. Cette trame correspond à celle
	 * du client vers le serveur.
	 * 
	 * @param frame frame utilisé pour le pattern visitor.
	 */
	public void visit(PrivateConnexCliToSrvFrame frame) {
		context.silentlyClose();
	}

	/**
	 * Cette trame correspond à la trame qui est envoyé lorsqu'un client répond à
	 * une demande de connexion priv�e positive a un autre client. Cette trame
	 * correspond à celle du client vers le serveur.
	 * 
	 * @param frame frame utilisé pour le pattern visitor.
	 */
	public void visit(PrivateYesRepConnexCliToSrvFrame frame) {
		context.silentlyClose();
	}

	/**
	 * Cette trame correspond à la trame qui est envoyé lorsqu'un client fait une
	 * demande de connexion privée a un autre client. Cette trame correspond à celle
	 * du serveur vers le client.
	 * 
	 * @param frame frame utilisé pour le pattern visitor.
	 */
	public void visit(PrivateConnexSrvToCliFrame frame) {
		context.silentlyClose();
	}

	/**
	 * Cette trame correspond à la trame qui est envoyé lorsqu'un client répond à
	 * une demande de connexion privée négative a un autre client. Cette trame
	 * correspond à celle du client vers le serveur.
	 * 
	 * @param frame frame utilisé pour le pattern visitor.
	 */
	public void visit(PrivateNoRepConnexCliToSrvFrame frame) {
		context.silentlyClose();
	}

	/**
	 * Cette trame correspond à la trame qui est envoyé lorsqu'un client répond à
	 * une demande de connexion privée positive a un autre client. Cette trame
	 * correspond à celle du serveur vers le client.
	 * 
	 * @param frame frame utilisé pour le pattern visitor.
	 */
	public void visit(PrivateYesRepConnexSrvToCliFrame frame) {
		context.silentlyClose();
	}

	/**
	 * Cette trame correspond à la trame qui est envoyé lorsqu'un client répond à
	 * une demande de connexion privée positive a un autre client. Cette trame
	 * correspond à celle du serveur vers le client.
	 * 
	 * @param frame frame utilisé pour le pattern visitor.
	 */
	public void visit(PrivateNoRepConnexSrvToCliFrame frame) {
		context.silentlyClose();
	}

	/**
	 * Cette trame correspond à la trame qui est envoyé lorsqu'un client répond à
	 * une demande de connexion privée positive a un autre client. Cette trame
	 * correspond à celle du client vers le serveur.
	 * 
	 * @param frame frame utilisé pour le pattern visitor.
	 */
	public void visit(PrivateAuthFrame frame) {
		context.silentlyClose();
	}


	/**
	 * Cette trame correspond à la trame qui est envoyé lorsqu'un client envoi un
	 * fichier privé à un autre client.
	 * 
	 * @param frame frame utilisé pour le pattern visitor.
	 */
	public void visit(FileFrame frame) {
		context.silentlyClose();
	}

	/**
	 * Cette trame correspond à la trame qui est envoyé lorsque la base de donnée
	 * dit que ce n'est pas possible.
	 * 
	 * @param frame frame utilisé pour le pattern visitor.
	 */
	public void visit(ConnexionBddNoFrame frame) {
		context.silentlyClose();
	}

	/**
	 * Cette trame correspond à la trame qui est envoyé lorsque la base de donnée
	 * dit que c'est possible.
	 * 
	 * @param frame frame utilisé pour le pattern visitor.
	 */
	public void visit(ConnexionBddYesFrame frame) {
		context.silentlyClose();
	}

	/**
	 * Cette trame correspond à la trame qui est envoyé lorsqu'il y a une erreur.
	 * 
	 * @param privateErrorFrame frame utilisé pour le pattern visitor.
	 */
	public void visit(PrivateErrorFrame privateErrorFrame) {
		context.silentlyClose();
	}

}
