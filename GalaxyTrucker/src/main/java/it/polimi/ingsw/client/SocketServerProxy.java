package it.polimi.ingsw.client;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.ActionMessageAdapter;
import it.polimi.ingsw.model.Cargo.CargoType;
import it.polimi.ingsw.model.Cargo.util.CargoTypeAdapter;
import it.polimi.ingsw.model.ComponentTile.ComponentTile;
import it.polimi.ingsw.model.ComponentTile.ComponentTileAdapter;
import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.CoordinatesAdapter;
import it.polimi.ingsw.model.cards.server.ServerCard;
import it.polimi.ingsw.model.cards.util.ServerCardAdapter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;

import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class SocketServerProxy implements ServerProxy {
    private final AtomicBoolean socketIsClosingIntentionally;

    private final Gson gson;

    private ClientController clientController;

    private Socket socket;
    private final String serverIpAddress;
    private final int serverIpPort;

    private PrintWriter socketOutputWriter;
    private BufferedReader socketInputReader;

    private Thread socketListenerThread;

    /*Constructor of SocketServerProxy
    * @param socket
    * @param output
    * @param input
    * @param clientController
     */
    public SocketServerProxy(String serverIpAddress, int serverIpPort, ClientController clientController) throws IOException {
        this.serverIpAddress = serverIpAddress;
        this.serverIpPort = serverIpPort;
        this.clientController = clientController;

        socketIsClosingIntentionally = new AtomicBoolean(false);

        gson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .registerTypeAdapter(ActionMessage.class, new ActionMessageAdapter())
                .registerTypeAdapter(ServerCard.class, new ServerCardAdapter())
                .registerTypeAdapter(CargoType.class, new CargoTypeAdapter())
                .registerTypeAdapter(ComponentTile.class, new ComponentTileAdapter())
                .registerTypeAdapter(Coordinates.class, new CoordinatesAdapter())
                .create();
    }

    /** sends to Server an ActionMessage in JSON format
     * @param message ActionMessage to send to the server
     */
    @Override
    public void send(ActionMessage message) {
        String json = gson.toJson(message);
        this.socketOutputWriter.println(json);
        this.socketOutputWriter.flush();
    }

    @Override
    public boolean connectToServer(String nickname, int playersNumber, int gameLevel) throws IOException {
        // creates the connection to the WelcomeSocket
        this.socket = new Socket(serverIpAddress, serverIpPort);
        this.socketOutputWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        this.socketInputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // preparing ActionMessage with Player params
        ActionMessage responseMessage = new ActionMessage("playerParams", nickname);
        responseMessage.setData("nickname", nickname);
        responseMessage.setData("possibleNewGameLevel", gameLevel);
        responseMessage.setData("possibleNewGamePlayersNumber", playersNumber);

        // waits for a "getPlayerParams" ActionMessage
        String getPlayerParamsMessage = socketInputReader.readLine();
        ActionMessage requestMessage = gson.fromJson(getPlayerParamsMessage, ActionMessage.class);
        while (!requestMessage.getActionName().equals("getPlayerParams")){
            getPlayerParamsMessage = socketInputReader.readLine();
            requestMessage = gson.fromJson(getPlayerParamsMessage, ActionMessage.class);
        }

        // send response with Player's Params
        socketOutputWriter.println(gson.toJson(responseMessage));

        // waits for a "ConnectionConfirmation" ActionMessage
        String connectionConfirmationMessage = socketInputReader.readLine();
        ActionMessage confirmationMessage = gson.fromJson(connectionConfirmationMessage, ActionMessage.class);
        while (!confirmationMessage.getActionName().equals("ConnectionConfirmation")){
            connectionConfirmationMessage = socketInputReader.readLine();
            confirmationMessage = gson.fromJson(connectionConfirmationMessage, ActionMessage.class);
        }

        socketListenerThread = new Thread(()->{
            boolean connectionIsActive = true;
            while(!Thread.currentThread().isInterrupted() && connectionIsActive){
                try{
                    String receivedMessage = socketInputReader.readLine();
                    if(receivedMessage != null){

                        ActionMessage actionMessage = gson.fromJson(receivedMessage, ActionMessage.class);
                        clientController.getCurrentState().processFromServer(actionMessage);
                    }
                }
                catch (IOException e){
                    if(!socketIsClosingIntentionally.get()){
                        connectionIsActive = false;
                        ActionMessage connectionFallenMessage = new ActionMessage("connectionFallen", "");
                        clientController.getCurrentState().processFromServer(connectionFallenMessage);
                    }
                }
            }
        });

        socketIsClosingIntentionally.compareAndSet(true, false);
        socketListenerThread.start();

        return true;
    }

    /** Stops the thread that loops listening for server's messages on the socket and closes the communication
     */
    public void closeConnection(){
        socketIsClosingIntentionally.compareAndSet(false, true);
        socketListenerThread.interrupt();
        try {
            socket.close();
            socketInputReader.close();
            socketOutputWriter.close();
        } catch (IOException ignored) {}
    }
}
