package it.polimi.ingsw.model.cards.generic;

import it.polimi.ingsw.model.Cargo.CargoType;
import it.polimi.ingsw.model.cards.visitor.GenericCardVisitor;

import java.util.ArrayList;

public class SmugglersCard extends EnemyCard{
    private int cargoPenalty;
    private final ArrayList<CargoType> cargoPrize;

    public SmugglersCard(int cargoPenalty, ArrayList<CargoType> cargoPrize, int flightDayLoss, int requiredFirePower,
                         int cardLevel, String fileName) {
        super(flightDayLoss, requiredFirePower, cardLevel, fileName);
        this.cargoPenalty = cargoPenalty;
        this.cargoPrize = cargoPrize;
    }

    public int getCargoPenalty() {
        return cargoPenalty;
    }

    public ArrayList<CargoType> getCargoPrize() {
        return cargoPrize;
    }

    @Override
    public void accept(GenericCardVisitor visitor) {
        visitor.visit(this);
    }
}
