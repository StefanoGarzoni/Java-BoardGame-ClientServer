package it.polimi.ingsw.model.cards.client.concrete;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.client.ClientController;
import it.polimi.ingsw.model.ComponentTile.Connector;
import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.ShipBoard;
import it.polimi.ingsw.model.cards.client.ClientCard;
import it.polimi.ingsw.model.cards.generic.EnemyCard;
import it.polimi.ingsw.model.cards.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

public abstract class EnemyClientCard extends ClientCard {
    private EnemyCard cardData;

    private int availableBatteries = 0;
    private int currentAskedBatteryForDoubleCannon;
    private final HashMap<Coordinates, Integer> batteryTilesAvailability = new HashMap<>();

    private final ArrayList<Pair<Coordinates, Integer>> availableDoubleCannons = new ArrayList<>();
    private final ArrayList<Coordinates> doubleCannonToActivate = new ArrayList<>();
    private final HashMap<Coordinates, Integer> selectedBatteriesCoordinates = new HashMap<>();
    private int currentAskedDoubleCannon;

    protected BiConsumer<ActionMessage, ClientController> nextMethodToCall;

    /* constructor */
    EnemyClientCard(){
        methodsFromServerMap.put("askCannonsUsage", this::askForDoubleCannonToActivate);
        methodsFromServerMap.put("notifyEnemyUpdate", this::updateClientModel);
        methodsFromServerMap.put("notifyDefeat", this::notifyDefeat);
        methodsFromServerMap.put("notifyDraw", this::notifyDraw);
        methodsFromServerMap.put("notEnoughResources", this::notEnoughResources);
    }

    /* battery usage */

