package it.polimi.ingsw.model.cards.client;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.client.ClientController;
import it.polimi.ingsw.model.cards.Card;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class ClientCard {
    public final Map<String, BiConsumer<ActionMessage, ClientController>> methodsFromViewMap;
    public final Map<String, BiConsumer<ActionMessage, ClientController>> methodsFromServerMap;

    public ClientCard(){
        methodsFromViewMap = new HashMap<>();
        methodsFromServerMap = new HashMap<>();
    }


    public abstract String getFileName();

    /** Sets up the card status
     *
     * @param clientController controller containing classes that the card can use to send messages to server,
     *                         modify the client view or the client model
     */
    public abstract void run(ClientController clientController);

    /** Method that sorts server's messages to the correct function that handle the server request
     *
     * @param actionMessage message from the server
     * @param clientController controller containing classes that the card can use to send messages to server,
     *                         modify the client view or the client model
     */
    public abstract void processFromServer(ActionMessage actionMessage, ClientController clientController);

    /** Method that sorts user's messages to the correct function that handle the user request
     *
     * @param actionMessage message from the server
     * @param clientController controller containing classes that the card can use to send messages to server,
     *                         modify the client view or the client model
     */
    public abstract void processFromClient(ActionMessage actionMessage, ClientController clientController);

    public abstract Card getCardData();
}
