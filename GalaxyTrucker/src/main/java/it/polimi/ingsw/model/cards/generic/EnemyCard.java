package it.polimi.ingsw.model.cards.generic;

import it.polimi.ingsw.model.cards.Card;

public abstract class EnemyCard extends Card {
    protected int flightDayLoss;
    protected int requiredFirePower;

    protected EnemyCard(int flightDayLoss, int requiredFirePower,int cardLevel, String filename)  {
        super(cardLevel, filename);
        this.flightDayLoss = flightDayLoss;
        this.requiredFirePower = requiredFirePower;
    }


    public int getFlightDayLoss() {
        return flightDayLoss;
    }

    public int getRequiredFirePower() {
        return requiredFirePower;
    }

}