    protected void askUserForBatteriesUsage(ClientController clientController){
        if(currentAskedBatteryForDoubleCannon < doubleCannonToActivate.size()){
            AtomicReference<String> messageToShow = new AtomicReference<>("Select the coordinates [<x> <y>] from which extract the " + currentAskedBatteryForDoubleCannon + "/" + doubleCannonToActivate.size() + "cannon's battery." +
                    "Available battery at: ");
            batteryTilesAvailability.forEach((coordinates, availableBatteries) -> {
                if(availableBatteries > 0){
                    messageToShow.updateAndGet(v -> v + coordinates + " / ");
                }
                else{
                    batteryTilesAvailability.remove(coordinates);
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

            nextMethodToCall = this::answerForBatteriesUsage;
            sendFirePowerToServer(clientController);
        }
    }

    protected void answerForBatteriesUsage(ActionMessage actionMessage, ClientController clientController){
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
        if(batteryTilesAvailability.containsKey(selectedCoordinates)){
            int currentBatteriesUsed = batteryTilesAvailability.get(selectedCoordinates);
            selectedBatteriesCoordinates.put(selectedCoordinates, currentBatteriesUsed+1);
        }
        else{
            selectedBatteriesCoordinates.put(selectedCoordinates, 1);
        }

        // update the available batteries at that coordinates
        int availableBatteriesAtSelectedCoordinates = batteryTilesAvailability.get(selectedCoordinates);
        batteryTilesAvailability.put(selectedCoordinates, availableBatteriesAtSelectedCoordinates-1);

        currentAskedBatteryForDoubleCannon++;
        askUserForBatteriesUsage(clientController);
    }

    /* cannon usage asking */

    /** Shows a messages to the client, asking if he wants to activate double cannon, until he has
     * no more batteries or no more double cannon to activate.
     *
     * @param clientController controller that contains a view reference
     */
    protected void askUserForCannonUsage(ClientController clientController){
        if(availableBatteries > 0 && currentAskedBatteryForDoubleCannon < availableDoubleCannons.size()){
            clientController.getView().showMessage(
                    "You can activate "+availableDoubleCannons.size()+" double cannon and you have "+availableBatteries+"."+
                            "Do you want [y/n] to activate cannon at coordinates: "+availableDoubleCannons.get(currentAskedDoubleCannon).getFirst()+
                            " with " + availableDoubleCannons.get(currentAskedDoubleCannon).getSecond()+" firepower?"
            );
        }
        else {
            clientController.getView().showMessage(
                    "You have ended choosing double cannon to activate"
            );

            currentAskedBatteryForDoubleCannon = 0;
            nextMethodToCall = this::answerForBatteriesUsage;
            askUserForBatteriesUsage(clientController);
        }
    }

    /** This method is called when server sends an ActionMessage "firePowerCheck"
     *
     * @param actionMessage sent by the server
     * @param clientController controller of the client
     */
    protected void askForDoubleCannonToActivate(ActionMessage actionMessage, ClientController clientController){
        if(actionMessage.getReceiver().equals(clientController.getNickname())){
            Map<Coordinates, Integer> doubleCannonInfos = (Map<Coordinates, Integer>) actionMessage.getData("cannons");
            doubleCannonInfos.forEach(((coordinates, firePower) -> {
                availableDoubleCannons.add(new Pair<>(coordinates, firePower));
            }));

            // extraction of batteries information from ActionMessage parameter
            Map<Coordinates, Map<Integer, Map<String, Connector[]>>> allBatteriesInformation = (Map<Coordinates, Map<Integer, Map<String, Connector[]>>>)  actionMessage.getData("batteries");
            allBatteriesInformation.forEach((coordinate, otherInfos) -> {
                AtomicReference<Integer> numOfBatteries = new AtomicReference<>(0);

                otherInfos.forEach((batteries, tileInfo) -> {
                    numOfBatteries.set(batteries);
                });

                batteryTilesAvailability.put(coordinate, numOfBatteries.get());
                availableBatteries += numOfBatteries.get();
            });

            nextMethodToCall = this::answerForDoubleCannonToActivate;
            currentAskedDoubleCannon = 0;
            askUserForCannonUsage(clientController);
        }
    }

    protected void answerForDoubleCannonToActivate(ActionMessage actionMessage, ClientController clientController){
        String playerAnswer = ((ArrayList<String>) actionMessage.getData("answer")).getFirst();
        if(playerAnswer.equals("y")){
            Coordinates selectedCoordinates = availableDoubleCannons.get(currentAskedDoubleCannon).getFirst();
            doubleCannonToActivate.add(selectedCoordinates);
            availableBatteries--;

            availableDoubleCannons.removeIf((cannonInfo) -> cannonInfo.getFirst().equals(selectedCoordinates));
        }
        currentAskedDoubleCannon++;
        askUserForCannonUsage(clientController);
    }

    protected void sendFirePowerToServer(ClientController clientController){
        ActionMessage messageToServer = new ActionMessage("setFirePower", clientController.getNickname());
        messageToServer.setData("doubleCannonsCoordinates", doubleCannonToActivate);
        messageToServer.setData("batteriesCoordinates", selectedBatteriesCoordinates);

        clientController.getServerProxy().send(messageToServer);
    }

    /* result notification */
    protected void notifyDefeat(ActionMessage actionMessage, ClientController clientController){
        if(actionMessage.getReceiver().equals(clientController.getNickname()))
            clientController.getView().showMessage("You have lost against this enemy!");
    }

    protected void notifyDraw(ActionMessage actionMessage, ClientController clientController){
        if(actionMessage.getReceiver().equals(clientController.getNickname()))
            clientController.getView().showMessage("You have drawn against this enemy! Wait for other players");
    }

    /* model update */
    protected void updateClientModel(ActionMessage actionMessage, ClientController clientController){
        Map<String, ShipBoard> playerShipboards = (Map<String, ShipBoard>) actionMessage.getData("updatedShipboards");
        Map<String, Integer> updatedPositions = (Map<String, Integer>) actionMessage.getData("updatedPositions");
        Map<String, Integer> updatedCredits = (Map<String, Integer>) actionMessage.getData("updatedCredits");

        playerShipboards.forEach((nickname, shipboard)->{
            clientController.getViewFlightBoard().getViewShipBoard(nickname).updateViewShipBoard(shipboard);
        });
        updatedPositions.forEach((nickname, absPosition)->{
            clientController.getViewFlightBoard().getViewShipBoard(nickname).setAbsPosition(absPosition);
        });
        updatedCredits.forEach((nickname, credits)->{
            clientController.getViewFlightBoard().getViewShipBoard(nickname).setPoints(credits);
        });

        clientController.getView().showMessage("Shipboards have been updated!");
        clientController.getView().showShipboard(clientController.getViewFlightBoard().getViewShipBoard(clientController.getNickname()));
    }

    protected void notEnoughResources(ActionMessage actionMessage, ClientController clientController){
        if(actionMessage.getReceiver().equals(clientController.getNickname())){
            clientController.getView().showMessage("You don't have enough enough resources!");
        }
        else{
            clientController.getView().showMessage("Player " + actionMessage.getReceiver() + " does not have enough resources");
        }
    }
}
