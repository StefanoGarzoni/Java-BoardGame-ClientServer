package it.polimi.ingsw.model.cards.client.concrete;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.client.ClientController;
import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.generic.EnslaversCard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EnslaversClientCard extends EnemyClientCard{
    private final EnslaversCard cardData;

    private Map<Coordinates, Integer> availableAstronautsToRemove;
    private final Map<Coordinates, Integer> selectedAstronautsToRemove = new HashMap<>();
    int astronautsToRemoveNumber;
    private int currentAstronautsToRemove;

    /* constructor */

    public EnslaversClientCard(int creditPrize, int creditPenalty, int flightDayLoss, int requiredFirePower, int cardLevel, String fileName) {
        cardData = new EnslaversCard(creditPrize, creditPenalty, flightDayLoss, requiredFirePower, cardLevel, fileName);
    }

    /* Service methods */

    @Override
    public String getFileName() { return cardData.getFileName(); }

    @Override
    public void run(ClientController clientController) {
        clientController.getView().showMessage("Enslavers card has been drawn!");
        clientController.getView().showCard(this.cardData);

        methodsFromServerMap.put("askWinner", this::communicateWinningPrize);
        methodsFromServerMap.put("askCrewToRemove", this::askPlayerToRemoveAstronauts);

        methodsFromViewMap.put("answer", this::userAnswerSorter);
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

    private void userAnswerSorter(ActionMessage actionMessage, ClientController clientController){
        if(nextMethodToCall != null){
            nextMethodToCall.accept(actionMessage, clientController);
        }
        else{
            // TODO: error message
            clientController.getView().showErrorMessage("Invalid command");
        }
    }

    @Override
    public Card getCardData() {
        return cardData;
    }

    /* managing winning prize */
    private void askPlayerToClaimPrize(ClientController clientController){
        String messageToSend = "You have won Slavers! Do you accept [y/n] to loose "+cardData.getFlightDayLoss()+
                " to gain "+cardData.getCreditPrize()+" credits?";

        nextMethodToCall = this::answerForClaimingPrice;

        clientController.getView().showMessage(messageToSend);
    }

    private void answerForClaimingPrice(ActionMessage actionMessage, ClientController clientController){
        ArrayList<String> playerAnswer = (ArrayList<String>) actionMessage.getData("answer");

        sendClaimingPriceDecision(clientController, playerAnswer.getFirst().equals("y"));
    }

    private void sendClaimingPriceDecision(ClientController clientController, boolean doesClaim){
        ActionMessage messageToServer = new ActionMessage("claimPrize", clientController.getNickname());
        messageToServer.setData("doesClaim", doesClaim);
        clientController.getServerProxy().send(messageToServer);
    }

    private void communicateWinningPrize(ActionMessage actionMessage, ClientController clientController){
        if(actionMessage.getReceiver().equals(clientController.getNickname())){
            nextMethodToCall = this::answerForClaimingPrice;
            askPlayerToClaimPrize(clientController);
        }
    }

    /* astronauts penalty */
    private void askPlayerToRemoveCurrentAstronaut(ClientController clientController){
        final StringBuilder messageToSend = new StringBuilder("You have lost! Select coordinates [<x> <y>] where to remove this n." +
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
                    availableAstronautsToRemove.containsKey(selectedCoordinates)
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
            sendAstronautsRemovingDecision(clientController);
        }
    }

    private void sendAstronautsRemovingDecision(ClientController clientController){
        ActionMessage messageToServer = new ActionMessage("cabinCoordinates", clientController.getNickname());
        messageToServer.setData("cabinCoordinates", selectedAstronautsToRemove);
        clientController.getServerProxy().send(messageToServer);
    }

    private void askPlayerToRemoveAstronauts(ActionMessage actionMessage, ClientController clientController){
        if(actionMessage.getReceiver().equals(clientController.getNickname())){
            availableAstronautsToRemove = (Map<Coordinates, Integer>) actionMessage.getData("cabins");

            astronautsToRemoveNumber = cardData.getCrewPenalty();
            currentAstronautsToRemove = 0;

            if(!availableAstronautsToRemove.isEmpty()){
                nextMethodToCall = this::answerForRemovingAstronaut;
                askPlayerToRemoveCurrentAstronaut(clientController);
            }
            else
                sendAstronautsRemovingDecision(clientController);
        }
    }
}