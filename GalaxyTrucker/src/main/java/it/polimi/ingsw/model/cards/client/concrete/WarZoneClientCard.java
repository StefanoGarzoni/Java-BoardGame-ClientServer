package it.polimi.ingsw.model.cards.client.concrete;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.client.ClientController;
import it.polimi.ingsw.client.View;
import it.polimi.ingsw.model.Cargo.CargoType;
import it.polimi.ingsw.model.ComponentTile.Connector;
import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.Direction;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.client.ClientCard;
import it.polimi.ingsw.model.cards.generic.WarZoneCard;
import it.polimi.ingsw.model.cards.util.FireShot;
import it.polimi.ingsw.model.cards.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class WarZoneClientCard extends ClientCard {
    private final WarZoneCard cardData;

    // battery asking
    private int availableBatteries;
    private int batteriesToAskFor;
    private int currentlyAskedBattery;
    private HashMap<Coordinates, Integer> batteryTilesAvailability;

    private Pair<Direction, Direction> shieldsToActivate = null;
    private int currentAskedShieldToActivate;
    private final ArrayList<Pair<Coordinates, Pair<Direction, Direction>>> availableShields = new ArrayList<>();

    // cannons
    private ArrayList<Coordinates> availableDoubleCannons;
    private int currentAskedDoubleCannon;

    // information to send to the server
    private final ArrayList<Coordinates> doubleCannonToActivate = new ArrayList<>();
    private final ArrayList<Coordinates> doubleEnginesToActivate = new ArrayList<>();
    private final HashMap<Coordinates, Integer> batteriesCoordinates = new HashMap<>();

    // engines
    private ArrayList<Coordinates> availableDoubleEngines;
    private int currentAskedDoubleEngine;

    // cargos
    private int numberOfCargosToRemove;
    private int currentCargoToRemoveIndex;
    private final ArrayList<Pair<Coordinates, CargoType>> possibleCargosToRemove = new ArrayList<>();
    private final HashMap<Coordinates, CargoType> selectedCoordinatesToRemoveCargos = new HashMap<>();
    ArrayList<Coordinates> coordinatesContainingCargoColor;

    private Map<Coordinates, Integer> availableAstronautsToRemove;
    private final Map<Coordinates, Integer> selectedAstronautsToRemove = new HashMap<>();
    int astronautsToRemoveNumber;
    private int currentAstronautsToRemove;

    private int currentBranch = 0;
    private ArrayList<ArrayList<Coordinates>> branches;

    private Consumer<ClientController> toServerSenderMethod;
    private BiConsumer<ActionMessage, ClientController> nextMethodToCall;

    /* constructor */

    public WarZoneClientCard(WarZoneCard warZoneCard){
        cardData = warZoneCard;
    }

    public WarZoneClientCard(int cargoLoss, int crewLoss, int flightDayLoss, ArrayList<FireShot> shotsList, int cardLevel, String fileName){
        cardData = new WarZoneCard(cargoLoss, crewLoss, flightDayLoss, shotsList, cardLevel, fileName);
    }

    /* service methods */

    @Override
    public String getFileName() { return cardData.getFileName(); }

    @Override
    public void run(ClientController clientController) {
        clientController.getView().showMessage("War Zone card has been drawn!");
        clientController.getView().showCard(this.cardData);

        methodsFromServerMap.put("solveLeastCrew", this::applyFlightDayPenalty);
        methodsFromServerMap.put("askCannonsUsage", this::askForDoubleCannonToActivate);
        methodsFromServerMap.put("askEngineUsage", this::askForDoubleEngineToActivate);
        methodsFromServerMap.put("sendShot", this::handleShot);
        methodsFromServerMap.put("branchingResolve", this::resolveBranches);
        methodsFromServerMap.put("askCrewToRemove", this::askPlayerToRemoveAstronauts);
        methodsFromServerMap.put("askCargoToRemove", this::askUserToRemoveCargos);
        methodsFromServerMap.put("notEnoughResources", this::notEnoughResources);

        methodsFromViewMap.put("answer", this::userAnswerSorter);
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
            return;
        }
    }

    private void userAnswerSorter(ActionMessage actionMessage, ClientController clientController){
        if(nextMethodToCall != null){
            nextMethodToCall.accept(actionMessage, clientController);
        }
        else{
            // TODO: error message
        }
    }

    /* battery usage */

    private void askUserForBatteriesUsage(ClientController clientController){
        if(currentlyAskedBattery < batteriesToAskFor){
            AtomicReference<String> messageToShow = new AtomicReference<>("Select the index of the coordinates from which extract the " + currentlyAskedBattery + " of " + batteriesToAskFor + " battery." +
                    "Available battery at: ");
            batteryTilesAvailability.forEach((coordinates, availableBatteries) -> {
                if(availableBatteries > 0){
                    messageToShow.updateAndGet(v -> v + coordinates + " / ");
                }
            });

            clientController.getView().showMessage(
                    messageToShow.get()
            );
        }
        else {
            clientController.getView().showMessage(
                    "You have ended choosing sources for the batteries you want to use"
            );

            nextMethodToCall = null;
            toServerSenderMethod.accept(clientController);
        }
    }

    private void answerForBatteriesUsage(ActionMessage actionMessage, ClientController clientController){
        toServerSenderMethod = this::sendFirePowerToServer;

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

        currentlyAskedBattery++;
        askUserForBatteriesUsage(clientController);
    }

    /* cannon usage asking */

    /** This method is called when server sends an ActionMessage "firePowerCheck"
     *
     * @param actionMessage sent by the server
     * @param clientController controller of the client
     */
    private void askForDoubleCannonToActivate(ActionMessage actionMessage, ClientController clientController){
        if(actionMessage.getReceiver().equals(clientController.getNickname())){
            availableDoubleCannons = (ArrayList<Coordinates>) actionMessage.getData("cannons");
            batteryTilesAvailability = (HashMap<Coordinates, Integer>)  actionMessage.getData("batteries");

            availableBatteries = 0;
            for(Integer storedBattery : batteryTilesAvailability.values())
                availableBatteries += storedBattery;

            nextMethodToCall = this::answerForDoubleCannonToActivate;
            currentAskedDoubleCannon = 0;
            askUserForCannonUsage(clientController);
        }
    }

    /** Shows a messages to the client, asking if he wants to activate double cannon, until he has
     * no more batteries or no more double cannon to activate.
     *
     * @param clientController controller that contains a view reference
     */
    private void askUserForCannonUsage(ClientController clientController){
        if(availableBatteries > 0 && !availableDoubleCannons.isEmpty()){
            clientController.getView().showMessage(
                    "You can activate "+availableDoubleCannons.size()+" double cannon and you have "+availableBatteries+"."+
                            "Do you want [y/n] to activate cannon at coordinates: "+availableDoubleCannons.get(currentAskedDoubleCannon)+ "?"
            );
        }
        else {
            clientController.getView().showMessage(
                    "You have ended choosing double cannon to activate"
            );

            currentlyAskedBattery = 0;
            toServerSenderMethod = this::sendFirePowerToServer;
            nextMethodToCall = this::answerForBatteriesUsage;

            batteriesCoordinates.clear();
            askUserForBatteriesUsage(clientController);
        }
    }

    private void answerForDoubleCannonToActivate(ActionMessage actionMessage, ClientController clientController){
        String playerAnswer = ((ArrayList<String>) actionMessage.getData("answer")).getFirst();
        if(playerAnswer.equals("y")){
            Coordinates selectedCoordinates = availableDoubleCannons.get(currentAskedDoubleCannon);
            doubleCannonToActivate.add(selectedCoordinates);
            availableBatteries--;
        }
        currentAskedDoubleCannon++;
        askUserForCannonUsage(clientController);
    }

    private void sendFirePowerToServer(ClientController clientController){
        ActionMessage messageToServer = new ActionMessage("setFirePower", clientController.getNickname());
        messageToServer.setData("firePowerCoordinates", doubleCannonToActivate);
        messageToServer.setData("batteriesCoordinates", batteriesCoordinates);

        clientController.getServerProxy().send(messageToServer);
    }

    /* flight days penalty */

    private void applyFlightDayPenalty(ActionMessage actionMessage, ClientController clientController){
        String penalizedPlayerNickname = actionMessage.getReceiver();
        int newPlayerAbsPosition =  actionMessage.getInt("absolutePosition");

        clientController.getViewFlightBoard().getViewShipBoard(penalizedPlayerNickname).setAbsPosition(newPlayerAbsPosition);

        String messageString;
        if(penalizedPlayerNickname.equals(clientController.getNickname()))
            messageString = "You have lost "+cardData.getFlightDayLoss()+" because of the current card";
        else
            messageString = penalizedPlayerNickname+" have lost "+cardData.getFlightDayLoss()+" because of the current card";

        clientController.getView().showMessage(messageString);
        clientController.getView().showFlightBoard();
    }

    /* engine usage asking */

    /** This method is called when server sends an ActionMessage "firePowerCheck"
     *
     * @param actionMessage sent by the server
     * @param clientController controller of the client
     */
    private void askForDoubleEngineToActivate(ActionMessage actionMessage, ClientController clientController){
        if(actionMessage.getReceiver().equals(clientController.getNickname())){
            availableDoubleEngines = (ArrayList<Coordinates>) actionMessage.getData("doubleEngine");
            batteryTilesAvailability = (HashMap<Coordinates, Integer>)  actionMessage.getData("batteries");

            availableBatteries = 0;
            for(Integer storedBattery : batteryTilesAvailability.values())
                availableBatteries += storedBattery;

            nextMethodToCall = this::answerForDoubleEngineToActivate;
            currentAskedDoubleEngine = 0;
            askUserForEngineUsage(clientController);
        }
    }

    /** Shows a messages to the client, asking if he wants to activate double cannon, until he has
     * no more batteries or no more double cannon to activate.
     *
     * @param clientController controller that contains a view reference
     */
    private void askUserForEngineUsage(ClientController clientController){
        if(availableBatteries > 0 && !availableDoubleCannons.isEmpty()){
            clientController.getView().showMessage(
                    "You can activate "+availableDoubleEngines.size()+" double engine and you have "+availableBatteries+"."+
                            "Do you want [y/n] to activate cannon at coordinates: "+availableDoubleEngines.get(currentAskedDoubleEngine)+ "?"
            );
        }
        else {
            clientController.getView().showMessage(
                    "You have ended choosing double engines to activate"
            );

            currentlyAskedBattery = 0;
            toServerSenderMethod = this::sendEnginePowerToServer;
            nextMethodToCall = this::answerForBatteriesUsage;

            batteriesCoordinates.clear();
            askUserForBatteriesUsage(clientController);
        }
    }

    private void answerForDoubleEngineToActivate(ActionMessage actionMessage, ClientController clientController){
        String playerAnswer = ((ArrayList<String>) actionMessage.getData("answer")).getFirst();
        if(playerAnswer.equals("y")){
            Coordinates selectedCoordinates = availableDoubleEngines.get(currentAskedDoubleEngine);
            doubleEnginesToActivate.add(selectedCoordinates);
            availableBatteries--;
        }
        currentAskedDoubleEngine++;
        askUserForEngineUsage(clientController);
    }

    private void sendEnginePowerToServer(ClientController clientController){
        ActionMessage messageToServer = new ActionMessage("setEnginePower", clientController.getNickname());
        messageToServer.setData("enginePowerCoordinates", doubleEnginesToActivate);
        messageToServer.setData("batteriesCoordinates", batteriesCoordinates);

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
                numberOfCargosToRemove =  actionMessage.getInt("numberOfCargosToRemove");
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
        final StringBuilder messageToSend = new StringBuilder("Select coordinates where to remove the n." +
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

    /* astronauts penalty */

    private void askPlayerToRemoveCurrentAstronaut(ClientController clientController){
        final StringBuilder messageToSend = new StringBuilder("You have lost! Select coordinates where to remove this n." +
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

            astronautsToRemoveNumber = cardData.getCrewLoss();
            currentAstronautsToRemove = 0;

            if(!availableAstronautsToRemove.isEmpty()){
                nextMethodToCall = this::answerForRemovingAstronaut;
                askPlayerToRemoveCurrentAstronaut(clientController);
            }
            else
                sendAstronautsRemovingDecision(clientController);
        }
    }

    /* Shots handling */

    private void handleShot(ActionMessage actionMessage, ClientController clientController){
        if(actionMessage.getReceiver().equals(clientController.getNickname())){
            int currentShotIndex =  actionMessage.getInt("shotIndex");
            int currentShotRoll =  actionMessage.getInt("roll");

            FireShot currentShot = cardData.getShotsList().get(currentShotIndex);

            clientController.getView().showMessage(
                    "Your n."+ currentShotIndex +"/"+cardData.getShotsList().size()+" "+currentShot.getSize()
                            +" from "+currentShot.getDirection().name()+" meteor's dice roll is: "+ currentShotRoll
            );

            handlePossibleShields(actionMessage, clientController);
        }
    }

    /* shields activation */
    private void handlePossibleShields(ActionMessage actionMessage, ClientController clientController){
        if(actionMessage.getReceiver().equals(clientController.getNickname())){
            batteryTilesAvailability.clear();
            availableShields.clear();

            Map<Coordinates, Map<String, Map<Direction[], Connector[]>>> allShieldsInformation = (Map<Coordinates, Map<String, Map<Direction[], Connector[]>>>) actionMessage.getData("possibleShields");
            Map<Coordinates, Map<Integer, Map<String, Connector[]>>> allBatteriesInformation = (Map<Coordinates, Map<Integer, Map<String, Connector[]>>>)  actionMessage.getData("batteries");

            // extracting shields information
            allBatteriesInformation.forEach(((coordinates, otherInfos) -> {
                otherInfos.forEach((batteriesQuantity, lastInfos) -> {
                    batteryTilesAvailability.put(coordinates, batteriesQuantity);
                });
            }));

            // extracting shields information
            allShieldsInformation.forEach(((coordinates, otherInfos) -> {
                otherInfos.forEach((fileName, lastInfos) -> {
                    lastInfos.forEach(((directions, connectors) -> {
                        availableShields.add(new Pair<>(coordinates, new Pair<>(directions[0], directions[1])));
                    }));
                });
            }));

            // calculating available batteries
            availableBatteries = 0;
            for(Integer storedBattery : batteryTilesAvailability.values())
                availableBatteries += storedBattery;

            currentAskedShieldToActivate = 0;

            nextMethodToCall = this::userAnswerForShieldsActivation;

            askUserForShieldsActivation(clientController);
        }
    }

    private void askUserForShieldsActivation(ClientController clientController){
        if(availableBatteries > 0 && currentAskedShieldToActivate < availableShields.size()){
            String messageToUser = "Do you want to activate [y/n] shield at coordinate "
                    +availableShields.get(currentAskedShieldToActivate).getFirst()+
                    "that can defend you from "+availableShields.get(currentAskedShieldToActivate).getSecond().getFirst().name()+
                    "and "+availableShields.get(currentAskedShieldToActivate).getSecond().getSecond().name();
            clientController.getView().showMessage(messageToUser);
        }
        else{
            nextMethodToCall = this::answerForBatteriesUsage;
            toServerSenderMethod = this::sendShieldsDecision;

            currentlyAskedBattery = 0;
            batteriesToAskFor = 1;

            batteriesCoordinates.clear();
            askUserForBatteriesUsage(clientController);
        }
    }

    private void userAnswerForShieldsActivation(ActionMessage actionMessage, ClientController clientController){
        String userAnswer = ((ArrayList<String>) actionMessage.getData("answer")).getFirst();

        if(userAnswer.equals("y")){
            shieldsToActivate = availableShields.get(currentAskedShieldToActivate).getSecond();

            toServerSenderMethod = this::sendShieldsDecision;
            nextMethodToCall = this::answerForBatteriesUsage;
            askUserForBatteriesUsage(clientController);
        }
        else{
            currentAskedShieldToActivate++;
            askUserForShieldsActivation(clientController);
        }
    }

    private void sendShieldsDecision(ClientController clientController){
        ActionMessage messageToServer;

        if(shieldsToActivate != null){
            messageToServer = new ActionMessage("activateShield", clientController.getNickname());
            messageToServer.setData("shieldDirections", shieldsToActivate);
            messageToServer.setData("batteryCoordinates", batteriesCoordinates.entrySet().iterator().next().getKey());
        }
        else{
            messageToServer = new ActionMessage("doNothing", clientController.getNickname());
        }

        clientController.getServerProxy().send(messageToServer);
    }

    /* resolve branching */

    private void resolveBranches(ActionMessage actionMessage, ClientController clientController){
        branches = (ArrayList<ArrayList<Coordinates>>) actionMessage.getData("trunks");

        nextMethodToCall = this::answerUserToSolveBranches;
        askUserToSolveBranches(clientController);
    }

    private void askUserToSolveBranches(ClientController clientController){
        View view = clientController.getView();

        if(currentBranch < branches.size()){
            clientController.getView().showShipboard(clientController.getViewFlightBoard().getViewShipBoard(clientController.getNickname()));
            view.showHighlightedTiles(branches.get(currentBranch));

            view.showMessage("Do you want [y/n] to keep this branch?");
        }
        else {
            sendBranchingDecision(clientController);
        }

    }

    private void answerUserToSolveBranches(ActionMessage actionMessage, ClientController clientController){
        String userAnswer = ((ArrayList<String>) actionMessage.getData("answer")).getFirst();

        if(userAnswer.equals("y")){
            sendBranchingDecision(clientController);
        }
        else{
            currentBranch++;
            askUserToSolveBranches(clientController);
        }
    }

    private void sendBranchingDecision(ClientController clientController){
        ActionMessage messageToServer = new ActionMessage("resolveBranching", clientController.getNickname());

        messageToServer.setData("branchToKeep", branches.get(currentBranch));

        clientController.getServerProxy().send(messageToServer);
    }

    private void notEnoughResources(ActionMessage actionMessage, ClientController clientController){
        if(actionMessage.getReceiver().equals(clientController.getNickname())){
            clientController.getView().showMessage("Not enough resources");
        }
    }

}
