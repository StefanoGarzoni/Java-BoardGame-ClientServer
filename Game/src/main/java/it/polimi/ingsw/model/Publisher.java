package it.polimi.ingsw.model;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.controller.clientProxies.ClientProxy;

public interface Publisher {
    /** Registers a ClientProxy to the list of Observers to be notified by model's updates
     *
     * @param clientProxy to be registered
     */
    public void register(ClientProxy clientProxy);

    /** Unregisters a ClientProxy to the list of Observers to be notified by model's updates
     *
     * @param clientProxy to be unregistered
     */
    public void unregister(ClientProxy clientProxy);

    /** Notify the actionMessage to each observing ClientProxy
     *
     * @param actionMessage to be sent to clients
     */
    public void notify(ActionMessage actionMessage);
}
