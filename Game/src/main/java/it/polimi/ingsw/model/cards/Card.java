package it.polimi.ingsw.model.cards;

import it.polimi.ingsw.model.cards.visitor.GenericCardVisitor;

public abstract class Card {
    protected final int cardLevel;
    protected final String filename;

    protected Card(int cardLevel, String filename) {
        this.cardLevel = cardLevel;
        this.filename = filename;
    }

    public int getCardLevel() {
        return cardLevel;
    }

    public String getFileName() {
        return filename;
    }

    public abstract void accept(GenericCardVisitor visitor);
}
