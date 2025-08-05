package it.polimi.ingsw.model.cards.server.concrete;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.Cargo.CargoType;
import it.polimi.ingsw.model.cards.generic.PlanetCard;
import it.polimi.ingsw.model.cards.server.CardNotifier;
import it.polimi.ingsw.model.cards.server.ServerCard;
import it.polimi.ingsw.model.cards.util.Planet;
import it.polimi.ingsw.model.cards.util.PlanetOccupiedException;
import it.polimi.ingsw.model.cards.visitor.ServerCardVisitor;
import it.polimi.ingsw.model.states.PlayingState;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Planet card.
 * <p>Players can choose to land, if they do, they receive some cargo and lose flight days</p>
 * @author Francesco Montefusco
 */
public class PlanetServerCard extends ServerCard {
    private PlanetCard cardData;

    private ArrayList<Player> activePlayerList;
    private ArrayList<Player> landingPlayers;
    private int currentPlayerIndex;
    private ArrayList<Integer> freePlanetIndexes;
    private HashMap<Player, HashMap<Coordinates, CargoType>> cargoCoordinates;

    public PlanetServerCard(String fileName, int level, ArrayList<Planet> planets, int flightDayLoss) {
        cardData = new PlanetCard(planets, flightDayLoss, level, fileName);

        mapMethods = new HashMap<>();
        mapMethods.put("land", this::land);
        mapMethods.put("setCargo", this::setCargo);
    }

    /**
     * Remote method accessible by the players, allow them to land on a planet, it also notifies them of a successful landing with an ActionMessage: <ul>
     *     <li>planetIndex: the planet the player landed on</li>
     * </ul>
     * @param message actionMessage containing the data set by the player: <ul>
     *                <li>planetIndex: the cardLevel of the planet they are interested in landing on</li>
     * </ul>
     */
    private void land(ActionMessage message){
        if(!message.getSender().equals(activePlayerList.get(currentPlayerIndex).getNickname())){
            throw new RuntimeException("wrong player answered");
        }
        Player player = flightBoard.getPlayerByNickname(message.getSender());

        int planetIndex = message.getInt("planetIndex");
        boolean hasLanded = (boolean) message.getData("hasLanded"); //TODO aggiungerlo ad RMI

        if(!hasLanded){
            if(currentPlayerIndex == activePlayerList.size() - 1 && freePlanetIndexes.size() == cardData.getPlanets().size()){
                solveCard();
            }
        }
        else {
            try {
                cardData.getPlanets().get(planetIndex).land(player);
            } catch (PlanetOccupiedException e) {
                throw new RuntimeException(e);
            }
            landingPlayers.add(player);
            freePlanetIndexes.remove(Integer.valueOf(planetIndex));
            ActionMessage am = new ActionMessage("playerLanded", "server");
            am.setReceiver(player.getNickname());
            am.setData("planetIndex", planetIndex);
            cardNotifier.appendPossibleCargo(player, am);
            publisher.notify(am);
        }

        if(freePlanetIndexes.isEmpty() || currentPlayerIndex == activePlayerList.size() - 1){
            askCargo();
        }
        else{
            askToLand(activePlayerList.get(++currentPlayerIndex));
        }
    }

    /**
     * Sends an ActionMessage for each planet to the landedPlayer to ask for the cargo positioning on the ship:
     * <ul>
     *     cargoList: the list of cargos on the planet
     * </ul>
     */
    private void askCargo(){
        int planetIndex = 0;
        int playerWithNoCargoCounter = 0;
        int playerCounter = 0;
        for(Planet planet : cardData.getPlanets()){
            Player landedPlayer = planet.getPlayer();
            if(landedPlayer == null) continue;
            playerCounter++;
            if(landedPlayer.getShipBoard().getCargoTilesPosition(false).isEmpty()){
                playerWithNoCargoCounter++;
                ActionMessage am = new ActionMessage("noCargoAvailable", "server");
                am.setReceiver(landedPlayer.getNickname());
                publisher.notify(am);
                continue;
            }
            ActionMessage am = new ActionMessage("loadCargo", "server");
            am.setData("planetIndex", planetIndex);
            cardNotifier.appendPossibleCargo(landedPlayer, am);
            am.setReceiver(landedPlayer.getNickname());
            publisher.notify(am);
            planetIndex++;
        }
        //solveCard();
    }

    private void askToLand(Player player){
        ActionMessage am = new ActionMessage("planetLanding", "server");
        am.setReceiver(player.getNickname());
        publisher.notify(am);
    }

    /**
     * Saves the cargo position sent by a player
     * @param message an actionMessage containing data set by the player: <ul>
     *                <li>cargoCoordinates: an hashMap with all the pairs of coordinates and cargotype</li>
     * </ul>
     */
    private void setCargo(ActionMessage message){
        HashMap<Coordinates, CargoType> pair = (HashMap<Coordinates, CargoType>) message.getData("cargoCoordinates");
        cargoCoordinates.put(flightBoard.getPlayerByNickname(message.getSender()), pair);
        if(landingPlayers.stream().allMatch(cargoCoordinates::containsKey)){
            solveCard();
        }
    }

    /**
     * Solves the card, modifying the model accordingly to the internal state of the card, and then notifies the clients
     */
    private void solveCard(){
        HashMap<String, ShipBoard> modifiedShipboards = new HashMap<>();
        HashMap<String, Integer> modifiedPositions = new HashMap<>();
        for( Planet planet : cardData.getPlanets() ) {
            if( planet.getPlayer() == null) continue;
            Player landedPlayer = planet.getPlayer();
            flightBoard.movePlayerInFlightBoard(landedPlayer, -1*cardData.getFlightDayLoss());
            modifiedPositions.put(landedPlayer.getNickname(), landedPlayer.getAbsPosition());

            if(cargoCoordinates == null) continue;
            cargoCoordinates.forEach((player, cargoCoord) ->{
                for(HashMap.Entry<Coordinates, CargoType> entry : cargoCoord.entrySet()) {
                    ShipBoard playerBoard = landedPlayer.getShipBoard();
                    playerBoard.increaseCargo(entry.getKey(), entry.getValue(), false);
                }
            });


            modifiedShipboards.put(landedPlayer.getNickname(), landedPlayer.getShipBoard());
        }
        ActionMessage am = new ActionMessage("planetSolved", "server");
        am.setData("modifiedShipboards", modifiedShipboards);
        am.setData("modifiedPositions", modifiedPositions);
        publisher.notify(am);
        endCard(publisher, flightBoard);

    }

    @Override
    public void play(FlightBoard flightBoard, ModelPublisher publisher) {
        cardSetup(flightBoard, publisher);
        activePlayerList = flightBoard.getActivePlayers();
        currentPlayerIndex = 0;
        freePlanetIndexes = new ArrayList<>();
        cargoCoordinates = new HashMap<>();
        landingPlayers = new ArrayList<>();
        for(int i = 0; i < cardData.getPlanets().size(); i++){
            freePlanetIndexes.add(i);
        }

        askToLand(activePlayerList.get(0));


    }

    @Override
    public void accept(ServerCardVisitor visitor, ModelPublisher publisher, PlayingState playingState) {
        visitor.visitForPlanet(this, publisher, playingState);
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
