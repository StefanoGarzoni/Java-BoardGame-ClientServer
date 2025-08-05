package it.polimi.ingsw.model.states;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.Game;
import it.polimi.ingsw.model.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class DistributeResourcesState extends GameState{
    private Map<String, Consumer<ActionMessage>> mapMethods;
    int playerHaveFinished;

    DistributeResourcesState(Game game) {
        super(game);
        playerHaveFinished = 0;
        mapMethods = new HashMap<>();
        mapMethods.put("placeAliens",this::placeAliens);
    }

    @Override
    public void run() {
        ActionMessage am = new ActionMessage("distributeResourcesState", "server");
        game.getPublisher().notify(am);
        for(Player p : game.getFlightBoard().getActivePlayers()){
            checkForAliens(p.getNickname());
        }
    }

    @Override
    public void receiveAction(ActionMessage actionMessage) {
        synchronized(game) {
            try {
                mapMethods.get(actionMessage.getActionName()).accept(actionMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void checkForAliens(String nickname) {
        ActionMessage am = new ActionMessage("possibleAliensPosition", "server");
        am.setData("positions", game.getFlightBoard().getPlayerByNickname(nickname).getShipBoard().getPossibleAliensPosition());
        am.setReceiver(nickname);
        game.getPublisher().notify(am);
    }

    private void placeAliens(ActionMessage actionMessage) {
        ArrayList<ArrayList<Coordinates>> coordinates = (ArrayList<ArrayList<Coordinates>>) actionMessage.getData("coordinatesChoosen");

        if(coordinates == null) {
            playerHaveFinished++;
            checkPlayerFinished();
            return;
        }

        for(Coordinates c: coordinates.get(0)){ //brown aliens coordinates
            game.getFlightBoard().getPlayerByNickname(actionMessage.getSender()).getShipBoard().placeAlienAt(c);
        }
        for(Coordinates c: coordinates.get(1)){ //purple aliens coordinates
            game.getFlightBoard().getPlayerByNickname(actionMessage.getSender()).getShipBoard().placeAlienAt(c);
        }
        assignmentResources(game.getFlightBoard().getPlayerByNickname(actionMessage.getSender()));
        playerHaveFinished++;
        checkPlayerFinished();
    }

    private void assignmentResources(Player p) {
        try {
            //da rivedere per il discorso alieni, se il calcolo della total crew è corretto
            int batteries = p.getShipBoard().calculateStartingTotalBatteries();
            int crew = p.getShipBoard().calculateTotalCrew();
            //p.getShipBoard().calculateTotalCargoValue();
            ActionMessage am = new ActionMessage("resourcesDistributedConfirmation", "server");
            //am.setData("batteries", batteries);
            //am.setData("crew", crew);
            am.setData("shipBoard", p.getShipBoard());
            am.setReceiver(p.getNickname());
            game.getPublisher().notify(am);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkPlayerFinished(){
        if(playerHaveFinished == game.getFlightBoard().getActivePlayers().size()){
            game.setState(new PlayingState(game));
            game.getCurrentState().run();
        }
    }


}
