package it.polimi.ingsw.controller.WelcomeProxies;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketWelcomeServer implements WelcomeServer {
    private final int serverPort = 1025;
    private final ServerSocket serverSocket;
    private final ClientProxySorter clientProxySorter;

    public SocketWelcomeServer(ClientProxySorter clientProxySorter) throws IOException {
        super();
        serverSocket = new ServerSocket(serverPort);
        this.clientProxySorter = clientProxySorter;
    }

    public void start() throws IOException {
        //TODO: guarda sequence diagrams

        while(true){
            Socket clientSocket = serverSocket.accept();
            new SocketWelcomeProxyThread(clientSocket, clientProxySorter).start();
        }
    }
}
