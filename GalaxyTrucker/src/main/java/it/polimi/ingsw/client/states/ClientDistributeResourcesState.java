package it.polimi.ingsw.client.states;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.client.ClientController;
import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.ShipBoard;
import it.polimi.ingsw.model.clientModel.ViewShipBoard;


import java.util.ArrayList;


public class ClientDistributeResourcesState extends ClientState {
    private Boolean alreadyAnsweredBrownAlien;
    private Boolean alreadyAnsweredPurpleAlien;
    private ArrayList<Coordinates> brownAlienPossibleCoordinates;
    private ArrayList<Coordinates> purpleAlienPossibleCoordinates;

    private final ArrayList<ArrayList<Coordinates>> chosenCoordinatesList;

    public ClientDistributeResourcesState(ClientController clientController){
        super(clientController);
        this.alreadyAnsweredBrownAlien = false;
        this.alreadyAnsweredPurpleAlien = false;

        brownAlienPossibleCoordinates = null;
        purpleAlienPossibleCoordinates = null;

        chosenCoordinatesList = new ArrayList<>();
        chosenCoordinatesList.add(new ArrayList<>());
        chosenCoordinatesList.add(new ArrayList<>());
    }

    public void run(){
        methodsFromServerMap.put("playingState", this::nextState);
        methodsFromServerMap.put("possibleAliensPosition", this::showAlienPossiblePosition);
        methodsFromServerMap.put("resourcesDistributedConfirmation", this::placeAliens);

        methodsFromViewMap.put("placeBrownAlien", this::placeBrownAlienFromView);
        methodsFromViewMap.put("placePurpleAlien", this::placePurpleAlienFromView);
    }

    @Override
    public void processFromServer(ActionMessage actionMessage) {
        synchronized (clientController) {
            try {
                methodsFromServerMap.get(actionMessage.getActionName()).accept(actionMessage);
            } catch (NullPointerException e) {
                this.clientController.getView().showErrorMessage("Server invalid communication");
            }
        }
    }

    @Override
    public void processFromView(ActionMessage actionMessage) {
        synchronized (clientController){
            try {
                this.methodsFromViewMap.get(actionMessage.getActionName()).accept(actionMessage);
            }catch (NullPointerException e){
                this.clientController.getView().showErrorMessage("Message not correct");
            }
        }
    }

    /** Handles server-to-client request to place aliens.
     * This is the procedure:
     * 1. player will be asked for brown aliens first (if he can).
     * 2. Then he will be asked for purple alien (if he can)
     * 3. After player has chosen, the decisions will be sent to the server.
     * If the player can't or doesn't want to place aliens, the list corresponding to the alien's color will be empty
     *
     * @param actionMessage containing the lists with the possible placements for brown and purple aliens
     */
    private void showAlienPossiblePosition(ActionMessage actionMessage) {

        if(actionMessage.getReceiver().equals(clientController.getNickname())){
            ArrayList<ArrayList<Coordinates>> aliensPossibleCoordinatesList = (ArrayList<ArrayList<Coordinates>>) actionMessage.getData("positions");

            this.brownAlienPossibleCoordinates = aliensPossibleCoordinatesList.get(0);
            this.purpleAlienPossibleCoordinates = aliensPossibleCoordinatesList.get(1);

            if(!brownAlienPossibleCoordinates.isEmpty()){
                askForBrownAlien();
            }
            else if(!purpleAlienPossibleCoordinates.isEmpty()){
                askForPurpleAlien();
            }
            else{
                clientController.getView().showMessage("You can't place any aliens");
                sendChosenCoordinatesList();
            }
        }
    }

    private void nextState(ActionMessage actionMessage) {
        this.clientController.setState(new ClientPlayingState(this.clientController));
        this.clientController.getCurrentState().run();
    }

    private void sendToServerProxy(ActionMessage actionMessage) {
        this.clientController.getServerProxy().send(actionMessage);
    }

    /** Updates aliens client position in all players shipboards
     *
     * @param actionMessage containing a player shipboard
     */
    private void placeAliens(ActionMessage actionMessage){
        ViewShipBoard viewShipBoard = clientController.getViewFlightBoard().getViewShipBoard(actionMessage.getReceiver());

        ShipBoard shipBoard = (ShipBoard) actionMessage.getData("shipBoard");

        viewShipBoard.updateViewShipBoard(shipBoard);
        if(actionMessage.getReceiver().equals(clientController.getNickname()))
            this.clientController.getView().showShipboard(viewShipBoard);
    }

