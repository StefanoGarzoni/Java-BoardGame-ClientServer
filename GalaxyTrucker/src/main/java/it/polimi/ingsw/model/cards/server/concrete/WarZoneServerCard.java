package it.polimi.ingsw.model.cards.server.concrete;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.Cargo.CargoType;
import it.polimi.ingsw.model.cards.generic.WarZoneCard;
import it.polimi.ingsw.model.cards.server.CardNotifier;
import it.polimi.ingsw.model.cards.server.ServerCard;
import it.polimi.ingsw.model.cards.util.FireShot;
import it.polimi.ingsw.model.cards.util.Pair;
import it.polimi.ingsw.model.cards.util.ShotSize;
import it.polimi.ingsw.model.cards.visitor.ServerCardVisitor;
import it.polimi.ingsw.model.states.PlayingState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.function.Consumer;

/**
 * War zone card.
 * <p>Players will be checked for crew, firepower, and engine power, the player with the least of each will receive a penalty.</p>
 * <p>if two players are even, the one ahead in the race gets the penalty.</p>
 * <p>Penalties are:</p><ul>
 *     <li>flight days removed, for the one with the least crew</li>
 *     <li>crew members/cargo removed, for the one with the least firepower</li>
 *     <li>fire shots received, for the one with the least engine power</li>
 * </ul>
 * @author Francesco Montefusco
 */
public class WarZoneServerCard extends ServerCard {
    private WarZoneCard cardData;

    private HashMap<Player, Double> firePowerMap;
    private HashMap<Player, Integer> enginePowerMap;
    private HashMap<Coordinates, CargoType> cargoToRemove;
    private HashMap<Coordinates, Integer> crewToRemove;
    private Player currentPenalizedPlayer;
    private int currentShotIndex;
    private int currentRoll;
    private int currentPhase;
    private HashMap<Player, Coordinates> brokenTileForCurrentShot;

    public WarZoneServerCard(int cargoLoss, int crewLoss, int flightDayLoss, ArrayList<FireShot> shotsList, int level, String fileName) {
        cardData = new WarZoneCard(cargoLoss, crewLoss, flightDayLoss, shotsList, level, fileName);
        mapMethods = new HashMap<>();

        mapMethods.put("setFirePower", this::setFirePower);
        mapMethods.put("setEnginePower", this::setEnginePower);
        mapMethods.put("setCrewToRemove", this::setCrewToRemove);
        mapMethods.put("setCargoToRemove", this::setCargoToRemove);
        mapMethods.put("activateShield", this::activateShield);
    }

    /**
     * Notifies the clients of the crew check
     */
    private void crewCheck(){
        ActionMessage am = new ActionMessage("crewCheck", "server");
        publisher.notify(am);
        findLeastCrew();
        currentPhase++;
    }

    /**
     * Notifies the clients of the firepower check
     */
    private void firePowerCheck(){
        ActionMessage am = new ActionMessage("firePowerCheck", "server");
        publisher.notify(am);
        for(Player player : flightBoard.getActivePlayers()){
            if(!cardNotifier.askCannonsUsage(player)) {
                firePowerMap.put(player, player.getShipBoard().getPotentialFirePowerSingle());
            }
        }
        currentPhase++;
    }

    /**
     * Notifies the clients of the engine power check
     */
    private void enginePowerCheck(){
        ActionMessage am = new ActionMessage("enginePowerCheck", "server");
        publisher.notify(am);
        cardNotifier.askEngineUsage(flightBoard.getActivePlayers()); //FIXME
    }


