package it.polimi.ingsw.controller.clientProxies;

import it.polimi.ingsw.controller.Controller;

public abstract class ClientProxy implements Observer{
    private final String playerNickname;
    private Controller controller;

    ClientProxy(String playerNickname){
        this.playerNickname = playerNickname;
        this.controller = null;
    }

    public void setController(Controller controller) {
        if(this.controller == null)
            this.controller = controller;
    }

    public String getPlayerNickname() { return playerNickname; }

    Controller getController() { return controller; }

    /** Closes connection with the client
     */
    public abstract void closeConnectionToClient();
}
