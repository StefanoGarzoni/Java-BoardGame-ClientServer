package it.polimi.ingsw.model.cards.visitor;

import it.polimi.ingsw.model.FlightBoard;
import it.polimi.ingsw.model.ModelPublisher;
import it.polimi.ingsw.model.cards.server.EnemyServerCard;
import it.polimi.ingsw.model.cards.server.concrete.*;
import it.polimi.ingsw.model.cards.server.concrete.AbandonedShipServerCard;
import it.polimi.ingsw.model.states.PlayingState;

/**
 * Concrete implementation of the {@link ServerCardVisitor}, part of the visitor pattern to activate the cards effect
 */
public class ServerCardSolverVisitor implements ServerCardVisitor {
    FlightBoard flightBoard;

    public ServerCardSolverVisitor(FlightBoard flightBoard) {
        this.flightBoard = flightBoard;
    }

    @Override
    public void visitForPlanet(PlanetServerCard planetCard, ModelPublisher publisher, PlayingState playingState) {
        planetCard.setPlayingState(playingState);
        planetCard.play(flightBoard, publisher);
    }

    @Override
    public void visitForAbandonedShip(AbandonedShipServerCard abandonedShipCard, ModelPublisher publisher, PlayingState playingState) {
        abandonedShipCard.setPlayingState(playingState);
        abandonedShipCard.play(flightBoard, publisher);
    }

    @Override
    public void visitForAbandonedStation(AbandonedStationServerCard abandonedStationCard, ModelPublisher publisher, PlayingState playingState) {
        abandonedStationCard.setPlayingState(playingState);
        abandonedStationCard.play(flightBoard, publisher);
    }

    @Override
    public void visitForWarzone(WarZoneServerCard warZoneCard, ModelPublisher publisher, PlayingState playingState) {
        warZoneCard.setPlayingState(playingState);
        warZoneCard.play(flightBoard, publisher);
    }

    @Override
    public void visitForOpenSpace(OpenSpaceServerCard openSpaceCard, ModelPublisher publisher, PlayingState playingState) {
        openSpaceCard.setPlayingState(playingState);
        openSpaceCard.play(flightBoard, publisher);
    }

    @Override
    public void visitForEnemy(EnemyServerCard enemyCard, ModelPublisher publisher, PlayingState playingState) {
        enemyCard.setPlayingState(playingState);
        enemyCard.play(flightBoard, publisher);
    }

    @Override
    public void visitForStardust(StardustServerCard stardustCard, ModelPublisher publisher, PlayingState playingState) {
        stardustCard.setPlayingState(playingState);
        stardustCard.play(flightBoard, publisher);
    }

    @Override
    public void visitForEpidemic(EpidemicServerCard epidemicCard, ModelPublisher publisher, PlayingState playingState) {
        epidemicCard.setPlayingState(playingState);
        epidemicCard.play(flightBoard, publisher);
    }

    @Override
    public void visitForMeteorSwarm(MeteorSwarmServerCard meteorSwarmCard, ModelPublisher publisher, PlayingState playingState) {
        meteorSwarmCard.setPlayingState(playingState);
        meteorSwarmCard.play(flightBoard, publisher);
    }
}
