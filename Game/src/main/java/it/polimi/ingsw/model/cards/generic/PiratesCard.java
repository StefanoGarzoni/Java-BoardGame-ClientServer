package it.polimi.ingsw.model.cards.generic;

import it.polimi.ingsw.model.cards.util.FireShot;
import it.polimi.ingsw.model.cards.visitor.GenericCardVisitor;

import java.util.ArrayList;

public class PiratesCard extends EnemyCard{
    private ArrayList<FireShot> shotPenalty;
    int creditPrize;

    public PiratesCard(ArrayList<FireShot> shotPenalty, int creditPrize, int flightDayLoss, int requiredFirePower,
                       int cardLevel, String fileName) {
        super(flightDayLoss, requiredFirePower, cardLevel, fileName);
        this.shotPenalty = shotPenalty;
        this.creditPrize = creditPrize;
    }

    public int getCreditPrize() {
        return creditPrize;
    }

    public int getRequiredFirePower() {
        return requiredFirePower;
    }

    public ArrayList<FireShot> getShotPenalty() {
        return shotPenalty;
    }

    @Override
    public void accept(GenericCardVisitor visitor) {
        visitor.visit(this);
    }
}
