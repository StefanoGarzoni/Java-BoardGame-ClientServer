package it.polimi.ingsw.model.cards.visitor;

import it.polimi.ingsw.model.ModelPublisher;
import it.polimi.ingsw.model.cards.server.EnemyServerCard;
import it.polimi.ingsw.model.cards.server.concrete.*;
import it.polimi.ingsw.model.cards.server.concrete.AbandonedShipServerCard;
import it.polimi.ingsw.model.states.PlayingState;

public interface ServerCardVisitor {
    public void visitForPlanet(PlanetServerCard planetCard, ModelPublisher publisher, PlayingState playingState);
    public void visitForAbandonedShip(AbandonedShipServerCard abandonedShipCard, ModelPublisher publisher, PlayingState playingState);
    public void visitForAbandonedStation(AbandonedStationServerCard abandonedStationCard, ModelPublisher publisher, PlayingState playingState);
    public void visitForWarzone(WarZoneServerCard warZoneCard, ModelPublisher publisher, PlayingState playingState);
    public void visitForOpenSpace(OpenSpaceServerCard openSpaceCard, ModelPublisher publisher, PlayingState playingState);
    public void visitForEnemy(EnemyServerCard enemyCard, ModelPublisher publisher, PlayingState playingState);
    public void visitForStardust(StardustServerCard stardustCard, ModelPublisher publisher, PlayingState playingState);
    public void visitForEpidemic(EpidemicServerCard epidemicCard, ModelPublisher publisher, PlayingState playingState);
    public void visitForMeteorSwarm(MeteorSwarmServerCard meteorSwarmCard, ModelPublisher publisher, PlayingState playingState);
}
