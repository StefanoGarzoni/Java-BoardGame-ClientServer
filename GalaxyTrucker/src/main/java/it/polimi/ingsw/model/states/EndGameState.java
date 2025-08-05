package it.polimi.ingsw.model.states;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.controller.AllGamesManagers.AllCurrentGames;
import it.polimi.ingsw.model.Game;

public class EndGameState extends GameState{
    EndGameState(Game game) {
        super(game);
    }

    @Override
    public void run() {
        ActionMessage am = new ActionMessage("gameIsFinished", "server");
        game.getPublisher().notify(am);
        AllCurrentGames.getInstance(false).removeGame(game);
        game.deleteAllPublisher();
    }

    @Override
    public void receiveAction(ActionMessage actionMessage) {
        synchronized (game) {
            ActionMessage am = new ActionMessage("gameIsFinished", "server");
            am.setReceiver(actionMessage.getSender());
            game.getPublisher().notify(am);
        }
    }
}
