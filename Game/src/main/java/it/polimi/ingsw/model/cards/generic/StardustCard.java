package it.polimi.ingsw.model.cards.generic;

import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.visitor.GenericCardVisitor;

public class StardustCard extends Card {
    public StardustCard(int level, String filename) {
        super(level, filename);
    }

    @Override
    public void accept(GenericCardVisitor visitor) {
        visitor.visit(this);
    }
}
