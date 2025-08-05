package it.polimi.ingsw.model.cards.server.concrete;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.model.FlightBoard;
import it.polimi.ingsw.model.ModelPublisher;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.cards.generic.StardustCard;
import it.polimi.ingsw.model.cards.server.ServerCard;
import it.polimi.ingsw.model.cards.visitor.ServerCardVisitor;
import it.polimi.ingsw.model.states.PlayingState;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Stardust event card.
 * <p>Each player loses a flight day for each exposed connector</p>
 * @author Francesco Montefusco
 */
public class StardustServerCard extends ServerCard {
    private StardustCard cardData;

    public StardustServerCard(int level, String fileName) {
        cardData = new StardustCard(level, fileName);
    }

    /**
     * Solves the card and notifies the players the updated model
     */
    private void solve(){
        ArrayList<Player> players = flightBoard.getActivePlayers();
        HashMap<String, Integer> updatedPositionsMap = new HashMap<>();
        ActionMessage am = new ActionMessage("dayFlightLoss", "server");

        for(Player player : players){
            int flightDayPenalty = player.getShipBoard().countExposedConnectors();
            flightBoard.movePlayerInFlightBoard(player, -1 * flightDayPenalty);
            publisher.notify(am);
            updatedPositionsMap.put(player.getNickname(), player.getAbsPosition());
        }
        am.setData("updatedPositions", updatedPositionsMap);
        publisher.notify(am);
        endCard(publisher, flightBoard);

    }

    @Override
    public void play(FlightBoard flightBoard, ModelPublisher publisher) {
        cardSetup(flightBoard, publisher);

        ActionMessage am = new ActionMessage("StarDustCard", "server");
        publisher.notify(am);
        solve();
    }

    @Override
    public void accept(ServerCardVisitor visitor, ModelPublisher publisher, PlayingState playingState) {
        visitor.visitForStardust(this, publisher, playingState);
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
