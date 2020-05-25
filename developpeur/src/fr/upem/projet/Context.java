package fr.upem.projet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;

import fr.upem.projet.frame.ConnexionRepServFrame;
import fr.upem.projet.frame.Frame;
import fr.upem.projet.reader.FrameReader;
import fr.upem.projet.reader.Reader;
import fr.upem.projet.visitor.AbstractVisitor;
import fr.upem.projet.visitor.PublicServerAuthVisitor;
import fr.upem.projet.visitor.PublicServerConnectedVisitor;

/**
 * @author Calonne, Juillard
 * 
 *         Cette classe représente un context qui utilise des méthodes pour
 *         recevoir, envoyer des ByteBuffers.
 *
 */
public class Context {
	public SocketChannel sc;
	private final static int BUFFER_SIZE = 1_024;
	private final ByteBuffer bbin = ByteBuffer.allocateDirect(BUFFER_SIZE);
	private final ByteBuffer bbout = ByteBuffer.allocateDirect(BUFFER_SIZE);
	private final Queue<ByteBuffer> queue = new LinkedList<>();
	protected boolean closed;
	private final FrameReader messageReader;
	public SelectionKey uniqueKey;
	protected AbstractVisitor visitorHandler;
	private final SilentlyCloseable sl;
	public String login;
	public String password;
	boolean isReady;
	private final Selector selector;
	public boolean deconnected;
	private final Object lock = new Object();
	public byte[] address;

	private Context(SocketChannel sc, SelectionKey uniqueKey, SilentlyCloseable sl, Selector selector, boolean isReady,
			String login) {
		this.sc = sc;
		messageReader = new FrameReader(bbin);
		this.uniqueKey = uniqueKey;
		this.sl = sl;
		this.selector = selector;
		this.isReady = isReady;
		this.login = login;
		if (sc != null) {
			try {
				InetSocketAddress adr = (InetSocketAddress) sc.getRemoteAddress();
				address = adr.getAddress().getAddress();
			} catch (IOException e) {
				closed = true;
			}
		}

	}

	public Context(SocketChannel scPrivate, SelectionKey keyPrivate, SilentlyCloseable sl) {
		this(scPrivate, keyPrivate, sl, null, true, null);
	}

	public Context(String login, SilentlyCloseable sl) {
		this(null, null, sl, null, false, login);
	}

	public Context(SilentlyCloseable server, SelectionKey key, Selector selector) {
		this((SocketChannel) key.channel(), key, server, selector, true, null);

	}

	public void setVisitorHandler(AbstractVisitor visitorHandler) {
		this.visitorHandler = visitorHandler;
	}

	/**
	 * Performs the read action on sc
	 *
	 * The convention is that both buffers are in write-mode before the call to
	 * doRead and after the call
	 *
	 * @throws IOException Renvoi un exeption si la connexion est fermé pendant la
	 *                     lecture
	 */
	public void doRead() throws IOException {
		if (sc.read(bbin) == -1) {
			closed = true;
		}
		processIn();
		updateInterestOps();
	}

	/**
	 * Performs the write action on sc
	 *
	 * The convention is that both buffers are in write-mode before the call to
	 * doWrite and after the call.
	 *
	 * @throws IOException Renvoi un exeption si la connexion est fermé pendant la
	 *                     l'écriture
	 */
	public void doWrite() throws IOException {
		bbout.flip();
		sc.write(bbout);
		bbout.compact();
		processOut();
		updateInterestOps();
	}

	/**
	 * Vérifie que la SocketChannel est bien connectée.
	 * 
	 * @throws IOException Renvoi un exeption si la connexion est fermé pendant la
	 *                     connexion
	 */
	public void doConnect() throws IOException {
		if (!sc.finishConnect()) {
			return;
		}
		updateInterestOps();
	}

	/**
	 * Change la valeur de l'interestOps de la SelectionKey de l'objet Context
	 */
	public void updateInterestOps() {
		if (deconnected) {
			uniqueKey.cancel();
			uniqueKey = null;
			deconnected = false;
			return;
		}
		int newIOps = 0;
		if (bbin.hasRemaining() && !closed) {
			newIOps |= SelectionKey.OP_READ;
		}
		if (bbout.position() > 0 && !closed) {
			newIOps |= SelectionKey.OP_WRITE;
		}
		if (0 == newIOps) {
			silentlyClose();
		} else {
			uniqueKey.interestOps(newIOps);
		}
	}

	/*
	 * Process the content of bbin
	 *
	 * The convention is that bbin is in write-mode before the call to process and
	 * after the call
	 *
	 */
	public void processIn() {
		for (;;) {
			Reader.ProcessStatus status = messageReader.process();
			switch (status) {
			case DONE:
				Frame frame = (Frame) messageReader.get();
				frame.accept(visitorHandler);
				messageReader.reset();
				break;
			case REFILL:
				return;
			case ERROR:
				closed = true;
				silentlyClose();
				return;
			}
		}
	}

	/**
	 * Try to fill bbout from the message queue Warning il est flip dans le while
	 */
	public void processOut() {
		if (!isReady) {
			return;
		}
		synchronized (lock) {
			while (!queue.isEmpty() && bbout.remaining() > queue.peek().position()) {
				bbout.put(queue.poll().flip());
				
			}
		}
		
	}

	/**
	 * Appelle la méthode silentlyClose du serveur ou du client auquel ce Context
	 * est rattaché
	 */
	public void silentlyClose() {
		try {
			sc.close();
			closed = true;
			sl.silentlyClose(uniqueKey);
		} catch (IOException e) {
			// ignore exception
		}
	}

	/**
	 * 
	 * /** contextBdd Add a message to the message queue, tries to fill bbOut and
	 * updateInterestOps value
	 * 
	 * @param bb bytebuffer qu'on ajoute à la queue
	 */
	public void queueMessage(ByteBuffer bb) {
		synchronized (lock) {
			queue.add(bb);
			if (isReady) {
				processOut();
				updateInterestOps();
			}
		}
	}

	/**
	 * @param isConnected true si le client avec qui ce context communique peut se
	 *                    connecter
	 * @param server      Server qui a décidé si le client pouvait se connecter ou
	 *                    non.
	 */
	public void bddAnswer(boolean isConnected, ServerChatHack server) {
		try {
			uniqueKey = sc.register(selector, SelectionKey.OP_WRITE);
			uniqueKey.attach(this);
			updateInterestOps();
			queueMessage(new ConnexionRepServFrame(isConnected).asByteBuffer());
			if (isConnected) {
				visitorHandler = new PublicServerConnectedVisitor(server, this);
			} else {
				visitorHandler = new PublicServerAuthVisitor(server, this);
			}
		} catch (ClosedChannelException e) {
			System.out.println("problème pendant l'attachement");
			closed = true;
		}
	}

	/**
	 * @param scPrivate  SocketChannel qui servira a cummuniquer avec le client.
	 * @param keyPrivate SelectionKey qui est enregistré sur le Selector
	 * @param login      du client
	 */
	public void ClientIsReady(SocketChannel scPrivate, SelectionKey keyPrivate, String login) {
		this.sc = scPrivate;
		this.uniqueKey = keyPrivate;
		this.login = login;
		this.isReady = true;
		try {
			InetSocketAddress adr = (InetSocketAddress) sc.getRemoteAddress();
			address = adr.getAddress().getAddress();
		} catch (IOException e) {
			silentlyClose();
		}
		processOut();
	}
}