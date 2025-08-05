package it.polimi.ingsw.model.cards.client.concrete;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.client.ClientController;
import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.client.ClientCard;
import it.polimi.ingsw.model.cards.generic.OpenSpaceCard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

public class OpenSpaceClientCard extends ClientCard {
    private final OpenSpaceCard cardData;

    private int availableBatteries;
    private int currentAskedBatteryForDoubleEngine;
    private HashMap<Coordinates, Integer> batteryTilesAvailability;

    private ArrayList<Coordinates> availableDoubleEngine;
    private final ArrayList<Coordinates> doubleEngineToActivate = new ArrayList<>();
    private final HashMap<Coordinates, Integer> batteriesCoordinates = new HashMap<>();
    private int currentAskedDoubleEngine;

    private BiConsumer<ActionMessage, ClientController> nextMethodToCall;

    /* constructors */

    public OpenSpaceClientCard(OpenSpaceCard openSpaceCard) { this.cardData = openSpaceCard; }

    public OpenSpaceClientCard(int cardLevel, String fileName) {
        this.cardData = new OpenSpaceCard(cardLevel, fileName);
    }

    /* service methods */

    @Override
    public String getFileName() { return cardData.getFileName(); }

    @Override
    public void run(ClientController clientController) {
        clientController.getView().showMessage("OpenSpace card has been drawn!");
        clientController.getView().showCard(this.cardData);

        methodsFromServerMap.put("askEngineUsage", this::askForDoubleEngineToActivate);
        methodsFromServerMap.put("solveOpenSpace", this::updateCurrentPositions);
        methodsFromServerMap.put("notEnoughResources", this::notEnoughResources);

        methodsFromViewMap.put("answer", this::userAnswerSorter);
    }

    @Override
    public Card getCardData() {
        return cardData;
    }

    private void userAnswerSorter(ActionMessage actionMessage, ClientController clientController){
        if(nextMethodToCall != null){
            nextMethodToCall.accept(actionMessage, clientController);
        }
        else{
            // TODO: error message
        }
    }

    @Override
    public void processFromServer(ActionMessage actionMessage, ClientController clientController){
        try {
            this.methodsFromServerMap.get(actionMessage.getActionName()).accept(actionMessage, clientController);
        } catch (Exception e) {
            return;
        }
    }
    @Override
    public void processFromClient(ActionMessage actionMessage, ClientController clientController){
        try {
            this.methodsFromViewMap.get(actionMessage.getActionName()).accept(actionMessage, clientController);
        } catch (Exception e) {
            return;
        }
    }

    /* engine usage asking */

    /** Shows a messages to the client, asking if he wants to activate double cannon, until he has
     * no more batteries or no more double cannon to activate.
     *
     * @param clientController controller that contains a view reference
     */
    private void askUserForEngineUsage(ClientController clientController){
        if(availableBatteries > 0 && currentAskedDoubleEngine < availableDoubleEngine.size()){
            clientController.getView().showMessage(
                    "You can activate "+ availableDoubleEngine.size()+" double engine and you have "+availableBatteries+" batteries."+
                            "Do you want [y/n] to activate cannon at coordinates: "+ availableDoubleEngine.get(currentAskedDoubleEngine)+ "?"
            );
        }
        else {
            clientController.getView().showMessage("You have ended choosing double engines to activate");

            currentAskedBatteryForDoubleEngine = 0;
            nextMethodToCall = this::answerForBatteriesUsage;
            askUserForBatteriesUsage(clientController);
        }
    }


    private void answerForBatteriesUsage(ActionMessage actionMessage, ClientController clientController){
        ArrayList<String> playerAnswer = (ArrayList<String>) actionMessage.getData("answer");
        Coordinates selectedCoordinates;

        // controls on user input
        try{
            int xCoordinate = Integer.parseInt(playerAnswer.getFirst());
            int yCoordinate = Integer.parseInt(playerAnswer.get(1));
            selectedCoordinates = new Coordinates(xCoordinate, yCoordinate);

            if( ! (batteryTilesAvailability.containsKey(selectedCoordinates) && batteryTilesAvailability.get(selectedCoordinates) > 0) ){
                throw new Exception();
            }
        }
        catch (Exception e){
            askUserForBatteriesUsage(clientController);
            return;
        }

        // update the map containing from which tile user wants to get batteries
        if(batteriesCoordinates.containsKey(selectedCoordinates)){
            int currentBatteriesUsed = batteriesCoordinates.get(selectedCoordinates);
            batteriesCoordinates.put(selectedCoordinates, currentBatteriesUsed+1);
        }
        else{
            batteriesCoordinates.put(selectedCoordinates, 1);
        }

        // update the available batteries at that coordinates
        int availableBatteriesAtSelectedCoordinates = batteryTilesAvailability.get(selectedCoordinates);
        batteryTilesAvailability.put(selectedCoordinates, availableBatteriesAtSelectedCoordinates-1);

        currentAskedBatteryForDoubleEngine++;
        askUserForBatteriesUsage(clientController);
    }

