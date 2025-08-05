package it.polimi.ingsw.model.cards.client.concrete;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.client.ClientController;
import it.polimi.ingsw.model.Cargo.CargoType;
import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.ShipBoard;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.client.ClientCard;
import it.polimi.ingsw.model.cards.generic.PlanetCard;
import it.polimi.ingsw.model.cards.util.Planet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class PlanetClientCard extends ClientCard {
    private final PlanetCard cardData;

    private int currentPlanetIndex;
    ArrayList<Integer> freePlanetIndexes;

    private ArrayList<CargoType> cargosToPlace;
    int currentCargoToPlaceIndex;
    private Map<Coordinates, Integer> specialCargoPlacements;
    private Map<Coordinates, Integer> standardCargoPlacements;
    private final HashMap<Coordinates, CargoType> selectedCargosPlacement = new HashMap<>();

    private BiConsumer<ActionMessage, ClientController> nextMethodToCall;

    /* constructors */

    public PlanetClientCard(PlanetCard planetCard) { cardData = planetCard; }

    public PlanetClientCard(ArrayList<Planet> planets, int flightDayLoss, int cardLevel, String fileName){
        cardData = new PlanetCard(planets, flightDayLoss, cardLevel, fileName);

        freePlanetIndexes = new ArrayList<>();
        for(int i = 0; i < planets.size(); i++){
            freePlanetIndexes.add(i);
        }
    }

    /* service methods */

    @Override
    public String getFileName() { return cardData.getFileName(); }

    @Override
    public void run(ClientController clientController) {
        clientController.getView().showMessage("PlanetCard card has been drawn!");
        clientController.getView().showCard(this.cardData);

        methodsFromViewMap.put("answer", this::userAnswerSorter);

        methodsFromServerMap.put("planetLanding", this::askUserToLand);
        methodsFromServerMap.put("loadCargo", this::askUserToPlaceCargos);
        methodsFromServerMap.put("playerLanded", this::setPlayerLanded);
        methodsFromServerMap.put("planetSolved", this::updateClientModel);
        methodsFromServerMap.put("noCargoAvailable", this::manageUnavailableCargo);
    }


    @Override
    public Card getCardData() {
        return cardData;
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
            e.printStackTrace();
            return;
        }
    }

    private void userAnswerSorter(ActionMessage actionMessage, ClientController clientController){
        if(nextMethodToCall != null){
            nextMethodToCall.accept(actionMessage, clientController);
        }
        else{
            clientController.getView().showErrorMessage("You have nothing to do");
        }
    }

    /* ask player for landing decision */

    private void askUserToLandOnPlanet(ClientController clientController){
        if(currentPlanetIndex < cardData.getPlanets().size()){
            StringBuilder messageToShow = new StringBuilder("Do you want to land on this planet [y/n] and loose " +
                    cardData.getFlightDayLoss() + " flight days? Cargos: ");

            for(CargoType cargo : cardData.getPlanets().get(currentPlanetIndex).getCargoList()){
                messageToShow.append(cargo.getColor()).append(" / ");
            }

            clientController.getView().showMessage(messageToShow.toString());
            nextMethodToCall = this::answerForPlanetLanding;
        }
        else {
            clientController.getView().showMessage("You have ended choosing a planet where to land");

            nextMethodToCall = null;
        }
    }

    private void answerForPlanetLanding(ActionMessage actionMessage, ClientController clientController){
        String playerAnswer = ((ArrayList<String>) actionMessage.getData("answer")).getFirst();

        if(playerAnswer.equals("y")){
            sendLandingDecision(clientController, true);
        }
        else{
            if(freePlanetIndexes.indexOf(currentPlanetIndex) == freePlanetIndexes.size() - 1){ // the asked planet was the last one
                sendLandingDecision(clientController, false);
            }
            else{
                currentPlanetIndex = freePlanetIndexes.get(freePlanetIndexes.indexOf(currentPlanetIndex)+1);
                askUserToLandOnPlanet(clientController);
            }
        }
    }

    private void sendLandingDecision(ClientController clientController, boolean hasLanded){
        ActionMessage messageToServer = new ActionMessage("land", clientController.getNickname());

        if(hasLanded){
            messageToServer.setData("hasLanded", true);
            messageToServer.setData("planetIndex", currentPlanetIndex);
        }
        else{
            messageToServer.setData("planetIndex", -1);
            messageToServer.setData("hasLanded", false);
        }

        clientController.getServerProxy().send(messageToServer);

        nextMethodToCall = null;
    }

    private void askUserToLand(ActionMessage actionMessage, ClientController clientController){
        String askedNickname = actionMessage.getReceiver();
        if(askedNickname.equals(clientController.getNickname())){
            // shows all possible planets with relatives cargos
            currentPlanetIndex = freePlanetIndexes.getFirst();

            nextMethodToCall = this::answerForPlanetLanding;
            askUserToLandOnPlanet(clientController);
        }
        else{
            clientController.getView().showMessage(askedNickname+ " is choosing whether to Land on a planet ");
        }
    }

    /* Cargos placement */

    private void askUserToPlaceCargos(ActionMessage actionMessage, ClientController clientController){
        if(actionMessage.getReceiver().equals(clientController.getNickname())){
            int planetIndex = actionMessage.getInt("planetIndex");
            specialCargoPlacements = (Map<Coordinates, Integer>) actionMessage.getData("specialCargos");
            standardCargoPlacements = (Map<Coordinates, Integer>) actionMessage.getData("allCargos");

            cargosToPlace = cardData.getPlanets().get(planetIndex).getCargoList();
            currentCargoToPlaceIndex = 0;
            nextMethodToCall = this::answerForCargoPlacement;
            if(specialCargoPlacements.isEmpty() && standardCargoPlacements.isEmpty()){
                clientController.getView().showErrorMessage("No cargo storage available, dumping cargo into space.");
            }
            else {
                askUserToPlaceCurrentCargo(clientController);
            }
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
            clientController.getView().showMessage("You have ended choosing where to place your cargos");
            sendPlacingCargoDecision(clientController);
        }
    }

    private void answerForCargoPlacement(ActionMessage actionMessage, ClientController clientController){
        ArrayList<String> playerAnswer = (ArrayList<String>) actionMessage.getData("answer");
        Coordinates selectedCoordinates;

        if(!playerAnswer.getFirst().equals("n")){
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

    /* Landing player update */

    private void setPlayerLanded(ActionMessage actionMessage, ClientController clientController){
        String playerNickname = actionMessage.getReceiver();
        int planetIndex =  actionMessage.getInt("planetIndex");

        // removes the planet from the list
        //cardData.getPlanets().remove(planetIndex);
        freePlanetIndexes.remove(Integer.valueOf(planetIndex));

        if(playerNickname.equals(clientController.getNickname())){
            clientController.getView().showMessage("You landed on the selected planet!");
        }
        else{
            clientController.getView().showMessage(playerNickname+" landed on the planet n. "+planetIndex+"!");
        }
    }

    private void updateClientModel(ActionMessage actionMessage, ClientController clientController){
        HashMap<String, ShipBoard> updatedShipBoard = (HashMap<String, ShipBoard>) actionMessage.getData("modifiedShipboards");
        HashMap<String, Integer> updatedPositions = (HashMap<String, Integer>) actionMessage.getData("modifiedPositions");

        updatedShipBoard.forEach((nickname, shipboard) -> {
            clientController.getViewFlightBoard().getViewShipBoard(nickname).updateViewShipBoard(shipboard);
        });

        updatedPositions.forEach((nickname, newAbsPosition) -> {
            clientController.getViewFlightBoard().getViewShipBoard(nickname).setAbsPosition(newAbsPosition);
        });

        clientController.getView().showMessage("Your Shipboard and game Flight Board may have been modified by this card!");
    }

    private void manageUnavailableCargo(ActionMessage actionMessage, ClientController clientController) {
        if(actionMessage.getReceiver().equals(clientController.getNickname())){
            clientController.getView().showMessage("You landed on the planet, but you have no cargo tiles!");
        }
        else clientController.getView().showMessage(actionMessage.getReceiver() + " does not have storage for his new cargos!");
    }
}