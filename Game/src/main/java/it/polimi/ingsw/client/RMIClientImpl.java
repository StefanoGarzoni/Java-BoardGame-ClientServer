package it.polimi.ingsw.client;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.controller.Controller;
import it.polimi.ingsw.model.Cargo.CargoType;
import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.Deck;
import it.polimi.ingsw.model.Direction;
import it.polimi.ingsw.model.ShipBoard;

import java.util.ArrayList;
import java.util.Map;

public class RMIClientImpl implements RMIClient {
    private final String playerNickname;
    private final ClientController controller;

    RMIClientImpl(String playerNickname, ClientController controller){
        this.playerNickname = playerNickname;
        this.controller = controller;
    }


    @Override
    public void whereYouAre(String state) {
        ActionMessage am = new ActionMessage(state, "server");
        controller.getCurrentState().processFromServer(am);
    }

    @Override
    public void giveYouColor(Map<String, String> playerColors) {
        ActionMessage am = new ActionMessage("colors", "server");
        am.setData("playerColor", playerColors);
        controller.getCurrentState().processFromServer(am);
    }

    @Override
    public void tileIsBooked(String receiver, String tileFileName) {
        ActionMessage am = new ActionMessage("tileIsBooked", "server");
        am.setReceiver(receiver);
        am.setData("fileName", tileFileName);
        controller.getCurrentState().processFromServer(am);
    }

    @Override
    public void tileIsFree(String receiver, String tileFileName) {
        ActionMessage am = new ActionMessage("tileIsFree", "server");
        am.setReceiver(receiver);
        am.setData("fileName", tileFileName);
        controller.getCurrentState().processFromServer(am);
    }

    @Override
    public void sendTileCovered(String receiver, String tileFileName) {
        ActionMessage am = new ActionMessage("tileCovered", "server");
        am.setReceiver(receiver);
        am.setData("fileName", tileFileName);
        controller.getCurrentState().processFromServer(am);
    }

    @Override
    public void tileUncoveredTaken(String receiver, String tileFileName) {
        ActionMessage am = new ActionMessage("tileUncoveredTaken", "server");
        am.setReceiver(receiver);
        am.setData("fileName", tileFileName);
        controller.getCurrentState().processFromServer(am);
    }

    @Override
    public void tileIsPlaced(String receiver, String tileFileName, Coordinates coordinates, Direction direction) {
        ActionMessage am = new ActionMessage("tileIsPlaced", "server");
        am.setReceiver(receiver);
        am.setData("fileName", tileFileName);
        am.setData("coordinates", coordinates);
        am.setData("direction", direction);
        controller.getCurrentState().processFromServer(am);
    }

    @Override
    public void deckIsViewing(String playerNickName, Integer deckNumber, Deck deck) {
        ActionMessage am = new ActionMessage("deckIsViewing", "server");
        am.setData("player",playerNickName);
        am.setData("deck", deck);
        am.setData("deckNumber", deckNumber);
        controller.getCurrentState().processFromServer(am);
    }

    @Override
    public void deckReleased(String playerNickName, Integer deckNumber) {
        ActionMessage am = new ActionMessage("deckIsViewing", "server");
        am.setData("player",playerNickName);
        am.setData("deckNumber", deckNumber);
        controller.getCurrentState().processFromServer(am);
    }

    @Override
    public void playerIsDoneConfermation(String playerNickName) {
        ActionMessage am = new ActionMessage("playerIsDoneConfermation", "server");
        am.setData("player",playerNickName);
        controller.getCurrentState().processFromServer(am);
    }

    @Override
    public void canFlipHG(Boolean permit, Boolean isLastTime, String playerHasToSendMilliseconds) {
        ActionMessage am = new ActionMessage("canFlipHG", "server");
        am.setData("permit",permit);
        am.setData("isLastTime",isLastTime);
        am.setData("playerHasToSendMilliseconds",playerHasToSendMilliseconds);
        controller.getCurrentState().processFromServer(am);
    }

