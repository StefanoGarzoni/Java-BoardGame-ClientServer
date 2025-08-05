package it.polimi.ingsw.controller.clientProxies;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.client.RMIClient;
import it.polimi.ingsw.controller.Controller;
import it.polimi.ingsw.model.Cargo.CargoType;
import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.Direction;
import it.polimi.ingsw.model.ShipBoard;
import it.polimi.ingsw.model.Deck;

import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class RMIClientProxy extends ClientProxy{
    // client's remote object
    RMIClient clientRemoteObject;
    RMIServerImpl serverRemoteObject;
    Map<String, Consumer<ActionMessage>> mapMethods = new HashMap<>();

    /** Creates an RMIClientProxy, fetches the client's remote object for server-client remote methods invocations,
     * and exposes the server's remote object for client-server remote methods invocations
     *
     * @param nickname of the Client's player
     * @param clientRmiIp Ip address at which the client exposes his remote object
     * @throws RemoteException when a communication issues occurs
     * @throws NotBoundException if the client has no "ClientRemoteObject" bound on the RMI Registry at the provided ip address
     * @throws MalformedURLException server error that occurs if the server's remote object rebind URL is not valid
     */
    public RMIClientProxy(String nickname, String clientRmiIp, Controller controller) throws RemoteException, NotBoundException, MalformedURLException{

        super(nickname);
        setController(controller);

        mapMethods.put("lobbyState",this::whereYouAre);
        mapMethods.put("BuildingArrangementState",this::whereYouAre);
        mapMethods.put("Colors",this::giveYouColor);
        mapMethods.put("ClientShipBoardBuildingState",this::whereYouAre);
        mapMethods.put("tileIsBooked",this::tileIsBooked);
        mapMethods.put("tileIsFree",this::tileIsFree);
        mapMethods.put("sendTileCovered",this::sendTileCovered);
        mapMethods.put("tileUncoveredTaken",this::tileUncoveredTaken);
        mapMethods.put("tileIsPlaced",this::tileIsPlaced);
        mapMethods.put("deckIsViewing",this::deckIsViewing);
        mapMethods.put("deckReleased",this::deckReleased);
        mapMethods.put("playerIsDoneConfermation",this::playerIsDoneConfermation);
        mapMethods.put("canFlipHG",this::canFlipHG);
        mapMethods.put("finishedTimerConfermation",this::finishedTimerConfermation);
        mapMethods.put("wrongTilesCouplings",this::wrongTilesCouplings);
        mapMethods.put("ShipBoardChecksState",this::whereYouAre);
        mapMethods.put("tilesBranchsRemoved",this::tilesBranchsRemoved);
        mapMethods.put("possibleAliensPosition",this::possibleAliensPosition);
        mapMethods.put("DistributeResourcesState",this::whereYouAre);
        mapMethods.put("resourcesDistributedConfermation",this::resourcesDistributedConfermation);
        mapMethods.put("chooseWhichGroupsKeep",this::chooseWhichGroupsKeep);
        mapMethods.put("decksCreated",this::decksCreated);

        //TODO mancano i metodi che "invocano" le carte ...
        mapMethods.put("cardReleased",this::cardReleased);
        mapMethods.put("playingState",this::whereYouAre);
        mapMethods.put("cardDrawing",this::cardDrawing);
        mapMethods.put("cardsFinished",this::cardsFinished);
        //mapMethods.put("matchFinished",this::matchFinished);

        mapMethods.put("abandonedShipLanding",this::abandonedShipLanding);
        mapMethods.put("abandonedStationLanding",this::abandonedStationLanding);
        mapMethods.put("cardInGame",this::cardInGame);
        mapMethods.put("solveAbandonedShip",this::solveAbandonedShip);
        mapMethods.put("solveAbandonedStation",this::solveAbandonedStation);
        mapMethods.put("solveEpidemicCard",this::solveEpidemicCard);
        mapMethods.put("currentShot",this::currentShot);
        mapMethods.put("destroyTile",this::destroyTile);
        mapMethods.put("askEngineUsage",this::askEngineUsage);
        mapMethods.put("solveOpenSpace",this::solveOpenSpace);
        mapMethods.put("loadCargo",this::loadCargo);
        mapMethods.put("solvePlanetCard",this::solvePlanetCard);
        mapMethods.put("solveStardust",this::solveStardust);
        mapMethods.put("leastCrewPlayer",this::leastCrewPlayer);
        mapMethods.put("solveLeastCrew",this::solveLeastCrew);


        mapMethods.put("DetermineScoresState",this::whereYouAre);
        mapMethods.put("isDeterminingScores",this::whereYouAre);
        mapMethods.put("finalScores",this::finalScores);
        mapMethods.put("gameIsFinished",this::whereYouAre);


        // client's remote object fetching
        Registry registry = LocateRegistry.getRegistry(clientRmiIp);
        clientRemoteObject = (RMIClient) registry.lookup("ClientRemoteObject");

        // server's remote object exposure for this player
        LocateRegistry.createRegistry(1099);
        serverRemoteObject = new RMIServerImpl(nickname, getController());
        RMIServer stub = (RMIServer) UnicastRemoteObject.exportObject(serverRemoteObject, 0);
        Naming.rebind("rmi://localhost/RmiServer?"+nickname, stub);
    }

    private void decksCreated(ActionMessage actionMessage) {
        clientRemoteObject.decksCreated(
                (ArrayList<Deck>)actionMessage.getData("decks")
        );
    }

    private void solveLeastCrew(ActionMessage actionMessage) {
        clientRemoteObject.solveLeastCrew(
                actionMessage.getReceiver(),
                actionMessage.getInt("flightDayPenalty")
        );
    }

    private void leastCrewPlayer(ActionMessage actionMessage) {
        clientRemoteObject.leastCrewPlayer(
                actionMessage.getReceiver()
        );
    }

    private void solveStardust(ActionMessage actionMessage) {
        clientRemoteObject.solveStardust(
                (Map<String, Integer>)actionMessage.getData("updatedPosition")
        );
    }

    private void solvePlanetCard(ActionMessage actionMessage) {
        clientRemoteObject.solvePlanetCard(
                (Map<String, ShipBoard>)actionMessage.getData("updatedShipboards"),
                (Map<String, Integer>)actionMessage.getData("updatedPosition")
        );
    }

    private void loadCargo(ActionMessage actionMessage) {
        clientRemoteObject.loadCargo(
                actionMessage.getReceiver(),
                (ArrayList<CargoType>)actionMessage.getData("cargoList")
        );
    }

    private void solveOpenSpace(ActionMessage actionMessage) {
        clientRemoteObject.solveOpenSpace(
                (Map<String, Integer>)actionMessage.getData("updatedPositions")
        );
    }

    private void askEngineUsage(ActionMessage actionMessage) {
        clientRemoteObject.askEngineUsage(
                actionMessage.getReceiver(),
                (Map<Coordinates, Integer> )actionMessage.getData("doubleEngine")
        );
    }

    private void destroyTile(ActionMessage actionMessage) {
        clientRemoteObject.destroyTile(
                actionMessage.getReceiver(),
                actionMessage.getInt("shotIndex")
        );
    }

    private void currentShot(ActionMessage actionMessage) {
        clientRemoteObject.currentShot(
                actionMessage.getInt("shotIndex"),
                actionMessage.getInt("currentRoll")
        );
    }

    private void solveEpidemicCard(ActionMessage actionMessage) {
        clientRemoteObject.solveEpidemicCard(
                (Map<String, ShipBoard>)actionMessage.getData("updatedShipboards")
        );
    }

    private void solveAbandonedStation(ActionMessage actionMessage) {
        clientRemoteObject.solveAbandonedStation(
                actionMessage.getReceiver(),
                actionMessage.getInt("claimerPlayerPos"),
                (ShipBoard)actionMessage.getData("claimerPlayerShipboard")
        );
    }

    private void solveAbandonedShip(ActionMessage actionMessage) {
        clientRemoteObject.solveAbandonedShip(
                actionMessage.getReceiver(),
                actionMessage.getInt("claimerPlayerPos"),
                (ShipBoard) actionMessage.getData("claimerPlayerShipboard"),
                actionMessage.getInt("claimerPlayerCredits")
        );
    }

    private void cardInGame(ActionMessage actionMessage) {
        clientRemoteObject.cardInGame(
                actionMessage.getReceiver(),
                (String)actionMessage.getData("fileName")
        );
    }

    private void abandonedStationLanding(ActionMessage actionMessage) {
        clientRemoteObject.abandonedStationLanding(
                actionMessage.getReceiver()
        );
    }

    private void abandonedShipLanding(ActionMessage actionMessage) {
        clientRemoteObject.abandonedShipLanding(
                actionMessage.getReceiver()
        );
    }

    private void cardsFinished(ActionMessage actionMessage) {
        clientRemoteObject.cardsFinished();
    }

    private void tileIsBooked(ActionMessage actionMessage) {
        clientRemoteObject.tileIsBooked(
                (String)actionMessage.getReceiver(),
                (String)actionMessage.getData("fileName")
        );
    }

    private void tileIsFree(ActionMessage actionMessage) {
        //sempre settato a "all" iul receiver
        clientRemoteObject.tileIsFree(
                (String)actionMessage.getReceiver(),
                (String)actionMessage.getData("fileName")
        );
    }

    private void sendTileCovered(ActionMessage actionMessage) {
        clientRemoteObject.sendTileCovered(
                (String)actionMessage.getReceiver(),
                (String)actionMessage.getData("fileName")
        );
    }

    private void tileUncoveredTaken(ActionMessage actionMessage) {
        clientRemoteObject.tileUncoveredTaken(
                (String)actionMessage.getReceiver(),
                (String)actionMessage.getData("fileName")
        );
    }

    private void tileIsPlaced(ActionMessage actionMessage) {
        clientRemoteObject.tileIsPlaced(
                (String)actionMessage.getReceiver(),
                (String)actionMessage.getData("fileName"),
                (Coordinates) actionMessage.getData("coordinates"),
                (Direction) actionMessage.getData("direction")
        );
    }

    private void deckIsViewing(ActionMessage actionMessage) {
        clientRemoteObject.deckIsViewing(
                (String)actionMessage.getData("player"),
                actionMessage.getInt("deckNumber"),
                (Deck)actionMessage.getData("deck")
        );
    }

    private void deckReleased(ActionMessage actionMessage) {
        clientRemoteObject.deckReleased(
                (String)actionMessage.getData("player"),
                actionMessage.getInt("deckNumber")
        );
    }

    private void playerIsDoneConfermation(ActionMessage actionMessage) {
        clientRemoteObject.playerIsDoneConfermation((String)actionMessage.getData("player"));
    }

    private void canFlipHG(ActionMessage actionMessage) {
        clientRemoteObject.canFlipHG(
                (Boolean)actionMessage.getData("permit"),
                (Boolean)actionMessage.getData("lastTime"),
                actionMessage.getData("serverWaitMillisecondFrom")!= null ?
                        (String)actionMessage.getData("serverWaitMillisecondFrom") : null);
    }

    private void finishedTimerConfermation(ActionMessage actionMessage) {
        clientRemoteObject.finishedTimerConfermation();
    }

    private void wrongTilesCouplings(ActionMessage actionMessage) {
        clientRemoteObject.wrongTilesCouplings(
                (String)actionMessage.getReceiver(),
                (ArrayList<ArrayList<Coordinates>>)actionMessage.getData("couplings"),
                (ArrayList<Coordinates>)actionMessage.getData("wrongEngines")
        );
    }

    private void chooseWhichGroupsKeep(ActionMessage actionMessage) {
        clientRemoteObject.chooseWhichGroupsKeep(
                (String)actionMessage.getReceiver(),
                (ArrayList<ArrayList<Coordinates>>)actionMessage.getData("groups")
        );
    }

    private void tilesBranchsRemoved(ActionMessage actionMessage) {
        clientRemoteObject.tilesBranchsRemoved(
                (String)actionMessage.getReceiver(),
                (ShipBoard)actionMessage.getData("shipBoard")
        );
    }

    private void possibleAliensPosition(ActionMessage actionMessage) {
        clientRemoteObject.possibleAliensPosition(
                (String)actionMessage.getReceiver(),
                (ArrayList<ArrayList<Coordinates>>)actionMessage.getData("positions")
        );
    }

    private void resourcesDistributedConfermation(ActionMessage actionMessage) {
        clientRemoteObject.resourcesDistributedConfermation(
                (String)actionMessage.getReceiver(),
                (ShipBoard)actionMessage.getData("shipBoard")
        );
    }

    private void cardReleased(ActionMessage actionMessage) {
        clientRemoteObject.cardReleased();
    }

    private void cardDrawing(ActionMessage actionMessage) {
        clientRemoteObject.cardDrawing(
                (String)actionMessage.getReceiver(),
                (Boolean)actionMessage.getData("lastCard"),
                (String)actionMessage.getData("cardFileName")
        );
    }

    private void finalScores(ActionMessage actionMessage) {
        clientRemoteObject.finalScores(
                (String)actionMessage.getReceiver(),
                actionMessage.getInt("scores")
        );
    }

    private void giveYouColor(ActionMessage actionMessage) {
        Map<String, String> playerColors = new HashMap<>();
        for(String key : mapMethods.keySet()){
            playerColors.put(key, (String)actionMessage.getData(key));
        }
        clientRemoteObject.giveYouColor(playerColors);
    }

    private void whereYouAre(ActionMessage actionMessage) {
        clientRemoteObject.whereYouAre(actionMessage.getActionName());
    }

    /** Removes the remote object that the server exposes for client-to-server communication for
     * this ClientProxy's player.
     */
    @Override
    public void closeConnectionToClient(){
        try{ UnicastRemoteObject.unexportObject(serverRemoteObject, false); }
        catch (NoSuchObjectException e) { e.printStackTrace(System.out); }
    }

    @Override
    public void send(ActionMessage actionMessage) {
        mapMethods.get(actionMessage.getActionName()).accept(actionMessage);
    }
}
