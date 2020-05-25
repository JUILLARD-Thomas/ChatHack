package fr.upem.projet.frame;


public class IpFrame {

	private final int size;
	private final byte[] adrIp;
	private final int port;
	
	
	
	public IpFrame(int size, byte[] adrIp, int port) {
		this.size = size;
		this.adrIp = adrIp;
		this.port = port;
	}



    /**
     * @return renvoi un int correspondant à la taille
     *
     */
	public int getSize() {
		return size;
	}
	
    /**
     * @return renvoi un tableau de byte correspondant à l'adresse IP
     *
     */
	public byte[] getAdrIp() {
		return adrIp;
	
	}
    /**
     * @return renvoi un int correspondant au port
     *
     */
	public int getPort() {
		return port;
	}
}