    @Override
    public void finishedTimerConfermation() {
        ActionMessage am = new ActionMessage("finishedTimerConfermation", "server");
        controller.getCurrentState().processFromServer(am);
    }

    @Override
    public void wrongTilesCouplings(String receiver, ArrayList<ArrayList<Coordinates>> couplings, ArrayList<Coordinates> wrongEngines) {
        ActionMessage am = new ActionMessage("wrongTilesCouplings", "server");
        am.setReceiver(receiver);
        am.setData("couplings",couplings);
        am.setData("wrongEngines",wrongEngines);
        controller.getCurrentState().processFromServer(am);
    }

    @Override
    public void tilesBranchsRemoved(String receiver, ShipBoard shipboard) {
        ActionMessage am = new ActionMessage("tilesBranchsRemoved", "server");
        am.setData("shipBoard",shipboard);
        am.setReceiver(receiver);
        controller.getCurrentState().processFromServer(am);
    }

    @Override
    public void possibleAliensPosition(String receiver, ArrayList<ArrayList<Coordinates>> positions) {
        ActionMessage am = new ActionMessage("possibleAliensPosition", "server");
        am.setReceiver(receiver);
        am.setData("positions", positions);
        controller.getCurrentState().processFromServer(am);
    }

    @Override
    public void resourcesDistributedConfermation(String receiver, ShipBoard shipBoard) {
        ActionMessage am = new ActionMessage("resourcesDistributedConfermation", "server");
        am.setReceiver(receiver);
        am.setData("shipBoard", shipBoard);
        controller.getCurrentState().processFromServer(am);
    }

    @Override
    public void cardReleased() {
        ActionMessage am = new ActionMessage("cardReleased", "server");
        controller.getCurrentState().processFromServer(am);
    }

    @Override
    public void cardDrawing(String receiver, Boolean isLastCard, String cardFileName) {
        ActionMessage am = new ActionMessage("cardReleased", "server");
        am.setReceiver(receiver);
        am.setData("isLastCard", isLastCard);
        am.setData("fileName", cardFileName);
        controller.getCurrentState().processFromServer(am);
    }

    @Override
    public void cardsFinished() {
        ActionMessage am = new ActionMessage("cardsFinished", "server");
        controller.getCurrentState().processFromServer(am);
    }

    @Override
    public void finalScores(String receiver, Integer scores) {
        ActionMessage am = new ActionMessage("finalScores", "server");
        am.setReceiver(receiver);
        am.setData("scores", scores);
        controller.getCurrentState().processFromServer(am);
    }

    @Override
    public void chooseWhichGroupsKeep(String receiver, ArrayList<ArrayList<Coordinates>> groups) {
        ActionMessage am = new ActionMessage("chooseWhichGroupsKeep", "server");
        am.setReceiver(receiver);
        am.setData("groups", groups);
        controller.getCurrentState().processFromServer(am);
    }

    @Override
    public void decksCreated(ArrayList<Deck> decks) {
        ActionMessage am = new ActionMessage("decksCreated", "server");
        am.setData("decks", decks);
        controller.getCurrentState().processFromServer(am);
    }

    @Override
    public void abandonedShipLanding(String receiver) {
        ActionMessage am = new ActionMessage("abandonedShipLanding", "server");
        am.setReceiver(receiver);
        controller.getCurrentState().processFromServer(am);
    }

    @Override
    public void abandonedStationLanding(String receiver) {
        ActionMessage am = new ActionMessage("abandonedStationLanding", "server");
        am.setReceiver(receiver);
        controller.getCurrentState().processFromServer(am);
    }

    @Override
    public void cardInGame(String receiver, String fileName) {
        ActionMessage am = new ActionMessage("cardInGame", "server");
        am.setReceiver(receiver);
        am.setData("fileName", fileName);
        controller.getCurrentState().processFromServer(am);
    }

