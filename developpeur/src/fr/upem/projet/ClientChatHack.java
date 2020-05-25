package fr.upem.projet;

import static fr.upem.projet.frame.IdFrame.ConnexionRepServ;
import static fr.upem.projet.frame.IdFrame.File;
import static fr.upem.projet.frame.IdFrame.PrivateConnexCliToSrv;
import static fr.upem.projet.frame.IdFrame.PrivateMsg;
import static fr.upem.projet.frame.IdFrame.PrivateNoRepConnexCliToSrv;
import static fr.upem.projet.frame.IdFrame.PrivateYesRepConnexCliToSrv;
import static fr.upem.projet.frame.IdFrame.PublicClientToServ;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import fr.upem.projet.frame.ConnexionMdpFrame;
import fr.upem.projet.frame.ConnexionSansMdpFrame;
import fr.upem.projet.frame.FileFrame;
import fr.upem.projet.frame.IdFrame;
import fr.upem.projet.frame.MessageFrame;
import fr.upem.projet.frame.PrivateAuthFrame;
import fr.upem.projet.frame.PrivateConnexCliToSrvFrame;
import fr.upem.projet.frame.PrivateNoRepConnexCliToSrvFrame;
import fr.upem.projet.frame.PrivateYesRepConnexCliToSrvFrame;
import fr.upem.projet.visitor.PrivateClientAuthVisitor;
import fr.upem.projet.visitor.PrivateClientConnectedVisitor;
import fr.upem.projet.visitor.PublicClientVisitor;

/**
 * @author Calonne, Juillard
 * 
 *         Client qui sert à lancer un client.
 *
 */
public class ClientChatHack implements SilentlyCloseable {

	static private class Command {
		final IdFrame opcode;
		final String text;

		public Command(IdFrame opcode, String text) {
			this.opcode = opcode;
			this.text = text;
		}
	}

	private final Selector selector;
	private SocketChannel sc;
	private ServerSocketChannel serverSocketChannel;
	private final InetSocketAddress serverAddress;
	private final String login;
	private final String password;
	public final String pathFile;
	private final Random rand;
	private SelectionKey uniqueKey;
	private final BlockingQueue<Command> consoleQueue = new ArrayBlockingQueue<Command>(1);
	public final Map<String, Long> mapNameToken = new HashMap<String, Long>();
	public final Map<Long, String> mapTokenName = new HashMap<Long, String>();
	public final Map<String, Context> mapNameContextPri = new HashMap<String, Context>();
	public final ArrayBlockingQueue<String> demandConnexion = new ArrayBlockingQueue<String>(10);
	private int port;
	private final Thread consoleThread;

	/**
	 * @param hostname nom de l'hote
	 * @param port     port sur lequel est connect� le serveur
	 * @param pathFile chemin pour envoyer ou recevoir des fichiers
	 * @param login    nom du client
	 * @param pwd      mot de passe du client
	 * @throws IOException Si la connexion à la base de donnée ne se fait pas.
	 */
	public ClientChatHack(String hostname, int port, String pathFile, String login, String pwd) throws IOException {
		this.login = login;
		this.password = pwd;
		this.pathFile = pathFile;
		this.rand = new Random();
		this.selector = Selector.open();
		this.serverAddress = new InetSocketAddress(hostname, port);
		this.sc = SocketChannel.open(serverAddress);
		this.consoleThread = new Thread(this::consoleThreadRun);
		this.consoleThread.setDaemon(true);
	}

	public ClientChatHack(String hostname, int port, String pathFile, String login) throws IOException {
		this(hostname, port, pathFile, login, null);
	}

	public static void main(String[] args) throws IOException {
		if (args.length == 5) {
			var client = new ClientChatHack(args[0], Integer.parseInt(args[1]), args[2], args[3], args[4]);
			client.launch();
		} else if (args.length == 4) {
			var client = new ClientChatHack(args[0], Integer.parseInt(args[1]), args[2], args[3]);
			client.launch();
		} else {
			usage();
		}
	}

	/**
	 * Thread qui lit les commandes clavier et les envoie au bon contexte
	 */
	private void consoleThreadRun() {
		try (var sc = new Scanner(System.in)) {
			try {
				while (sc.hasNextLine()) {
					var line = sc.nextLine();
					if (!line.equals("")) {
						ProcessConsole(line);
						selector.wakeup();
					}
				}
			} catch (InterruptedException e) {
				// handle exception
			} finally {
				System.out.println("STOP console");
			}
		}
	}