    /** Handles the answer of a player fot brown alien placing
     *
     * @param actionMessage containing the player's brown alien answer
     */
    private void placeBrownAlienFromView(ActionMessage actionMessage) {

        if(brownAlienPossibleCoordinates != null && !brownAlienPossibleCoordinates.isEmpty()){

            String confirmation = (String) actionMessage.getData("confirmation");

            if(confirmation.equals("n")){
                alreadyAnsweredBrownAlien = true;
            }
            else{
                try {
                    Coordinates chosenCoordinates = (Coordinates) actionMessage.getData("coordinates");
                    if(!alreadyAnsweredBrownAlien && brownAlienPossibleCoordinates.contains(chosenCoordinates)) {
                        chosenCoordinatesList.get(0).add(chosenCoordinates);

                        alreadyAnsweredBrownAlien = true;
                        brownAlienPossibleCoordinates.remove(chosenCoordinates);
                    }else {
                        this.clientController.getView().showErrorMessage("Invalid Coordinates");
                    }
                }catch (NullPointerException e){
                    this.clientController.getView().showErrorMessage("Too early to place alien");
                }
            }

            if(purpleAlienPossibleCoordinates != null && !purpleAlienPossibleCoordinates.isEmpty()){
                askForPurpleAlien();
            }
            else{
                sendChosenCoordinatesList();
            }

        }
        else {
            clientController.getView().showMessage("You can't place any brown alien");
        }

    }

    /** Handles the answer of a player fot purple alien placing
     *
     * @param actionMessage containing the player's brown alien answer
     */
    private void placePurpleAlienFromView(ActionMessage actionMessage) throws NullPointerException{

        if(purpleAlienPossibleCoordinates != null && !purpleAlienPossibleCoordinates.isEmpty()){

            String confirmation = (String) actionMessage.getData("confirmation");

            if(confirmation.equals("n")){
                alreadyAnsweredPurpleAlien = true;
            }
            else{
                try {
                    Coordinates chosenCoordinates = (Coordinates) actionMessage.getData("coordinates");
                    if(!alreadyAnsweredPurpleAlien && purpleAlienPossibleCoordinates.contains(chosenCoordinates)){
                        chosenCoordinatesList.get(1).add(chosenCoordinates);

                        alreadyAnsweredPurpleAlien = true;
                        purpleAlienPossibleCoordinates.remove(chosenCoordinates);
                    }else {
                        this.clientController.getView().showErrorMessage("Invalid Coordinates");
                    }
                }catch (NullPointerException e){
                    this.clientController.getView().showErrorMessage("Too early to place alien");
                }
            }

            sendChosenCoordinatesList();
        }
        else {
            clientController.getView().showMessage("You can't place any purple alien");
        }
    }

    private void askForPurpleAlien(){
        StringBuilder stringBuilder = new StringBuilder();
        for(Coordinates c: purpleAlienPossibleCoordinates)
            stringBuilder.append(c.toString() +" ");

        this.clientController.getView().showMessage("Select coordinates to place [<x>,<y>] the purple alien?" + stringBuilder);

        this.clientController.getView().showShipboard(clientController.getViewFlightBoard().getViewShipBoard(clientController.getNickname()));
    }

    private void askForBrownAlien(){
        StringBuilder stringBuilder = new StringBuilder();
        for(Coordinates c: brownAlienPossibleCoordinates)
            stringBuilder.append(c.toString() +" ");

        this.clientController.getView().showMessage("Select coordinates to place [<x>,<y>] the brown alien?" + stringBuilder);

        this.clientController.getView().showShipboard(clientController.getViewFlightBoard().getViewShipBoard(clientController.getNickname()));
    }

    /** Sends aliens placing chosen coordinates
     */
    private void sendChosenCoordinatesList(){
        ActionMessage actionMessage = new ActionMessage("placeAliens", clientController.getNickname());
        actionMessage.setData("coordinatesChoosen",this.chosenCoordinatesList);
        this.sendToServerProxy(actionMessage);
    }
}


