package it.polimi.ingsw.model.states;
import java.util.function.Consumer;
import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.ComponentTile.CabinTile;
import it.polimi.ingsw.model.ComponentTile.ComponentTile;
import it.polimi.ingsw.model.ComponentTile.Connector;
import it.polimi.ingsw.model.ComponentTile.FixedComponentTile;

import java.util.HashMap;
import java.util.Map;

public class LobbyState extends GameState{
    private Map<String,Consumer<ActionMessage>> mapMethods;

    public LobbyState(Game game) {
        super(game);
        mapMethods = new HashMap<String,Consumer<ActionMessage>>();
    }

    @Override
    public void run() {
        game.setInLobbyState();
        mapMethods.put("addPlayer", this::addPlayer);
    }

    @Override
    public void receiveAction(ActionMessage actionMessage) {
        try{
            mapMethods.get(actionMessage.getActionName()).accept(actionMessage);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private synchronized void addPlayer(ActionMessage actionMessage){

        if(game.getPlayersNumber()>game.getFlightBoard().getCurrentPlayersNumber()){
            char color;
            ComponentTile c;
            ShipBoard shipBoard = new ShipBoard(game.getGameLevel());
            switch(game.getFlightBoard().getCurrentPlayersNumber()){
                case 0:
                    color = 'Y';
                    c = new CabinTile(
                            "GT-new_tiles_16_for_web61.jpg",
                            new Connector(3),new Connector(3),
                            new Connector(3),
                            new Connector(3));
                    break;
                case 1:
                    color = 'R';
                    c = new CabinTile(
                            "GT-new_tiles_16_for_web52.jpg",
                            new Connector(3),new Connector(3),
                            new Connector(3),
                            new Connector(3));
                    break;
                case 2:
                    color = 'B';
                    c = new CabinTile(
                            "GT-new_tiles_16_for_web33.jpg",
                            new Connector(3),new Connector(3),
                            new Connector(3),
                            new Connector(3));
                    break;
                case 3:
                    color = 'G';
                    c = new CabinTile(
                            "GT-new_tiles_16_for_web34.jpg",
                            new Connector(3),new Connector(3),
                            new Connector(3),
                            new Connector(3));
                    break;
                default:
                    color = 'n';
                    //caso che non dovrebbe mai succedere, tile centrale messa casualmente a giallo
                    c = new CabinTile(
                            "GT-new_tiles_16_for_web33.jpg",
                            new Connector(3),new Connector(3),
                            new Connector(3),
                            new Connector(3));
                    break;
            }
            shipBoard.placeBoardComponent(new Coordinates(2,3), c, Direction.NORTH);
            Player player = new Player((String)actionMessage.getData("nickname"),color, shipBoard);
            game.getFlightBoard().addPlayer(player);
            //game.getPublisher().notify();
            //addPlayer aumenta anche il numero di player correnti etc vero?

            if(game.getPlayersNumber()==game.getFlightBoard().getCurrentPlayersNumber()){
                game.setNoInLobbyState();
                game.setState(new BuildingArrangementState(game));
                game.getCurrentState().run();
            }/*else{
                ActionMessage am = new ActionMessage("lobbyState", "sever");
                //forse dovremmo mettere nel messaggio la path dell'immagine del caricamento?
                game.getPublisher().notify(am); //TODO do we need this?
            }*/
        }
    }
}
