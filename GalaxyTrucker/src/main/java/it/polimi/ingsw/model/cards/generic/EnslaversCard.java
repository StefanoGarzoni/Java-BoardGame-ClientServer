package it.polimi.ingsw.model.cards.generic;

import it.polimi.ingsw.model.cards.visitor.GenericCardVisitor;

public class EnslaversCard extends EnemyCard {
    private int crewPenalty;
    private int creditPrize;

    public EnslaversCard(int creditPrize, int creditPenalty, int flightDayLoss, int requiredFirePower, int cardLevel, String filename) {
        super(flightDayLoss, requiredFirePower, cardLevel, filename);
        this.creditPrize = creditPrize;
        this.crewPenalty = creditPenalty;
    }

    public int getCreditPrize() {
        return creditPrize;
    }

    public int getCrewPenalty() {
        return crewPenalty;
    }

    @Override
    public void accept(GenericCardVisitor visitor) {
        visitor.visit(this);
    }
}
