package it.polimi.ingsw.client.states;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.client.ClientController;
import it.polimi.ingsw.model.clientModel.ViewShipBoard;

import java.util.Arrays;

public class ClientDetermineScoreState extends ClientState{

    public ClientDetermineScoreState(ClientController clientController){
        super(clientController);

    }

    @Override
    public void run() {
        methodsFromServerMap.put("finalScores", this::showFinalScores);
        methodsFromServerMap.put("gameIsFinished", this::nextState);
    }

    /** This method shows all players final scores
     *
     * @param actionMessage containing all final shipboards
     */
    public void showFinalScores(ActionMessage actionMessage){
        ViewShipBoard[] viewShipBoards = this.clientController.getViewFlightBoard().getViewShipBoards();
        ViewShipBoard[] sortedViewShipBoards = Arrays.stream(viewShipBoards).sorted((p1,p2)->{
            if(p1.getPoints()> p2.getPoints()) return 1;
            else if(p1.getPoints()<p2.getPoints()) return -1;
            else return 1;
        }).toArray(ViewShipBoard[]::new);
        this.clientController.getView().showScoreBoard(sortedViewShipBoards);
    }

    private void nextState(ActionMessage actionMessage){
        clientController.getView().showMessage("Game has ended!");
        putUserInSetupState();
    }
    @Override
    public void processFromServer(ActionMessage actionMessage) {
        try{
            synchronized (clientController) {
                this.methodsFromServerMap.get(actionMessage.getActionName()).accept(actionMessage);
            }
        }catch(Exception e){
            clientController.getView().showMessage("server message ignored");
        }
    }

    @Override
    public void processFromView(ActionMessage actionMessage) {
        try{
            synchronized (clientController) {
                this.methodsFromViewMap.get(actionMessage.getActionName()).accept(actionMessage);
            }

        }catch(Exception e){
            clientController.getView().showMessage("invalid command in this state");
        }
    }
}
