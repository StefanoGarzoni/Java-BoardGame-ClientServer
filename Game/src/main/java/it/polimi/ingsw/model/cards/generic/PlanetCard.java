package it.polimi.ingsw.model.cards.generic;

import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.util.Planet;
import it.polimi.ingsw.model.cards.visitor.GenericCardVisitor;

import java.util.ArrayList;

public class PlanetCard extends Card {
    private ArrayList<Planet> planets;
    private int flightDayLoss;

    public PlanetCard(ArrayList<Planet> planets, int flightDayLoss, int cardLevel, String filename) {
        super(cardLevel, filename);
        this.planets = planets;
        this.flightDayLoss = flightDayLoss;
    }

    //Consider getCargo(index) instead
    public ArrayList<Planet> getPlanets() {
        return planets;
    }

    public int getFlightDayLoss() {
        return flightDayLoss;
    }

    @Override
    public void accept(GenericCardVisitor visitor) {
        visitor.visit(this);
    }
}