    /**
     * Remote method accessible by the players, allows them to set their firepower after receiving the message from {@link WarZoneServerCard#firePowerCheck()}
     * @param message an {@link ActionMessage} containing parameters set by the player:<ul>
     *                <li>firePowerCoordinates: the cannons to activate as a list of {@link Coordinates} selected by the user</li>
     *                <li>batteriesCoordinate: a Map of {@link Coordinates} and {@code Integers} for the batteries to remove</li>
     * </ul>
     */
    private void setFirePower(ActionMessage message){
        Player currPlayer = flightBoard.getPlayerByNickname(message.getSender());
        if(firePowerMap.containsKey(currPlayer)){
            //exception
            return;
        }
        else{
            ArrayList<Coordinates> playerActiveDoubCannons = (ArrayList<Coordinates>) message.getData("firePowerCoordinates");
            firePowerMap.put(currPlayer, calculateFirePower(playerActiveDoubCannons, currPlayer));
            ((HashMap<Coordinates, Integer>) message.getData("batteriesCoordinates")).forEach((coordinate, number) -> {
                currPlayer.getShipBoard().decreaseTotalBatteries(coordinate, number);
            });
        }

        //if we have received an answer from all players
        if(flightBoard.getActivePlayers().stream().allMatch(firePowerMap::containsKey)){
            findLeastFirePower();
        }

    }

    /**
     * Remote method accessible by the players, allows them to set their firepower after receiving the message from {@link WarZoneServerCard#enginePowerCheck()}
     * @param message an {@link ActionMessage} containing parameters set by the players: <ul>
     *                <li>enginePower: the engines to activate as a list of {@link Coordinates} selected by the user</li>
     *                <li>batteriesCoordinate: a Map of {@link Coordinates} and {@code Integer} for the batteries to remove</li>
     * </ul>
     */
    private void setEnginePower(ActionMessage message){
        Player currPlayer = flightBoard.getPlayerByNickname(message.getSender());
        if(enginePowerMap.containsKey(currPlayer)){
            //exception
            return;
        }
        else{
            ArrayList<Coordinates> playerActiveDoubEngines = (ArrayList<Coordinates>) message.getData("enginePowerCoordinates");
            enginePowerMap.put(currPlayer, calculateEnginePower(playerActiveDoubEngines, currPlayer));
            ((HashMap<Coordinates, Integer>)message.getData("batteriesCoordinates")).forEach((coordinate, number) -> {
               currPlayer.getShipBoard().decreaseTotalBatteries(coordinate, number);
            });
        }

        if(flightBoard.getActivePlayers().stream().allMatch(enginePowerMap::containsKey)){
            findLeastEnginePower();
        }
    }

    /**
     * Calculates the firepower of the current player given an array of double cannons to activate
     * @param playerActiveCannonCoordinates the double cannons activated by the players
     * @param player the player that is being checked
     * @return the total firepower of the current player
     */
    private double calculateFirePower(ArrayList<Coordinates> playerActiveCannonCoordinates, Player player){
        double firePower = player.getShipBoard().getPotentialFirePowerSingle();
        DoubleAdder firePowerAdder = new DoubleAdder();
        Map<Coordinates, Integer> doubleCannonsCoord = player.getShipBoard().getPotentialFirePowerDouble();

        playerActiveCannonCoordinates.forEach(coord -> {
            if(doubleCannonsCoord.containsKey(coord)){
                firePowerAdder.add(doubleCannonsCoord.get(coord));
            }
            else{
                //TODO exception there is no cannon there
            }
        });
        return firePower + firePowerAdder.sum();
    }

    /**
     * Calculates the firepower of the current player given an array of double engines to activate
     * @param playerActiveEngineCoordinates the double engine activated by the players
     * @param player the player that is being checked
     * @return the total engine power of the current player
     */
    private int calculateEnginePower(ArrayList<Coordinates> playerActiveEngineCoordinates, Player player){
        int enginePower = (int) player.getShipBoard().getPotentialEnginePowerSingle();
        AtomicInteger enginePowerCounter = new AtomicInteger(0);
        Map<Coordinates, Integer> doubleEngineCoord = player.getShipBoard().getPotentialEnginePowerDouble();

        playerActiveEngineCoordinates.forEach(coord -> {
            if(doubleEngineCoord.containsKey(coord)){
                enginePowerCounter.addAndGet(doubleEngineCoord.get(coord));
            }
            else{
                //TODO exception there is no engine there
            }
        });
        return enginePower + enginePowerCounter.get();
    }

