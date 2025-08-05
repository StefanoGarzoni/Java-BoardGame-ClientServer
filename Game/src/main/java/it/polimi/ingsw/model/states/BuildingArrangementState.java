package it.polimi.ingsw.model.states;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.FileUploaders.CardsLoader;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class BuildingArrangementState extends GameState{
    BuildingArrangementState(Game game) {
        super(game);
    }

    @Override
    public void run() {

        //notify that the lobbystate is finished
        ActionMessage am = new ActionMessage("BuildingArrangementState", "server");
        //am.setData("timer", 10);
        game.getPublisher().notify(am);

        //notify at the player their color
        ActionMessage colorsMessage = new ActionMessage("Colors", "server");
        for(Player p : game.getFlightBoard().getPlayers()){
            colorsMessage.setData(p.getNickname(),p.getColor());
        }
        game.getPublisher().notify(colorsMessage);

        //start set the attributes that they will be crucial to play
        if(game.getGameLevel()==2){
            game.getFlightBoard().setHourGlass();
        }

        int lapLength = switch (game.getGameLevel()) {
            case 1 -> 18;
            case 2 -> 24;
            case 3 -> 34;
            default -> -1;
        };
        game.getFlightBoard().setLapLength(lapLength);

        String cardsFilePath = "gameAssets/cards.json";
        try{
            CardsLoader cardsLoader = new CardsLoader(cardsFilePath);

            ArrayList<Deck> decks = cardsLoader.getRandomServerDecksFromCardsJson(game.getGameLevel());
            game.getFlightBoard().setDecks(decks);

            ActionMessage actionMessage = new ActionMessage("decksCreated", "server");
            actionMessage.setData("decks", decks);
            game.getPublisher().notify(actionMessage);
        }
        catch (FileNotFoundException e){
            //FIXME: cosa fare in questo caso?? Non deve permettere l'avanzamento degli stati
            e.printStackTrace();
            return;
        }

        ComponentTilesBunch tilesBunch = new ComponentTilesBunch();
        game.getFlightBoard().setTilesBunch(tilesBunch);

        game.setState(new ShipBoardBuildingState(game));
        game.getCurrentState().run();
    }

    @Override
    public void receiveAction(ActionMessage actionMessage) {
        game.getPublisher().notify(new ActionMessage("BuildingArrangementState", "server"));
    }
}