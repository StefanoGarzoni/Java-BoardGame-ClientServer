package it.polimi.ingsw.client;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.client.states.ClientState;
import it.polimi.ingsw.client.states.ClientSetupState;
import it.polimi.ingsw.model.clientModel.ViewFlightBoard;

public class ClientController {

    private ServerProxy serverProxy;
    private ViewFlightBoard viewFlightBoard;
    private ClientState currentState;
    private View view ;
    private String nickname;

    public ClientController(View view){
        this.view = view;

        this.currentState = new ClientSetupState(this);
    }

    public String getNickname() { return nickname; }

    public void setNickname(String nickname) { this.nickname = nickname; }

    public void setServerProxy(ServerProxy serverProxy){ this.serverProxy = serverProxy; }

    public ServerProxy getServerProxy() {
        return serverProxy;
    }

    public ClientState getCurrentState() {
        return currentState;
    }

    public View getView() {
        return view;
    }

    public void setState(ClientState clientState){
        this.currentState = clientState;
    }

    public void setViewFlightBoard(ViewFlightBoard viewFlightBoard) {
        this.viewFlightBoard = viewFlightBoard;
    }

    public ViewFlightBoard getViewFlightBoard() {
        return viewFlightBoard;
    }
}
