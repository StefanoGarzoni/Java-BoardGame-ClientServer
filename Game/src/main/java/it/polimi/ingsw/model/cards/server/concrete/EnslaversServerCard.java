package it.polimi.ingsw.model.cards.server.concrete;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.model.Cargo.CargoType;
import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.FlightBoard;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.cards.generic.EnslaversCard;
import it.polimi.ingsw.model.cards.server.CardNotifier;
import it.polimi.ingsw.model.cards.server.EnemyServerCard;
import it.polimi.ingsw.model.cards.server.ServerCard;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Enslaver enemy Card.
 * <p>Implementation of {@link EnemyServerCard}</p>
 * <p>If a player loses, they lose crew members</p>
 * @author Francesco Montefusco
 */
public class EnslaversServerCard extends EnemyServerCard {
    private EnslaversCard cardData;

    private Map<Coordinates, Integer> cabinsCoordinates;
    private Map<Player, Integer> penaltyLeft;

    public EnslaversServerCard(String fileName, int cardLevel, int crewPenalty, int creditPrize, int requiredFirePower, int flightDayLoss) {
        cardData = new EnslaversCard(creditPrize, crewPenalty, flightDayLoss, requiredFirePower, cardLevel, fileName);

        this.cabinsCoordinates = new HashMap<>();
        this.penaltyLeft = new HashMap<>();
        mapMethods.put("cabinCoordinates", this::setCabinCoordinates);
        mapMethods.put("setCargoCoordinates", this::setCargoToRemove);
        mapMethods.put("setBatteriesCoordinates", this::setBatteriesToRemove);
    }

    /**
     * Remote method accessible by the player, allows them to set the coordinates of the crew to remove
     * @param actionMessage contains parameters sent by the client:<ul>
     *                      <li>cabinCoordinates: a map of coordinates and integers to know where to remove crew and how many of them</li>
     * </ul>
     * @see ActionMessage
     */
    private void setCabinCoordinates(ActionMessage actionMessage) {
        AtomicInteger counter = new AtomicInteger();
        counter.set(cardData.getCrewPenalty());

        Player senderPlayer = flightBoard.getPlayerByNickname(actionMessage.getSender());
        cabinsCoordinates = (HashMap<Coordinates, Integer>) actionMessage.getData("cabinCoordinates");
        cabinsCoordinates.forEach((coord, numCrew) -> {
            senderPlayer.getShipBoard().decreaseCrewInCabin(coord, numCrew);
            counter.addAndGet(-1);
        });

        defeatedPlayers.remove(senderPlayer);
//        if(counter.get() == 0){
//            defeatedPlayers.remove(senderPlayer);
//        }
//        else{
//            removeCargo(counter.get(), senderPlayer);
//        }
        if(isEnemyOver()) endEnemy();
    }

    public void setCargoToRemove(ActionMessage am) {
        Player senderPlayer = flightBoard.getPlayerByNickname(am.getSender());
        if(!penaltyLeft.containsKey(senderPlayer)){
            //TODO exception
        }
        AtomicInteger counter = new AtomicInteger();
        counter.set(penaltyLeft.get(senderPlayer));
        HashMap<Coordinates, CargoType> cargoToRemove = (HashMap<Coordinates, CargoType>) am.getData("cargoCoordinates");
        cargoToRemove.forEach((coordinates, cargoType) -> {
            senderPlayer.getShipBoard().increaseCargo(coordinates, cargoType, true);
            counter.addAndGet(-1);
        });

        if(counter.get() == 0){
            defeatedPlayers.remove(senderPlayer);
        }
        else{
            penaltyLeft.replace(senderPlayer, counter.get());
        }

        if(isEnemyOver()) endEnemy();

    }

    public void setBatteriesToRemove(ActionMessage am) {
        Player senderPlayer = flightBoard.getPlayerByNickname(am.getSender());
        if(!penaltyLeft.containsKey(senderPlayer)){
            //TODO Exception
        }
        AtomicInteger counter = new AtomicInteger();
        counter.set(penaltyLeft.get(senderPlayer));
        HashMap<Coordinates, Integer> batteriesToRemove = (HashMap<Coordinates, Integer>) am.getData("batteriesCoordinates");
        batteriesToRemove.forEach((coordinates, integer) -> {
            senderPlayer.getShipBoard().decreaseTotalBatteries(coordinates, integer);
        });
        defeatedPlayers.remove(senderPlayer);

        if(isEnemyOver()) endEnemy();
    }

    @Override
    protected void initEnemy() {
        mapMethods.put("setCabinCoordinates", this::setCabinCoordinates);
        this.requiredFirePower = cardData.getRequiredFirePower();

    }

    @Override
    public void assignPenalty(Player player, FlightBoard flightBoard) {
        cardNotifier.askCrewToRemove(player);
    }

    @Override
    public void assignPrize(Player player, FlightBoard flightBoard) {
        player.increaseCredits(cardData.getCreditPrize());
        flightBoard.movePlayerInFlightBoard(player, cardData.getFlightDayLoss());
        winnerPlayer = null;

        if(defeatedPlayers.isEmpty()){
            endEnemy();
        }
    }

    @Override
    public String getFileName(){
        return cardData.getFileName();
    }
}
