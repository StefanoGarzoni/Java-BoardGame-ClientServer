package it.polimi.ingsw.controller.WelcomeProxies;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.controller.Controller;
import it.polimi.ingsw.controller.clientProxies.ClientProxy;
import it.polimi.ingsw.controller.AllGamesManagers.AllCurrentGames;
import it.polimi.ingsw.controller.clientProxies.RMIClientProxy;
import it.polimi.ingsw.controller.clientProxies.SocketClientProxy;
import it.polimi.ingsw.model.Game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.rmi.NotBoundException;

public class ClientProxySorter {
    private static ClientProxySorter instance;
    private final AllCurrentGames allCurrentGames;

    private ClientProxySorter(AllCurrentGames allCurrentGames){ this.allCurrentGames = allCurrentGames; }
    public static ClientProxySorter getInstance(AllCurrentGames allCurrentGames){
        if(instance == null)
            instance = new ClientProxySorter(allCurrentGames);
        return instance;
    }

    //TODO: sincronizzazione
    /** Inserts a ClientProxy in a game.
     * If the ClientProxy is of a player who was already playing in a game but then logs in again (e.g. crashed),
     * it will be assigned to the same old game.
     * If not, the methods searches for a not-yet started game.
     * If there isn't any game in lobby state, a new one is created.
     *
     */
    public void insertClientProxyInAGame(int possibleNewGameLevel, int possibleNewGamePlayersNumber, String nickname,
                                         Socket socket, PrintWriter writer, BufferedReader reader, boolean isSocket, String clientRmiIp){

        Game designatedGame = allCurrentGames.getGameContainingNickname(nickname);
        ClientProxy clientProxy;


        if(designatedGame != null){
            Controller c = allCurrentGames.getControllerOf(designatedGame);
            try {
                if(isSocket)
                    clientProxy = new SocketClientProxy(nickname, socket, writer, reader, c); //TODO RMI
                else
                    clientProxy = new RMIClientProxy(nickname, clientRmiIp, c);
                designatedGame.getPublisher().register(clientProxy);
                notifyClientProxyForPersistence(clientProxy);

            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (NotBoundException e) {
                throw new RuntimeException(e);
            }
        }
        else{
            designatedGame = getGameToJoin(possibleNewGameLevel, possibleNewGamePlayersNumber);
            Controller c = allCurrentGames.getControllerOf(designatedGame);
            try {
                if(isSocket)
                    clientProxy = new SocketClientProxy(nickname, socket, writer, reader, c); //TODO RMI
                else
                    clientProxy = new RMIClientProxy(nickname, clientRmiIp, c);
                designatedGame.getPublisher().register(clientProxy);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (NotBoundException e) {
                throw new RuntimeException(e);
            }
        }

        // invokes LobbyState method to manage model update due to new player
        ActionMessage addPlayerMessage = new ActionMessage("addPlayer", clientProxy.getPlayerNickname());
        addPlayerMessage.setData("nickname", clientProxy.getPlayerNickname());
        designatedGame.getCurrentState().receiveAction(addPlayerMessage);
    }

    /**
     *
     * @param clientProxy
     */
    private void notifyClientProxyForPersistence(ClientProxy clientProxy){
        //TODO:
    }

    /** This method returns, if exists, the first game in lobby state.
     * If any game is in lobby, it returns a new one.
     *
     * @return Game to join
     */
    private Game getGameToJoin(int possibleNewGameLevel, int possibleNewGamePlayersNumber){
        Game designatedGame = allCurrentGames.getFirstGameInLobby();

        // no game in lobby state
        if(designatedGame == null){
            designatedGame = new Game(possibleNewGameLevel, possibleNewGamePlayersNumber);
            allCurrentGames.addGame(designatedGame);
        }

        return designatedGame;
    }
}