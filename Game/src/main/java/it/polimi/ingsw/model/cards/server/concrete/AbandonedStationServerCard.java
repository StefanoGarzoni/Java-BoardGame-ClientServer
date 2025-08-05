package it.polimi.ingsw.model.cards.server.concrete;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.model.Cargo.CargoType;
import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.FlightBoard;
import it.polimi.ingsw.model.ModelPublisher;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.cards.generic.AbandonedStationCard;
import it.polimi.ingsw.model.cards.server.ServerCard;
import it.polimi.ingsw.model.cards.visitor.ServerCardVisitor;
import it.polimi.ingsw.model.states.PlayingState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Abandoned Station Card,
 * <p>if a player has enough crew, he can lose flight days and gain credits</p>
 * @author Francesco Montefusco
 */
public class AbandonedStationServerCard extends ServerCard {
    private Player claimerPlayer;
    private AbandonedStationCard cardData;

    private List<Player> playersWithEnoughCrew;
    private int currentPlayerIndex;
    private HashMap<CargoType, Coordinates> cargoCoordinatesMap;

    public AbandonedStationServerCard(ArrayList<CargoType> cgList, int flightDayLoss, int requiredCrew, int cardLevel, String fileName) {
        cardData = new AbandonedStationCard(flightDayLoss, requiredCrew, cgList, fileName, cardLevel);

        mapMethods.put("land", this::land);
        mapMethods.put("setCargoCoordinates", this::setCargoCoordinatesMap);
    }

    /**
     * Checks if <code>player</code> has enough crew to land on the station.
     * @param player the player to check
     * @return if he has enough crew
     */
    private boolean canLand(Player player){
        return(player.getShipBoard().getTotalCrew() > cardData.getRequiredCrew());
    }

    private void findEligiblePlayers(){
        for (Player player : flightBoard.getActivePlayers()) {
            if(canLand(player)){
                playersWithEnoughCrew.add(player);
            }
        }
    }

    /**
     * Sends an actionMessage aimed at the player to ask
     * @param player the player to ask to
     */
    private void askToLand(Player player){
        if(canLand(player)){
            ActionMessage am = new ActionMessage("abandonedStationLanding", "server");
            am.setReceiver(player.getNickname());
            publisher.notify(am);
        }
    }

    /**
     * Remote method accessible by the players, a player can land
     * @param message contains parameters sent by the client
     */
    private void land(ActionMessage message){
        Player player = flightBoard.getPlayerByNickname(message.getSender());
        boolean hasLanded = (boolean) message.getData("hasLanded");

        if(!hasLanded){
            if(currentPlayerIndex == playersWithEnoughCrew.size()){
                endCard(publisher, flightBoard); //No one landed
            }
            currentPlayerIndex++;
        }

        if(player != playersWithEnoughCrew.get(currentPlayerIndex)){
            return;
            //TODO Exception, maybe we can remove this check since we are already checking at the start of the card
        }
        else if(canLand(player)){
            setClaimerPlayer(player);
            ActionMessage am = new ActionMessage("playerLanded", "server");
            am.setReceiver(player.getNickname());
            cardNotifier.appendPossibleCargo(player, am);
            publisher.notify(am);
        }
        else{
            //exception ?
        }
    }


    /**
     * Remote method accessible by the players, sends the coordinates of the cargo
     * @param message contains parameters sent by the client <ul>
     *                <li>cargoCoordinates: a Map of The cargo to remove and the respective Coordinates</li>
     * </ul>
     */
    private void setCargoCoordinatesMap(ActionMessage message){
        if(flightBoard.getPlayerByNickname(message.getSender()) != claimerPlayer){
            //TODO exception
            return;
        }
        cargoCoordinatesMap = (HashMap<CargoType, Coordinates>) message.getData("cargoCoordinates");
        solveCard();
    }

    /**
     * Sets the player that decides to land
     * @param player the player that needs to land
     */
    private void setClaimerPlayer(Player player){
        claimerPlayer = player;
    }

    /**
     * modify the model accordingly to card state and notify the clients with an ActionMessage: <ul>
     *     <li>"claimerShipBoard" contains the updated shipBoard of the claimer player</li>
     *     <li>"claimerPlayerPosition" contains the updated position of the claimer player</li>
     * </ul>
     * @see ActionMessage
     */
    private void solveCard(){
        ActionMessage actionMessage = new ActionMessage("solveAbandonedStation", "server");

        flightBoard.movePlayerInFlightBoard(claimerPlayer, cardData.getFlightDayLoss());
        cargoCoordinatesMap.forEach((cargo,coordinates) -> {
            claimerPlayer.getShipBoard().increaseCargo(coordinates, cargo, false);
        });

        actionMessage.setData("claimerShipBoard", claimerPlayer.getShipBoard());
        actionMessage.setData("claimerPlayerPosition", claimerPlayer.getAbsPosition());
        publisher.notify(actionMessage);
        endCard(publisher, flightBoard);

    }


    @Override
    public void play(FlightBoard flightBoard, ModelPublisher publisher) {
        cardSetup(flightBoard, publisher);
        currentPlayerIndex = 0;
        playersWithEnoughCrew = new ArrayList<>();

        findEligiblePlayers();

        if(playersWithEnoughCrew.isEmpty()){
            endCard(publisher, flightBoard);
            return;
        }

        askToLand(playersWithEnoughCrew.get(currentPlayerIndex));
    }

    @Override
    public void accept(ServerCardVisitor visitor, ModelPublisher publisher, PlayingState playingState) {
        visitor.visitForAbandonedStation(this, publisher, playingState);
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
