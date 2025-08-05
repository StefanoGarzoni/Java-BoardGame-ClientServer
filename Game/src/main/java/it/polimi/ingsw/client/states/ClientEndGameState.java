package it.polimi.ingsw.client.states;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.client.ClientController;

public class ClientEndGameState extends ClientState{

    public ClientEndGameState(ClientController clientController) {
        super(clientController);
    }

    @Override
    public void run() {
        this.clientController.setState(new ClientSetupState(this.clientController));
    }

    @Override
    public void processFromServer(ActionMessage actionMessage) {

    }

    @Override
    public void processFromView(ActionMessage actionMessage) {

    }
}
