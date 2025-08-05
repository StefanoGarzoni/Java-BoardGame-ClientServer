package it.polimi.ingsw.model.cards.server.concrete;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.ComponentTile.CannonTile;
import it.polimi.ingsw.model.ComponentTile.Connector;
import it.polimi.ingsw.model.ComponentTile.FixedComponentTile;
import it.polimi.ingsw.model.cards.generic.MeteorSwarmCard;
import it.polimi.ingsw.model.cards.server.CardNotifier;
import it.polimi.ingsw.model.cards.server.ServerCard;
import it.polimi.ingsw.model.cards.util.MeteorShot;
import it.polimi.ingsw.model.cards.util.Pair;
import it.polimi.ingsw.model.cards.util.ShotSize;
import it.polimi.ingsw.model.cards.visitor.ServerCardVisitor;
import it.polimi.ingsw.model.states.PlayingState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Meteor card,
 * Sends meteors to all players
 * <p>meteors are instances of {@link MeteorShot}</p>
 * @author Francesco Montefusco
 */
public class MeteorSwarmServerCard extends ServerCard {
    private MeteorSwarmCard cardData;
    private Die die;

    private int currentPlayerIndex;
    private int currentMeteorIndex;
    private HashMap<Player, Boolean> playerSafetyForCurrentShot;
    private HashMap<Player, Coordinates> brokenTileForCurrentShot;
    private int rollForCurrentShot;


    public MeteorSwarmServerCard(String fileName, int level, ArrayList<MeteorShot> meteorList) {
        cardData = new MeteorSwarmCard(meteorList, level, fileName);
        die = new Die(6);
        mapMethods.put("activateShield", this::activateShield);
        mapMethods.put("activateCannon", this::activateCannon);
        mapMethods.put("doNothing", this::doNothing);
        mapMethods.put("resolveBranching", this::resolveBranching);
    }

    /**
     * Remote method accessible by the players to accept their fate for the current shot
     * @param message to set the sender player as vulnerable
     */
    private void doNothing(ActionMessage message) {
        Player currPlayer = flightBoard.getPlayerByNickname(message.getSender());
        if(playerSafetyForCurrentShot.containsKey(currPlayer)) {
            //TODO exception already did something
        }
        playerSafetyForCurrentShot.put(currPlayer, false);
        checkAnswers();
    }

    /**
     * Remote method accessible by players to activate a shield for the current shot
     * @param am contains parameters set by the player: <ul>
     *           <il>shieldDirections: a {@link Pair} of {@link Direction} to know which sides are covered</il>
     *           <il>batteryCoordinates: Coordinate of the battery spent to activate the shield</il>
     * </ul>
     */
    private void activateShield(ActionMessage am){
        Pair<Direction, Direction> shieldDirection = (Pair<Direction, Direction>) am.getData("shieldDirections");
        ArrayList<MeteorShot> meteorShots = cardData.getMeteorList();
        Player currPlayer = flightBoard.getPlayerByNickname(am.getSender());
        MeteorShot currentShot = meteorShots.get(currentMeteorIndex);

        Coordinates batteryCoordinate = (Coordinates) am.getData("batteryCoordinates");
        currPlayer.getShipBoard().decreaseTotalBatteries(batteryCoordinate, 1);

        //Only cannons can defend big meteors
        if(currentShot.getSize() == ShotSize.BIG){
            playerSafetyForCurrentShot.put(currPlayer, false);
        }
        //Shield does not cover the direction
        else if(meteorShots.get(currentMeteorIndex).getDirection() != shieldDirection.getFirst() &&
                meteorShots.get(currentMeteorIndex).getDirection() != shieldDirection.getSecond()) {
            playerSafetyForCurrentShot.put(currPlayer, false);
        }
        else {
            playerSafetyForCurrentShot.put(currPlayer, true);
        }
        checkAnswers();
    }

    /**
     * Check that all player have answered
     */
    private void checkAnswers(){
        if(flightBoard.getActivePlayers().stream().allMatch(playerSafetyForCurrentShot::containsKey)) {
            hitMeteor();

            if(currentMeteorIndex >= cardData.getMeteorList().size()-1){
                endCard(publisher, flightBoard);
            }
            else{
                sendCurrentMeteor();
            }
        }
    }

    /**
     * Called when all players have answered for the current shot, rolls the dice and hits the player that haven't defended
     */
    private void hitMeteor(){
        playerSafetyForCurrentShot.forEach((player, isShielded) -> {
            if(isShielded){return;}
            else {
                MeteorShot meteor = cardData.getMeteorList().get(currentMeteorIndex);
                //Simulate hit, if it truncates, ask player, otherwise just notify
                Pair<Coordinates, ArrayList<ArrayList<Coordinates>>> trunks = meteor.hitTile(player, rollForCurrentShot);
                if(trunks.getFirst() != null) {
                    brokenTileForCurrentShot.put(player, trunks.getFirst());
                }
                if(trunks.getSecond() != null){
                    cardNotifier.notifyTrunk(trunks.getSecond(), player);
                }

                sendAsteroidHit(rollForCurrentShot, player);
            }
        });
        currentMeteorIndex++;
    }

