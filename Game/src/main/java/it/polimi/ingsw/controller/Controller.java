package it.polimi.ingsw.controller;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.model.Game;

public class Controller {
    private final Game game;

    public Controller (Game game){ this.game = game; }

    /** Methods that forwards an ActionMessage to the relative Game and manages PONG messages
     *
     * @param actionMessage to be forwarded
     */
    public void sendActionToGame(ActionMessage actionMessage){
        if(actionMessage.getActionName().equals("PONG")){
            game.getPublisher().markPlayerAsAlive(actionMessage.getSender());
        }
        else if(actionMessage.getActionName().equals("leaveGame")){
            String playerNickname = actionMessage.getSender();

            // update model to set player out of game
            game.getFlightBoard().getPlayerByNickname(playerNickname).putOutOfPlay();
            game.getPublisher().unregister(playerNickname);

            // communicate to all players that player has left the game
            ActionMessage am = new ActionMessage("playerLeftGame", "server");
            am.setReceiver(playerNickname);
            game.getPublisher().notify(am);
        }
        else {
            game.getCurrentState().receiveAction(actionMessage);
        }
    }
}
