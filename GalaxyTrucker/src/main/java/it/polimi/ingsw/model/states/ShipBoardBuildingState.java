package it.polimi.ingsw.model.states;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.model.ComponentTile.ComponentTile;
import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.Direction;
import it.polimi.ingsw.model.Game;
import it.polimi.ingsw.model.Player;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ShipBoardBuildingState extends GameState {
    ScheduledExecutorService endBuildingDueToHourGlassScheduler = Executors.newSingleThreadScheduledExecutor();
    ScheduledFuture<?> endBuildingDueToHourGlassFutureInvocation = null;


    private final Map<String, Consumer<ActionMessage>> mapMethods;
    private final ArrayList<Player> playerHaveFinished;

    public ShipBoardBuildingState(Game game) {
        super(game);

        playerHaveFinished = new ArrayList<>();
        mapMethods = new HashMap<>();
        mapMethods.put("placeTileAt", this::placeTileAt);
        mapMethods.put("bookTile", this::bookTile);
        mapMethods.put("viewDeck", this::viewDeck);
        mapMethods.put("poseDeck", this::poseDeck);
        mapMethods.put("getCoveredTile", this::getCoveredTile);
        mapMethods.put("getUncoveredTile", this::getUncoveredTile);
        mapMethods.put("poseTile", this::poseTile);
        mapMethods.put("flipHourGlass", this::flipHourGlass);
        mapMethods.put("playerIsDone", this::playerIsDone);
    }

    @Override
    public void run() {
        ActionMessage am = new ActionMessage("ClientShipBoardBuildingState", "server");
        System.out.println("message");
        game.getPublisher().notify(am);

        // FIXME: hourglass must be flipped (temporarily solution)
        flipHourGlass(new ActionMessage("flipHourGlass", "server"));
    }

    @Override
    public synchronized void receiveAction(ActionMessage actionMessage) {
        synchronized (game) {
            try {
                mapMethods.get(actionMessage.getActionName()).accept(actionMessage);
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }
    }

    //il client deve tener traccia di quante ne stai book e nel caso blocca l'opzione di fare book su una tile
    private void bookTile(ActionMessage actionMessage) {
        try {
            game.getFlightBoard().getPlayerByNickname(actionMessage.getSender()).getShipBoard().addBookedComponent(
                    game.getFlightBoard().getPlayerByNickname(actionMessage.getSender()).getTileInHand()
            );

            ActionMessage am = new ActionMessage("tileIsBooked", "server");
            am.setReceiver(actionMessage.getSender());
            am.setData("fileName",
                    game.getFlightBoard().getPlayerByNickname(actionMessage.getSender()).getTileInHand().getFileName());

            game.getFlightBoard().getPlayerByNickname(actionMessage.getSender()).setTileInHand(null);
            game.getPublisher().notify(am);

        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void poseTile(ActionMessage actionMessage) {
        try {
            ComponentTile ct = game.getFlightBoard().getPlayerByNickname(actionMessage.getSender()).getTileInHand();
            game.getFlightBoard().getPlayerByNickname(actionMessage.getSender()).setTileInHand(null);
            game.getFlightBoard().getTilesBunch().setTileAsUncovered(ct);

            ActionMessage am = new ActionMessage("tileIsFree","server");
            am.setReceiver(actionMessage.getSender());
            am.setData("fileName", ct.getFileName());
            game.getPublisher().notify(am);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void getCoveredTile(ActionMessage actionMessage) {
        try {
            ComponentTile ct = game.getFlightBoard().getTilesBunch().getRandomFromCovered();
            ActionMessage am = new ActionMessage("sendTileCovered","server");
            am.setData("fileName", ct.getFileName());
            am.setReceiver(actionMessage.getSender());
            game.getFlightBoard().getPlayerByNickname(actionMessage.getSender()).setTileInHand(ct);
            game.getPublisher().notify(am);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void getUncoveredTile(ActionMessage actionMessage){
        try{
            ComponentTile ct = game.getFlightBoard().getTilesBunch().takeUncoveredTile((String)actionMessage.getData("fileName"));

            if(ct != null){
                game.getFlightBoard().getPlayerByNickname(actionMessage.getSender()).setTileInHand(ct);
                ActionMessage am = new ActionMessage("tileUncoveredTaken","server");
                am.setData("fileName", ct.getFileName());
                am.setReceiver(actionMessage.getSender());
                game.getPublisher().notify(am);
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    //se la tile proviene dal mucchio e quindi la ha in mano o è una booked è gestitp qua, il messaggio da inviaro è lo stesso
    private void placeTileAt(ActionMessage actionMessage) {
        try {
            ComponentTile booked = game.getFlightBoard().getPlayerByNickname(actionMessage.getSender()).getShipBoard().useBookedComponent((String) actionMessage.getData("fileName"));
            ComponentTile inHand = game.getFlightBoard().getPlayerByNickname(actionMessage.getSender()).getTileInHand();

            Direction direction = (Direction) (actionMessage.getData("direction"));

            Coordinates coordinates = (Coordinates)actionMessage.getData("coordinates");

            // this control has the objective of avoid placing of a booked tile if the player has a tile in hand
            if (booked != null && inHand == null) {
                game.getFlightBoard().getPlayerByNickname(actionMessage.getSender()).getShipBoard().placeBoardComponent(coordinates, booked, direction);

            } else if (inHand.getFileName().equals(actionMessage.getData("fileName"))) {
                game.getFlightBoard().getPlayerByNickname(actionMessage.getSender()).setTileInHand(null);
                game.getFlightBoard().getPlayerByNickname(actionMessage.getSender()).getShipBoard().placeBoardComponent(coordinates, inHand, direction);

            } else {
                //eccezione
                throw new RuntimeException();
            }

            ActionMessage am = new ActionMessage("tileIsPlaced", "server");
            am.setData("fileName",actionMessage.getData("fileName"));
            am.setData("coordinates", coordinates);
            am.setData("direction",direction);
            am.setReceiver(actionMessage.getSender());
            game.getPublisher().notify(am);

            game.getFlightBoard().getTilesBunch().remove((String) actionMessage.getData("fileName"));

        }catch (Exception e){
            throw new RuntimeException(e);
        }

    }

    private void viewDeck(ActionMessage actionMessage) {
        try {
            if (game.getFlightBoard().getDeck((Integer) actionMessage.getData("deckNumber")).isFree()) {
                game.getFlightBoard().getDeck(actionMessage.getInt("deckNumber")).takeDeck(actionMessage.getSender());
                ActionMessage am = new ActionMessage("deckIsViewing", "server");
                am.setData("deckNumber", actionMessage.getData("deckNumber"));
                am.setData("deck", game.getFlightBoard().getDeck(actionMessage.getInt("deckNumber")));
                am.setData("player", actionMessage.getSender());
                game.getPublisher().notify(am);
            } else {
                //eccezione o non facciamo niente?
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private void poseDeck(ActionMessage actionMessage) {
        try {

            if(!game.getFlightBoard().getDeck(actionMessage.getInt("deckNumber")).isFree()){
                game.getFlightBoard().getDeck(actionMessage.getInt("deckNumber")).freeDeck(actionMessage.getSender());
                ActionMessage am = new ActionMessage("deckReleased", "server");
                am.setData("deckNumber", actionMessage.getData("deckNumber"));
                am.setData("player", actionMessage.getSender());
                game.getPublisher().notify(am);
            }else{
                //eccezione
            }

        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private void playerIsDone(ActionMessage actionMessage) {
        try{
            playerHaveFinished.add(game.getFlightBoard().getPlayerByNickname(actionMessage.getSender()));
            int newPlayerPosition = switch (playerHaveFinished.size()){
                        case 1 -> 7;
                        case 2 -> 4;
                        case 3 -> 1;
                        case 4 -> 2;
                        default -> throw new RuntimeException("4 or more players registered");
            };

            game.getFlightBoard().getPlayerByNickname(actionMessage.getSender()).setAbsPosition(newPlayerPosition);
            ActionMessage am = new ActionMessage("playerIsDoneConformation", "server");
            am.setData("player", actionMessage.getSender());
            game.getPublisher().notify(am);

            if(playerHaveFinished.size()==game.getFlightBoard().getActivePlayers().size()){
                if(endBuildingDueToHourGlassFutureInvocation != null)       // if the hourglass has been flipped for the last time
                    endBuildingDueToHourGlassFutureInvocation.cancel(false);
                endShipBoardBuilding();
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private void flipHourGlass(ActionMessage actionMessage) {

        try {
            ActionMessage am;
            if(game.getGameLevel()==2){

                if(game.getFlightBoard().getNumFlipHG() < 2){
                    am  = new ActionMessage("canFlipHG", "server");
                    if(game.getFlightBoard().HGIsFree()){
                        game.getFlightBoard().flipHourGlass();
                        am.setData("permit", true);

                        Instant startingTimerTimestamp = Instant.now();
                        am.setData("startingTimerTimestamp", startingTimerTimestamp);
                    }else{
                        am.setData("permit", false);
                    }
                    am.setData("lastTime",false);

                    game.getPublisher().notify(am);

                }else if(game.getFlightBoard().getNumFlipHG() == 2){
                    boolean askingPlayerHasFinished = false;

                    for(Player p: playerHaveFinished){
                        if(p.getNickname().equals(actionMessage.getSender())){
                            am  = new ActionMessage("canFlipHG", "server");
                            if(game.getFlightBoard().HGIsFree()){
                                game.getFlightBoard().flipHourGlass();
                                am.setData("permit", true);

                                Instant startingTimerTimestamp = Instant.now();
                                am.setData("startingTimerTimestamp", startingTimerTimestamp);

                                // scheduling of the end of building phase 90 seconds in the future
                                endBuildingDueToHourGlassFutureInvocation = endBuildingDueToHourGlassScheduler.schedule(this::endShipBoardBuilding, 90, TimeUnit.SECONDS);
                            }else{
                                am.setData("permit", false);
                            }
                            am.setData("lastTime",true);

                            game.getPublisher().notify(am);

                            askingPlayerHasFinished=true;
                            break;
                        }
                    }
                    if(!askingPlayerHasFinished){
                        am  = new ActionMessage("canFlipHG", "server");
                        am.setData("permit", false);
                        am.setData("lastTime",false);
                        game.getPublisher().notify(am);
                    }

                }
            }

        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private void endShipBoardBuilding(){
        if(playerHaveFinished.size()!=game.getFlightBoard().getActivePlayers().size()){
            for(Player p : game.getFlightBoard().getPlayers()){
                if(!playerHaveFinished.contains(p)){
                    playerHaveFinished.add(p);
                }
            }
        }
        game.setState(new ShipBoardsChecksState(game));
        game.getFlightBoard().setPlayerPosition(playerHaveFinished);
        game.getCurrentState().run();
    }
}
