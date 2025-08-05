package it.polimi.ingsw.model.cards.visitor;

import it.polimi.ingsw.model.cards.generic.*;

public interface GenericCardVisitor {
    void visit(AbandonedShipCard card);
    void visit(AbandonedStationCard card);
    void visit(EnslaversCard card);
    void visit(EpidemicCard card);
    void visit(MeteorSwarmCard card);
    void visit(OpenSpaceCard card);
    void visit(PiratesCard card);
    void visit(PlanetCard card);
    void visit(SmugglersCard card);
    void visit(StardustCard card);
    void visit(WarZoneCard card);
}
