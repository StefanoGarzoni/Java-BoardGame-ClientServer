package it.polimi.ingsw.controller.WelcomeProxies;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class RMIWelcomeServer implements WelcomeServer {
    private final int serverPort = 1099;
    private final ClientProxySorter clientProxySorter;

    public RMIWelcomeServer(ClientProxySorter clientProxySorter){
        this.clientProxySorter = clientProxySorter;
    }

    @Override
    public void start() {
        try{
            LocateRegistry.createRegistry(serverPort);
            RMIWelcome remoteObject = new RMIWelcomeImpl(clientProxySorter);
            RMIWelcome stub = (RMIWelcome) UnicastRemoteObject.exportObject(remoteObject, 0);
            Naming.rebind("rmi://localhost:"+serverPort+"/Welcome", stub);
        }
        catch (RemoteException | MalformedURLException e){
            e.printStackTrace(System.out);
        }
    }
}
