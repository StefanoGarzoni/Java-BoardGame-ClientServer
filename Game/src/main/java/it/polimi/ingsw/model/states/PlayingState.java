package it.polimi.ingsw.model.states;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.model.Game;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.ShipBoard;
import it.polimi.ingsw.model.cards.server.ServerCard;
import it.polimi.ingsw.model.cards.visitor.ServerCardSolverVisitor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class PlayingState extends GameState{
    private Map<String, Consumer<ActionMessage>> mapMethods;
    ServerCardSolverVisitor cardSolver;
    ServerCard cardInGame;

    PlayingState(Game game) {
        super(game);
        cardInGame = null;
        cardSolver = new ServerCardSolverVisitor(game.getFlightBoard());
        mapMethods = new HashMap<>();
        mapMethods.put("drawCard", this::drawCard);
        mapMethods.put("handleCard", this::handleCard);
        mapMethods.put("stopCard", this::stopCard);
        //mapMethods.put("releaseCard", this::releaseCard);
    }

    //TODO se la carta manda un messaggio quando è stata risolta, non serve questo metodo
    // forse serve per notificare tutti?

    /*
    public void releaseCard(ActionMessage actionMessage) {
        cardInGame = null;
        ActionMessage am = new ActionMessage("cardReleased", "server");
        //am.setReceiver(actionMessage.getSender());
        game.getPublisher().notify(am);
    }
    */

    private synchronized void handleCard(ActionMessage actionMessage) {
        cardInGame.receive(actionMessage);
    }

    private synchronized void drawCard(ActionMessage actionMessage) {
        if(cardInGame == null) {
            if(game.getFlightBoard().cardsStillInDeck() > 0){
                cardInGame = game.getFlightBoard().randomCardDrawing();

                sendCardToPlayers(actionMessage.getSender());

                cardInGame.accept(cardSolver, game.getPublisher(), this);
            }
            else {
                endMatch();
            }
        }
    }

    private void sendCardToPlayers(String receiverNickname){
        ActionMessage am = new ActionMessage("cardDrawing", "server");

        am.setReceiver(receiverNickname);
        am.setData("cardFileName", cardInGame.getFileName());

        if( game.getFlightBoard().isLastCard())
            am.setData("lastCard", true);
        else
            am.setData("lastCard", false);

        game.getPublisher().notify(am);
    }

    private void endMatch() {
        game.setState(new DetermineScoresState(game));
        game.getCurrentState().run();
    }

    public void releaseCard(){
        cardInGame = null;
    }

    public void stopCard(ActionMessage actionMessage){
        releaseCard();

        ActionMessage cardHasBeenStopped = new ActionMessage("cardHasBeenStopped", "server");

        HashMap<String, ShipBoard> modifiedShipboards = new HashMap<>();
        game.getFlightBoard().getActivePlayers().forEach((player)->{
            modifiedShipboards.put(player.getNickname(), player.getShipBoard());
        });
        cardHasBeenStopped.setData("updatedShipBoards", modifiedShipboards);

        game.getPublisher().notify(cardHasBeenStopped);
    }

    @Override
    public void run(){
        ActionMessage am = new ActionMessage("playingState", "server");
        game.getPublisher().notify(am);
    }

    @Override
    public void receiveAction(ActionMessage actionMessage) {
        synchronized (game){
            try {
                if (cardInGame == null && actionMessage.getActionName().equals("drawCard"))
                    mapMethods.get(actionMessage.getActionName()).accept(actionMessage);
                else if(actionMessage.getActionName().equals("stopCard")) {
                    mapMethods.get(actionMessage.getActionName()).accept(actionMessage);
                }
                else
                    cardInGame.receive(actionMessage);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
