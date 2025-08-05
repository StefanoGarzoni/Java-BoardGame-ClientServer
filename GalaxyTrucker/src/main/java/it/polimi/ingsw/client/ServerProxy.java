package it.polimi.ingsw.client;

import it.polimi.ingsw.ActionMessage;

import java.io.IOException;
import java.rmi.NotBoundException;

public interface ServerProxy {

    /** Sends an ActionMessage to the server using the connection
     *
     * @param message to be sent to the server
     */
    void send(ActionMessage message);

    /** Creates a connection with the server and sends all parameters that are necessary to insert a player in a game.
     * Finally, it starts listening on the connection for all server-to-client communication and transfers them to the current state.
     *
     * @param nickname nickname of the player that is asking to connect to the server
     * @param playersNumber number of players of a new game, that must be created if there isn't another that can accept the user as player (every existing game is full of player)
     * @param gameLevel level of a new game, that must be created if there isn't another that can accept the user as a player (every existing game is full of player)
     * @return true, if the connection is successfully. false otherwise
     * @throws IOException if a problem with the connection occurs
     * @throws NotBoundException if a problem with RMI connection occurs
     */
    boolean connectToServer(String nickname, int playersNumber, int gameLevel) throws IOException, NotBoundException;

    /** Ends up the connection and frees all communication resources
     */
    void closeConnection();
}

