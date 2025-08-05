package it.polimi.ingsw.model.cards.client.concrete;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.client.ClientController;
import it.polimi.ingsw.model.Cargo.CargoType;
import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.client.ClientCard;
import it.polimi.ingsw.model.cards.generic.SmugglersCard;
import it.polimi.ingsw.model.cards.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

public class SmugglersClientCard extends EnemyClientCard {
    private final SmugglersCard cardData;

    private ArrayList<CargoType> cargosToPlace;
    int currentCargoToPlaceIndex;
    private Map<Coordinates, Integer> specialCargoPlacements;
    private Map<Coordinates, Integer> standardCargoPlacements;
    private final HashMap<Coordinates, CargoType> selectedCargosPlacement = new HashMap<>();

    private int numberOfCargosToRemove;
    private int currentCargoToRemoveIndex;
    private final ArrayList<Pair<Coordinates, CargoType>> possibleCargosToRemove = new ArrayList<>();
    private final HashMap<Coordinates, CargoType> selectedCoordinatesToRemoveCargos = new HashMap<>();
    ArrayList<Coordinates> coordinatesContainingCargoColor;


    /* Constructor */

    public SmugglersClientCard(int cargoPenalty, ArrayList<CargoType> cargoPrize, int flightDayLoss, int requiredFirePower,
                               int cardLevel, String fileName) {
        cardData = new SmugglersCard(cargoPenalty, cargoPrize, flightDayLoss, requiredFirePower, cardLevel, fileName);
    }

    /* Service methods */

    @Override
    public String getFileName() { return cardData.getFileName(); }

