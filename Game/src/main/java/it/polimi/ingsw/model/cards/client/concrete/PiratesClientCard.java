package it.polimi.ingsw.model.cards.client.concrete;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.client.ClientController;
import it.polimi.ingsw.client.View;
import it.polimi.ingsw.model.ComponentTile.Connector;
import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.Direction;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.generic.PiratesCard;
import it.polimi.ingsw.model.cards.util.FireShot;
import it.polimi.ingsw.model.cards.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PiratesClientCard extends EnemyClientCard{
    private final PiratesCard cardData;

    private Pair<Direction, Direction> shieldsToActivate = null;
    private Coordinates batteryCoordinates = null;

    private int availableBatteries;
    private HashMap<Coordinates, Integer> batteryTilesAvailability = new HashMap<>();

    private int currentAskedShieldToActivate;
    private final ArrayList<Pair<Coordinates, Pair<Direction, Direction>>> availableShields = new ArrayList<>();

    private BiConsumer<ActionMessage, ClientController> nextMethodToCall;
    private Consumer<ClientController> toServerSenderMethod;

    private int currentBranch = 0;
    private ArrayList<ArrayList<Coordinates>> branches;

    /* constructor */

    public PiratesClientCard(ArrayList<FireShot> shotPenalty, int creditPrize, int flightDayLoss, int requiredFirePower,
                      int cardLevel, String fileName) {
        super();
        cardData = new PiratesCard(shotPenalty, creditPrize, flightDayLoss, requiredFirePower, cardLevel, fileName);
    }

    /* Service methods */

    @Override
    public String getFileName() { return cardData.getFileName(); }

    @Override
    public void run(ClientController clientController) {
        clientController.getView().showMessage("Pirates card has been drawn!");
        clientController.getView().showCard(this.cardData);

        methodsFromServerMap.put("currentShot", this::handleShot);
        methodsFromServerMap.put("appendPossibleShields", this::handlePossibleShields);
        methodsFromServerMap.put("askWinner", this::communicateWinningPrize);
        methodsFromServerMap.put("branchingResolve", this::resolveBranches);

        methodsFromViewMap.put("answer", this::userAnswerSorter);
    }

    @Override
    public void processFromServer(ActionMessage actionMessage, ClientController clientController){
        try {
            this.methodsFromServerMap.get(actionMessage.getActionName()).accept(actionMessage, clientController);
        } catch (Exception e) {
            System.err.println(actionMessage.getActionName());
            e.printStackTrace();
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
            // TODO: error message
            clientController.getView().showErrorMessage("Invalid command");
        }
    }

    @Override
    public Card getCardData() {
        return cardData;
    }

    /* Shots handling */

    private void handleShot(ActionMessage actionMessage, ClientController clientController){
        if(actionMessage.getReceiver().equals(clientController.getNickname())){
            int currentShotIndex = actionMessage.getInt("shotIndex");
            int currentMeteorRoll = actionMessage.getInt("currentRoll");

            FireShot currentShot = cardData.getShotPenalty().get(currentShotIndex);

            clientController.getView().showMessage(
                    "Your n."+ currentShotIndex +"/"+cardData.getShotPenalty().size()+" "+currentShot.getSize()
                            +" from "+currentShot.getDirection().name()+" meteor's dice roll is: "+ currentMeteorRoll
            );

            handlePossibleShields(actionMessage, clientController);
        }
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

            // extracting shields information
            allShieldsInformation.forEach(((coordinates, otherInfos) -> {
                otherInfos.forEach((fileName, lastInfos) -> {
                    lastInfos.forEach(((directions, connectors) -> {
                        availableShields.add(new Pair<>(coordinates, new Pair<>(directions[0], directions[1])));
                    }));
                });
            }));

            // extracting batteries information
            allBatteriesInformation.forEach(((coordinates, otherInfos) -> {
                otherInfos.forEach((batteriesQuantity, lastInfos) -> {
                    batteryTilesAvailability.put(coordinates, batteriesQuantity);
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
            nextMethodToCall = this::answerForShieldsBatteriesUsage;
            toServerSenderMethod = this::sendShieldsDecision;
            askUserForShieldsBatteriesUsage(clientController);
        }
    }

    private void userAnswerForShieldsActivation(ActionMessage actionMessage, ClientController clientController){
        String userAnswer = ((ArrayList<String>) actionMessage.getData("answer")).getFirst();

        if(userAnswer.equals("y")){
            shieldsToActivate = availableShields.get(currentAskedShieldToActivate).getSecond();

            toServerSenderMethod = this::sendShieldsDecision;
            nextMethodToCall = this::answerForShieldsBatteriesUsage;
            askUserForShieldsBatteriesUsage(clientController);
        }
        else{
            currentAskedShieldToActivate++;
            askUserForShieldsActivation(clientController);
        }
    }

    /* battery usage */

    private void answerForShieldsBatteriesUsage(ActionMessage actionMessage, ClientController clientController){
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
            askUserForShieldsBatteriesUsage(clientController);
            return;
        }

        batteryCoordinates = selectedCoordinates;
        toServerSenderMethod.accept(clientController);
    }

    private void askUserForShieldsBatteriesUsage(ClientController clientController){
        AtomicReference<String> messageToShow = new AtomicReference<>("Select coordinates [<x> <y>] from which extract the battery." +
                "Available battery at coordinates: ");

        batteryTilesAvailability.forEach((coordinates, availableBatteries) -> {
            if(availableBatteries > 0){
                messageToShow.updateAndGet(v -> v + coordinates + " / ");
            }
        });

        clientController.getView().showMessage(messageToShow.get());
    }

    /* prize handling */

    private void askPlayerToClaimPrize(ClientController clientController){
        String messageToSend = "You have won Pirates! Do you accept [y/n] to loose "+cardData.getFlightDayLoss()+
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
            currentBranch = 0;
            askUserToSolveBranches(clientController);
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
}
