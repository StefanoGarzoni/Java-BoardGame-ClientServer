package it.polimi.ingsw.model.clientModel;

import it.polimi.ingsw.model.Deck;
import it.polimi.ingsw.model.FileUploaders.CardsLoader;
import it.polimi.ingsw.model.cards.client.ClientCard;
import it.polimi.ingsw.model.cards.server.ServerCard;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class ClientDeck {


    private final ArrayList<ClientCard> cards;
    private String whoIsViewing;

    public ClientDeck(ArrayList<ClientCard> cards){
        this.cards = new ArrayList<>(cards);
    }

    /** Return and removes from the deck the card with the required fileName, if present.
     *
     * @param fileName fileName of the card to return
     * @return requested ClientCard, if present in deck. Otherwise, returns null.
     */
    public ClientCard getClientCard(String fileName){
        ClientCard cardToReturn = null;

        for(ClientCard card : cards){
            if(card.getFileName().equals(fileName)){
                cardToReturn = card;
                cards.remove(card);
                break;
            }
        }

        return cardToReturn;
    }

    /** Returns all ClientCards in ClientDeck
     *
     * @return list of ClientCard
     */
    public List<ClientCard> getAllClientCards(){ return cards; }

    /** Get the nickname of the player who is viewing the deck
     *
     * @return nickname
     */
    public String getWhoIsViewing(){ return whoIsViewing; }

    /** Sets the nickname of the player who is viewing this deck
     *
     * @param whoIsViewing Username of the player who is viewing
     */
    public void setWhoIsViewing(String whoIsViewing) {
        this.whoIsViewing = whoIsViewing;
    }
}
