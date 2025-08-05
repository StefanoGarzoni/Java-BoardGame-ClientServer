package it.polimi.ingsw.controller.WelcomeProxies;

import it.polimi.ingsw.controller.clientProxies.ClientProxy;
import it.polimi.ingsw.controller.clientProxies.RMIClientProxy;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class RMIWelcomeImpl implements RMIWelcome {
    ClientProxySorter proxySorter;

    RMIWelcomeImpl(ClientProxySorter clientProxySorter) { this.proxySorter = clientProxySorter; }

    @Override
    public boolean connectToServer(
            String nickname,
            String clientRmiRegistryIp,
            int possibleNewGameLevel,
            int possibleNewGamePlayersNumber
    ) throws NotBoundException, RemoteException {

        proxySorter.insertClientProxyInAGame(possibleNewGameLevel,
                possibleNewGamePlayersNumber, nickname, null, null, null, false, clientRmiRegistryIp);

        return true;
    }

    /** This function returns a free TCP port number.
     *
     * @return free TCP port
     * @throws IOException when IO problems occurs while searching for a free TCP port
     */
//    private int findFreeTCPPort() throws IOException {
//        int freeTCPPort;
//
//        ServerSocket auxiliaryServerSocket = new ServerSocket(0);
//        freeTCPPort = auxiliaryServerSocket.getLocalPort();
//        auxiliaryServerSocket.close();
//
//        return freeTCPPort;
//    }
}