package it.polimi.ingsw.model.states;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.model.Game;

import java.io.Serializable;

public abstract class GameState implements Serializable {
    protected final Game game;

    GameState(Game game){
        this.game = game;
    }

    public abstract void run();
    public abstract void receiveAction(ActionMessage actionMessage);
}
