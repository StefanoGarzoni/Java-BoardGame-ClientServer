package it.polimi.ingsw.model;

import it.polimi.ingsw.model.cards.server.ServerCard;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Deck {
    ArrayList<ServerCard> cards;
    Random rand;
    String whoTookIt;

    public Deck(ArrayList<ServerCard> cards){
        this.cards = new ArrayList<ServerCard>(cards);
        this.rand = new Random();
        this.whoTookIt = null;
    }

    public List<ServerCard> getAllCards(){ return cards; }

    /**
     * @return Random card from the deck
     */
    public ServerCard randomCardDrawing() {
        int index = rand.nextInt(cards.size());
        ServerCard toReturnCard = cards.get(index);
        cards.remove(index);
        return toReturnCard;
    }

    public int getSize() { return cards.size(); }

    public boolean isFree() {
        return whoTookIt == null;
    }

    public void freeDeck(String nickname){
        if(whoTookIt.equals(nickname))
            whoTookIt = null;
    }
    public void takeDeck(String nickname){
        if( nickname!=null)
            whoTookIt = nickname;
    }
    public String getWhoTookIt() { return this.whoTookIt; }
}
