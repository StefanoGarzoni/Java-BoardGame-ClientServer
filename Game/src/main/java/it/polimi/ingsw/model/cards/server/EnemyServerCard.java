package it.polimi.ingsw.model.cards.server;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.FlightBoard;
import it.polimi.ingsw.model.ModelPublisher;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.cards.util.CheckResult;
import it.polimi.ingsw.model.cards.visitor.ServerCardVisitor;
import it.polimi.ingsw.model.states.PlayingState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.DoubleAdder;

public abstract class EnemyServerCard extends ServerCard {
    protected ArrayList<Player> defeatedPlayers;
    protected Player winnerPlayer;
    protected Player currentPlayer;
    protected HashMap<Coordinates, Integer> currPlayerBatteriesCoordinates;
    protected double currentPlayerFirePower;

    protected int requiredFirePower;

    protected EnemyServerCard() {
        mapMethods.put("setFirePower", this::setFirePower);
        mapMethods.put("claimPrize", this::claimPrize);
    }

    public abstract void assignPenalty(Player player, FlightBoard flightBoard);
    public abstract void assignPrize(Player player, FlightBoard flightBoard);

    /**
     * Enemy specific configuration
     */
    protected abstract void initEnemy();

    protected boolean isEnemyOver(){
        return (winnerPlayer == null && defeatedPlayers.isEmpty());
    }

    private void askFirePower(){
        if(!cardNotifier.askCannonsUsage(currentPlayer)){
            currentPlayerFirePower = currentPlayer.getShipBoard().getPotentialFirePowerSingle();
            if(checkPlayer(currentPlayer) != CheckResult.GREATER && currentPlayer != flightBoard.getActivePlayers().getLast()) {
                int newIndex = flightBoard.getActivePlayers().indexOf(currentPlayer) + 1;
                currentPlayer = flightBoard.getActivePlayers().get(newIndex);
                askFirePower();
            }
            else if(currentPlayer == flightBoard.getActivePlayers().getLast()){
                solve();
            }
        }
    }

    /**
     * Remote method accessible for the current player, sets the firepower usage
     * @param message an actionMessage for the player to pass data to: <ul>
     *                <il>doubleCannonsCoordinates: the player chosen double cannons</il>
     *                <il>batteriesCoordinates: an arrayList of battery coordinates</il>
     * </ul>
     */
    private void setFirePower(ActionMessage message) {
        if(flightBoard.getPlayerByNickname(message.getSender()) != currentPlayer) {
            //TODO Exception
            return;
        }
        currentPlayerFirePower = calculateFirePower((ArrayList<Coordinates>) message.getData("doubleCannonsCoordinates"));
        currPlayerBatteriesCoordinates = (HashMap<Coordinates, Integer>) message.getData("batteriesCoordinates");
        currPlayerBatteriesCoordinates.forEach((coordinates, integer) -> {
            currentPlayer.getShipBoard().decreaseTotalBatteries(coordinates, integer);
        });
        checkPlayer(currentPlayer);
        if(winnerPlayer != null || currentPlayer == flightBoard.getActivePlayers().getLast()){
            solve();
        }
        else {
            int newIndex = flightBoard.getActivePlayers().indexOf(currentPlayer) + 1;
            currentPlayer = flightBoard.getActivePlayers().get(newIndex);
            askFirePower();
        }
    }

    /**
     * Calculates the firepower given an array of cannons to activate
     * @param currPlayerActiveCannonCoordinates the double cannons activated by the players
     * @return the total firepower for the active player
     */
    private double calculateFirePower(ArrayList<Coordinates> currPlayerActiveCannonCoordinates) {
        double firePower = currentPlayer.getShipBoard().getPotentialFirePowerSingle();
        DoubleAdder firePowerAdder = new DoubleAdder();
        Map<Coordinates, Integer> doubleCannonsCoord = currentPlayer.getShipBoard().getPotentialFirePowerDouble();

        currPlayerActiveCannonCoordinates.forEach(coordinates -> {
                   if(doubleCannonsCoord.containsKey(coordinates)) {
                       firePowerAdder.add((double) doubleCannonsCoord.get(coordinates));
                   }
                   else{
                       //TODO exception there is no cannon there
                   }
                });
        return firePower + firePowerAdder.sum();
    }

    /**
     * checks how a player does against the enemy
     * @param player the player to test
     * @return GREATER if he defeats the enemy, LESS if he gets defeated, and EQUALS if it's a draw
     */
    private CheckResult checkPlayer(Player player){
        if(currentPlayerFirePower < requiredFirePower){
            defeatedPlayers.add(player);
            notifyDefeat();
            return CheckResult.LESS;
        }
        else if (currentPlayerFirePower > requiredFirePower){
            winnerPlayer = player;
            notifyDraw();
            return CheckResult.GREATER;
        }
        return CheckResult.EQUALS;
    }

    protected void notifyUpdates(){
        ActionMessage message = new ActionMessage("notifyEnemyUpdate", "server");

        Map<String, ShipBoard> updatedShipboards = new HashMap<>();
        Map<String, Integer> updatedPositions = new HashMap<>();
        Map<String, Integer> updatedCredits = new HashMap<>();
        for(Player player : flightBoard.getActivePlayers()){
            updatedShipboards.put(player.getNickname(), player.getShipBoard());
            updatedPositions.put(player.getNickname(), player.getAbsPosition());
            updatedCredits.put(player.getNickname(),player.getCredits());
        }

        message.setData("updatedShipboards", updatedShipboards);
        message.setData("updatedPositions", updatedPositions);
        message.setData("updatedCredits", updatedCredits);
        publisher.notify(message);
    }

    private void notifyDefeat(){
        ActionMessage am = new ActionMessage("notifyDefeat", "server");
        am.setReceiver(currentPlayer.getNickname());
        publisher.notify(am);
    }

    private void notifyDraw(){
        ActionMessage am = new ActionMessage("notifyDraw", "server");
        am.setReceiver(currentPlayer.getNickname());
        publisher.notify(am);
    }

    protected void askWinner(){
        ActionMessage am = new ActionMessage("askWinner", "server");
        am.setReceiver(winnerPlayer.getNickname());
        publisher.notify(am);
    }

    public void claimPrize(ActionMessage message){
        if(!message.getSender().equals(winnerPlayer.getNickname())) {
            //TODO Exception wrong player;
        }
        boolean answer = (boolean) message.getData("doesClaim");
        if(answer) assignPrize(winnerPlayer, flightBoard);
        else winnerPlayer = null;

    }

    /**
     * modify the model accordingly to the card state
     */
    protected void solve(){
        for(Player player: defeatedPlayers){
            assignPenalty(player, flightBoard);
        }
        if(winnerPlayer != null){
            askWinner();
        }
    }

    protected void endEnemy(){
        if(winnerPlayer != null || !defeatedPlayers.isEmpty()){
            //TODO exception missing player answer
        }
        notifyUpdates();
        endCard(publisher, flightBoard);
    }

    @Override
    public void play(FlightBoard flightBoard, ModelPublisher publisher){
        cardSetup(flightBoard, publisher);
        ArrayList<Player> players = flightBoard.getActivePlayers();
        currentPlayer = players.getFirst();
        defeatedPlayers = new ArrayList<>();
        winnerPlayer = null;
        initEnemy();

        askFirePower();
    }

    @Override
    public void accept(ServerCardVisitor visitor, ModelPublisher publisher, PlayingState playingState) {
        visitor.visitForEnemy(this, publisher, playingState);
    }

    @Override
    public void receive(ActionMessage actionMessage) {
        mapMethods.get(actionMessage.getActionName()).accept(actionMessage);
    }
}