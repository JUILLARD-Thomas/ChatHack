package fr.upem.projet;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import fr.upem.projet.frame.DemandLoginFrame;
import fr.upem.projet.frame.DemandLoginPasswordFrame;
import fr.upem.projet.visitor.BddVisitor;
import fr.upem.projet.visitor.PublicServerAuthVisitor;

/**
 * @author Calonne, JUILLARD
 * 
 *         Classe utilisé pour lancer un serveur.
 *
 */
public class ServerChatHack implements SilentlyCloseable {

	private final ServerSocketChannel serverSocketChannel;
	private final SocketChannel bddSocketChannel;
	private final Context contextBdd;
	private final Selector selector;
	private final Random rand = new Random();
	private final Map<Long, Context> mapIdContextBdd = new HashMap<>();
	public final Map<String, Context> mapNameContext = new HashMap<String, Context>();
	private final Map<SelectionKey, Context> mapKeyContext = new HashMap<SelectionKey, Context>();
	private final SelectionKey bddKey;

	/**
	 * @param port    Port sur lequel devra se mettre ce serveur
	 * @param portBdd port sur lequel nous pouvons converser avec la base de donnée
	 * @throws IOException S'il y a un problème avec la connexion avec la base de données.s
	 */
	public ServerChatHack(int port, int portBdd) throws IOException {
		serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.bind(new InetSocketAddress(port));
		bddSocketChannel = SocketChannel.open(new InetSocketAddress(portBdd));
		selector = Selector.open();
		bddSocketChannel.configureBlocking(false);
			
		bddKey = bddSocketChannel.register(selector, SelectionKey.OP_CONNECT);
		contextBdd = new Context(bddSocketChannel, bddKey, this);
		contextBdd.setVisitorHandler(new BddVisitor(contextBdd, this));
		bddKey.attach(contextBdd);

	}

	/**
	 * Lance l'execution du serveur
	 * 
	 * @throws IOException S'il y a un problème avec la SocketChannel qu'on register.
	 */
	public void launch() throws IOException {
		serverSocketChannel.configureBlocking(false);
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		System.out.println("Le serveur est opérationnel.");
		while (!Thread.interrupted()) {
			try {
				selector.select(this::treatKey);
			} catch (UncheckedIOException tunneled) {
				throw tunneled.getCause();
			}
		}
	}

	/**
	 * Envoi un message à un destinataire
	 * 
	 * @param name nom du destinataire
	 * @param buff trame à envoyer
	 */
	public void sendTo(String name, ByteBuffer buff) {
		mapNameContext.get(name).queueMessage(buff);
		mapNameContext.get(name).updateInterestOps();
	}

	/**
	 * Traite la key en fonction de si elle est en OP_READ, OP_WRITE, OP_CONNECT,
	 * OP_ACCEPT.
	 * 
	 * @param key SelectionKey qui va être traité
	 */
	private void treatKey(SelectionKey key) {
		try {
			if (key.isValid() && key.isConnectable()) {
				((Context) key.attachment()).doConnect();
			}

			if (key.isValid() && key.isAcceptable()) {
				doAccept(key);
			}

		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		try {
			if (key.isValid() && key.isWritable()) {
				((Context) key.attachment()).doWrite();
			}
			if (key.isValid() && key.isReadable()) {
				((Context) key.attachment()).doRead();
			}
		} catch (IOException e) {
			System.out.println("CONNECTION CLOSED with client");
			silentlyClose(key);
		}
	}

	/**
	 * accepte une connexion et la register sur le selector
	 * 
	 * @param key
	 * @throws IOException
	 */
	private void doAccept(SelectionKey key) throws IOException {
		SocketChannel sc = serverSocketChannel.accept();
		if (sc == null) {
			return;
		}
		sc.configureBlocking(false);
		var s = sc.register(selector, SelectionKey.OP_READ);
		var c = new Context(this, s, selector);
		c.setVisitorHandler(new PublicServerAuthVisitor(this, c));
		s.attach(c);
	}

	@Override
	public void silentlyClose(SelectionKey key) {
		Channel sc = (Channel) key.channel();
		try {
			Context c = (Context) key.attachment();

			if (null != c && null != c.login) {
				mapNameContext.remove(c.login);
			}

			if (mapKeyContext.containsKey(key)) {
				var log = mapKeyContext.get(key).login;
				mapNameContext.remove(log);
			}
			sc.close();
		} catch (IOException e) {
		}
	}

	/**
	 * V�rifie dans la base de donnée que personne ne possède ce login
	 * 
	 * @param context context qui veut avoir ce login
	 * @param login   login où on doit v�rifier que personne ne le poss�de
	 */
	public void demandLogin(Context context, String login) {
		var idBdd = rand.nextLong();
		mapIdContextBdd.put(idBdd, context);
		var frame = new DemandLoginFrame(login, idBdd);
		contextBdd.queueMessage(frame.asByteBuffer());
	}

	/**
	 * V�rifie dans la base de donnée que c'est le bon couple login/password
	 * 
	 * @param context  context qui veut avoir ce login
	 * @param login    nom d'utilisateur
	 * @param password mot de passe de l'utilisateur
	 */
	public void demandLoginPassword(Context context, String login, String password) {
		var idBdd = rand.nextLong();
		mapIdContextBdd.put(idBdd, context);
		var frame = new DemandLoginPasswordFrame(idBdd, login, password);
		contextBdd.queueMessage(frame.asByteBuffer());
	}

	/**
	 * R�ponse de la base de donnée
	 * 
	 * @param idBdd   id de la requête faite à la base de donnée
	 * @param answBdd réponse de la base de donnée à la requête idBdd
	 */
	public void bddAnswer(long idBdd, boolean answBdd) {
		var context = mapIdContextBdd.remove(idBdd);
		if (null == context) {
			throw new AssertionError();
		}
		context.bddAnswer(answBdd != (null == context.password), this);
	}

	/**
	 * Add a message to all connected clients queue
	 *
	 * @param buff buffer dans lequel se trouve un message � envoyer
	 */
	public void broadcast(ByteBuffer buff) {
		for (var k : selector.keys()) {
			if (k.isValid() && null != k.attachment() && k != bddKey) {
				buff.flip();
				var buffCopy = ByteBuffer.allocate(buff.remaining());
				buffCopy.put(buff);
				((Context) k.attachment()).queueMessage(buffCopy);
			}
		}
	}

	public static void main(String[] args) throws NumberFormatException, IOException {
		if (args.length != 2) {
			usage();
			return;
		}
		new ServerChatHack(Integer.parseInt(args[0]), Integer.parseInt(args[1])).launch();
	}

	private static void usage() {
		System.out.println("Usage : portServer PortBdd");
	}
}
