package it.polimi.ingsw.model.cards.generic;

import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.visitor.GenericCardVisitor;

public class OpenSpaceCard extends Card {
    public OpenSpaceCard(int cardLevel, String fileName) {
        super(cardLevel, fileName);
    }

    @Override
    public void accept(GenericCardVisitor visitor) {
        visitor.visit(this);
    }
}
