package it.polimi.ingsw.controller.AllGamesManagers;

import it.polimi.ingsw.controller.Controller;
import it.polimi.ingsw.model.Game;
import it.polimi.ingsw.model.ModelPublisher;

import java.io.*;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AllCurrentGames {
    private static AllCurrentGames instance;
    private final ConcurrentHashMap<Game, Controller> games;

    private AllCurrentGames(boolean usePersistence){
        games = new ConcurrentHashMap<>();

        PeriodicGamesBackupper periodicGamesBackupper = new PeriodicGamesBackupper(this);
        if(usePersistence){
            loadBackedUpGames(periodicGamesBackupper.getBackupFileName());
        }

        // start the thread that checks all player connection, to identify disconnections
        new PlayersAlivenessChecker(this).start();
        // periodicGamesBackupper.run();
    }

    /** Singleton implementation of AllCurrentGames constructor
     *
     * @param usePersistence if true, all games stored on disk will be uploaded. otherwise no game will be uploaded
     * @return an instance of AllCurrentGames class
     */
    public static AllCurrentGames getInstance(boolean usePersistence){
        if(instance == null)
            instance = new AllCurrentGames(usePersistence);
        return instance;
    }

    private void loadBackedUpGames(String backupFileName){
        try{
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(backupFileName));
            Set<Game> games = (Set<Game>) in.readObject();

            for(Game game : games){
                this.games.put(game, new Controller(game));
            }
        }
        catch (FileNotFoundException | ClassNotFoundException e){ System.out.println("Backup file or class not found"); }
        catch (IOException e){ e.printStackTrace(System.out); }
    }

    public void addGame(Game game){ games.put(game, new Controller(game)); }
    public void removeGame(Game game){ games.remove(game); }

    /** Returns a controller that handle a specific game's communication with client
     *
     * @param game game of which to return the controller
     * @return controller of the selected game
     */
    public Controller getControllerOf(Game game){ return games.get(game); }

    /** Returns, if present, the Game where a player has the specified nickname
     *
     * @param nickname nickname to search in all games
     * @return null if the nickname is not present in any game, otherwise the Game containing the specified nickname
     */
    public Game getGameContainingNickname(String nickname){
        for(Game game : games.keySet())
            if(game.existsPlayer(nickname))
                return game;
        return null;
    }

    /** Returns, if present, the first game which is in Lobby State, so still not started
     *
     * @return Game in lobby state
     */
    public Game getFirstGameInLobby() {
        for(Game g : games.keySet()){
            if(g.isInLobbyState())
                return g;
        }
        return null;
    }

    public Set<Game> getAllGames(){ return games.keySet(); }

    /** Returns publishers of all existing Games
     *
     * @return list of all games' publishers
     */
    protected List<ModelPublisher> getAllGamesPublishers(){
        return games.keySet().stream().map(Game::getPublisher).toList();
    }
}