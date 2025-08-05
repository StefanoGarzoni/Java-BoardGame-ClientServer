package it.polimi.ingsw.client;

import it.polimi.ingsw.model.Cargo.CargoType;
import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.Deck;
import it.polimi.ingsw.model.Direction;
import it.polimi.ingsw.model.ShipBoard;

import java.rmi.Remote;
import java.util.ArrayList;
import java.util.Map;

public interface RMIClient extends Remote {

    void whereYouAre(String state);

    void giveYouColor(Map<String, String> playerColors);

    void tileIsBooked(String receiver, String tileFileName);

    void tileIsFree(String receiver, String tileFileName);

    void sendTileCovered(String receiver, String tileFileName);

    void tileUncoveredTaken(String receiver, String tileFileName);

    void tileIsPlaced(String receiver, String tileFileName, Coordinates coordinates, Direction direction);

    void deckIsViewing(String playerNickName, Integer deckNumber, Deck deck);

    void deckReleased(String playerNickName, Integer deckNumber);

    void playerIsDoneConfermation(String playerNickName);

    void canFlipHG(Boolean permit, Boolean isLastTime, String playerHasToSendMilliseconds);

    void finishedTimerConfermation();

    void wrongTilesCouplings(String receiver, ArrayList<ArrayList<Coordinates>> couplings, ArrayList<Coordinates> wrongEngines);

    void tilesBranchsRemoved(String receiver, ShipBoard shipboard);

    void possibleAliensPosition(String receiver, ArrayList<ArrayList<Coordinates>> positions);

    void resourcesDistributedConfermation(String receiver, ShipBoard shipBoard);

    void cardReleased();

    void cardDrawing(String receiver, Boolean isLastCard, String cardFileName);

    void cardsFinished();

    void finalScores(String receiver, Integer scores);

    void chooseWhichGroupsKeep(String receiver, ArrayList<ArrayList<Coordinates>> groups);

    void decksCreated(ArrayList<Deck> decks);

//--------------------------------------------PlayingState-----------------------------

    void abandonedShipLanding(String receiver);

    void abandonedStationLanding(String receiver);

    void cardInGame(String receiver, String fileName);

    void solveAbandonedShip(String receiver, int claimerPlayerPos, ShipBoard claimerPlayerShipboard, int claimerPlayerCredits);

    void solveAbandonedStation(String receiver, int claimerPlayerPos, ShipBoard claimerPlayerShipboard);

    void solveEpidemicCard(Map<String, ShipBoard> updatedShipboards);

    void currentShot(int shotIndex, int currentRoll);

    void destroyTile(String receiver, int shotIndex);

    void askEngineUsage(String receiver, Map<Coordinates, Integer> doubleEngines);

    void solveOpenSpace(Map<String, Integer> updatedPositions);

    void loadCargo(String receiver, ArrayList<CargoType> cargoList);

    void solvePlanetCard(Map<String, ShipBoard> updatedShipboards, Map<String, Integer> updatedPositions);

    void solveStardust(Map<String, Integer> updatedPositions);

    void leastCrewPlayer(String receiver);

    void solveLeastCrew(String receiver, int flightDayPenalty);


//-------------------------------------------------------------------------------------

    void pong();
}
