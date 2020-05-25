package fr.upem.projet.frame;

import static fr.upem.projet.frame.IdFrame.File;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import fr.upem.projet.visitor.AbstractVisitor;

public class FileFrame implements Frame {
	private final ByteBuffer[] bbs;
	private final String name;
	private final Charset charset = StandardCharsets.UTF_8;
	private final Long sizeFile;



	public FileFrame(String name, ByteBuffer[] bbs, long sizeFile) {
		this.bbs = bbs;
		this.name = name;
		this.sizeFile = sizeFile;
	}
	
	@Override
	public void accept(AbstractVisitor visitor) {
		visitor.visit(this);		
	}
    /**
     * @return renvoie un String correspondant au Nom
     *
     */
	public String getName() {
		return name;
	}
    /**
     * @return renvoie un bytebuffer 
     *
     */
	public ByteBuffer[] getByteBuffers() {
		return bbs;
	}
    /**
     *@return renvoie une liste de  Bytebuffer contenant la trame correspondante � l'objet FileFrame
     *
     */
	public List<ByteBuffer> asByteBuffers() {
		var listBbs = new ArrayList<ByteBuffer>();
		var bbName = charset.encode(name);
		var bb = ByteBuffer.allocate(Byte.BYTES + Long.BYTES + Integer.BYTES + bbName.remaining());
		bb.put(File.getId()).putInt(bbName.remaining()).put(bbName).putLong(sizeFile);
		listBbs.add(bb);
		for(var bb2 : bbs) {
			bb2.flip();
			var bbTmp = ByteBuffer.allocate(bb2.remaining());
			bbTmp.put(bb2);
			listBbs.add(bbTmp);
		}
		return listBbs;
	}	
}
