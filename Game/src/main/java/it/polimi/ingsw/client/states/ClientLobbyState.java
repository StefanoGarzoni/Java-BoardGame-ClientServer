package it.polimi.ingsw.client.states;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.client.ClientController;

import java.util.HashMap;
import java.util.function.Consumer;

public class ClientLobbyState extends ClientState{

    public ClientLobbyState(ClientController clientController){
        super(clientController);

        methodsFromServerMap.put("BuildingArrangementState", this::nextState);
    }

    public void run(){
        clientController.getView().showMessage("You are in lobby! Wait for other players to join!");
    }

    public void processFromServer(ActionMessage actionMessage){
        synchronized (clientController) {
            try {
                System.out.println(actionMessage.getActionName().equals("BuildingArrangementState"));
                System.out.println(methodsFromServerMap.get(actionMessage.getActionName()));

                methodsFromServerMap.get(actionMessage.getActionName()).accept(actionMessage);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                System.out.println(actionMessage.toString());
                // e.printStackTrace(System.out);
            }
        }
    }
    public void processFromView(ActionMessage actionMessage){
    }

    /** Sets the next state when the server orders it
     *
     * @param actionMessage sent by the server
     */
    public void nextState(ActionMessage actionMessage){
        clientController.setState(new ClientBuildingArrangementState(this.clientController));
        clientController.getCurrentState().run();
    }

}
