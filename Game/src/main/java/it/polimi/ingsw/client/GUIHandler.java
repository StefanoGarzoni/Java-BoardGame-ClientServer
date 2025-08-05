package it.polimi.ingsw.client;

public class GUIHandler implements ViewHandler {

    ServerProxy serverProxy;
    ClientController controller;
    public GUIHandler(ClientController controller){
        this.serverProxy = serverProxy;
        this.controller = controller;
    }

    @Override
    public void run() {
    }
}
