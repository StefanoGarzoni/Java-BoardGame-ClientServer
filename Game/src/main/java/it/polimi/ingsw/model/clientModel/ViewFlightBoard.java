package it.polimi.ingsw.model.clientModel;

import it.polimi.ingsw.model.Deck;
import it.polimi.ingsw.model.cards.client.ClientCard;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ViewFlightBoard {
    private int playersNum;
    private int gameLevel;

    private final ClientComponentTilesBunch clientTilesBunch;
    private final ViewShipBoard[] viewShipBoards ;
    private final AtomicInteger timer;
    private final ArrayList<ClientDeck> decks;

    public ViewFlightBoard(int playersNum) throws FileNotFoundException{
        this.playersNum = playersNum;
        this.gameLevel = 2;
        this.viewShipBoards = new ViewShipBoard[playersNum];
        clientTilesBunch = new ClientComponentTilesBunch();
        timer = new AtomicInteger();
        decks = new ArrayList<>();
    }

    /** Returns the position of a player during the game
     *
     * @param name nickname of the player
     * @return current position of the player in the game
     */
    private int getPlayerIndex(String name){
        for (int i = 0; i< playersNum; i++){
            if(viewShipBoards[i].getNickname().equals(name)){
                return i;
            }
        }
        return -1;
    }

    public ViewShipBoard getViewShipBoard(String name){
        return this.viewShipBoards[this.getPlayerIndex(name)];
    }

    public ViewShipBoard getViewShipBoard(int i) {
        return this.viewShipBoards[i];
    }

    public ViewShipBoard[] getViewShipBoards() {
        return viewShipBoards;
    }

    public int getTimer() {
        return timer.get();
    }
    public void setTimer(int secondsLeft){ timer.set(secondsLeft); }

    public void setDeckAtIndex(ClientDeck deck, int index){ decks.add(index, deck); }

    public Boolean isDeckSet(int index) { return decks.get(index) != null; }

    public Boolean isDeckFree(int index) {
        return decks.get(index).getWhoIsViewing() == null;
    }

    /** Gets the nickname of the player that is viewing the selected deck
     *
     * @param index of the deck to get viewer's nickname
     * @return nickname of the user who is viewing the deck, null if the deck is free
     */
    public String getWhoIsViewingDeck(int index){ return decks.get(index).getWhoIsViewing(); }

    /** Sets the nickname of the player that is viewing the selected deck
     *
     * @param index index of the deck that is being viewed
     * @param nickname name of the player who is currently viewing the deck
     */
    public void setWhoIsViewingDecks(int index, String nickname) { decks.get(index).setWhoIsViewing(nickname); }

    /** Returns the index of the deck that a player is viewing
     *
     * @param playerNickname nickname of the player to get deck
     * @return index of the deck
     */
    public int getDeckThatIsViewing(String playerNickname){
        int deckIndex = -1;

        for(int i = 0; i < decks.size(); i++){
            String whoIsViewingDeck = decks.get(i).getWhoIsViewing();
            if(whoIsViewingDeck != null && whoIsViewingDeck.equals(playerNickname)){
                deckIndex = i;
                break;
            }
        }

        return deckIndex;
    }

    public int getDecksNumber() { return decks.size(); }
    public ClientDeck getDeck(int index) { return decks.get(index); }

    /** Searches and returns a card by its fileName, if present in one deck.
     *
     * @param fileName fileName of the required card
     * @return required ClientCard, if present in one deck
     */
    public ClientCard getCardFromDecks(String fileName) {
        for(ClientDeck deck : decks){
            ClientCard cardToReturn = deck.getClientCard(fileName);
            if(cardToReturn != null){
                return cardToReturn;
            }
        }
        return null;
    }

    public ClientComponentTilesBunch getClientTilesBunch(){
        return clientTilesBunch;
    }

    /**
     * Method used by views to get the color and position of each player, to draw the flightboard
     * @return a map containing the color of each player and their position, used by the view
     */
    public Map<Character, Integer> getPlayerPositions(){
        Map <Character, Integer> playerPositions = new HashMap<>();
        for(ViewShipBoard viewShipBoard : viewShipBoards){
            ViewShipBoard playerShipboard = getViewShipBoard(viewShipBoard.getNickname());
            playerPositions.put(playerShipboard.getColor(), playerShipboard.getAbsPosition());
        }
        return playerPositions;
    }

    /**
     * Method used by views to get color and names of each player, to draw player information
     * @return a map containing the name of each player and their colors
     */
    public Map<String, Character> getPlayerNames(){
        Map<String, Character> playerNames = new HashMap<>();
        for(ViewShipBoard viewShipBoard : viewShipBoards){
            ViewShipBoard playerShipboard = getViewShipBoard(viewShipBoard.getNickname());
            playerNames.put(playerShipboard.getNickname(), playerShipboard.getColor());
        }
        return playerNames;
    }
}
