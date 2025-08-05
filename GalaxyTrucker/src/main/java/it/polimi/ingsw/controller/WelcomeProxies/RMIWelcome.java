package it.polimi.ingsw.controller.WelcomeProxies;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIWelcome extends Remote {
    /** Remote method that manages the connection of a new player to the server.
     * When a player wants to join a game, firstly needs to call this method.
     * It creates the new player's ClientProxy, and assigns it to a game.
     * <p>
     * After a successful connection, the client can fetch a new Server's RMI remote object to communicate in the next
     * game phases. The remote Object will be rebound at the Object name "RmiServer?<client_nickname>".
     *
     * @param nickname of the player that is connecting
     * @param clientRmiRegistryIp client's RMI registry IP address.
     *                            Server will fetch the client's remote object from this registry.
     * @param possibleNewGameLevel level of the game that will be created if it won't already exist a joinable game
     * @param possibleNewGamePlayersNumber number of players of the game that will be created if it won't already exist a joinable game
     *
     * @return true if the connection is successful, false otherwise
     *
     * @throws NotBoundException if "ClientRemoteObject" is not exposed via the RMI Registry
     * @throws RemoteException if some communication issues occurs
     */
    boolean connectToServer(
            String nickname,
            String clientRmiRegistryIp,
            int possibleNewGameLevel,
            int possibleNewGamePlayersNumber
    ) throws NotBoundException, RemoteException;
}