    public void resolveBranching(ActionMessage am){
        Player player = flightBoard.getPlayerByNickname(am.getReceiver());
        Coordinates branchToKeep = (Coordinates) am.getData("branchToKeep");
        Coordinates hitTile = brokenTileForCurrentShot.get(player); //FIXME

        player.getShipBoard().delBranch(branchToKeep, hitTile);
    }

    /**
     * Remote method accessible for players to activate cannons
     * @param am message for the player to pass data to: <ul>
     *           <il>cannonDirection: The {@link Direction} the cannon is pointing</il>
     *           <il>cannonCoordinate: the {@link Coordinates} of the cannon</il>
     *           <il>batteryCoordinate: The {@link Coordinates} of the battery, if it's a double cannon</il>
     * </ul>
     */
    private void activateCannon(ActionMessage am){
        Coordinates cannonCoordinates = (Coordinates) am.getData("cannonCoordinate");
        Player currPlayer = flightBoard.getPlayerByNickname(am.getSender());
        FixedComponentTile cannon = currPlayer.getShipBoard().getShipBoardComponent(cannonCoordinates);

        Direction direction = cannon.getDirection();

        if(currPlayer.getShipBoard().getPotentialFirePowerDouble().containsKey(cannonCoordinates)){
            currPlayer.getShipBoard().decreaseTotalBatteries((Coordinates) am.getData("batteryCoordinate"), 1);
        }

        MeteorShot currentShot = cardData.getMeteorList().get(currentMeteorIndex);
        if(direction != currentShot.getDirection()){
            playerSafetyForCurrentShot.put(currPlayer, false);
        }
        else if(currentShot.getDirection() == Direction.NORTH && rollForCurrentShot != cannonCoordinates.getCol()){
            playerSafetyForCurrentShot.put(currPlayer, false);
        }
        else{
            playerSafetyForCurrentShot.put(currPlayer, true);
        }
        checkAnswers();
    }


    /**
     * Sends an {@code ActionMessage} to notify for a new meteor shot with the following information <ul>
     *     <li>meteorIndex: the cardLevel of the meteorShot</li>
     *     <li>currentMeteorRoll: the dice throw</li>
     * </ul>
     * then a message for the shields {@link it.polimi.ingsw.model.cards.server.CardNotifier#sendPossibleShields(List)}
     * <p>players can then respond by using {@link MeteorSwarmServerCard#activateShield(ActionMessage)},
     * {@link MeteorSwarmServerCard#activateCannon(ActionMessage)} or {@link MeteorSwarmServerCard#doNothing(ActionMessage)}</p>
     */
    private void sendCurrentMeteor(){
        rollForCurrentShot = flightBoard.rollDie();

        ActionMessage am = new ActionMessage("meteorShot", "server");
        am.setData("meteorIndex", currentMeteorIndex);
        am.setData("currentMeteorRoll", rollForCurrentShot);
        publisher.notify(am);
        if(cardData.getMeteorList().get(currentMeteorIndex).getSize() == ShotSize.BIG){
            for(Player player : flightBoard.getActivePlayers()){
                if(!cardNotifier.askCannonsUsage(player)){
                    playerSafetyForCurrentShot.put(player, false);
                }
            }
        }
        else {
            for(Player player: flightBoard.getActivePlayers()){
                if(!cardNotifier.sendPossibleShields(player)){
                    playerSafetyForCurrentShot.put(player, false);
                }
            }
        }
        checkAnswers();
    }

    /**
     * Notify a player that they have been hit by an asteroid with an action message containing: <ul>
     *     <li>meteorIndex: which meteor are we playing (the full list is passed at the start of the card</li>
     *     <li>shipboard: the updated shipboard, after the hit</li>
     * </ul>
     * @param roll the die roll, on which line the shot is coming from
     * @param player the player to hit
     */
    private void sendAsteroidHit(int roll, Player player){
        ActionMessage am = new ActionMessage("destroyTile", "server");
        am.setReceiver(player.getNickname());
        am.setData("meteorIndex", currentMeteorIndex);
        am.setData("shipBoard", player.getShipBoard());
        publisher.notify(am);

    }

    @Override
    public void play(FlightBoard flightBoard, ModelPublisher modelPublisher) {
        cardSetup(flightBoard, modelPublisher);
        playerSafetyForCurrentShot = new HashMap<>();
        brokenTileForCurrentShot = new HashMap<>();
        ActionMessage am = new ActionMessage("MeteorSwarm", "server");
        am.setData("fileName", cardData.getFileName());

        sendCurrentMeteor();
    }

    @Override
    public void accept(ServerCardVisitor visitor, ModelPublisher publisher, PlayingState playingState) {
        visitor.visitForMeteorSwarm(this, publisher, playingState);
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
