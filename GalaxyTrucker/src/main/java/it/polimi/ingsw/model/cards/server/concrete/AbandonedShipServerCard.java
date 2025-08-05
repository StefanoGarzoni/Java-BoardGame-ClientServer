package it.polimi.ingsw.model.cards.server.concrete;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.cards.generic.AbandonedShipCard;
import it.polimi.ingsw.model.cards.server.ServerCard;
import it.polimi.ingsw.model.cards.visitor.ServerCardVisitor;
import it.polimi.ingsw.model.states.PlayingState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Abandoned Ship Card,
 * <p>A player can sacrifice crew and flight days to gain credits</p>
 * @author Francesco Montefusco
 */
public class AbandonedShipServerCard extends ServerCard {
    private AbandonedShipCard cardData;
    private Player claimerPlayer;


    //These fields are for the internal state while the card is in play, they are not initialized by the constructor
    private HashMap<Coordinates, Integer> crewToRemove;
    private List<Player> playersWithEnoughCrew;
    private int currentPlayerIndex;

    public AbandonedShipServerCard(String fileName, int cardLevel, int creditReward, int flightDayLoss, int crewLoss) {
        cardData = new AbandonedShipCard(creditReward, flightDayLoss, crewLoss, cardLevel, fileName);
        mapMethods.put("cabinCoordinates", this::land);
    }

    /**
     * sets the player to claim the ship
     * @param player the player who claims the ship first
     */
    private void setClaimerPlayer(Player player){
        claimerPlayer = player;
    }

    /**
     * asks given player if they are interested in landing, generating and publishing an action message
     * @param player the player to ask
     */
    private void askToLand(Player player){
        ActionMessage am = new ActionMessage("abandonedShipLanding", "server");
        am.setReceiver(player.getNickname());
        am.setData("cabins", player.getShipBoard().getCrewPositions());
        publisher.notify(am);
    }

    /**
     * solves the card, modifying the model accordingly to the player decisions, then notifies all the players
     */
    public void solveCard(){
        ActionMessage am = new ActionMessage("solveAbandonedShip", "server");
        am.setReceiver(claimerPlayer.getNickname());

        ShipBoard claimerPlayerShip = claimerPlayer.getShipBoard();

        flightBoard.movePlayerInFlightBoard(claimerPlayer, cardData.getFlightDayLoss());
        am.setData("positions", claimerPlayer.getAbsPosition());

        //removing the crew
        for(HashMap.Entry<Coordinates, Integer> entry : crewToRemove.entrySet()){
            claimerPlayerShip.decreaseCrewInCabin(entry.getKey(), entry.getValue());
        }
        am.setData("claimerPlayerShipBoard", claimerPlayerShip);

        claimerPlayer.increaseCredits(cardData.getCreditReward());
        am.setData("claimerPlayerCredits", claimerPlayer.getCredits());

        publisher.notify(am);
        endCard(publisher, flightBoard);
    }

    /**
     * remote method accessible by the players, they have to send whether they want to land or not
     * @param actionMessage the message containing the parameters the player sent <ul>
     *                      <li>hasLanded (boolean): if the player decides to land</li>
     *                      <li>crewCoordinatesArray (HashMap &lt;Coordinates,Integer&rt;): a map containing where to remove the crew and how many of them</li>
     *                      </ul>
     *
     */

    private void land(ActionMessage actionMessage){
        if(!actionMessage.getSender().equals(playersWithEnoughCrew.get(currentPlayerIndex).getNickname())){
            //TODO exception
            return;
        }

        if(playersWithEnoughCrew.get(currentPlayerIndex).getNickname().equals(actionMessage.getSender())){}

        if((Boolean) actionMessage.getData("hasLanded")){
            setClaimerPlayer(playersWithEnoughCrew.get(currentPlayerIndex));
            crewToRemove = (HashMap<Coordinates, Integer>) actionMessage.getData("crewCoordinatesArray");
            solveCard();
            return;
        }

        if(!(Boolean) actionMessage.getData("hasLanded")){
            if(currentPlayerIndex == playersWithEnoughCrew.size() - 1){
                endCard(publisher, flightBoard);
            }
            currentPlayerIndex++;
            askToLand(playersWithEnoughCrew.get(currentPlayerIndex));
        }
    }

    private void findEligiblePlayers(){
        flightBoard.getActivePlayers().forEach(player -> {
            if(player.getShipBoard().getTotalCrew() >= cardData.getCrewLoss()){ playersWithEnoughCrew.add(player); }
        });
    }

    @Override
    public void play(FlightBoard flightBoard,  ModelPublisher publisher) {
        cardSetup(flightBoard, publisher);
        playersWithEnoughCrew = new ArrayList<>();
        currentPlayerIndex = 0;
        findEligiblePlayers();

        ActionMessage am = new ActionMessage("AbandonedShip","server");
        am.setData("fileName", cardData.getFileName());
        publisher.notify(am);

        if(playersWithEnoughCrew.isEmpty()){
            endCard(publisher, flightBoard);
            return;
        }
        askToLand(playersWithEnoughCrew.get(currentPlayerIndex));
    }

    @Override
    public void accept(ServerCardVisitor visitor, ModelPublisher publisher, PlayingState playingState) {
        visitor.visitForAbandonedShip(this, publisher, playingState);
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
