package it.polimi.ingsw.client;

import it.polimi.ingsw.model.ComponentTile.ComponentTile;
import it.polimi.ingsw.model.ComponentTilesBunch;
import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.ShipBoard;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.clientModel.ClientComponentTilesBunch;
import it.polimi.ingsw.model.clientModel.ClientDeck;
import it.polimi.ingsw.model.clientModel.ViewFlightBoard;
import it.polimi.ingsw.model.clientModel.ViewShipBoard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface View {

    void showCreateGame();
    void showLobby(ArrayList<Player> players);
    void showTileBunch(ClientComponentTilesBunch clientComponentTilesBunch);
    void showTileInHand(ComponentTile tile);
    void showShipboard(ViewShipBoard shipBoard);
    void showDeck(ClientDeck deck);
    void showHourGlass(long time);

    void showAlienPlacements();

    void showBatteries();

    void showCrew();

    void showCargo();

    void showFlightBoard(ViewFlightBoard viewFlightBoard);

    void showFlightBoard();

    void showHighlightedTiles(ArrayList<Coordinates> highlightedTiles);

    void showCard(Card cardData);

    void showBrokenTile();

    void showMessage(String message);

    void showMessage(List<String> messages);

    void showErrorMessage(String message);

    void showDices();

    void showScoreBoard(ViewShipBoard[] viewShipBoards);

    void showDisconnect();

}