    /**
     * Remote method accessible by the player that lost the firepower check
     * @param message an {@link ActionMessage} containing parameters set by the player: <ul>
     *                <li>crewMap: an {@code HashMap} that contains pairs of {@link Coordinates} and {@code Integer} to remove the crew as required</li>
     * </ul>
     */
    private void setCrewToRemove(ActionMessage message){
        Player currPlayer = flightBoard.getPlayerByNickname(message.getSender());
        if(currPlayer != currentPenalizedPlayer){
            //TODO Exception, wrong player answered
            return;
        }
        crewToRemove = (HashMap<Coordinates, Integer>) message.getData("crewMap");
        solveLeastFirePower();
    }

    /**
     * Remote method accessible by the player that lost the firepower check
     * @param message an {@link ActionMessage} containing parameters set by the player: <ul>
     *                <li>cargoMap: an {@code HashMap} that contains pairs of {@link Coordinates} and {@link CargoType} to remove the cargos as required</li>
     * </ul>
     */
    private void setCargoToRemove(ActionMessage message){
        Player currPlayer = flightBoard.getPlayerByNickname(message.getSender());
        if(currPlayer != currentPenalizedPlayer){
            //TODO Exception, wrong player answered
            return;
        }
        cargoToRemove = (HashMap<Coordinates, CargoType>) message.getData("cargoMap");
        solveLeastFirePower();
    }

    /**
     * Notify the player that lost the engine power check of the current shot, expecting a call to {@link WarZoneServerCard#activateShield(ActionMessage)}
     */
    private void sendCurrentShot(){
        ActionMessage am = new ActionMessage("sendShot", "server");
        Die die = new Die(6);
        currentRoll = die.roll() + die.roll();
        am.setData("currentShotIndex", currentShotIndex);
        am.setData("roll", currentRoll);
        am.setReceiver(currentPenalizedPlayer.getNickname());
        //We send shields only if the shot is small
        if(cardData.getShotsList().get(currentShotIndex).getSize() == ShotSize.SMALL) cardNotifier.appendPossibleShields(currentPenalizedPlayer, am);
        publisher.notify(am);
    }

    /**
     * Remote method accessible by the player that lost the engine power check
     * @param am an {@link ActionMessage} containing parameters set by the player: <ul>
     *           <li>doesActivate: whether a shield is activated or not</li>
     * </ul>
     */
    private void activateShield(ActionMessage am){
        boolean activateShield = (boolean) am.getData("doesActivate");
        Coordinates battery = (Coordinates) am.getData("batteryCoordinates");

        Player currPlayer = flightBoard.getPlayerByNickname(am.getReceiver());
        FireShot currentShot = cardData.getShotsList().get(currentShotIndex);

        currPlayer.getShipBoard().decreaseTotalBatteries(battery, 1);

        if(currPlayer != currentPenalizedPlayer){
            //TODO exception, wrong player answered
            return;
        }
        Pair<Direction, Direction> shieldDir = (Pair<Direction, Direction>) am.getData("shieldDirections");

        if(!activateShield) {
            manageShot();
        }
        else if(currentShot.getDirection() != shieldDir.getFirst() && currentShot.getDirection() != shieldDir.getSecond()){
            manageShot();
        }
        else if(currentShot.getSize() == ShotSize.BIG){
            manageShot();
        }

        currentShotIndex++;
        if(cardData.getShotsList().size() == currentShotIndex){
            endCard(publisher, flightBoard);
        }
        sendCurrentShot();
    }

    /**
     * checks the result of a shot, and manages whether the trunk exits or not
     */
    private void manageShot(){
        FireShot currentShot = cardData.getShotsList().get(currentShotIndex);

        Pair<Coordinates,ArrayList<ArrayList<Coordinates>>> trunks = currentShot.hitTile(currentPenalizedPlayer, currentRoll);
        if(trunks != null){
            cardNotifier.notifyTrunk(trunks.getSecond(), currentPenalizedPlayer);
        }


    }

