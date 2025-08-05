package it.polimi.ingsw.client.states.CommandData;

import it.polimi.ingsw.ActionMessage;

import java.util.function.Consumer;

public class ClientCommandMetaData {

    private final String commandDescription;
    private final Consumer<ActionMessage> handler;

    public ClientCommandMetaData(String commandDescription, Consumer<ActionMessage> handler){
        this.commandDescription = commandDescription;
        this.handler = handler;
    }

    /** Gets the description of the command
     *
     * @return the description of the current command if not null, otherwise returns the commandName
     */
    public String getDescription() { return commandDescription; }
    public Consumer<ActionMessage> getHandler() { return handler; }


}
