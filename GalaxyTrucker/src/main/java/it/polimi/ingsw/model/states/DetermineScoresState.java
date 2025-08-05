package it.polimi.ingsw.model.states;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.model.Game;
import it.polimi.ingsw.model.Player;

import java.util.ArrayList;

public class DetermineScoresState extends GameState{
    DetermineScoresState(Game game) {
        super(game);
    }

    @Override
    public void run(){
        int count =  4;
        int points = 0;
        ActionMessage am = new ActionMessage("DetermineScoresState", "server");
        game.getPublisher().notify(am);

        ArrayList<String> bestShip = searchMostBeautifullShip();

        for(Player p : game.getFlightBoard().getPlayers()){
            //+8 se hai la nave più bella
            if(bestShip.contains(p.getNickname())){
                points+=8;
            }

            //TODO ma quindi ad ogni eliminazione di pezzi dovrei aggiornare questo attributo??
            //-1 per ogni componente perso
            points-=p.getShipBoard().getLostTilesNumber();

            //sommo il valore dei cargo
            p.getShipBoard().calculateTotalCargoValue();
            points+=p.getShipBoard().getTotalCargoValue();

            //punti in base alla posizione finale
            points+=count;

            //invio il messaggio al player contenente il suo scores

            ActionMessage am2 = new ActionMessage("finalScores", "server");
            am2.setData("scores",points);
            am2.setReceiver(p.getNickname());
            game.getPublisher().notify(am2);

            //inizializzo le variabili utili al conteggio
            points=0;
            count--;
        }

        game.setState(new EndGameState(game));
        game.getCurrentState().run();
    }

    @Override
    public void receiveAction(ActionMessage actionMessage) {
        synchronized (game) {
            ActionMessage am = new ActionMessage("isDeterminingScores", "server");
            am.setReceiver(actionMessage.getSender());
            game.getPublisher().notify(am);
        }
    }

    private ArrayList<String> searchMostBeautifullShip(){
        int exposedConnectors = 1000;
        int tmp;
        ArrayList<String> nicknames= new ArrayList<>();
        for(Player p : game.getFlightBoard().getPlayers()){
            tmp=p.getShipBoard().countExposedConnectors();
            if(exposedConnectors > tmp ){
                nicknames.removeAll(nicknames);
                nicknames.add(p.getNickname());
                exposedConnectors = tmp;
            }
            else if(exposedConnectors == tmp){
                nicknames.add(p.getNickname());
                exposedConnectors = tmp;
            }
        }

        return nicknames;
    }
}
