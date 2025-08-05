package it.polimi.ingsw.model.cards.client.concrete;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.client.ClientController;
import it.polimi.ingsw.model.ShipBoard;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.client.ClientCard;
import it.polimi.ingsw.model.cards.generic.EpidemicCard;
import it.polimi.ingsw.model.clientModel.ViewShipBoard;

import java.util.ArrayList;
import java.util.HashMap;

public class EpidemicClientCard extends ClientCard {
    private final EpidemicCard cardData;

    /* Constructors */

    public EpidemicClientCard(EpidemicCard epidemicCard){ this.cardData = epidemicCard; }

    public EpidemicClientCard(int cardLevel, String fileName) {
        cardData = new EpidemicCard(cardLevel, fileName);
    }

    /* Service methods */

    @Override
    public String getFileName() { return cardData.getFileName(); }

    @Override
    public void run(ClientController clientController) {
        clientController.getView().showMessage("Epidemic card has been drawn!");
        clientController.getView().showCard(this.cardData);

        methodsFromServerMap.put("solveEpidemicCard", this::updateShipBoards);
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

    /* Client Model update */

    private void updateShipBoards(ActionMessage actionMessage, ClientController clientController){
        HashMap<String, ShipBoard> updatedShipBoards = (HashMap<String, ShipBoard>) actionMessage.getData("updatedShipBoards");

        updatedShipBoards.forEach((nickname, updatedShipBoard) -> {
            clientController.getViewFlightBoard().getViewShipBoard(nickname).updateViewShipBoard(updatedShipBoard);
        });

        // update view of current player
        ViewShipBoard userShipboard = clientController.getViewFlightBoard().getViewShipBoard(clientController.getNickname());
        clientController.getView().showMessage(
                "Epidemic card has been applied! This is your updated shipboard"
        );
        clientController.getView().showShipboard(userShipboard);
    }
}
