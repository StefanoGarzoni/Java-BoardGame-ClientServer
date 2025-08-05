package it.polimi.ingsw.model.cards.client.concrete;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.client.ClientController;
import it.polimi.ingsw.model.Cargo.CargoType;
import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.ShipBoard;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.client.ClientCard;
import it.polimi.ingsw.model.cards.generic.AbandonedShipCard;
import it.polimi.ingsw.model.cards.generic.AbandonedStationCard;
import it.polimi.ingsw.model.clientModel.ViewShipBoard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

// problemi generali come identifico la carta? qui non mi manda il file name
public class AbandonedStationClientCard extends ClientCard {
    private final AbandonedStationCard cardData;

    private ArrayList<CargoType> cargosToPlace;
    int currentCargoToPlaceIndex;
    private Map<Coordinates, Integer> specialCargoPlacements;
    private Map<Coordinates, Integer> standardCargoPlacements;
    private final HashMap<Coordinates, CargoType> selectedCargosPlacement = new HashMap<>();

    private BiConsumer<ActionMessage, ClientController> nextMethodToCall;

    /* constructors */

    public AbandonedStationClientCard(AbandonedStationCard abandonedStationCard) { cardData = abandonedStationCard; }

    public AbandonedStationClientCard(int flightDayLoss, int requiredCrew, ArrayList<CargoType> cargoList, String fileName, int cardLevel) {
        cardData = new AbandonedStationCard(flightDayLoss, requiredCrew, cargoList, fileName, cardLevel);
    }

    /* service methods */

    @Override
    public String getFileName() { return cardData.getFileName(); }

    @Override
    public void run(ClientController clientController) {
        clientController.getView().showMessage("Abandoned Station card has been drawn!");
        clientController.getView().showCard(this.cardData);

        methodsFromViewMap.put("answer", this::userAnswerSorter);

        methodsFromServerMap.put("abandonedStationLanding", this::askPlayerToLandOnStation);
        methodsFromServerMap.put("playerLanded", this::askUserToPlaceCargos);
        methodsFromViewMap.put("solveAbandonedStation", this::solveAbandonedStation);
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

    /* ask to land */

    private void askPlayerToLandOnStation(ActionMessage actionMessage, ClientController clientController){
        String targetPlayer = actionMessage.getReceiver();
        if(targetPlayer.equals(clientController.getNickname())){
            String messageForUser = "Do you want to land [y/n] on this Abandoned Station? " +
                    "If so, you will lose "+cardData.getFlightDayLoss()+" flight days, and gain this cargos: ";

            for(CargoType cargo : cardData.getCargoList()){
                messageForUser += cargo.getColor() + " / ";
            }

            nextMethodToCall = this::answerForLanding;
            clientController.getView().showMessage(messageForUser);
        }
    }

    private void answerForLanding(ActionMessage actionMessage, ClientController clientController){
        String playerAnswer = ((ArrayList<String>) actionMessage.getData("answer")).getFirst();

        sendLandingDecision(clientController, playerAnswer.equals("y"));
    }

    private void sendLandingDecision(ClientController clientController, boolean hasLanded){
        ActionMessage messageToServer = new ActionMessage("land", clientController.getNickname());

        messageToServer.setData("hasLanded", hasLanded);

        clientController.getServerProxy().send(messageToServer);
    }

    /* ask where to place cargos */

    private void askUserToPlaceCargos(ActionMessage actionMessage, ClientController clientController){
        if(actionMessage.getReceiver().equals(clientController.getNickname())){
            specialCargoPlacements = (Map<Coordinates, Integer>) actionMessage.getData("specialCargos");
            standardCargoPlacements = (Map<Coordinates, Integer>) actionMessage.getData("allCargos");

            cargosToPlace = cardData.getCargoList();
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
            final StringBuilder messageToSend = new StringBuilder("Select coordinates <x> <y> where to place this n." +
                    currentCargoToPlaceIndex + "/" + cargosToPlace.size() + " " + cargosToPlace.get(currentCargoToPlaceIndex).getColor() + " cargo: ");

            specialCargoPlacements.forEach((coordinates, freeSlots) -> {
                messageToSend.append(coordinates.toString()+ "["+freeSlots+" slots] /");
            });

            if(!cargosToPlace.get(currentCargoToPlaceIndex).isSpecial()){
                standardCargoPlacements.forEach((coordinates, freeSlots) -> {
                    messageToSend.append(coordinates.toString()+ "["+freeSlots+" slots] /");
                });
            }

            messageToSend.append("Write 'n' if you don't want to load cargo");

            clientController.getView().showMessage(messageToSend.toString());
        }
        else{
            clientController.getView().showMessage("You have ended choosing where to place your cargos");
            sendPlacingCargoDecision(clientController);
        }
    }

    private void answerForCargoPlacement(ActionMessage actionMessage, ClientController clientController){
        ArrayList<String> playerAnswer = (ArrayList<String>) actionMessage.getData("answer");
        Coordinates selectedCoordinates;

        if(!playerAnswer.getFirst().equals("n")){   // a player may not want to load a cargo
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
        }

        // if the last selected is not the last cargo
        currentCargoToPlaceIndex++;
        if(currentCargoToPlaceIndex < cargosToPlace.size()){
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

    private void solveAbandonedStation(ActionMessage actionMessage, ClientController clientController){
        String receiver = actionMessage.getReceiver();
        ShipBoard playerUpdatedShipboard = (ShipBoard)  actionMessage.getData("claimerShipBoard");

        ViewShipBoard playerShipboard = clientController.getViewFlightBoard().getViewShipBoard(receiver);
        playerShipboard.updateViewShipBoard(playerUpdatedShipboard);

        String messageForClient;
        if(receiver.equals(clientController.getNickname()))
            messageForClient = "You have ";
        else
            messageForClient = receiver + " has ";

        messageForClient += "won this card!";

        clientController.getView().showMessage(messageForClient);
    }
}
