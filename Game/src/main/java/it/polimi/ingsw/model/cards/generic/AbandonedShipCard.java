package it.polimi.ingsw.model.cards.generic;

import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.visitor.GenericCardVisitor;

public class AbandonedShipCard extends Card {
    private final int creditReward;
    private final int flightDayLoss;
    private final int crewLoss;

    public AbandonedShipCard(int creditReward, int flightDayLoss, int crewLoss, int cardLevel, String fileName) {
        super(cardLevel, fileName);
        this.creditReward = creditReward;
        this.flightDayLoss = flightDayLoss;
        this.crewLoss = crewLoss;
    }

    public int getCreditReward() {
        return creditReward;
    }

    public int getFlightDayLoss() {
        return flightDayLoss;
    }

    public int getCrewLoss() {
        return crewLoss;
    }

    @Override
    public void accept(GenericCardVisitor visitor) {
        visitor.visit(this);
    }
}