    /**
     * Obtains the player with the least crew members onboard and sends them an ActionMessage with the player as receiver
     */
    private void findLeastCrew(){
        ArrayList<Player> players = flightBoard.getActivePlayers();
        int currLeastCrew = Integer.MAX_VALUE;
        Player currPlayerToPenalize = null;
        for(Player player : players){
            if(player.getShipBoard().getTotalCrew() < currLeastCrew){
                currLeastCrew = player.getShipBoard().getTotalCrew();
                currPlayerToPenalize = player;
            }
        }
        currentPenalizedPlayer = currPlayerToPenalize;
        ActionMessage am = new ActionMessage("leastCrewPlayer", "server");
        am.setReceiver(currPlayerToPenalize.getNickname());
        publisher.notify(am);
        solveLeastCrew();
        firePowerCheck();

    }

    /**
     * Obtains the player with the least firepower
     */
    private void findLeastFirePower(){
         currentPenalizedPlayer = Collections.min(firePowerMap.entrySet(), Map.Entry.comparingByValue()).getKey();
         ActionMessage am = new ActionMessage("leastFirePower", "server");
         am.setReceiver(currentPenalizedPlayer.getNickname());

         if(cardData.getCrewLoss() > cardData.getCargoLoss()){
             removeCargo(cardData.getCargoLoss(), currentPenalizedPlayer);
         }
         else{
             cardNotifier.appendCrewCabins(currentPenalizedPlayer, am); // FIXME
         }

         solveLeastFirePower();
         enginePowerCheck();
    }

    /**
     * Obtains the player with the least engine power
     */
    private void findLeastEnginePower(){
        currentPenalizedPlayer = Collections.min(enginePowerMap.entrySet(), Map.Entry.comparingByValue()).getKey();
        sendCurrentShot();
    }

    /**
     * Apply the penalties for the one with the least crew and notify the clients
     */
    private void solveLeastCrew(){
        flightBoard.movePlayerInFlightBoard(currentPenalizedPlayer, cardData.getFlightDayLoss());
        ActionMessage am = new ActionMessage("solveLeastCrew", "server");
        am.setReceiver(currentPenalizedPlayer.getNickname());
        am.setData("absolutePosition", currentPenalizedPlayer.getAbsPosition());
        publisher.notify(am);
    }

    /**
     * Apply the penalties for the one with the least firepower and notify the clients
     */
    private void solveLeastFirePower(){
        Player playerToPenalize = flightBoard.getPlayerByNickname(currentPenalizedPlayer.getNickname());
        if(cardData.getCargoLoss() > cardData.getCrewLoss()) {
            cargoToRemove.forEach((coordinate, cargo) -> {
                playerToPenalize.getShipBoard().increaseCargo(coordinate, cargo, true);
            });
        }
        else if(cardData.getCrewLoss() > cardData.getCargoLoss()) {
            crewToRemove.forEach((coordinate, number) -> {
                playerToPenalize.getShipBoard().decreaseCrewInCabin(coordinate, number);
            });
        }
        else{
            //todo exception
        }
    }


    @Override
    public void play(FlightBoard flightBoard, ModelPublisher publisher) {
        cardSetup(flightBoard, publisher);
        ActionMessage am = new ActionMessage("WarZoneCard", "server");
        am.setData("fileName", cardData.getFileName());
        brokenTileForCurrentShot = new HashMap<>();
        firePowerMap = new HashMap<>();
        enginePowerMap = new HashMap<>();

        publisher.notify(am);
        currentPhase = 0;

        crewCheck();
    }

    @Override
    public void accept(ServerCardVisitor visitor, ModelPublisher publisher, PlayingState playingState) {
        visitor.visitForWarzone(this, publisher, playingState);
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
