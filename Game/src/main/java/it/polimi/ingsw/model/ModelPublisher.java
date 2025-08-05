package it.polimi.ingsw.model;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.controller.clientProxies.ClientProxy;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ModelPublisher implements Publisher{
    private final ConcurrentHashMap<ClientProxy, Boolean> clientProxies;

    ModelPublisher(){ clientProxies = new ConcurrentHashMap<>(); }

    @Override
    public void register(ClientProxy clientProxy) { clientProxies.put(clientProxy, true); }

    @Override
    public void unregister(ClientProxy clientProxy) { clientProxies.remove(clientProxy); }
    public void unregister(String playerNickname) {
        clientProxies.entrySet().removeIf(entry -> entry.getKey().getPlayerNickname().equals(playerNickname));
    }

    /** Sends an ActionMessage to all clients that were registered
     *
     * @param actionMessage to be sent to clients
     */
    @Override
    public void notify(ActionMessage actionMessage) {
        for(ClientProxy c : clientProxies.keySet()){
            c.send(actionMessage);
        }
    }

    /** Sets all ClientProxy as not alive.
     * The aliveness status will be updated if a pong is received back from the Client
     */
    public void resetPlayerAliveness(){
        clientProxies.replaceAll((key, value) -> value = false);
    }

    /** Updates the aliveness status of a player
     *
     * @param nickname of the player who is alive
     */
    public void markPlayerAsAlive(String nickname){
        for(Map.Entry<ClientProxy, Boolean> entry : clientProxies.entrySet()){
            if(entry.getKey().getPlayerNickname().equals(nickname))
                entry.setValue(true);
        }
    }

    /** Removes all client proxies which are not alive
     */
    public void removeNotAlivePlayers(){
        Iterator<Map.Entry<ClientProxy, Boolean>> iterator = clientProxies.entrySet().iterator();

        while(iterator.hasNext()){
            Map.Entry<ClientProxy, Boolean> entry = iterator.next();

            if(!entry.getValue()){  // if the activity flag is set to false
                entry.getKey().closeConnectionToClient();
                iterator.remove();
            }
        }
    }
}
