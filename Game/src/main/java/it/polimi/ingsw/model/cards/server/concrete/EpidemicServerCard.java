package it.polimi.ingsw.model.cards.server.concrete;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.cards.generic.EpidemicCard;
import it.polimi.ingsw.model.cards.server.ServerCard;
import it.polimi.ingsw.model.cards.util.Pair;
import it.polimi.ingsw.model.cards.visitor.ServerCardVisitor;
import it.polimi.ingsw.model.states.PlayingState;

import java.util.*;

/**
 * Epidemic card, special event.
 * <p>Each player loses a crew member for each cabin attached to another cabin</p>
 * @author Francesco Montefusco
 */
public class EpidemicServerCard extends ServerCard {
    private EpidemicCard cardData;

    public EpidemicServerCard(String fileName, int level){
        super();
        cardData = new EpidemicCard(level, fileName);
    }

    private final int[][] coordinateTest = { {1,0}, {0,1}, {-1,0}, {0,-1} };

    /**
     * Finds adjacent cabins to know where the crew has to be removed
     * @param cabinCoordinates a {@code Set} of {@code Coordinates}, all the cabins positions
     * @return a {@code Set} of {@code Pair} of {@code Coordinates}, each element of the set is a pair of adjacent cabins
     */
    private Set<Pair<Coordinates, Coordinates>> findAdjacentPairs(Set<Coordinates> cabinCoordinates) {

        Set<Pair<Coordinates, Coordinates>> pairs = new HashSet<>();

        cabinCoordinates.forEach(c -> {
            for(int[] adjacentValue : coordinateTest){
                Coordinates newCoordinate = new Coordinates(c.getRow() + adjacentValue[0] ,c.getCol() + adjacentValue[1]);
                if(cabinCoordinates.contains(newCoordinate)){
                    pairs.add(new Pair<>(c, newCoordinate));
                }
            }
        });
        return pairs;
    }

    @Override
    public void play(FlightBoard flightBoard, ModelPublisher modelPublisher) {
        cardSetup(flightBoard, modelPublisher);
        ActionMessage am = new ActionMessage("EpidemicCard", "server");
        modelPublisher.notify(am);

        ActionMessage amFinish = new ActionMessage("solveEpidemicCard", "server");
        Map<String, ShipBoard> updatedShipBoards = new HashMap<>();

        for (Player player : flightBoard.getActivePlayers()) {
            Map<Coordinates, Integer> crewPositions = player.getShipBoard().getCrewPositions();

            Set<Pair<Coordinates, Coordinates>> pairs = findAdjacentPairs(crewPositions.keySet());

            for (Pair<Coordinates, Coordinates> pair : pairs) {
                player.getShipBoard().decreaseCrewInCabin(pair.getFirst(), 1);
                player.getShipBoard().decreaseCrewInCabin(pair.getSecond(), 1);
            }
            updatedShipBoards.put(player.getNickname(), player.getShipBoard());
        }

        amFinish.setData("updatedShipBoards", updatedShipBoards);
        modelPublisher.notify(amFinish);
        endCard(publisher, flightBoard);
    }

    @Override
    public void accept(ServerCardVisitor visitor, ModelPublisher publisher, PlayingState playingState) {
        visitor.visitForEpidemic(this, publisher, playingState);
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
