package it.polimi.ingsw.client.states;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.client.ClientController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class ClientState {
    final ClientController clientController;
    final Map<String, Consumer<ActionMessage>> methodsFromViewMap;
    final Map<String, Consumer<ActionMessage>> methodsFromServerMap;

    public ClientState(ClientController clientController){
        this.clientController = clientController;

        methodsFromServerMap = new HashMap<>();
        methodsFromViewMap = new HashMap<>();

        methodsFromViewMap.put("leaveGame", this::leaveGame);
        methodsFromViewMap.put("showAllCommands", this::showAllCommands);

        methodsFromServerMap.put("playerLeftGame", this::playerLeftGame);
        methodsFromServerMap.put("connectionFallen", this::connectionFallen);
        methodsFromServerMap.put("PING", this::sendPong);
    }

    /** This method must prepare the state to handle user and server future requests
     */
    public abstract void run();

    /** This method is called so the state can handle a message coming from the server
     *
     * @param actionMessage message containing all information needed to handle the request correctly
     */
    public abstract void processFromServer(ActionMessage actionMessage);

    /** This method is called so the state can handle a message coming from the user
     *
     * @param actionMessage message containing all information needed to handle the request correctly
     */
    public abstract void processFromView(ActionMessage actionMessage);

    void playerLeftGame(ActionMessage actionMessage){
        String playerNickname = actionMessage.getReceiver();
        if(!playerNickname.equals(clientController.getNickname())){
            clientController.getView().showMessage(playerNickname + " has left the game!");
        }
    }

    /** Send a response for a server's ping, to communicate that the client is still alive
     *
     * @param actionMessage message containing a PONG
     */
    void sendPong(ActionMessage actionMessage){
        ActionMessage pongResponse = new ActionMessage("PONG", clientController.getNickname());
        clientController.getServerProxy().send(pongResponse);
    }

    /** Handles the request of the player to leave the game, forwarding it to the setup state
     *
     * @param actionMessage to communicate the intention to leave a game
     */
    void leaveGame(ActionMessage actionMessage){
        ActionMessage messageToServer = new ActionMessage("leaveGame", clientController.getNickname());
        clientController.getServerProxy().send(messageToServer);
        putUserInSetupState();
    }

    /** Handles a connection crash between client and server, forwarding the player in setup state
     *
     * @param actionMessage to communicate a connection crash
     */
    void connectionFallen(ActionMessage actionMessage){
        clientController.getView().showErrorMessage("Connection with the Server has fallen!");
        putUserInSetupState();
    }

    /** Puts the player in setup state with a delay of 10 seconds, so the user can enter a new game
     */
    void putUserInSetupState(){
        AtomicInteger countdown = new AtomicInteger(10);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(
                () -> {
                    if(countdown.getAndDecrement() > 0)
                        clientController.getView().showErrorMessage("You will be redirected to the home page in "+countdown+" seconds");
                    else{
                        scheduler.shutdown();

                        clientController.setState(new ClientSetupState(clientController));
                        clientController.getCurrentState().run();

                        clientController.getServerProxy().closeConnection();
                        clientController.setViewFlightBoard(null);
                    }
                },
                0,
                1,
                TimeUnit.SECONDS
        );
    }

    /** Prints all available commands in the current state using the view
     */
    protected void showAllCommands(){
        List<String> messages = new ArrayList<>();
        messages.add("These are all available commands:");
        methodsFromViewMap.forEach((commandName, consumer) -> {
            messages.add(commandName);
        });
        clientController.getView().showMessage(messages);
    }

    protected void showAllCommands(ActionMessage request){
        showAllCommands();
    }
}