    @Override
    public void run(ClientController clientController) {
        clientController.getView().showMessage("Smuggler card has been drawn!");
        clientController.getView().showCard(this.cardData);

        methodsFromServerMap.put("askCargoToRemove", this::askUserToRemoveCargos);
        methodsFromServerMap.put("addCargo", this::askUserToPlaceCargos);

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
            clientController.getView().showErrorMessage("Invalid command");
        }
    }

    /* Adding cargo prize */

    private void askUserToPlaceCargos(ActionMessage actionMessage, ClientController clientController){
        if(actionMessage.getReceiver().equals(clientController.getNickname())){
            specialCargoPlacements = (Map<Coordinates, Integer>) actionMessage.getData("specialCargos");
            standardCargoPlacements = (Map<Coordinates, Integer>) actionMessage.getData("allCargos");

            cargosToPlace = cardData.getCargoPrize();
            currentCargoToPlaceIndex = 0;
            nextMethodToCall = this::answerForCargoPlacement;
            askUserToPlaceCurrentCargo(clientController);
        }
    }

    private void askUserToPlaceCurrentCargo(ClientController clientController){
        // skips all cargos that can't be stored by this player
        while(
                currentCargoToPlaceIndex < cargosToPlace.size() &&
                        ((cargosToPlace.get(currentCargoToPlaceIndex).isSpecial() && specialCargoPlacements.isEmpty())
                                || (!cargosToPlace.get(currentCargoToPlaceIndex).isSpecial() && specialCargoPlacements.isEmpty() && standardCargoPlacements.isEmpty()))
        ){
            clientController.getView().showMessage("You have not a place for the "+currentCargoToPlaceIndex+" "+cargosToPlace.get(currentCargoToPlaceIndex).getColor()+"cargo");
            currentCargoToPlaceIndex++;
        }

        if(currentCargoToPlaceIndex < cargosToPlace.size()){
            final StringBuilder messageToSend = new StringBuilder("Select coordinates [<x> <y>] where to place this n." +
                    currentCargoToPlaceIndex + "/" + cargosToPlace.size() + " " + cargosToPlace.get(currentCargoToPlaceIndex).getColor() + " cargo: ");

            specialCargoPlacements.forEach((coordinates, freeSlots) -> {
                messageToSend.append(coordinates.toString()+ "["+freeSlots+" slots] /");
            });

            if(!cargosToPlace.get(currentCargoToPlaceIndex).isSpecial()){
                standardCargoPlacements.forEach((coordinates, freeSlots) -> {
                    messageToSend.append(coordinates.toString()+ "["+freeSlots+" slots] /");
                });
            }

            clientController.getView().showMessage(messageToSend.toString());
        }
        else {
            sendPlacingCargoDecision(clientController);
            clientController.getView().showMessage("You have ended choosing where to place your cargos");
        }
    }

    private void answerForCargoPlacement(ActionMessage actionMessage, ClientController clientController){
        ArrayList<String> playerAnswer = (ArrayList<String>) actionMessage.getData("answer");
        Coordinates selectedCoordinates;

        // controls on user input
        try{
            int xCoordinate = Integer.parseInt(playerAnswer.getFirst());
            int yCoordinate = Integer.parseInt(playerAnswer.get(1));
            selectedCoordinates = new Coordinates(xCoordinate, yCoordinate);

            if(
                    (cargosToPlace.get(currentCargoToPlaceIndex).isSpecial() && !specialCargoPlacements.containsKey(selectedCoordinates))
                            || (!cargosToPlace.get(currentCargoToPlaceIndex).isSpecial() && !(standardCargoPlacements.containsKey(selectedCoordinates) || specialCargoPlacements.containsKey(selectedCoordinates)))
            ){
                if(cargosToPlace.get(currentCargoToPlaceIndex).isSpecial())
                    clientController.getView().showMessage("You can't place a special cargo here");
                else
                    clientController.getView().showMessage("You can't place a standard cargo here");

                throw new Exception();
            }
        }
        catch (Exception e){
            askUserToPlaceCurrentCargo(clientController);
            return;
        }

        // sets coordinates to remove and removes this coordinates from the available for the next choices
        selectedCargosPlacement.put(selectedCoordinates, cargosToPlace.get(currentCargoToPlaceIndex));

        int availableSlots;
        if(cargosToPlace.get(currentCargoToPlaceIndex).isSpecial()){
            availableSlots = specialCargoPlacements.get(selectedCoordinates);
            if(availableSlots > 1)
                specialCargoPlacements.put(selectedCoordinates, availableSlots-1);
            else
                specialCargoPlacements.remove(selectedCoordinates);
        }
        else{
            availableSlots = standardCargoPlacements.get(selectedCoordinates);
            if(availableSlots > 1)
                standardCargoPlacements.put(selectedCoordinates, availableSlots-1);
            else
                standardCargoPlacements.remove(selectedCoordinates);
        }

        if(currentCargoToPlaceIndex < cargosToPlace.size()-1){
            currentCargoToPlaceIndex++;
            askUserToPlaceCurrentCargo(clientController);
        }
        else {
            sendPlacingCargoDecision(clientController);
        }
    }

    private void sendPlacingCargoDecision(ClientController clientController){
        ActionMessage messageToServer = new ActionMessage("setCargo", clientController.getNickname());
        messageToServer.setData("cargoCoordinates", selectedCargosPlacement);
        clientController.getServerProxy().send(messageToServer);
    }

    /* Removing cargo */
    private void askUserToRemoveCargos(ActionMessage actionMessage, ClientController clientController){
        if(actionMessage.getReceiver().equals(clientController.getNickname())){
            possibleCargosToRemove.clear();

            Map<Coordinates, CargoType> possibleCargosToRemoveMap = (Map<Coordinates, CargoType>) actionMessage.getData("possibleCargos");

            possibleCargosToRemoveMap.forEach((coordinates, cargoType) -> {
                possibleCargosToRemove.add(new Pair<>(coordinates, cargoType));
            });

            if(!possibleCargosToRemove.isEmpty()){
                numberOfCargosToRemove = actionMessage.getInt("numberOfCargosToRemove");
                currentCargoToRemoveIndex = 0;
                nextMethodToCall = this::answerForCargoToRemove;
                askUserToRemoveCurrentCargo(clientController);
            }
            else{
                sendRemovingCargoDecision(clientController);
            }
        }
    }

    private void askUserToRemoveCurrentCargo(ClientController clientController){
        final StringBuilder messageToSend = new StringBuilder("Select coordinates [<x> <y>] where to remove the n." +
                currentCargoToRemoveIndex + "/" + numberOfCargosToRemove + "  cargo: ");

        for(Coordinates coordinates : coordinatesContainingCargoColor){
            messageToSend.append(coordinates.toString() + " /");
        }

        clientController.getView().showMessage(messageToSend.toString());
    }

    private void answerForCargoToRemove(ActionMessage actionMessage, ClientController clientController){
        ArrayList<String> playerAnswer = (ArrayList<String>) actionMessage.getData("answer");
        Coordinates selectedCoordinates;

        // controls on user input
        try{
            int xCoordinate = Integer.parseInt(playerAnswer.getFirst());
            int yCoordinate = Integer.parseInt(playerAnswer.get(1));
            selectedCoordinates = new Coordinates(xCoordinate, yCoordinate);

            if(
                    possibleCargosToRemove.stream().noneMatch((pair) -> pair.getFirst().equals(selectedCoordinates))
            ){
                throw new Exception();
            }
        }
        catch (Exception e){
            askUserToRemoveCurrentCargo(clientController);
            return;
        }

        // sets coordinates to remove and removes this coordinates from the available for the next choices
        selectedCoordinatesToRemoveCargos.put(selectedCoordinates, possibleCargosToRemove.get(currentCargoToRemoveIndex).getSecond());
        coordinatesContainingCargoColor.remove(selectedCoordinates);

        if(currentCargoToRemoveIndex < possibleCargosToRemove.size() - 1){
            currentCargoToRemoveIndex++;
            askUserToRemoveCurrentCargo(clientController);
        }
        else {
            sendRemovingCargoDecision(clientController);
        }
    }

    private void sendRemovingCargoDecision(ClientController clientController){
        ActionMessage messageToServer = new ActionMessage("removeCargo", clientController.getNickname());
        messageToServer.setData("cargoType", selectedCoordinatesToRemoveCargos);
        clientController.getServerProxy().send(messageToServer);
    }
}
