package it.polimi.ingsw.controller;

import it.polimi.ingsw.controller.AllGamesManagers.AllCurrentGames;
import it.polimi.ingsw.controller.WelcomeProxies.ClientProxySorter;
import it.polimi.ingsw.controller.WelcomeProxies.RMIWelcomeServer;
import it.polimi.ingsw.controller.WelcomeProxies.SocketWelcomeServer;
import it.polimi.ingsw.controller.WelcomeProxies.WelcomeServer;

import java.io.IOException;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        AllCurrentGames allCurrentGames;
        allCurrentGames =  AllCurrentGames.getInstance(false);//(args[0].equals("--persistence")) ? AllCurrentGames.getInstance(true) : AllCurrentGames.getInstance(false);

        ArrayList<WelcomeServer> proxyServers = new ArrayList<>();
        ClientProxySorter clientProxySorter = ClientProxySorter.getInstance(allCurrentGames);

        // WelcomeProxyServers creation
        try{
            proxyServers.add(new SocketWelcomeServer(clientProxySorter));
            // proxyServers.add(new RMIWelcomeServer(clientProxySorter));
        }
        catch (IOException e){
            e.printStackTrace(System.out);
        }

        // WelcomeProxyServers starting
        for(WelcomeServer s : proxyServers){
            try{
                s.start();
            }
            catch (IOException e){
                e.printStackTrace(System.out);
            }
        }
    }
}