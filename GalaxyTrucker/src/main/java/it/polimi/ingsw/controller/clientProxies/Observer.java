package it.polimi.ingsw.controller.clientProxies;

import it.polimi.ingsw.ActionMessage;

public interface Observer {
    public void send(ActionMessage actionMessage);
}
