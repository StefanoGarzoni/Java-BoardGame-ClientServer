package it.polimi.ingsw.model.cards.client.concrete;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.client.ClientController;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.client.ClientCard;
import it.polimi.ingsw.model.cards.generic.StardustCard;
import it.polimi.ingsw.model.clientModel.ViewShipBoard;

import java.util.HashMap;

public class StardustClientCard extends ClientCard {
    private final StardustCard cardData;

    /* constructors */

    public StardustClientCard(StardustCard stardustCard){
        cardData = stardustCard;
    }

    public StardustClientCard(int cardLevel, String fileName) {
        cardData = new StardustCard(cardLevel, fileName);

        methodsFromServerMap.put("dayFlightLoss", this::updatePlayersPosition);
    }

    /* service methods */

    @Override
    public String getFileName() { return cardData.getFileName(); }

    @Override
    public void run(ClientController clientController) {
        clientController.getView().showMessage("Star Dust card has been drawn!");
        clientController.getView().showCard(this.cardData);
    }

    @Override
    public Card getCardData() {
        return cardData;
    }

    @Override
    public void processFromServer(ActionMessage actionMessage, ClientController clientController){
        try {
            this.methodsFromServerMap.get(actionMessage.getActionName()).accept(actionMessage, clientController);
        } catch (Exception e) {
            return;
        }
    }
    @Override
    public void processFromClient(ActionMessage actionMessage, ClientController clientController){
        try {
            this.methodsFromViewMap.get(actionMessage.getActionName()).accept(actionMessage, clientController);
        } catch (Exception e) {
            return;
        }
    }

    /* client model update */

    private void updatePlayersPosition(ActionMessage actionMessage, ClientController clientController){
        HashMap<String, Integer> updatedPositionsMap = (HashMap<String, Integer>) actionMessage.getData("updatedPositions");

        updatedPositionsMap.forEach((nickname, newAbsPosition) -> {
            ViewShipBoard shipBoard = clientController.getViewFlightBoard().getViewShipBoard(nickname);
            shipBoard.setAbsPosition(newAbsPosition);
        });

        ViewShipBoard userShipBoard = clientController.getViewFlightBoard().getViewShipBoard(clientController.getNickname());
        clientController.getView().showMessage("After StarDust card, your new absolute position is "+userShipBoard.getAbsPosition());
        clientController.getView().showFlightBoard();
    }
}
