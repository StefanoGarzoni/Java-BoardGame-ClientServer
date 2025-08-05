package it.polimi.ingsw.model.cards.server.concrete;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.model.Cargo.CargoType;
import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.FlightBoard;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.cards.generic.SmugglersCard;
import it.polimi.ingsw.model.cards.server.EnemyServerCard;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Smugglers enemy card.
 * <p>Implementation of {@link EnemyServerCard}.</p>
 * <p>Defeated players will lose cargo.</p>
 * @author Francesco Montefusco
 */
public class SmugglersServerCard extends EnemyServerCard {
    SmugglersCard cardData;

    public SmugglersServerCard(String fileName, int cargoPenalty, ArrayList<CargoType> cargoPrize, int flightDayLoss, int firePower, int level){
        cardData = new SmugglersCard(cargoPenalty, cargoPrize, flightDayLoss, firePower, level, fileName);

        mapMethods.put("sendCargo", this::sendCargoToAdd);
        mapMethods.put("removeCargo", this::setCargoToRemove);
    }

    private void sendCargoToRemove(){
        for(Player player : defeatedPlayers){
            removeCargo(cardData.getCargoPenalty(),player);
        }
    }

    /**
     * Remote method accessible by the players, allows them to distribute cargo inside the shipboard
     * @param am actionMessage with parameters set by the players contains: <ul>
     *           <li>{@code cargoCoordinates}: an HashMap of {@link CargoType} and {@link Coordinates}</li>
     * </ul>
     *
     */
    protected void sendCargoToAdd(ActionMessage am){
        if(!am.getSender().equals(winnerPlayer.getNickname())){
            //exception
            return;
        }
        HashMap<CargoType, Coordinates> cargoCoordinates = (HashMap<CargoType, Coordinates>) am.getData("cargoCoordinates");
        cargoCoordinates.forEach((cargo , coordinates) -> {
            winnerPlayer.getShipBoard().increaseCargo(coordinates, cargo, false);
        });

        if(defeatedPlayers.isEmpty()) endEnemy();

    }

    /**
     * Remote method accessible by the players, allows them to choose the cargo to remove inside the shipboard
     * @param am actionMessage with parameters set by the players, contains: <ul>
     *           <li>{@code cargoType}: an HashMap of {@link CargoType} and {@link Coordinates}</li>
     *
     * </ul>
     */
    protected void setCargoToRemove(ActionMessage am){
        Player currPlayer = flightBoard.getPlayerByNickname(am.getSender());
        if(!defeatedPlayers.contains(currPlayer)){
            //exception
            return;
        }
        HashMap<Coordinates, CargoType> cargoCoordinates = (HashMap<Coordinates, CargoType>) am.getData("cargoType");
        cargoCoordinates.forEach((cargo , coordinates) -> {
            currPlayer.getShipBoard().increaseCargo(cargo, coordinates, false);
            defeatedPlayers.remove(currPlayer);
        });
        if(isEnemyOver()) endEnemy();
    }

    protected void initEnemy(){
        this.requiredFirePower = cardData.getRequiredFirePower();
    }

    @Override
    public void assignPenalty(Player player, FlightBoard flightBoard) {
        sendCargoToRemove();
    }

    @Override
    public void assignPrize(Player player, FlightBoard flightBoard) {
        ActionMessage am = new ActionMessage("addCargo", "server");
        am.setReceiver(player.getNickname());
        cardNotifier.appendPossibleCargo(player, am);
        publisher.notify(am);
        flightBoard.movePlayerInFlightBoard(player, cardData.getFlightDayLoss());
    }

    @Override
    public String getFileName() {
        return cardData.getFileName();
    }
}
