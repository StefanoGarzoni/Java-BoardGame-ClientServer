package it.polimi.ingsw.model.cards.client.concrete;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.client.ClientController;
import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.ShipBoard;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.client.ClientCard;
import it.polimi.ingsw.model.cards.generic.AbandonedShipCard;
import it.polimi.ingsw.model.clientModel.ViewShipBoard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class AbandonedShipClientCard extends ClientCard {
    private final AbandonedShipCard cardData;

    private Map<Coordinates, Integer> availableAstronautsToRemove;
    private final Map<Coordinates, Integer> selectedAstronautsToRemove = new HashMap<>();
    private int astronautsToRemoveNumber;
    private int currentAstronautsToRemove;

    private BiConsumer<ActionMessage, ClientController> nextMethodToCall;

    /* constructors */

    public AbandonedShipClientCard(AbandonedShipCard abandonedShipCard) {
        cardData = abandonedShipCard;
    }

    public AbandonedShipClientCard(int creditReward, int flightDayLoss, int crewLoss, int cardLevel, String fileName) {
        cardData = new AbandonedShipCard(creditReward, flightDayLoss, crewLoss, cardLevel, fileName);
    }

    /* service methods */

    @Override
    public String getFileName() { return cardData.getFileName();}

    @Override
    public void run(ClientController clientController) {
        clientController.getView().showMessage("Abandoned Ship card has been drawn!");
        clientController.getView().showCard(this.cardData);

        methodsFromViewMap.put("answer", this::userAnswerSorter);

        methodsFromServerMap.put("abandonedShipLanding", this::askPlayerToLandOnShip);
        methodsFromServerMap.put("solveAbandonedShip", this::solveAbandonedShip);
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

    /* ask player to land */

    private void askPlayerToLandOnShip(ActionMessage actionMessage, ClientController clientController){
        if(actionMessage.getReceiver().equals(clientController.getNickname())){
            availableAstronautsToRemove = (Map<Coordinates, Integer>) actionMessage.getData("cabins");

            String messageForUser = "Do you want to land on this Abandoned Shipboard? " +
                    "If so, you will lose "+cardData.getCrewLoss()+" astronauts and "+cardData.getFlightDayLoss()+" flight days";

            clientController.getView().showMessage(messageForUser);

            nextMethodToCall = this::answerForLanding;
        }
    }

    private void answerForLanding(ActionMessage actionMessage, ClientController clientController){
        String playerAnswer = ((ArrayList<String>) actionMessage.getData("answer")).getFirst();

        if(playerAnswer.equals("y")){
            currentAstronautsToRemove = 0;
            astronautsToRemoveNumber = cardData.getCrewLoss();

            nextMethodToCall = this::answerForRemovingAstronaut;
            askPlayerToRemoveCurrentAstronaut(clientController);
        }
        else{
            sendLandingDecision(clientController, false);
        }
    }

    /* astronauts to remove */

    private void askPlayerToRemoveCurrentAstronaut(ClientController clientController){
        final StringBuilder messageToSend = new StringBuilder("Select coordinates [<x> <y>] where to remove this n." +
                currentAstronautsToRemove + "/" + astronautsToRemoveNumber + " astronaut:");

        availableAstronautsToRemove.forEach((coordinates, astronauts) -> {
            messageToSend.append(coordinates.toString()+ "["+astronauts+" slots] /");
        });

        clientController.getView().showMessage(messageToSend.toString());
    }

    private void answerForRemovingAstronaut(ActionMessage actionMessage, ClientController clientController){
        ArrayList<String> playerAnswer = (ArrayList<String>) actionMessage.getData("answer");
        Coordinates selectedCoordinates;

        // controls on user input
        try{
            int xCoordinate = Integer.parseInt(playerAnswer.getFirst());
            int yCoordinate = Integer.parseInt(playerAnswer.get(1));
            selectedCoordinates = new Coordinates(xCoordinate, yCoordinate);

            if(
                    !availableAstronautsToRemove.containsKey(selectedCoordinates)
            ){
                throw new Exception();
            }
        }
        catch (Exception e){
            askPlayerToRemoveCurrentAstronaut(clientController);
            return;
        }

        // sets coordinates to remove
        if(!selectedAstronautsToRemove.containsKey(selectedCoordinates))
            selectedAstronautsToRemove.put(selectedCoordinates, 1);
        else{
            int alreadyRemovedAstronautsFromSelectedCoordinates = selectedAstronautsToRemove.get(selectedCoordinates);
            selectedAstronautsToRemove.put(selectedCoordinates, alreadyRemovedAstronautsFromSelectedCoordinates+1);
        }

        // Removes the selected coordinates from the available for the next choices
        if(availableAstronautsToRemove.get(selectedCoordinates) == 1)
            availableAstronautsToRemove.remove(selectedCoordinates);
        else{
            int oldAvailableAstronautsAtSelectedCoordinates = selectedAstronautsToRemove.get(selectedCoordinates);
            selectedAstronautsToRemove.put(selectedCoordinates, oldAvailableAstronautsAtSelectedCoordinates-1);
        }

        // ask for the next astronauts or send message to server
        if(!(availableAstronautsToRemove.isEmpty() || currentAstronautsToRemove == astronautsToRemoveNumber-1)){
            currentAstronautsToRemove++;
            nextMethodToCall = this::answerForRemovingAstronaut;
            askPlayerToRemoveCurrentAstronaut(clientController);
        }
        else {
            sendLandingDecision(clientController, true);
        }
    }

    private void sendLandingDecision(ClientController clientController, boolean hasLanded){
        ActionMessage messageToServer = new ActionMessage("cabinCoordinates", clientController.getNickname());

        messageToServer.setData("hasLanded", hasLanded);

        messageToServer.setData("crewCoordinatesArray", selectedAstronautsToRemove);
        clientController.getServerProxy().send(messageToServer);
    }

    /* card prize */

    private void solveAbandonedShip(ActionMessage actionMessage, ClientController clientController){
        String receiver = actionMessage.getReceiver();
        int playerNewAbsPosition =  actionMessage.getInt("positions");
        ShipBoard playerUpdatedShipboard = (ShipBoard)  actionMessage.getData("claimerPlayerShipBoard");
        int playerUpdatedCredits =  actionMessage.getInt("claimerPlayerCredits");

        ViewShipBoard playerShipboard = clientController.getViewFlightBoard().getViewShipBoard(receiver);
        playerShipboard.setAbsPosition(playerNewAbsPosition);
        playerShipboard.updateViewShipBoard(playerUpdatedShipboard);
        playerShipboard.setPoints(playerUpdatedCredits);

        String messageForClient;
        if(receiver.equals(clientController.getNickname()))
            messageForClient = "You are ";
        else
            messageForClient = receiver + " is ";

        messageForClient += "now at position "+playerNewAbsPosition+" with "+playerUpdatedCredits+" credits";

        clientController.getView().showMessage(messageForClient);
    }
}
