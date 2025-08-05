package it.polimi.ingsw.client;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.controller.WelcomeProxies.RMIWelcome;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import it.polimi.ingsw.controller.clientProxies.RMIServer;
import it.polimi.ingsw.model.Cargo.CargoType;
import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.Direction;
import it.polimi.ingsw.model.cards.util.Pair;

public class RMIServerProxy implements ServerProxy {
    private final int rmiClientPortNumber = 1099;
    private final int rmiServerPortNumber = 1099;

    private final String serverIpAddress;

    private final Map<String, Consumer<ActionMessage>> mapMethods;
    private final ClientController clientController;

    private RMIServer serverRemoteObject;

    public RMIServerProxy(String serverIpAddress, ClientController clientController) throws UnknownHostException, RemoteException, MalformedURLException{
        this.serverIpAddress = serverIpAddress;
        this.clientController = clientController;

        mapMethods = new HashMap<>();
        mapMethods.put("placeTileAt", this::placeTileAt);
        mapMethods.put("bookTile", this::bookTile);
        mapMethods.put("viewDeck", this::viewDeck);
        mapMethods.put("poseDeck", this::poseDeck);
        mapMethods.put("getCoveredTile", this::getCoveredTile);
        mapMethods.put("getUncoveredTile", this::getUncoveredTile);
        mapMethods.put("poseTile", this::poseTile);
        mapMethods.put("flipHourGlass", this::flipHourGlass);
        mapMethods.put("startTimer", this::startTimer);
        mapMethods.put("playerIsDone", this::playerIsDone);
        mapMethods.put("playerChoice", this::playerChoice);
        mapMethods.put("placeAliens", this::placeAliens);
        mapMethods.put("groupsToRemove", this::groupsToRemove);
        mapMethods.put("drawCard", this::drawCard);
        mapMethods.put("matchFinished", this::matchFinished);
        mapMethods.put("handleCard", this::handleCard);
        mapMethods.put("landAbandonedStation", this::landAbandonedStation);
        mapMethods.put("landAbandonedShip", this::landAbandonedShip);
        mapMethods.put("setCargoCoordinates", this::setCargoCoordinates);
        mapMethods.put("setCabinCoordinates", this::setCabinCoordinates);
        mapMethods.put("activateShild", this::activateShild);
        mapMethods.put("setEnginePower", this::setEnginePower);
        mapMethods.put("setFirePower", this::setFirePower);
        mapMethods.put("setCargoToRemove", this::setCargoToRemove);
        mapMethods.put("activateCannon", this::activateCannon);
        //mapMethods.put("releaseCard", this::releaseCard);
        mapMethods.put("PONG", this::pong);
    }

    public void closeConnection(){}

    private void groupsToRemove(ActionMessage actionMessage) {
        serverRemoteObject.groupsToRemove(
                actionMessage.getInt("numberToKeep")
        );
    }


    /** Exports the RmiClient remote object
     *
     * @throws UnknownHostException if the local host name could not be resolved into an address
     * @throws RemoteException if remote object export fails
     * @throws MalformedURLException
     */
    private void exposeRmiClient(String nickname, ClientController clientController) throws UnknownHostException, RemoteException, MalformedURLException {
        String clientIpAddress = InetAddress.getLocalHost().getHostAddress();

        LocateRegistry.createRegistry(rmiClientPortNumber);
        RMIClient client = new RMIClientImpl(nickname, clientController);
        RMIClient stub = (RMIClient) UnicastRemoteObject.exportObject(client, 0);
        Naming.rebind("rmi://"+clientIpAddress+":"+rmiClientPortNumber+"/Client", stub);
    }

    @Override
    public void send(ActionMessage message) {
        try {
            mapMethods.get(message.getActionName()).accept(message);
        }
        catch(Exception e){
            return;
        }
    }

    @Override
    public boolean connectToServer(String nickname, int playersNumber, int gameLevel) throws IOException, NotBoundException {
        exposeRmiClient(nickname, clientController);

        Registry serverRegistry = LocateRegistry.getRegistry(serverIpAddress, rmiServerPortNumber);
        RMIWelcome welcomeProxy = (RMIWelcome) serverRegistry.lookup("rmi://"+serverIpAddress+":"+rmiServerPortNumber+"/Welcome");

        if(welcomeProxy.connectToServer(nickname, serverIpAddress, playersNumber, gameLevel)){
            serverRemoteObject = (RMIServer) serverRegistry.lookup("rmi://"+serverIpAddress+":"+rmiServerPortNumber+"/");
        }
        return true;
    }

