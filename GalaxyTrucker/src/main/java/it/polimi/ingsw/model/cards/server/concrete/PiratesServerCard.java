package it.polimi.ingsw.model.cards.server.concrete;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.cards.generic.PiratesCard;
import it.polimi.ingsw.model.cards.server.CardNotifier;
import it.polimi.ingsw.model.cards.server.EnemyServerCard;
import it.polimi.ingsw.model.cards.util.FireShot;
import it.polimi.ingsw.model.cards.util.Pair;
import it.polimi.ingsw.model.cards.util.ShotSize;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Pirate enemy card.
 * <p>Implementation of {@link EnemyServerCard}</p>
 * <p>If a player loses, they receive a series of {@link FireShot}</p>
 * @author Francesco Montefusco
 */
public class PiratesServerCard extends EnemyServerCard {
    private PiratesCard cardData;

    private int currentShotIndex;
    private int currentDice;
    private HashMap<Player, Boolean> playerShieldForCurrentShot;
    private HashMap<Player, Coordinates> brokenTileForCurrentShot;

   public PiratesServerCard(String fileName, int level, ArrayList<FireShot> shotPenalty, int creditPrize,
                            int flightDayLoss, int requiredFirePower) {
       cardData = new PiratesCard(shotPenalty, creditPrize, flightDayLoss, requiredFirePower, level, fileName);
       mapMethods.put("activateShield", this::activateShield);
       mapMethods.put("resolveBranching", this::resolveBranching);

   }

    /**
     * sends the index of the current shot
     */
   private void sendCurrentShot(){
       Die die = new Die(6);
       currentDice = die.roll() + die.roll();
       if(cardData.getShotPenalty().get(currentShotIndex).getSize() == ShotSize.BIG) hitShot();
       playerShieldForCurrentShot.clear();
       for(Player player: defeatedPlayers){
           cardNotifier.sendCurrentShot(currentShotIndex, currentDice, player);
       }
   }

    /**
     * Remote method accessible by the players, allows them to activate a shield
     * @param am the message containing parameters set by the players: <ul>
     *           <li>doesActivate: A boolean for whether a shield was activated or not </li>
     *           <li>shieldDirections: a Pair of directions, to know which side was covered</li>
     *           <li>batteryCoordinates</li>
     * </ul>
     */
   private void activateShield(ActionMessage am){
       boolean activateShield = (boolean) am.getData("doesActivate");
       Pair<Direction, Direction> shieldDir = (Pair<Direction, Direction>) am.getData("shieldDirections");
       FireShot currentShot = cardData.getShotPenalty().get(currentShotIndex);

       if(!activateShield) {
           playerShieldForCurrentShot.put(flightBoard.getPlayerByNickname(am.getSender()), false);
       }
       else if(currentShot.getDirection() != shieldDir.getFirst() &&
               currentShot.getDirection() != shieldDir.getSecond()
       ){
           playerShieldForCurrentShot.put(flightBoard.getPlayerByNickname(am.getSender()), false);
           flightBoard.getPlayerByNickname(am.getSender()).getShipBoard().decreaseTotalBatteries((Coordinates) am.getData("batteryCoordinate"), 1);
       }
       else {
           playerShieldForCurrentShot.put(flightBoard.getPlayerByNickname(am.getSender()), true);
           flightBoard.getPlayerByNickname(am.getSender()).getShipBoard().decreaseTotalBatteries((Coordinates) am.getData("batteryCoordinate"), 1);
       }
       if(defeatedPlayers.stream().allMatch(playerShieldForCurrentShot::containsKey)){
           sendCurrentShot();
       }
   }

    /**
     * Hits the current shot based on the card state
     */
   private void hitShot() {
       FireShot currentShot = cardData.getShotPenalty().get(currentShotIndex);

       playerShieldForCurrentShot.forEach((player, isShieldActive) -> {
           if (isShieldActive && currentShot.getSize() == ShotSize.SMALL) return;
           Pair<Coordinates,ArrayList<ArrayList<Coordinates>>> trunks = currentShot.hitTile(player, currentDice);
           if(trunks.getFirst() != null){
               brokenTileForCurrentShot.put(player, trunks.getFirst());
           }
           if(trunks.getSecond() != null){
               cardNotifier.notifyTrunk(trunks.getSecond(), player);
           }

       });
       currentShotIndex++;
       if (currentShotIndex == cardData.getShotPenalty().size() && isEnemyOver()){
           endEnemy();
       }
   }

    public void resolveBranching(ActionMessage am){
        Player player = flightBoard.getPlayerByNickname(am.getReceiver());
        Coordinates branchToKeep = (Coordinates) am.getData("branchToKeep");
        Coordinates hitTile = brokenTileForCurrentShot.get(player);

        player.getShipBoard().delBranch(branchToKeep, hitTile);
    }

   @Override
   protected void initEnemy(){
       currentShotIndex = 0;
       mapMethods.put("activateShield", this::activateShield);
       playerShieldForCurrentShot = new HashMap<>();
       this.requiredFirePower = cardData.getRequiredFirePower();
       brokenTileForCurrentShot = new HashMap<>();

   }

   @Override
   public void assignPenalty(Player player, FlightBoard flightBoard) {
       sendCurrentShot();
       player.setAbsPosition(cardData.getFlightDayLoss());
   }

   @Override
   public void assignPrize(Player player, FlightBoard flightBoard) {
       player.increaseCredits(cardData.getCreditPrize());
       flightBoard.movePlayerInFlightBoard(player, cardData.getFlightDayLoss());
   }

    @Override
    public String getFileName(){
        return cardData.getFileName();
    }
}