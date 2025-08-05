package it.polimi.ingsw.model.cards.generic;

import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.util.MeteorShot;
import it.polimi.ingsw.model.cards.visitor.GenericCardVisitor;

import java.util.ArrayList;

public class MeteorSwarmCard extends Card {

    private final ArrayList<MeteorShot> meteorList;

    public MeteorSwarmCard(ArrayList<MeteorShot> meteorList, int cardLevel, String fileName) {
        super(cardLevel, fileName);
        this.meteorList = meteorList;
    }

    public ArrayList<MeteorShot> getMeteorList() {
        return meteorList;
    }

    @Override
    public void accept(GenericCardVisitor visitor) {
        visitor.visit(this);
    }
}
