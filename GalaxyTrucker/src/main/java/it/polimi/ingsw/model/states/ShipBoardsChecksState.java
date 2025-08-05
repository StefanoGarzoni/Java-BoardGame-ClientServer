package it.polimi.ingsw.model.states;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.model.ComponentTile.ComponentTile;
import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.Game;
import it.polimi.ingsw.model.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

//FLUSSO : run -> checkShipboard -> | -> playerChoise -> checkForAliens | -> placeAliens ? -> changeState
public class ShipBoardsChecksState extends GameState{
    private Map<String, Consumer<ActionMessage>> mapMethods;
    private int playerHaveFinished;

    ShipBoardsChecksState(Game game) {
        super(game);
        playerHaveFinished = 0;
        mapMethods = new HashMap<>();
        mapMethods.put("playerChoice",this::playerChoice);
        mapMethods.put("groupsToRemove",this::groupsToRemove);
    }

    @Override
    public void run() {
        //FIXME remove
        //game.setState(new DistributeResourcesState(game));
        ActionMessage am = new ActionMessage("shipBoardChecksState", "server");
        game.getPublisher().notify(am);
        //FIXME remove
        //changeState();
        checkShipBoard();
    }

    @Override
    public void receiveAction(ActionMessage actionMessage) {
        synchronized (game) {
            try {
                mapMethods.get(actionMessage.getActionName()).accept(actionMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void checkShipBoard(){
        for(Player p: game.getFlightBoard().getActivePlayers()){
            ArrayList<ArrayList<Coordinates>> wrongPairs = p.getShipBoard().checkCorrectStructure();
            ArrayList<Coordinates> enginePairs = p.getShipBoard().checkEngineOrientation();

            if(wrongPairs.isEmpty() && enginePairs.isEmpty()) {
                ActionMessage am1 = new ActionMessage("yourShipboardIsPerfect", p.getNickname());
                game.getPublisher().notify(am1);

                ActionMessage am2 = new ActionMessage("playerChoice", p.getNickname());
                am2.setData("choicesToRemove", new ArrayList<Coordinates>());
                playerChoice(am2);
            }
            else{
                ActionMessage am = new ActionMessage("wrongTilesCouplings", "server");
                am.setData("couplings",wrongPairs);
                am.setData("wrongEngines",enginePairs);
                am.setReceiver(p.getNickname());
                game.getPublisher().notify(am);
            }
        }
    }


    private void playerChoice(ActionMessage actionMessage) {
        try {
            ArrayList<Coordinates> choicesToRemove = (ArrayList<Coordinates>) actionMessage.getData("choicesToRemove");
            for (Coordinates c : choicesToRemove) {
                game.getFlightBoard().getPlayerByNickname(actionMessage.getSender())
                        .getShipBoard().removeComponentTileAt(c);
                game.getFlightBoard().getPlayerByNickname(actionMessage.getSender())
                        .getShipBoard().increaseLostTiles();
            }
            /*
            for(Coordinates c : game.getFlightBoard().getPlayerByNickname(actionMessage.getSender()).getShipBoard().checkEngineOrientation()){
                game.getFlightBoard().getPlayerByNickname(actionMessage.getSender())
                        .getShipBoard().removeComponentTileAt(c);
            }
            */

            for(ComponentTile ct : game.getFlightBoard().getPlayerByNickname(actionMessage.getSender()).getShipBoard().getBookedComponents()){
                game.getFlightBoard().getPlayerByNickname(actionMessage.getSender())
                        .getShipBoard().increaseLostTiles();
            }

            ArrayList<ArrayList<Coordinates>> groups = game.getFlightBoard().getPlayerByNickname(actionMessage.getSender()).
                    getShipBoard().findConnectedGroups();

            if(groups.size()>1){
                ActionMessage am = new ActionMessage("chooseWhichGroupsKeep", "server");
                am.setData("groups",groups);
                am.setReceiver(actionMessage.getSender());
                game.getPublisher().notify(am);
            }else{
                ActionMessage am = new ActionMessage("tilesBranchesRemoved", "server");
                am.setData("shipBoard", game.getFlightBoard().getPlayerByNickname(actionMessage.getSender()).getShipBoard());
                am.setData("playerAbsolutePosition", game.getFlightBoard().getPlayerByNickname(actionMessage.getSender()).getAbsPosition());
                am.setReceiver(actionMessage.getSender());
                game.getPublisher().notify(am);

                playerHaveFinished++;
                if(playerHaveFinished == game.getFlightBoard().getActivePlayers().size()){
                    changeState();
                }
            }


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void groupsToRemove(ActionMessage actionMessage) {

        ArrayList<ArrayList<Coordinates>> groups = game.getFlightBoard().getPlayerByNickname(actionMessage.getSender()).
                getShipBoard().findConnectedGroups();

        int i = 0;
        for(ArrayList<Coordinates> group : groups) {
            if(i != actionMessage.getInt("numberToKeep")){
                for(Coordinates coordinates : group) {
                    game.getFlightBoard().getPlayerByNickname(actionMessage.getSender()).
                            getShipBoard().removeComponentTileAt(coordinates);
                    game.getFlightBoard().getPlayerByNickname(actionMessage.getSender())
                            .getShipBoard().increaseLostTiles();
                }
            }
            i++;
        }

        ActionMessage am = new ActionMessage("tilesBranchesRemoved", "server");
        am.setData("shipBoard", game.getFlightBoard().getPlayerByNickname(actionMessage.getSender()).getShipBoard());
        am.setData("playerAbsolutePosition", game.getFlightBoard().getPlayerByNickname(actionMessage.getSender()).getAbsPosition());
        am.setReceiver(actionMessage.getSender());
        game.getPublisher().notify(am);

        playerHaveFinished++;
        if(playerHaveFinished == game.getFlightBoard().getActivePlayers().size()){
            changeState();
        }
    }


    private void changeState(){
        game.setState(new DistributeResourcesState(game));
        game.getCurrentState().run();
    }
}
