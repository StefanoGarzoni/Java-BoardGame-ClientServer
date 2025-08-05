package it.polimi.ingsw.client.states;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.client.ClientController;
import it.polimi.ingsw.model.ShipBoard;
import it.polimi.ingsw.model.cards.client.ClientCard;
import it.polimi.ingsw.model.clientModel.ViewShipBoard;

import java.util.HashMap;

public class ClientPlayingState extends ClientState{
    private ClientCard currentCard;


    public ClientPlayingState(ClientController clientController) {
        super(clientController);
    }

    @Override
    public void run() {
        methodsFromServerMap.put("DetermineScoresState", this::nextState);
        methodsFromServerMap.put("cardsFinished", this::noMoreCards);
        methodsFromServerMap.put("cardDrawing", this::cardHasBeenDrawn);
        methodsFromServerMap.put("cardEnded", this::cardHasEnded);
        methodsFromServerMap.put("playingState", this::doNothing);
        methodsFromServerMap.put("cardHasBeenStopped", this::cardHasBeenStopped);

        methodsFromViewMap.put("drawCard", this::askForCardDrawing);
        methodsFromViewMap.put("showShipBoard", this::showShipBoard);
        methodsFromViewMap.put("showFlightBoard", this::showFlightBoard);
        methodsFromViewMap.put("stopCard", this::askToStopCard);

        ViewShipBoard playerShipboard = clientController.getViewFlightBoard().getViewShipBoard(clientController.getNickname());
        clientController.getView().showFlightBoard(clientController.getViewFlightBoard());
        clientController.getView().showShipboard(playerShipboard);
    }

    @Override
    public void processFromServer(ActionMessage actionMessage) {
        synchronized (clientController) {
            try {
                if (currentCard == null
                        || actionMessage.getActionName().equals("cardEnded")
                        || actionMessage.getActionName().equals("PING")
                        || actionMessage.getActionName().equals("cardHasBeenStopped")
                )
                    methodsFromServerMap.get(actionMessage.getActionName()).accept(actionMessage);
                else
                    currentCard.processFromServer(actionMessage, clientController);
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }
    }

    @Override
    public void processFromView(ActionMessage actionMessage) {
        synchronized (clientController) {
            try {
                if (currentCard == null || methodsFromViewMap.containsKey(actionMessage.getActionName()))
                    methodsFromViewMap.get(actionMessage.getActionName()).accept(actionMessage);
                else
                    currentCard.processFromClient(actionMessage, clientController);
            } catch (Exception e) {
                System.err.println("doesNoRecognize " + actionMessage.getActionName() + " fromView");
                // e.printStackTrace(System.out);
            }
        }
    }

    public void askToStopCard(ActionMessage actionMessage){
        ActionMessage stopCardRequest = new ActionMessage("stopCard", clientController.getNickname());
        clientController.getServerProxy().send(stopCardRequest);
    }

    private void askForCardDrawing(ActionMessage actionMessage){
        if(currentCard == null){
            ActionMessage messageToServer = new ActionMessage("drawCard", clientController.getNickname());
            clientController.getServerProxy().send(messageToServer);
        }
        else{
            clientController.getView().showErrorMessage("You can't draw a new card now");
        }
    }

    private void cardHasBeenDrawn(ActionMessage actionMessage){
        String cardFileName = (String) actionMessage.getData("cardFileName");

        ClientCard drawnCard = clientController.getViewFlightBoard().getCardFromDecks(cardFileName);
        if(drawnCard != null){
            currentCard = drawnCard;
            clientController.getView().showMessage("A new card has been drawn!");
            currentCard.run(clientController);
        }
        else {
            clientController.getView().showMessage("Error! Drawn card not found!");
            clientController.getView().showMessage("You can't continue... We must remove you from this game");
            putUserInSetupState();
        }
    }

    private void cardHasBeenStopped(ActionMessage actionMessage){
        HashMap<String, ShipBoard> modifiedShipboards = (HashMap<String, ShipBoard>) actionMessage.getData("updatedShipBoards");
        modifiedShipboards.forEach((nickname, shipboard) -> {
            clientController.getViewFlightBoard().getViewShipBoard(nickname).updateViewShipBoard(shipboard);
        });

        clientController.getView().showMessage("The card has been stopped! This is your updated shipboard. You can now draw a new card");
        currentCard = null;
        clientController.getView().showShipboard(clientController.getViewFlightBoard().getViewShipBoard(clientController.getNickname()));
    }

    private void noMoreCards(ActionMessage actionMessage){
        if(actionMessage.getReceiver().equals(clientController.getNickname()))
            clientController.getView().showErrorMessage("No more cards to draw");
    }

    private void nextState(ActionMessage actionMessage){
        this.clientController.setState(new ClientDetermineScoreState(this.clientController));
        clientController.getCurrentState().run();
    }

    private void cardHasEnded(ActionMessage actionMessage){
        currentCard = null;
        clientController.getView().showFlightBoard(clientController.getViewFlightBoard());
        clientController.getView().showMessage("This card has ended, now a new card can be drawn!");
    }

    private void doNothing(ActionMessage actionMessage){

    }

    private void showShipBoard(ActionMessage request){
        String playerName = (String) request.getData("playerName");
        if(playerName.isBlank()) clientController.getView()
                .showShipboard(clientController.getViewFlightBoard().getViewShipBoard(clientController.getNickname()));
        else clientController.getView().showShipboard(clientController.getViewFlightBoard().getViewShipBoard(playerName));
    }

    private void showFlightBoard(ActionMessage request){
        clientController.getView().showFlightBoard();
    }
}