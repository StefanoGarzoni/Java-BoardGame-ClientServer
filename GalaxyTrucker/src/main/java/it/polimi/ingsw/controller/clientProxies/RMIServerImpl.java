package it.polimi.ingsw.controller.clientProxies;

import com.sun.javafx.scene.traversal.ContainerTabOrder;
import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.controller.Controller;
import it.polimi.ingsw.model.Cargo.CargoType;
import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.Direction;
import it.polimi.ingsw.model.cards.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RMIServerImpl implements RMIServer{
    private final String playerNickname;
    private final Controller controller;

    RMIServerImpl(String playerNickname, Controller controller){
        this.playerNickname = playerNickname;
        this.controller = controller;
    }


    @Override
    public void placeTileAt(Direction direction, Coordinates coordinates, String fileName) {
        ActionMessage am = new ActionMessage("placeTileAt", playerNickname);
        am.setData("direction", direction);
        am.setData("coordinates", coordinates);
        am.setData("fileName", fileName);

        //TODO a cosa serve mandare il playerNickname al controller?
        controller.sendActionToGame(am);
    }

    @Override
    public void bookTile() {
        ActionMessage am = new ActionMessage("bookTile", playerNickname);
        controller.sendActionToGame(am);
    }

    @Override
    public void viewDeck(int deckNumber) {
        ActionMessage am = new ActionMessage("viewDeck", playerNickname);
        am.setData("deckNumber", deckNumber);
        controller.sendActionToGame(am);
    }

    @Override
    public void poseDeck(int deckNumber) {
        ActionMessage am = new ActionMessage("poseDeck", playerNickname);
        am.setData("deckNumber", deckNumber);
        controller.sendActionToGame(am);
    }

    @Override
    public void getCoveredTile() {
        ActionMessage am = new ActionMessage("getCoveredTile", playerNickname);
        controller.sendActionToGame(am);
    }

    @Override
    public void getUncoveredTile() {
        ActionMessage am = new ActionMessage("getUncoveredTile", playerNickname);
        controller.sendActionToGame(am);
    }

    @Override
    public void poseTile() {
        ActionMessage am = new ActionMessage("poseTile", playerNickname);
        controller.sendActionToGame(am);
    }

    @Override
    public void flipHourGlass() {
        ActionMessage am = new ActionMessage("flipHourGlass", playerNickname);
        controller.sendActionToGame(am);
    }

    @Override
    public void startTimer(long milliseconds) {
        ActionMessage am = new ActionMessage("startTimer", playerNickname);
        am.setData("milliseconds", milliseconds);
        controller.sendActionToGame(am);
    }

    @Override
    public void playerIsDone() {
        ActionMessage am = new ActionMessage("playerIsDone", playerNickname);
        controller.sendActionToGame(am);
    }

    @Override
    public void playerChoice(ArrayList<Coordinates> choisesToRemove) {
        ActionMessage am = new ActionMessage("playerChoice", playerNickname);
        am.setData("choisesToRemove", choisesToRemove);
        controller.sendActionToGame(am);
    }

    @Override
    public void groupsToRemove(Integer numberToKeep) {
        ActionMessage am = new ActionMessage("groupsToRemove", playerNickname);
        am.setData("numberToKeep", numberToKeep);
        controller.sendActionToGame(am);
    }

    @Override
    public void placeAliens(ArrayList<Coordinates> coordinatesChoosen) {
        ActionMessage am = new ActionMessage("placeAliens", playerNickname);
        am.setData("coordinatesChoosen", coordinatesChoosen);
        controller.sendActionToGame(am);
    }

    @Override
    public void drawCard() {
        ActionMessage am = new ActionMessage("drawCard", playerNickname);
        controller.sendActionToGame(am);
    }

    @Override
    public void matchFinished() {
        ActionMessage am = new ActionMessage("matchFinished", playerNickname);
        controller.sendActionToGame(am);
    }

    @Override
    public void handleCard() {
        ActionMessage am = new ActionMessage("handleCard", playerNickname);
        controller.sendActionToGame(am);
    }

    @Override
    public void releaseCard() {
        ActionMessage am = new ActionMessage("releaseCard", playerNickname);
        controller.sendActionToGame(am);
    }

    @Override
    public void landAbandonedStation() {
        ActionMessage am = new ActionMessage("landAbandonedStation", playerNickname);
        controller.sendActionToGame(am);
    }

    @Override
    public void landAbandonedShip() {
        ActionMessage am = new ActionMessage("landAbandonedShip", playerNickname);
        controller.sendActionToGame(am);
    }

    @Override
    public void setCargoCoordinates(HashMap<CargoType, Coordinates> cargoCoordinates) {
        ActionMessage am = new ActionMessage("setCargoCoordinates", playerNickname);
        am.setData("cargoCoordinates",cargoCoordinates);
        controller.sendActionToGame(am);
    }

    @Override
    public void setCabinCoordinates(HashMap<Coordinates, Integer> cabinCoordinates) {
        ActionMessage am = new ActionMessage("setCabinCoordinates", playerNickname);
        am.setData("cabinCoordinates",cabinCoordinates);
        controller.sendActionToGame(am);
    }

    @Override
    public void activateShild(Pair<Direction, Direction> shildDirections, Coordinates batteryCoordinates) {
        ActionMessage am = new ActionMessage("landAbandonedShip", playerNickname);
        am.setData("batteryCoordinates", batteryCoordinates);
        am.setData("shildDirections", shildDirections);
        controller.sendActionToGame(am);
    }

    @Override
    public void setEnginePower(ArrayList<Coordinates> doubleEngineCoordinates, HashMap<Coordinates, Integer> batteriesCoordinates) {
        ActionMessage am = new ActionMessage("setEnginePower", playerNickname);
        am.setData("doubleEngineCoordinates", doubleEngineCoordinates);
        am.setData("batteriesCoordinates", batteriesCoordinates);
        controller.sendActionToGame(am);
    }

    @Override
    public void setFirePower(ArrayList<Coordinates> doubleCannonsCoordinates, HashMap<Coordinates, Integer> batteriesCoordinates) {
        ActionMessage am = new ActionMessage("setFirePower", playerNickname);
        am.setData("doubleCannonsCoordinates", doubleCannonsCoordinates);
        am.setData("batteriesCoordinates", batteriesCoordinates);
        controller.sendActionToGame(am);
    }

    @Override
    public void setCargoToRemove(HashMap<Coordinates, CargoType> cargoType) {
        ActionMessage am = new ActionMessage("setCargoToRemove", playerNickname);
        am.setData("cargoType", cargoType);
        controller.sendActionToGame(am);
    }

    @Override
    public void activateCannon(Direction cannonDirection, Coordinates cannonCoordinates, Coordinates batteryCoordinates) {
        ActionMessage am = new ActionMessage("activateCannon", playerNickname);
        am.setData("cannonCoordinates", cannonCoordinates);
        am.setData("batteryCoordinates", batteryCoordinates);
        am.setData("cannonDirection", cannonDirection);
        controller.sendActionToGame(am);
    }


    public void pong(){
        ActionMessage actionMessage = new ActionMessage("PONG", playerNickname);
        controller.sendActionToGame(actionMessage);
    }
}