    private void placeTileAt(ActionMessage actionMessage){
        serverRemoteObject.placeTileAt(
                (Direction)actionMessage.getData("direction"),
                (Coordinates)actionMessage.getData("coordinates"),
                (String)actionMessage.getData("fileName")
        );
    }

    private void bookTile(ActionMessage actionMessage){
        serverRemoteObject.bookTile();
    }

    private void viewDeck(ActionMessage actionMessage){
        serverRemoteObject.viewDeck(
                actionMessage.getInt("deckNumber")
        );
    }

    private void poseDeck(ActionMessage actionMessage) {
        serverRemoteObject.poseDeck(
                actionMessage.getInt("deckNumber")
        );
    }

    private void getCoveredTile(ActionMessage actionMessage){
        serverRemoteObject.getCoveredTile();
    }

    private void getUncoveredTile(ActionMessage actionMessage){
        serverRemoteObject.getUncoveredTile();
    }

    private void poseTile(ActionMessage actionMessage){
        serverRemoteObject.poseTile();
    }

    private void flipHourGlass(ActionMessage actionMessage){
        serverRemoteObject.flipHourGlass();
    }

    private void startTimer(ActionMessage actionMessage) {
        serverRemoteObject.startTimer(
                (Long)actionMessage.getData("millisecond")
        );
    }

    private void playerIsDone(ActionMessage actionMessage){
        serverRemoteObject.playerIsDone();
    }

    private void playerChoice(ActionMessage actionMessage){
        serverRemoteObject.playerChoice(
                (ArrayList<Coordinates>)actionMessage.getData("choisesToRemove")
        );
    }

    private void placeAliens(ActionMessage actionMessage){
        serverRemoteObject.placeAliens(
                (ArrayList<Coordinates>)actionMessage.getData("coordinatesChoosen")
        );
    }

    private void drawCard(ActionMessage actionMessage){
        serverRemoteObject.drawCard();
    }

    private void matchFinished(ActionMessage actionMessage){
        serverRemoteObject.matchFinished();
    }

    private void handleCard(ActionMessage actionMessage){
        serverRemoteObject.handleCard();
    }

    private void releaseCard(ActionMessage actionMessage){
        serverRemoteObject.releaseCard();
    }

    private void activateCannon(ActionMessage actionMessage) {
        serverRemoteObject.activateCannon(
                (Direction)actionMessage.getData("cannonDirection"),
                (Coordinates) actionMessage.getData("cannonCoordinates"),
                (Coordinates)actionMessage.getData("batteryCoordinates")
        );
    }

    private void setCargoToRemove(ActionMessage actionMessage) {
        serverRemoteObject.setCargoToRemove(
                (HashMap<Coordinates, CargoType>)actionMessage.getData("cargoType")
        );
    }

    private void setFirePower(ActionMessage actionMessage) {
        serverRemoteObject.setFirePower(
                (ArrayList<Coordinates>)actionMessage.getData("doubleCannonsCoordinates"),
                (HashMap<Coordinates, Integer>)actionMessage.getData("batteriesCoordinates")
        );
    }

    private void setEnginePower(ActionMessage actionMessage) {
        serverRemoteObject.setEnginePower(
                (ArrayList<Coordinates>)actionMessage.getData("doubleCannonsCoordinates"),
                (HashMap<Coordinates, Integer>)actionMessage.getData("batteriesCoordinates")
        );
    }

    private void activateShild(ActionMessage actionMessage) {
        serverRemoteObject.activateShild(
                (Pair<Direction, Direction>)actionMessage.getData("shildDirections"),
                (Coordinates)actionMessage.getData("batteryCoordinates")
        );
    }

    private void setCabinCoordinates(ActionMessage actionMessage) {
        serverRemoteObject.setCabinCoordinates(
                (HashMap<Coordinates, Integer>) actionMessage.getData("cabinCoordinates")
        );
    }

    private void setCargoCoordinates(ActionMessage actionMessage) {
        serverRemoteObject.setCargoCoordinates(
                (HashMap<CargoType, Coordinates>)actionMessage.getData("cargoCoordinates")
        );
    }

    private void landAbandonedShip(ActionMessage actionMessage) {
        serverRemoteObject.landAbandonedShip();
    }

    private void landAbandonedStation(ActionMessage actionMessage) {
        serverRemoteObject.landAbandonedStation();
    }

    //----------------------------------------------------------------------------
    //TODO cosa deve fare???
    private void pong(ActionMessage actionMessage){}
}
