package fr.upem.projet.frame;

public enum IdFrame {
    ConnexionMdp((byte) 13), ConnexionRepServ((byte) 14), ConnexionSansMdp((byte) 2), PublicClientToServ((byte) 3),
    PublicServToClient((byte) 4), PrivateConnexCliToSrv((byte) 5), PrivateConnexSrvToCli((byte) 6),
    PrivateYesRepConnexCliToSrv((byte) 7), PrivateNoRepConnexCliToSrv((byte) 8), PrivateYesRepConnexSrvToCli((byte) 9),
    PrivateNoRepConnexSrvToCli((byte) 10), PrivateAuth((byte) 11), PrivateMsg((byte) 3), File((byte) 12),
    ConnexionBddNo((byte) 0), ConnexionBddYes((byte) 1), PrivateError((byte) 15);

    private byte id;

    private IdFrame(byte id) {
        this.id = id;
    }
    /**
     * @return renvoie un byte correspondant à l'id
     *
     */
    public byte getId() {
        return id;
    };


}