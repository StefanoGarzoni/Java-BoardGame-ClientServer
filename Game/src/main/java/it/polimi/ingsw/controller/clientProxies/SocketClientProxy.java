package it.polimi.ingsw.controller.clientProxies;

import com.google.gson.GsonBuilder;
import it.polimi.ingsw.ActionMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import com.google.gson.Gson;
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

public class SocketClientProxy extends ClientProxy{
    private final Gson gson;
    private final PrintWriter clientWriter;
    private final SocketClientProxyThread socketThread;
    private final BufferedReader clientReader;
    private final Socket clientSocket;

    /*
    public SocketClientProxy(String nickname, Socket clientSocket) throws IOException {
        super(nickname);
        gson = new Gson();

        this.clientSocket = clientSocket;
        clientWriter = new PrintWriter(clientSocket.getOutputStream(), true);

        socketThread = new SocketClientProxyThread(nickname, getController(), clientSocket);
        socketThread.start();
    }
     */

    public SocketClientProxy(String nickname, Socket clientSocket, PrintWriter writer, BufferedReader reader, Controller controller) throws IOException {
        super(nickname);
        gson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .registerTypeAdapter(ActionMessage.class, new ActionMessageAdapter())
                .registerTypeAdapter(ServerCard.class, new ServerCardAdapter())
                .registerTypeAdapter(CargoType.class, new CargoTypeAdapter())
                .registerTypeAdapter(ComponentTile.class, new ComponentTileAdapter())
                .registerTypeAdapter(Coordinates.class, new CoordinatesAdapter())
                .create();

        this.clientSocket = clientSocket;
        clientSocket.setSoTimeout(60000);
        clientWriter =  writer;
        clientReader = reader;
        setController(controller);

        socketThread = new SocketClientProxyThread(nickname, getController(), clientSocket);
        socketThread.start();
    }

    /** Closes the socket by which client and server communicate.
     * Because of this, a SocketException is thrown in SocketClientProxyThread and then the server's
     * listening on socket loop will stop.
     */
    @Override
    public void closeConnectionToClient() {
        try{ clientSocket.close(); }
        catch (IOException e) { e.printStackTrace(System.out); }
    }

    /** Function that sends the ActionMessage to the client
     *
     * @param actionMessage to be sent to the client
     */
    @Override
    public void send(ActionMessage actionMessage) {
        System.out.println(gson.toJson(actionMessage)); //FIXME remove after debugging
        clientWriter.println(gson.toJson(actionMessage));
    }
}
