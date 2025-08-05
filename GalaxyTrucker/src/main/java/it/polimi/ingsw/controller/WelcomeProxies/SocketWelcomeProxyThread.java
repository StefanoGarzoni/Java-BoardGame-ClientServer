package it.polimi.ingsw.controller.WelcomeProxies;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.ActionMessageAdapter;
import it.polimi.ingsw.controller.clientProxies.ClientProxy;
import it.polimi.ingsw.controller.clientProxies.SocketClientProxy;
import it.polimi.ingsw.model.Cargo.CargoType;
import it.polimi.ingsw.model.Cargo.util.CargoTypeAdapter;
import it.polimi.ingsw.model.ComponentTile.ComponentTile;
import it.polimi.ingsw.model.ComponentTile.ComponentTileAdapter;
import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.CoordinatesAdapter;
import it.polimi.ingsw.model.cards.server.ServerCard;
import it.polimi.ingsw.model.cards.util.ServerCardAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketWelcomeProxyThread extends Thread {
    private final Socket clientSocket;
    private final ClientProxySorter clientProxySorter;

    SocketWelcomeProxyThread(Socket clientSocket, ClientProxySorter clientProxySorter){
        setDaemon(true);
        this.clientSocket = clientSocket;
        this.clientProxySorter = clientProxySorter;
    }

    @Override
    public void run(){
        Gson gson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .registerTypeAdapter(ActionMessage.class, new ActionMessageAdapter())
                .registerTypeAdapter(ServerCard.class, new ServerCardAdapter())
                .registerTypeAdapter(CargoType.class, new CargoTypeAdapter())
                .registerTypeAdapter(ComponentTile.class, new ComponentTileAdapter())
                .registerTypeAdapter(Coordinates.class, new CoordinatesAdapter())
                .create();

        try {
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            // asks the client for Player's params
            ActionMessage toSend = new ActionMessage("getPlayerParams", "server");
            writer.println(gson.toJson(toSend));     // sends a request for params

            // TODO: shall we set a timer during which we need to receive the parameters?

            // waits for client response
            String playerParametersJSON = reader.readLine();
            ActionMessage actionMessage = gson.fromJson(playerParametersJSON, ActionMessage.class);
            String nickname = (String) actionMessage.getData("nickname");
            int possibleNewGameLevel = (int) actionMessage.getData("possibleNewGameLevel");
            int possibleNewGamePlayersNumber = (int) actionMessage.getData("possibleNewGamePlayersNumber");


            // sending a confirmation message
            toSend = new ActionMessage("ConnectionConfirmation", "server");
            writer.println(gson.toJson(toSend));

            // insertion of the player in a new game
            clientProxySorter.insertClientProxyInAGame(possibleNewGameLevel,
                    possibleNewGamePlayersNumber, nickname, clientSocket, writer, reader, true, "");
        }
        catch (IOException e){
            e.printStackTrace(System.out);
        }
    }

    private void closeResources(PrintWriter writer, BufferedReader reader){
        try{
            if(writer != null){ writer.close(); }
            if(reader != null){ reader.close(); }
            if(clientSocket != null && !clientSocket.isClosed()){
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
