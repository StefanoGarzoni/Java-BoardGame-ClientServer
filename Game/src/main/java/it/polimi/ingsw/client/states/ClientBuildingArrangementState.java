package it.polimi.ingsw.client.states;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.client.ClientController;
import it.polimi.ingsw.model.ComponentTile.CabinTile;
import it.polimi.ingsw.model.ComponentTile.ComponentTile;
import it.polimi.ingsw.model.ComponentTile.Connector;
import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.Deck;
import it.polimi.ingsw.model.Direction;
import it.polimi.ingsw.model.FileUploaders.CardsLoader;
import it.polimi.ingsw.model.cards.client.ClientCard;
import it.polimi.ingsw.model.cards.server.ServerCard;
import it.polimi.ingsw.model.clientModel.ClientDeck;
import it.polimi.ingsw.model.clientModel.ViewFlightBoard;
import it.polimi.ingsw.model.clientModel.ViewShipBoard;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.function.Consumer;

public class ClientBuildingArrangementState extends ClientState{
    private final String cardsFilePath = "gameAssets/cards.json";

    public ClientBuildingArrangementState(ClientController clientController){
        super(clientController);
    }

    /** Handles a "Color" message. This method setups all shipboards and sets players color
     *
     * @param actionMessage containing colors of players
     */
    public void setBuildingArrangementState(ActionMessage actionMessage){
        ViewFlightBoard viewFlightBoard;
        try {
            viewFlightBoard = new ViewFlightBoard(actionMessage.getKeysParams().size());
        }
        catch (FileNotFoundException e){
            // TODO: shall we block the execution and leave the game?
            System.out.println("ERROR: exception in setBuildingArrangementState");
            return;
        }

        this.clientController.setViewFlightBoard(viewFlightBoard);
        Set<String> names = actionMessage.getKeysParams();
        int i=0;
        for (var name: names){
            Character color = (Character) actionMessage.getData(name);
            viewFlightBoard.getViewShipBoards()[i] = new ViewShipBoard(name, color);

            ComponentTile centralCabin = createCentralCabin(color);
            viewFlightBoard.getViewShipBoards()[i].fixComponentTile(centralCabin, new Coordinates(2,3), Direction.NORTH);

            i++;
        }
    }

    /** Returns a central cabin with the selected color
     *
     * @param color of the central cabin
     * @return central cabin
     */
    private ComponentTile createCentralCabin(Character color){
        String fileName = switch (color) {
            case 'Y' -> "GT-new_tiles_16_for_web61.jpg";
            case 'R' -> "GT-new_tiles_16_for_web52.jpg";
            case 'G' -> "GT-new_tiles_16_for_web34.jpg";
            default -> "GT-new_tiles_16_for_web33.jpg";     // blue
        };

        return new CabinTile(fileName,
                new Connector(3),
                new Connector(3),
                new Connector(3),
                new Connector(3)
        );
    }

    /** Extracts all server's decks sent by the server and inserts a new ClientDeck among client's decks
     *
     * @param actionMessage message from the server, containing the list of Decks
     */
    private void setDecks(ActionMessage actionMessage){
        ArrayList<Deck> decks = (ArrayList<Deck>) actionMessage.getData("decks");
        System.out.println("decks: " + decks.size());

        // all cards loading
        CardsLoader cardsLoader;
        try{
            cardsLoader = new CardsLoader(cardsFilePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace(System.out);
            return;
        }

        // for each server deck, creates a client deck with the same cards
        for(int i = 0; i < decks.size(); i++){
            ArrayList<ClientCard> clientCards = new ArrayList<>();

            for(ServerCard card : decks.get(i).getAllCards()){
                ClientCard newClientCard = cardsLoader.getClientCard(card.getFileName());
                clientCards.add(newClientCard);
            }

            ClientDeck newClientDeck = new ClientDeck(clientCards);
            clientController.getViewFlightBoard().setDeckAtIndex(newClientDeck, i);
        }
    }

    @Override
    public void run() {
        this.methodsFromServerMap.put("Colors", this::setBuildingArrangementState);
        this.methodsFromServerMap.put("decksCreated", this::setDecks);
        this.methodsFromServerMap.put("ClientShipBoardBuildingState", this::nextState);
    }

    @Override
    public void processFromServer(ActionMessage actionMessage) {
        synchronized (clientController){
            this.methodsFromServerMap.get(actionMessage.getActionName()).accept(actionMessage);
        }
    }

    @Override
    public void processFromView(ActionMessage actionMessage) {
        synchronized (clientController) {
            this.methodsFromViewMap.get(actionMessage.getActionName()).accept(actionMessage);
        }
    }


    private void nextState(ActionMessage actionMessage){
        this.clientController.setState(new ClientShipBoardBuildingState(this.clientController));
        this.clientController.getCurrentState().run();
    }
}
