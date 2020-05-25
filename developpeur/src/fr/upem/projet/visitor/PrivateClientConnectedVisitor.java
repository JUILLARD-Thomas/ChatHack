package fr.upem.projet.visitor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import fr.upem.projet.ClientChatHack;
import fr.upem.projet.Context;
import fr.upem.projet.frame.FileFrame;
import fr.upem.projet.frame.MessageFrame;

/**
 * @author Calonne, Juillard
 * 
 *         Utiliser cette classe lorsque le context d'un client pour converser
 *         avec un client et que ce client est bien connecté.
 *
 */
public class PrivateClientConnectedVisitor extends AbstractVisitor{
	
	private final ClientChatHack clientCH; 
	private final Context context; 
	
	
	
	public PrivateClientConnectedVisitor (ClientChatHack clientCH, Context context) {
		super(context);
		this.clientCH = clientCH;
		this.context = context ; 
	}
		
	@Override
	public void visit(MessageFrame frame) {
		if (context.login != null) {
			System.out.println("[" + context.login + ": " + frame.getText() + "]");
		}
	}
	
	@Override
	public void visit(FileFrame frame) {

		var fileName = frame.getName();
		System.out.println("[" + context.login + " vous a envoyé le fichier : " + fileName + "]");
		ByteBuffer[] bbs = frame.getByteBuffers();

		for (var bb : bbs) {
			bb.flip();
		}
		var path = Path.of(clientCH.pathFile + fileName);
		try (var fc = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE);) {
			fc.write(bbs);
		} catch (IOException e) {
			throw new AssertionError();
		}
	}
	
	
	
}
