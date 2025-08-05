package it.polimi.ingsw.model.cards.server.concrete;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.FlightBoard;
import it.polimi.ingsw.model.ModelPublisher;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.cards.generic.OpenSpaceCard;
import it.polimi.ingsw.model.cards.server.ServerCard;
import it.polimi.ingsw.model.cards.visitor.ServerCardVisitor;
import it.polimi.ingsw.model.states.PlayingState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Open Space Card.
 * <p>each player will advance a number of flight days equals to the engine power.</p>
 * <p>each player can choose whether to activate or not double engines.</p>
 * <p>if a player has no engine power, they lose.</p>
 * @author Francesco Montefusco
 */
public class OpenSpaceServerCard extends ServerCard {
    private OpenSpaceCard cardData;

    private HashMap<Player, Integer> enginePowerMap;

    public OpenSpaceServerCard(String fileName, int level) {
        cardData = new OpenSpaceCard(level, fileName);
        mapMethods.put("setEnginePower", this::setEnginePowerMap);
    }

    /**
     * updates the model accordingly to the state of the card and then sends an actionMessage: <ul>
     *     <li>updatedPositions: hashMap containing player names and their position</li>
     * </ul>
     */
    public void solve(){
        ActionMessage am = new ActionMessage("solveOpenSpace", "server");
        HashMap<String, Integer> updatedPositions = new HashMap<>();
        ArrayList<Player> players = flightBoard.getActivePlayers();
        for(Player player: players){
            player.setAbsPosition(player.getAbsPosition() + enginePowerMap.get(player));
            updatedPositions.put(player.getNickname(), player.getAbsPosition());
        }

        am.setData("updatedPositions", updatedPositions);
        publisher.notify(am);
        endCard(publisher, flightBoard);

    }

    @Override
    public void play(FlightBoard flightBoard, ModelPublisher publisher) {
        enginePowerMap = new HashMap<>();
        cardSetup(flightBoard, publisher);

        ActionMessage am = new ActionMessage("openSpaceCard", "server");
        am.setData("fileName", cardData.getFileName());
        publisher.notify(am);

        askEngineUsage();
    }

    /**
     * Asks users which double engine they want to activate, if any
     */
    protected void askEngineUsage(){
        for(Player player: flightBoard.getActivePlayers()){
            if(!cardNotifier.askEngineUsage(player)){
                enginePowerMap.put(player, (int) player.getShipBoard().getPotentialEnginePowerSingle());
            }
        }
        checkIsOver();
    }



    /**
     * Remote method accessible by the players, allow them to set which engine they want to use
     * @param am action message containing: <ul>
     *           <li>doubleEngineCoordinates: the {@link Coordinates} of the engines selected by the user</li>
     *           <li>batteriesCoordinate: an {@code HashMap} of {@link Coordinates} and {@code Integer}, to know which batteries are to be used</li>
     * </ul>
     */
    public void setEnginePowerMap(ActionMessage am) {
        Player currPlayer = flightBoard.getPlayerByNickname(am.getSender());
        if(enginePowerMap.containsKey(currPlayer)){
            //TODO exception player already answered
        }

        int enginePower = calculateEnginePower((ArrayList<Coordinates>) am.getData("doubleEngineCoordinates"), currPlayer);
        enginePowerMap.put(currPlayer, enginePower);

        ((HashMap<Coordinates, Integer>) am.getData("batteriesCoordinate")).forEach((coordinate, number) -> {
            currPlayer.getShipBoard().decreaseTotalBatteries(coordinate, number);
        });
        checkIsOver();
    }

    /**
     * Calculates the engine power given an array of engine coordinates to activates
     * @param playerActiveEngineCoordinates an {@code ArrayList} of {@code Coordinates} of the double engine activated by the player
     * @param player the {@link Player} that sent the {@code Coordinates}
     * @return the total engine power
     */
    private int calculateEnginePower(ArrayList<Coordinates> playerActiveEngineCoordinates, Player player){
        int enginePower = (int) player.getShipBoard().getPotentialEnginePowerSingle();
        AtomicInteger enginePowerCounter = new AtomicInteger(0);
        Map<Coordinates, Integer> doubleEnginesCoords = player.getShipBoard().getPotentialEnginePowerDouble();

        playerActiveEngineCoordinates.forEach(coordinates -> {
            if(doubleEnginesCoords.containsKey(coordinates)){
                enginePowerCounter.addAndGet(doubleEnginesCoords.get(coordinates));
            }
            else{
                //TODO exception no engine there
            }
        });
        return enginePower + enginePowerCounter.get();
    }

    private void checkIsOver(){
        if(enginePowerMap.keySet().containsAll(flightBoard.getActivePlayers())){
            solve();
        }
    }

    @Override
    public void accept(ServerCardVisitor visitor, ModelPublisher publisher, PlayingState playingState) {
        visitor.visitForOpenSpace(this, publisher, playingState);
    }

    @Override
    public void receive(ActionMessage actionMessage) {
        mapMethods.get(actionMessage.getActionName()).accept(actionMessage);
    }

    @Override
    public String getFileName(){
        return cardData.getFileName();
    }

}