	/**
	 * @param line Ligne à analyser pour savoir si c'est un message public, privé ou
	 *             un envoie de fichier
	 * @throws InterruptedException
	 */
	private void ProcessConsole(String line) throws InterruptedException {
		if (!demandConnexion.isEmpty()) {
			ProcessConsoleConnexion(line);
		} else if (line.charAt(0) == '@') {
			ProcessConsolePrivateMsg(line);
		} else if (line.charAt(0) == '/') {
			ProcessConsolePrivateFile(line);
		} else {
			ProcessConsolePublic(line);
		}
	}

	/**
	 * @param line vérifie que l'utilisateur ait bien répondu à la demande de
	 *             connexion par "yes" ou "no"
	 */
	private void ProcessConsoleConnexion(String line) {
		try {
			if (line.equals("yes")) {
				if (null == serverSocketChannel) {
					serverSocketChannel = ServerSocketChannel.open();
					serverSocketChannel.bind(null);
					serverSocketChannel.configureBlocking(false);
					serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
					InetSocketAddress address = (InetSocketAddress) serverSocketChannel.getLocalAddress();
					this.port = address.getPort();
				}

				var name2 = demandConnexion.poll();
				consoleQueue.add(new Command(PrivateYesRepConnexCliToSrv, name2));
			} else if (line.equals("no")) {
				var name2 = demandConnexion.poll();
				consoleQueue.add(new Command(PrivateNoRepConnexCliToSrv, name2));
				mapNameToken.remove(name2);
			} else {
				System.out.println("Votre réponse n'a pas été prise en compte. "
						+ "Voulez-vous accepter la demande de connexion privée de : " + demandConnexion.peek()
						+ "  ? ['yes'/'no']");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
	}

	/**
	 * @param line ligne à décomposer si c'est un message privé
	 * @throws InterruptedException
	 */
	private void ProcessConsolePrivateMsg(String line) throws InterruptedException {
		var tmp = line.substring(1).split(" ", 2);

		if (mapNameToken.containsKey(tmp[0])) {
			System.out.println("Tu as déjà  fait une demande de connexion privée avec " + tmp[0]
					+ ", il recevra tes messages s'il accepte ton invitation !");
		}

		if (mapNameContextPri.containsKey(tmp[0])) {
			if (tmp.length == 2) {
				consoleQueue.add(new Command(PrivateMsg, line.substring(1)));
			} else if(!mapNameToken.containsKey(tmp[0])) {
				System.out.println("La connexion privée a déjà été établie avec " + tmp[0]);
			}

		} else {
			var c = new Context(tmp[0], this);
			mapNameContextPri.put(tmp[0], c);
			Long token = rand.nextLong();
			mapNameToken.put(tmp[0], token);
			c.queueMessage(new PrivateAuthFrame(token).asByteBuffer());

			if (2 == tmp.length) {
				c.queueMessage(new MessageFrame(tmp[1]).asByteBuffer());
			}
			consoleQueue.add(new Command(PrivateConnexCliToSrv, tmp[0]));
		}
	}

	/**
	 * envoi d'un message public
	 * 
	 * @param line correspond au message qui sera envoyé
	 * @throws InterruptedException
	 */
	private void ProcessConsolePublic(String line) throws InterruptedException {
		consoleQueue.add(new Command(PublicClientToServ, line));
	}

	/**
	 * envoi d'un fichier
	 * 
	 * @param line correspond à l'expediteur suivi d'un espace, puis le nom de
	 *             fichier
	 */
	private void ProcessConsolePrivateFile(String line) {
		var tmp = line.substring(1).split(" ", 2);

		if (mapNameToken.containsKey(tmp[0])) {
			System.out.println("Tu as déjà fait une demande de connexion privée avec " + tmp[0]
					+ ", il recevra tes fichiers s'il accepte ton invitation !");
		}

		if (mapNameContextPri.containsKey(tmp[0])) {
			if (tmp.length == 2) {
				consoleQueue.add(new Command(File, line.substring(1)));
			} else if(!mapNameToken.containsKey(tmp[0])) {
				System.out.println("La connexion privée a déjà été établie avec " + tmp[0]);
			}

		} else {
			var c = new Context(tmp[0], this);
			mapNameContextPri.put(tmp[0], c);
			Long token = rand.nextLong();
			mapNameToken.put(tmp[0], token);
			c.queueMessage(new PrivateAuthFrame(token).asByteBuffer());

			if (2 == tmp.length) {
				sendFile(tmp[1], tmp[0], c);
			}
			consoleQueue.add(new Command(PrivateConnexCliToSrv, tmp[0]));
		}
	}

	/**
	 * méthode qui lance le client.
	 *  Si 
	 * @throws IOException Si il y a un problème avec le SocketChannel
	 */
	public void launch() throws IOException {
		if (!connexion()) {
			return;
		}
		sc.configureBlocking(false);
		uniqueKey = sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		var context = new Context(sc, uniqueKey, this);
		context.setVisitorHandler(new PublicClientVisitor(this, context));
		uniqueKey.attach(context);
		consoleThread.start();
		while (!Thread.interrupted()) {
			selector.select(this::treatKey);
			treatConsoleQueue();
		}
	}

	/**
	 * @param name nom du client avec qui il conversera via ce port et cette adresse
	 * @param adr  adresse sur lequel ce client devra se connecter pour converser de
	 *             manière privée.
	 * @param port port sur lequel ce client devra se connecter pour converser de
	 *             manière privée.
	 */
	public void addPrivateConnexion(String name, InetAddress adr, int port) {
		var token = mapNameToken.remove(name);
		if (null != token) {
			try {
				var serverPrivateAddress = new InetSocketAddress(adr, port);
				SocketChannel scPrivate = SocketChannel.open();
				scPrivate.configureBlocking(false);
				scPrivate.connect(serverPrivateAddress);
				var keyPrivate = scPrivate.register(selector, SelectionKey.OP_CONNECT);
				var contextPri = mapNameContextPri.get(name);
				if (contextPri == null) {
					throw new AssertionError();
				}
				contextPri.ClientIsReady(scPrivate, keyPrivate, name);
				contextPri.setVisitorHandler(new PrivateClientConnectedVisitor(this, contextPri));
				keyPrivate.attach(contextPri);
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
		}
	}

	/**
	 * Traite la queue de la console et envoie le bon message dans le bon context
	 */
	private void treatConsoleQueue() {
		if (consoleQueue.isEmpty()) {
			return;
		}
		var command = consoleQueue.poll();
		switch (command.opcode) {
		case PublicClientToServ: {
			var frame = new MessageFrame(command.text);
			((Context) uniqueKey.attachment()).queueMessage(frame.asByteBuffer());
			return;
		}

		case PrivateConnexCliToSrv: {
			System.out.println("Une demande d'échanges privés a été faite à : " + command.text + ".");
			var frame = new PrivateConnexCliToSrvFrame(command.text, mapNameToken.get(command.text));
			((Context) uniqueKey.attachment()).queueMessage(frame.asByteBuffer());
			return;
		}

		case PrivateYesRepConnexCliToSrv: {
			var frame = new PrivateYesRepConnexCliToSrvFrame(command.text, port);
			((Context) uniqueKey.attachment()).queueMessage(frame.asByteBuffer());
			return;
		}

		case PrivateNoRepConnexCliToSrv: {
			var frame = new PrivateNoRepConnexCliToSrvFrame(command.text);
			((Context) uniqueKey.attachment()).queueMessage(frame.asByteBuffer());
			return;
		}

		case PrivateMsg: {
			var line = command.text;
			var tab = line.split(" ", 2);
			var dest = tab[0];
			var contextPrivate = mapNameContextPri.get(dest);
			if (null != contextPrivate) {
				contextPrivate.queueMessage(new MessageFrame(tab[1]).asByteBuffer());
			} else {
				throw new AssertionError();
			}
			return;
		}

		case File: {
			var line = command.text;
			var tab = line.split(" ", 2);
			var contextPrivate = mapNameContextPri.get(tab[0]);
			sendFile(tab[1], tab[0], contextPrivate);
			return;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + command.opcode);
		}
	}

	/**
	 * Envoi un fichier au destinaire dest
	 * 
	 * @param file    String qui correspond au fichier qui sera envoyer
	 * @param dest    destinataire qui recevra le fichier
	 * @param context contexte dans lequel sera envoyer le fichier
	 */
	private void sendFile(String file, String dest, Context context) {
		var path = Path.of(pathFile + file);
		try (var fc = FileChannel.open(path, StandardOpenOption.READ);) {

			if (null != context) {
				long size = (fc.size() / 512) + 1;
				if (size >= Integer.MAX_VALUE) {
					System.out.println("Votre fichier est trop volumineux.");
					return;
				}

				ByteBuffer[] bbs = new ByteBuffer[(int) size];
				for (int i = 0; i < size; i++) {
					bbs[i] = ByteBuffer.allocate(512);
				}
				for(var i = 0; i < size; i++) {
					fc.read(bbs[i]);
				}

				for (var bb : new FileFrame(file, bbs, fc.size()).asByteBuffers()) {
					context.queueMessage(bb);
				}
				System.out.println("Envoi du fichier '" + file + "' en cours.");
			} else {
				throw new AssertionError();
			}
		} catch (Exception e) {
			System.out.println("Il y a eu un problème avec l'envoi de votre fichier '" + file + "'. Vérifiez qu'il "
					+ "se trouve bien dans pathFile que vous avez donné en argument.");
		}

		return;
	}

	/**
	 * essaie de se connecter en non-bloquant
	 * 
	 * @return vrai si la connexion à été établie, faux sinon.
	 * @throws IOException
	 */
	private boolean connexion() throws IOException {
		ByteBuffer bb;
		if (password == null) {
			var frame = new ConnexionSansMdpFrame(login);
			bb = frame.asByteBuffer();
		} else {
			var frame = new ConnexionMdpFrame(login, password);
			bb = frame.asByteBuffer();
		}
		bb.flip();
		sc.write(bb);
		bb = ByteBuffer.allocate(Byte.BYTES * 2);
		if (readFully(sc, bb)) {
			bb.flip();
			if (bb.get() == ConnexionRepServ.getId() && bb.get() == 0) {
				System.out.println("Vous etes bien connecté");
				return true;
			}
		}
		System.out.println("Vous n'êtes pas connecté");
		return false;
	}

	/**
	 * Rempli le Bytebuffer bb
	 * 
	 * @param sc SoketChannel qui sert à récupérer les informations
	 * @param bb Bytebuffer qui sert à stocker les byte
	 * @return true si le ByteBuffer est rempli, et false sinon.
	 * @throws IOException
	 */
	static boolean readFully(SocketChannel sc, ByteBuffer bb) throws IOException {
		while (-1 != sc.read(bb)) {
			if (!bb.hasRemaining()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Vérifie que la key à quelque chose à faire et appelle la méthode
	 * correspondante
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
			System.out.println("CONNECTION CLOSED wIth client");
			silentlyClose();
		}
	}

	/**
	 * Accepte une key et la register auprès du selector
	 * 
	 * @param key SelectionKey qu'on va register
	 * @throws IOException
	 */
	private void doAccept(SelectionKey key) throws IOException {
		SocketChannel scPrivate = serverSocketChannel.accept();
		if (scPrivate == null) {
			return;
		}
		scPrivate.configureBlocking(false);
		var newKeyPrivate = scPrivate.register(selector, SelectionKey.OP_READ);
		var contextP = new Context(scPrivate, newKeyPrivate, this);
		contextP.setVisitorHandler(new PrivateClientAuthVisitor(this, contextP));
		newKeyPrivate.attach(contextP);
	}

	/**
	 * On ferme tout car il y a un problème
	 */
	private void silentlyClose() {
		try {
			sc.close();
			Thread.currentThread().interrupt();
		} catch (IOException e) {
			// ignore exception
		}
	}

	/**
	 * Ferme le client ou juste une SelectionKey en fonction de ce qui a été coupé
	 */
	@Override
	public void silentlyClose(SelectionKey key) {
		if (key == uniqueKey) {
			System.out.println("Le serveur a été interrompu. Revenez plus tard.");
			Thread.currentThread().interrupt();

		} else {
			var log = ((Context) key.attachment()).login;
			System.out.println(log + " vient de partir. Vous ne pouvez plus converser avec lui.");
			mapNameContextPri.remove(log);
			mapNameToken.remove(log);
			((Context) key.attachment()).deconnected = true;
		}

	}

	private static void usage() {
		System.out.println("Usage : hostname port pathFile login (password)");
	}
}
