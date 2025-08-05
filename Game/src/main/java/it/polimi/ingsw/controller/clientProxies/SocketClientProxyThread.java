package it.polimi.ingsw.controller.clientProxies;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.ActionMessageAdapter;
import it.polimi.ingsw.controller.Controller;
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
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class SocketClientProxyThread extends Thread{
    private final String playerNickname;
    private final BufferedReader clientReader;
    private Controller controller;
    private final Gson gson;
    private final AtomicBoolean isRunning;     // va impostato a false nel caso in cui la socket non si attiva

    SocketClientProxyThread(String playerNickname, Controller controller, Socket clientSocket) throws IOException {
        this.playerNickname = playerNickname;
        setDaemon(true);
        clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.controller = controller;
        gson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .registerTypeAdapter(ActionMessage.class, new ActionMessageAdapter())
                .registerTypeAdapter(ServerCard.class, new ServerCardAdapter())
                .registerTypeAdapter(CargoType.class, new CargoTypeAdapter())
                .registerTypeAdapter(ComponentTile.class, new ComponentTileAdapter())
                .registerTypeAdapter(Coordinates.class, new CoordinatesAdapter())
                .create();
        isRunning = new AtomicBoolean(true);
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    /** Function that listens for new messages and forwards it to the controller
     */
    public void run(){
            try{
                while(isRunning.get()) {
                    String receivedMessage = clientReader.readLine();
                    if (receivedMessage == null)
                        throw new IOException("Connection closed by the client");
                    else if(!receivedMessage.contains("PING"))
                        System.out.println("received message: " + receivedMessage);

                    ActionMessage actionMessage = gson.fromJson(receivedMessage, ActionMessage.class);
                    controller.sendActionToGame(actionMessage);
                }
            }
            catch (IOException e){
                isRunning.set(false);
                System.out.println("Connection with " + playerNickname + " has fallen!");
            }
            /*
            finally {
                try { clientReader.close(); }
                catch (IOException e) {System.out.println(e.getMessage());}
            }
             */
    }
}