    @Override
    public void solveAbandonedShip(String receiver, int claimerPlayerPos, ShipBoard claimerPlayerShipboard, int claimerPlayerCredits) {
        ActionMessage am = new ActionMessage("solveAbandonedShip", "server");
        am.setReceiver(receiver);
        am.setData("claimerPlayerPos", claimerPlayerPos);
        am.setData("claimerPlayerShipboard", claimerPlayerShipboard);
        am.setData("claimerPlayerCredits", claimerPlayerCredits);
        controller.getCurrentState().processFromServer(am);
    }

    @Override
    public void solveAbandonedStation(String receiver, int claimerPlayerPos, ShipBoard claimerPlayerShipboard) {
        ActionMessage am = new ActionMessage("solveAbandonedStation", "server");
        am.setReceiver(receiver);
        am.setData("claimerPlayerPos", claimerPlayerPos);
        am.setData("claimerPlayerShipboard", claimerPlayerShipboard);
        controller.getCurrentState().processFromServer(am);
    }

    @Override
    public void solveEpidemicCard(Map<String, ShipBoard> updatedShipboards) {
        ActionMessage am = new ActionMessage("solveEpidemicCard", "server");
        am.setData("updatedShipboards", updatedShipboards);
        controller.getCurrentState().processFromServer(am);
    }

    @Override
    public void currentShot(int shotIndex, int currentRoll) {
        ActionMessage am = new ActionMessage("currentShot", "server");
        am.setData("shotIndex", shotIndex);
        am.setData("currentRoll", currentRoll);
        controller.getCurrentState().processFromServer(am);
    }

    @Override
    public void destroyTile(String receiver, int shotIndex) {
        ActionMessage am = new ActionMessage("destroyTile", "server");
        am.setData("shotIndex", shotIndex);
        am.setReceiver(receiver);
        controller.getCurrentState().processFromServer(am);
    }

    @Override
    public void askEngineUsage(String receiver, Map<Coordinates, Integer> doubleEngines) {
        ActionMessage am = new ActionMessage("askEngineUsage", "server");
        am.setReceiver(receiver);
        am.setData("doubleEngines", doubleEngines);
        controller.getCurrentState().processFromServer(am);
    }

    @Override
    public void solveOpenSpace(Map<String, Integer> updatedPositions) {
        ActionMessage am = new ActionMessage("solveOpenSpace", "server");
        am.setData("updatedPositions", updatedPositions);
        controller.getCurrentState().processFromServer(am);
    }

    @Override
    public void loadCargo(String receiver, ArrayList<CargoType> cargoList) {
        ActionMessage am = new ActionMessage("loadCargo", "server");
        am.setReceiver(receiver);
        am.setData("cargoList", cargoList);
        controller.getCurrentState().processFromServer(am);
    }

    @Override
    public void solvePlanetCard(Map<String, ShipBoard> updatedShipboards, Map<String, Integer> updatedPositions) {
        ActionMessage am = new ActionMessage("solvePlanetCard", "server");
        am.setData("updatedShipboards", updatedShipboards);
        am.setData("updatedPositions", updatedPositions);
        controller.getCurrentState().processFromServer(am);
    }

    @Override
    public void solveStardust(Map<String, Integer> updatedPositions) {
        ActionMessage am = new ActionMessage("solveStardust", "server");
        am.setData("updatedPositions", updatedPositions);
        controller.getCurrentState().processFromServer(am);
    }

    @Override
    public void leastCrewPlayer(String receiver) {
        ActionMessage am = new ActionMessage("leastCrewPlayer", "server");
        am.setReceiver(receiver);
        controller.getCurrentState().processFromServer(am);
    }

    @Override
    public void solveLeastCrew(String receiver, int flightDayPenalty) {
        ActionMessage am = new ActionMessage("solveLeastCrew", "server");
        am.setReceiver(receiver);
        am.setData("flightDayPenalty", flightDayPenalty);
        controller.getCurrentState().processFromServer(am);
    }

    public void pong(){
        ActionMessage actionMessage = new ActionMessage("PONG", playerNickname);
        controller.getCurrentState().processFromServer(actionMessage);
    }
}
