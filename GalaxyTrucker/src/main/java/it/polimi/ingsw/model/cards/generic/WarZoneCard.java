package it.polimi.ingsw.model.cards.generic;

import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.util.FireShot;
import it.polimi.ingsw.model.cards.visitor.GenericCardVisitor;

import java.util.ArrayList;

public class WarZoneCard extends Card {
    private int cargoLoss = -1;
    private int crewLoss = -1;
    private final int flightDayLoss;
    private ArrayList<FireShot> shotsList;

    public WarZoneCard(int cargoLoss, int crewLoss, int flightDayLoss, ArrayList<FireShot> shotsList, int cardLevel, String fileName) {
        super(cardLevel, fileName);
        this.cargoLoss = cargoLoss;
        this.crewLoss = crewLoss;
        this.flightDayLoss = flightDayLoss;
        this.shotsList = shotsList;
    }

    public int getCargoLoss() {
        return cargoLoss;
    }

    public int getCrewLoss() {
        return crewLoss;
    }

    public int getFlightDayLoss() {
        return flightDayLoss;
    }

    public ArrayList<FireShot> getShotsList() {
        return shotsList;
    }

    @Override
    public void accept(GenericCardVisitor visitor) {
        visitor.visit(this);
    }
}
