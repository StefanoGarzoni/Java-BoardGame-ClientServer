package it.polimi.ingsw.model;

import it.polimi.ingsw.controller.clientProxies.ClientProxy;
import it.polimi.ingsw.model.states.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class Game implements Serializable {
    private final long hourGlassMilliseconds = 90000;
    // private int sessionID;
    private final int gameLevel;
    private final int playersNumber;
    private boolean isInLobbyState;
    private GameState currentState;
    private final FlightBoard flightBoard;
    private transient ModelPublisher modelPublisher;

    public Game(int level, int playersNumber) {
        this.gameLevel = level;
        this.isInLobbyState = false;
        this.playersNumber = playersNumber;
        currentState = new LobbyState(this);
        currentState.run();
        modelPublisher = new ModelPublisher();
        flightBoard = new FlightBoard();
        // this.sessionID = sessionID;
    }

    /** Custom de-serialization of Game.
     * In addition to defaultReadObject(), this method crates a new and empty ModelPublisher.
     *
     * @param in ObjectInputStream that contains the serialized Game
     * @throws IOException if an I/ O error occurs
     * @throws ClassNotFoundException if the stream is not currently reading objects
     */
    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
        in.defaultReadObject();
        modelPublisher = new ModelPublisher();
    }

    public void run() { currentState.run(); }

    public int getPlayersNumber() { return playersNumber; }

    public void setState(GameState nextState){
        currentState = nextState;
    }
    public boolean isInLobbyState() { return isInLobbyState; }
    public void setInLobbyState() { isInLobbyState = true; }
    public void setNoInLobbyState() { isInLobbyState = false; }

    public int getGameLevel(){ return gameLevel; }

    /** Adds a ClientProxy to the Game's publisher (observable)
     *
     * @param clientProxy to be added to publisher
     */
    public void addClientProxyToPublisher(ClientProxy clientProxy){
        modelPublisher.register(clientProxy);
    }

    /** Removes a ClientProxy from the Game's publisher (observable)
     *
     * @param clientProxy to be removed from publisher
     */
    public void removeClientProxyToPublisher(ClientProxy clientProxy){
        modelPublisher.unregister(clientProxy);
    }

    /** Function that checks if there is a Player in the game with the specified nickname
     *
     * @param nickname to search among players
     * @return true if present, false otherwise
     */
    public boolean existsPlayer(String nickname){
        return flightBoard.getPlayerByNickname(nickname) != null;
    }

    public FlightBoard getFlightBoard() { return flightBoard; }

    public ModelPublisher getPublisher() { return modelPublisher; }

    public GameState getCurrentState(){ return currentState; }

    public void deleteAllPublisher(){
        modelPublisher = null;
    }

}