    private void askUserForBatteriesUsage(ClientController clientController){
        if(currentAskedBatteryForDoubleEngine < doubleEngineToActivate.size()){
            AtomicReference<String> messageToShow = new AtomicReference<>("Select coordinates [<x> <y>] from which extract the " + currentAskedBatteryForDoubleEngine+1 + " of " + doubleEngineToActivate.size() + " engine's battery." +
                    "Available battery at coordinates: ");
            batteryTilesAvailability.forEach((coordinates, availableBatteries) -> {
                if(availableBatteries > 0){
                    messageToShow.updateAndGet(v -> v + coordinates + " / ");
                }
            });

            clientController.getView().showMessage(messageToShow.get());
        }
        else {
            clientController.getView().showMessage("You have ended choosing sources for the batteries you want to use");

            nextMethodToCall = null;
            sendEnginePowerToServer(clientController);
        }
    }

    /* double engine activation */

    private void answerForDoubleEngineToActivate(ActionMessage actionMessage, ClientController clientController){
        String playerAnswer = ((ArrayList<String>) actionMessage.getData("answer")).getFirst();
        if(playerAnswer.equals("y")){
            Coordinates selectedCoordinates = availableDoubleEngine.get(currentAskedDoubleEngine);
            doubleEngineToActivate.add(selectedCoordinates);
            availableBatteries--;
        }
        currentAskedDoubleEngine++;
        askUserForEngineUsage(clientController);
    }

    /** This method is called when server sends an ActionMessage "askEngineUsage"
     *
     * @param actionMessage sent by the server
     * @param clientController controller of the client
     */
    private void askForDoubleEngineToActivate(ActionMessage actionMessage, ClientController clientController){
        if(actionMessage.getReceiver().equals(clientController.getNickname())){
            Map<Coordinates, Integer> doubleEngines = (Map<Coordinates, Integer>) actionMessage.getData("doubleEngines");
            availableDoubleEngine = new ArrayList<>(doubleEngines.keySet());

            batteryTilesAvailability = (HashMap<Coordinates, Integer>)  actionMessage.getData("batteries");

            availableBatteries = 0;
            for(Integer storedBattery : batteryTilesAvailability.values())
                availableBatteries += storedBattery;

            nextMethodToCall = this::answerForDoubleEngineToActivate;
            currentAskedDoubleEngine = 0;
            askUserForEngineUsage(clientController);
        }
    }

    private void sendEnginePowerToServer(ClientController clientController){
        ActionMessage messageToServer = new ActionMessage("setEnginePower", clientController.getNickname());
        messageToServer.setData("doubleEngineCoordinates", doubleEngineToActivate);
        messageToServer.setData("batteriesCoordinate",batteriesCoordinates);

        clientController.getServerProxy().send(messageToServer);
    }

    /* client model updates */

    private void updateCurrentPositions(ActionMessage actionMessage, ClientController clientController){
        HashMap<String, Integer> updatedPositions = (HashMap<String, Integer>) actionMessage.getData("updatedPositions");

        updatedPositions.forEach((nickname, absPosition) -> {
            clientController.getViewFlightBoard().getViewShipBoard(nickname).setAbsPosition(absPosition);
        });

        int gainedPositionByUser = updatedPositions.get(clientController.getNickname()) -
                clientController.getViewFlightBoard().getViewShipBoard(clientController.getNickname()).getAbsPosition();
        clientController.getView().showMessage(
                "You have gained "+ gainedPositionByUser +" position by Open Space Card"
        );
        clientController.getView().showFlightBoard();
    }

    private void notEnoughResources(ActionMessage actionMessage, ClientController clientController){
        if(actionMessage.getReceiver().equals(clientController.getNickname())) {
            clientController.getView().showMessage("You don't have resources to pick");
        }
        else{
            clientController.getView().showMessage(actionMessage.getReceiver() + " does not have enough resources to pick");
        }
    }
}