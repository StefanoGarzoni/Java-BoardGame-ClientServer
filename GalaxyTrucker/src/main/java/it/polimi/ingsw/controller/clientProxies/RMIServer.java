package it.polimi.ingsw.controller.clientProxies;

import it.polimi.ingsw.model.Cargo.CargoType;
import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.Direction;
import it.polimi.ingsw.model.cards.util.Pair;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public interface RMIServer extends Remote {
    void placeTileAt(Direction direction, Coordinates coordinates, String fileName);

    void bookTile();

    void viewDeck(int deckNumber);

    void poseDeck(int deckNumber) ;

    void getCoveredTile();

    void getUncoveredTile();

    void poseTile();

    void flipHourGlass();

    void startTimer(long milliseconds) ;

    void playerIsDone();

    void playerChoice(ArrayList<Coordinates> choisesToRemove);

    void groupsToRemove(Integer numberToKeep);

    void placeAliens(ArrayList<Coordinates> coordinatesChoosen);

    void drawCard();

    void matchFinished();

    void handleCard();

    void releaseCard();

//------------------------PlayingState--------------------------------------
    void landAbandonedStation();

    void landAbandonedShip();

    void setCargoCoordinates(HashMap<CargoType, Coordinates> cargoCoordinates);

    void setCabinCoordinates(HashMap<Coordinates, Integer> cabinCoordinates);

    void activateShild(Pair<Direction, Direction> shildDirections, Coordinates batteryCoordinates);

    void setEnginePower(ArrayList<Coordinates> doubleEngineCoordinates, HashMap<Coordinates, Integer> batteriesCoordinates);

    void setFirePower(ArrayList<Coordinates> doubleCannonsCoordinates, HashMap<Coordinates, Integer> batteriesCoordinates);

    void setCargoToRemove(HashMap<Coordinates, CargoType> cargoType);

    void activateCannon(Direction cannonDirection, Coordinates cannonCoordinates, Coordinates batteryCoordinates);
//----------------------------------------------------------------------------
    void pong();
}
