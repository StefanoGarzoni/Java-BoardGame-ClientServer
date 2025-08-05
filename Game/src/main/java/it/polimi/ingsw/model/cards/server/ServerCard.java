package it.polimi.ingsw.model.cards.server;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.model.Cargo.*;
import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.FlightBoard;
import it.polimi.ingsw.model.ModelPublisher;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.cards.visitor.ServerCardSolverVisitor;
import it.polimi.ingsw.model.cards.visitor.ServerCardVisitor;
import it.polimi.ingsw.model.cards.visitor.ServerCardVisitor;
import it.polimi.ingsw.model.states.PlayingState;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Interface to describe cards shared functionalities.
 * <p>Cards must be defined through this interface</p>
 * @author Francesco Montefusco
 */
public abstract class ServerCard {
    protected transient Map<String, Consumer<ActionMessage>> mapMethods;
    protected CardNotifier cardNotifier;
    protected FlightBoard flightBoard;
    protected ModelPublisher publisher;
    private PlayingState playingState;

    public abstract String getFileName();

    protected ServerCard() {
        mapMethods = new HashMap<String, Consumer<ActionMessage>>();
    }

    /**
     * Starts a card and setups the card internal playing state, called by {@link it.polimi.ingsw.model.cards.visitor.ServerCardSolverVisitor}
     * @param flightBoard to read and modify the model
     * @param publisher to send messages to the clients
     */
    public abstract void play(FlightBoard flightBoard, ModelPublisher publisher);

    /**
     * Accept the card visitor, this method will be called by the current state
     * @param visitor an instance of the visitor for the desired behaviour
     * @param publisher to send messages to the clients
     * @param playingState to update the state when the card ends
     */
    public abstract void accept(ServerCardVisitor visitor, ModelPublisher publisher, PlayingState playingState);

    /**
     * Sends commands to the card, through the form of an {@link ActionMessage}
     * @param actionMessage the message
     */
    public abstract void receive(ActionMessage actionMessage);


    /**
     * Communicates to the clients that the card has ended,
     * @param publisher to send messages
     * @param flightBoard to get the first player in the race, so the one that should draw the card
     */
    protected void endCard(ModelPublisher publisher, FlightBoard flightBoard) {
        playingState.releaseCard();
        ActionMessage am = new ActionMessage("cardEnded", "server");
        publisher.notify(am);
    }

    /**
     * Notifies all clients that a card has started
     * @param publisher to send messages
     * @param fileName to get the card name to notify
     */
    @Deprecated
    protected void startCard(ModelPublisher publisher, String fileName) {
        ActionMessage am = new ActionMessage("cardHasBeenDrawn", "server");
        am.setData("fileName", fileName);
        publisher.notify(am);
    }

    /**
     * sets up the card
     * @param flightBoard to get the current state
     * @param publisher to notify
     */
    protected void cardSetup(FlightBoard flightBoard, ModelPublisher publisher) {
        this.flightBoard = flightBoard;
        this.publisher = publisher;
        this.cardNotifier = new CardNotifier(publisher);
    }



    protected void removeCargo(int cargoAmount, Player player){
        AtomicInteger cargoCounter = new AtomicInteger(cargoAmount);
        //A sortedMap comes handy since you can sort keys
        Map<String, ArrayList<Coordinates>> allCargos = player.getShipBoard().getTilesContainsCargoWithMostValue(4);
        Comparator<String> cargoComparator = (first, second) -> {
            //TODO IMPROVE
            int a;
            int b;
            switch (first){
                case "Red" -> a = 4;
                case "Yellow" -> a = 3;
                case "Green" -> a = 2;
                case "Blue" -> a = 1;
                default -> a = 0;
            }
            switch (second){
                case "Red" -> b = 4;
                case "Yellow" -> b = 3;
                case "Green" -> b = 2;
                case "Blue" -> b = 1;
                default -> b = 0;
            }
            return a - b;
        };

        TreeMap<String, ArrayList<Coordinates>> sortedCargos = new TreeMap<>(cargoComparator);
        sortedCargos.putAll(allCargos);
        Map<Coordinates, CargoType> coordiantesToQuery = new HashMap<>();

        for(Map.Entry<String, ArrayList<Coordinates>> entry : sortedCargos.entrySet()){
            String key = entry.getKey();
            ArrayList<Coordinates> value = entry.getValue();

            CargoType cargo;
            switch (key){
                case "Red" -> cargo = new RedCargo();
                case "Yellow" -> cargo = new YellowCargo();
                case "Green" -> cargo = new GreenCargo();
                case "Blue" -> cargo = new BlueCargo();
                default -> throw new IllegalArgumentException("Invalid key");
            }
            if(value.size() > cargoCounter.get()){
                //Add them to list
                value.forEach(coord -> {
                    coordiantesToQuery.put(coord, cargo);
                    cargoCounter.getAndDecrement();
                });
            }
            else if(value.size() == cargoCounter.get()){
                //We are done
                for(Coordinates coord : value) player.getShipBoard().increaseCargo(coord, cargo, true);
                cargoCounter.getAndDecrement();
                break;
            }
            else{
                //We need to remove everything but then we keep going
                for(Coordinates coord : value) player.getShipBoard().increaseCargo(coord, cargo, true);
            }
            if(cargoCounter.get() == 0) break;
        }

        cardNotifier.askCargoToRemove(player, coordiantesToQuery);

    }

    public void setPlayingState(PlayingState playingState) {
        this.playingState = playingState;
    }


}
