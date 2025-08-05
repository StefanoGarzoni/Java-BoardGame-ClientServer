package it.polimi.ingsw.model.cards.generic;

import it.polimi.ingsw.model.Cargo.CargoType;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.visitor.GenericCardVisitor;

import java.util.ArrayList;

public class AbandonedStationCard extends Card {
    private ArrayList<CargoType> cargoList;
    private int flightDayLoss;
    private int requiredCrew;

    public AbandonedStationCard(int flightDayLoss, int requiredCrew, ArrayList<CargoType> cargoList, String filename, int cardLevel) {
        super(cardLevel, filename);
        this.flightDayLoss = flightDayLoss;
        this.requiredCrew = requiredCrew;
        this.cargoList = cargoList;
    }

    public ArrayList<CargoType> getCargoList() {
        return cargoList;
    }

    public int getFlightDayLoss() {
        return flightDayLoss;
    }

    public int getRequiredCrew() {
        return requiredCrew;
    }

    @Override
    public void accept(GenericCardVisitor visitor) {
        visitor.visit(this);
    }
}
