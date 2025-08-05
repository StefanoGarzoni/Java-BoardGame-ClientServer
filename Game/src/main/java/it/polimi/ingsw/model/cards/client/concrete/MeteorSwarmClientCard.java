package it.polimi.ingsw.model.cards.client.concrete;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.client.ClientController;
import it.polimi.ingsw.client.View;
import it.polimi.ingsw.model.ComponentTile.Connector;
import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.Direction;
import it.polimi.ingsw.model.ShipBoard;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.client.ClientCard;
import it.polimi.ingsw.model.cards.generic.MeteorSwarmCard;
import it.polimi.ingsw.model.cards.util.MeteorShot;
import it.polimi.ingsw.model.cards.util.Pair;
import it.polimi.ingsw.model.clientModel.ViewShipBoard;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MeteorSwarmClientCard extends ClientCard {
    private final MeteorSwarmCard cardData;

    private int availableBatteries;
    private HashMap<Coordinates, Integer> batteryTilesAvailability;

    private int currentAskedShieldToActivate;
    private final ArrayList<Pair<Coordinates, Pair<Direction, Direction>>> availableShields = new ArrayList<>();

    private int currentAskedDoubleCannon;
    private final ArrayList<Pair<Coordinates, Integer>> availableDoubleCannons = new ArrayList<>();
    private Coordinates doubleCannonToActivate = null;

    private Pair<Direction, Direction> shieldsToActivate = null;
    private Coordinates batteryCoordinates = null;

    private int currentBranch = 0;
    private ArrayList<ArrayList<Coordinates>> branches;

    private BiConsumer<ActionMessage, ClientController> nextMethodToCall;
    private Consumer<ClientController> toServerSenderMethod;

    /* constructors */

    public MeteorSwarmClientCard(MeteorSwarmCard meteorSwarmCard){ cardData = meteorSwarmCard; }

    public MeteorSwarmClientCard(ArrayList<MeteorShot> meteorList, int cardLevel, String fileName) {
        cardData = new MeteorSwarmCard(meteorList, cardLevel, fileName);
    }

    /* service methods */

    @Override
    public String getFileName() { return cardData.getFileName();}

    @Override
    public void run(ClientController clientController) {
        clientController.getView().showMessage("Meteor Swarm card has been drawn!");
        clientController.getView().showCard(this.cardData);

        methodsFromServerMap.put("meteorShot", this::handleMeteorShot);
        methodsFromServerMap.put("appendPossibleShields", this::handlePossibleShields);
        methodsFromServerMap.put("branchingResolve", this::resolveBranches);
        methodsFromServerMap.put("destroyTile", this::updateDestroyedShipboard);
        methodsFromServerMap.put("askCannonsUsage", this::askForDoubleCannonToActivate);
        methodsFromServerMap.put("notEnoughResources", this::notEnoughResources);


        methodsFromViewMap.put("answer", this::userAnswerSorter);

        batteryTilesAvailability = new HashMap<>();
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
            clientController.getView().showErrorMessage("Wrong answer " + actionMessage.getData("answer"));
        }
    }

    @Override
    public void processFromServer(ActionMessage actionMessage, ClientController clientController){
        try {
            methodsFromServerMap.get(actionMessage.getActionName()).accept(actionMessage, clientController);
        } catch (Exception e) {
            System.err.println(actionMessage.getActionName());
            e.printStackTrace();
        }
    }
    @Override
    public void processFromClient(ActionMessage actionMessage, ClientController clientController){
        try {
            this.methodsFromViewMap.get(actionMessage.getActionName()).accept(actionMessage, clientController);
        } catch (Exception e) {
            System.err.println(actionMessage.getActionName());
            e.printStackTrace();
        }
    }

    /* meteor shots handling */

    private void handleMeteorShot(ActionMessage actionMessage, ClientController clientController){
        int currentMeteorIndex = actionMessage.getInt("meteorIndex");
        int currentMeteorRoll = actionMessage.getInt("currentMeteorRoll");

        MeteorShot currentMeteor = cardData.getMeteorList().get(currentMeteorIndex);

        clientController.getView().showMessage(
                "Your n."+ currentMeteorIndex +"/"+cardData.getMeteorList().size()+" "+currentMeteor.getSize()
                        +" from "+currentMeteor.getDirection().name()+" meteor's dice roll is: "+ currentMeteorRoll
        );
    }

    private void sendShieldsDecision(ClientController clientController){
        ActionMessage messageToServer;

        if(shieldsToActivate != null){
            messageToServer = new ActionMessage("activateShield", clientController.getNickname());
            messageToServer.setData("shieldDirections", shieldsToActivate);
            messageToServer.setData("batteryCoordinates", batteryCoordinates);
        }
        else{
            messageToServer = new ActionMessage("doNothing", clientController.getNickname());
        }

        clientController.getServerProxy().send(messageToServer);
    }

    /* shields activation */
    private void handlePossibleShields(ActionMessage actionMessage, ClientController clientController){
        if(actionMessage.getReceiver().equals(clientController.getNickname())){
            batteryTilesAvailability.clear();
            availableShields.clear();

            Map<Coordinates, Map<String, Map<Direction[], Connector[]>>> allShieldsInformation = (Map<Coordinates, Map<String, Map<Direction[], Connector[]>>>) actionMessage.getData("possibleShields");
            Map<Coordinates, Map<Integer, Map<String, Connector[]>>> allBatteriesInformation = (Map<Coordinates, Map<Integer, Map<String, Connector[]>>>)  actionMessage.getData("batteries");


            // extracting batteries information
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
            //askUserForBatteriesUsage(clientController);
            toServerSenderMethod.accept(clientController);

        }
    }

     private void userAnswerForShieldsActivation(ActionMessage actionMessage, ClientController clientController){
        ArrayList<String> userAnswers = (ArrayList<String>) actionMessage.getData("answer");
        if(!userAnswers.isEmpty()){
            String userAnswer = userAnswers.getFirst();

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
    }

    /* battery usage */

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

        batteryCoordinates = selectedCoordinates;
        toServerSenderMethod.accept(clientController);
    }

    private void askUserForBatteriesUsage(ClientController clientController){
        AtomicReference<String> messageToShow = new AtomicReference<>("Select coordinates [<x> <y>] from which extract the battery. " +
                "Available battery at coordinates: ");

        batteryTilesAvailability.forEach((coordinates, availableBatteries) -> {
            if(availableBatteries > 0){
                messageToShow.updateAndGet(v -> v + coordinates + " / ");
            }
        });

        clientController.getView().showMessage(messageToShow.get());
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

    /* shipboard updating after a shot */

    private void updateDestroyedShipboard(ActionMessage actionMessage, ClientController clientController){
        String hitPlayerNickname = actionMessage.getReceiver();
        int currentMeteorIndex = actionMessage.getInt("meteorIndex");

        ShipBoard playerShipboard = (ShipBoard) actionMessage.getData("shipBoard");

        clientController.getViewFlightBoard().getViewShipBoard(hitPlayerNickname).updateViewShipBoard(playerShipboard);

        if(hitPlayerNickname.equals(clientController.getNickname())){
            ViewShipBoard userShipBoard = clientController.getViewFlightBoard().getViewShipBoard(hitPlayerNickname);

            clientController.getView().showShipboard(userShipBoard);

            String messageToPlayer= "This is you shipboard after meteor n. "+currentMeteorIndex;
            clientController.getView().showMessage(messageToPlayer);
        }
    }

    /* Ask cannon usage */

    /** Shows a messages to the client, asking if he wants to activate double cannon, until he has
     * no more batteries or no more double cannon to activate.
     *
     * @param clientController controller that contains a view reference
     */
    private void askUserForCannonUsage(ClientController clientController){
        if(currentAskedDoubleCannon < availableDoubleCannons.size()){
            clientController.getView().showMessage(
                    "You can activate "+availableDoubleCannons.size()+" double cannon and you have "+availableBatteries+"."+
                            "Do you want [y/n] to activate cannon at coordinates: "+availableDoubleCannons.get(currentAskedDoubleCannon).getFirst()+
                            " with " + availableDoubleCannons.get(currentAskedDoubleCannon).getSecond()+" firepower?"
            );
        }
        else {
            clientController.getView().showMessage(
                    "You have chosen to not use double cannon"
            );

            sendFirePowerToServer(clientController);
        }
    }

    /** This method is called when server sends an ActionMessage "firePowerCheck"
     *
     * @param actionMessage sent by the server
     * @param clientController controller of the client
     */
    void askForDoubleCannonToActivate(ActionMessage actionMessage, ClientController clientController){
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

    private void answerForDoubleCannonToActivate(ActionMessage actionMessage, ClientController clientController){
        String playerAnswer = ((ArrayList<String>) actionMessage.getData("answer")).getFirst();
        if(playerAnswer.equals("y")){
            doubleCannonToActivate = availableDoubleCannons.get(currentAskedDoubleCannon).getFirst();
            availableBatteries--;

            nextMethodToCall = this::answerForBatteriesUsage;
            toServerSenderMethod = this::sendFirePowerToServer;
            askUserForBatteriesUsage(clientController);
        }
        else{
            currentAskedDoubleCannon++;
            askUserForCannonUsage(clientController);
        }
    }

    void sendFirePowerToServer(ClientController clientController){
        ActionMessage messageToServer;
        if(doubleCannonToActivate != null && batteryCoordinates != null){
            messageToServer = new ActionMessage("activateCannon", clientController.getNickname());
            messageToServer.setData("cannonCoordinate", doubleCannonToActivate);
            messageToServer.setData("batteryCoordinate", batteryCoordinates);
        }else {
            messageToServer = new ActionMessage("doNothing", clientController.getNickname());
        }

        clientController.getServerProxy().send(messageToServer);
    }

    void notEnoughResources(ActionMessage actionMessage, ClientController clientController){
        if(actionMessage.getReceiver().equals(clientController.getNickname())){
            clientController.getView().showMessage("You don't have not enough resources");
        }
        else {
            clientController.getView().showMessage("Player " + clientController.getNickname() + " does not have enough resources");
        }
    }
}
