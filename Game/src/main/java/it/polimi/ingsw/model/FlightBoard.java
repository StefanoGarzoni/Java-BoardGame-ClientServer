package it.polimi.ingsw.model;

import it.polimi.ingsw.model.cards.server.ServerCard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FlightBoard implements Serializable {
    private ArrayList<Player> players;
    private ArrayList<Deck> decks;
    transient Random rand;
    private int lapLength;
    private ComponentTilesBunch tilesBunch;
    private HourGlass hourGlass;
    private final Die die;

    public FlightBoard(){
        this.tilesBunch = null;
        this.players = new ArrayList<>();
        this.decks = null;
        this.rand = new Random();
        this.lapLength = -1;
        this.hourGlass = null;
        this.die = new Die(6);
    }

    public boolean HGIsFree(){
        return hourGlass.isFree();
    }

//    public void takeHG(){
//        hourGlass.takeHG();
//    }

    public void flipHourGlass(){
        hourGlass.flip();
    }

    public int getNumFlipHG(){
        return hourGlass.getFlippedTimes();
    }

    public ComponentTilesBunch getTilesBunch() {
        return tilesBunch;
    }

    public void setLapLength(int lapLength){
        if(this.lapLength == -1 && lapLength>0){
            this.lapLength = lapLength;
        }
    }

    //TODO rivedere i secondi corretti
    public void setHourGlass(){
        if(hourGlass == null){
            this.hourGlass = new HourGlass(90000);
        }
    }

    public void setDecks(ArrayList<Deck> decks){
        if(this.decks == null && decks.size()==3){
            this.decks = new ArrayList<>(decks);
        }
    }

    /** Simulates the result of a correct die roll
     *
     * @return random value from -2 to 8
     */
    public int rollDie(){
        return die.roll() + die.roll() - 4;
    }

    public int getLapLength() {
        return lapLength;
    }

    /** Sets TilesBunch reference. This operation is allowed only once, to guarantee its immutability
     *
     * @param tilesBunch reference to the desired tiles bunch
     */
    public void setTilesBunch(ComponentTilesBunch tilesBunch){
        if(this.tilesBunch == null){
            this.tilesBunch = tilesBunch;
        }
    }

    public void addPlayer(Player p){ players.add(p);}

    /** Returns a Player with the specified nickname, if present
     *
     * @param nickname of the desired Player
     * @return Player with that nickname
     */
    public Player getPlayerByNickname(String nickname){
        for (Player p : players) {
            if (p.getNickname().equals(nickname))
                return p;
        }
        return null;
    }

    // deck is immutable, so it can be returned without side effects
    public Deck getDeck(int deckNumber) { return  decks.get(deckNumber); }

    public boolean isLastCard(){
        return decks.isEmpty();
    }

    /** Randomly extract a card from a random deck
     *
     * @return Random card
     */
    public ServerCard randomCardDrawing(){
        if(decks.isEmpty())
            return null;

        int index = rand.nextInt(decks.size());     // generate a random cardLevel to choose a deck
        ServerCard toReturn = decks.get(index).randomCardDrawing();   // asks the deck for a random card

        if(decks.get(index).getSize() <= 0 )    // remove empty deck
            decks.remove(index);

        return toReturn;
    }

    /** Gets number of remaining cards
     *
     * @return number of cards remaining in the playing deck
     */
    public int cardsStillInDeck(){
        int tot=0;
        for(Deck d : decks){
            tot+=d.getSize();
        }
        return tot;
    }

    /** Returns a copy of the list of players, to prevent original list alteration
     *
     * @return List of players of the game
     */
    public ArrayList<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    /** Extracts all players who haven't left the game or whose connection didn't crash
     *
     * @return list of active players
     */
    public ArrayList<Player> getActivePlayers() {
        ArrayList<Player> activePlayers = new ArrayList<>();

        for(Player player : players)
            if(player.isInGame())
                activePlayers.add(player);

        return activePlayers;
    }

    /** Returns the number of player currently playing
     *
     * @return number of current players
     */
    public int getCurrentPlayersNumber(){ return players.size(); }

    /** Modifies both absolute position and lap counter of the Player.
     *
     * @param player Player that has to move
     * @param stepsNumber Number of steps that the player must do.
     *                    Positive number to advance, negative to step back
     */
    public void movePlayerInFlightBoard(Player player, int stepsNumber) {
        int tempNextPosition =  calculateFinalAbsolutePosition(player, stepsNumber);
        updatePlayerLapCounter(player, tempNextPosition);
        player.setAbsPosition(tempNextPosition);
    }

    /** Calculates the final absolute position of a Player, due to a movement
     *
     * @param player player that needs to be moved
     * @param stepsNumber of steps that the player must do.
     *      *                    Positive number to advance, negative to step back
     * @return final absolute position
     */
    private int calculateFinalAbsolutePosition(Player player, int stepsNumber){
        int tempNextPosition =  player.getAbsPosition() + stepsNumber;

        //For all players in between the starting position and nextPosition
        //      => increases/decreases nextPosition (because the moving player must jump that position)
        for(Player p : players) {
            int pPosition = p.getAbsPosition();

            // Note: the iteration of player (parameter) can't enter the if,
            // because the position is the same [player.getAbsPosition() <> pPosition condition always false]
            if( stepsNumber > 0 && ( player.getAbsPosition() < pPosition && pPosition > tempNextPosition ) )
                tempNextPosition++;
            else if (stepsNumber < 0 && ( player.getAbsPosition() > pPosition && pPosition > tempNextPosition ) )
                tempNextPosition--;
        }

        return tempNextPosition;
    }

    /** Updates the lap counter of the player, analyzing his starting and final position.
     *  This function must be invoked before the player's absolute position is set to the new value.
     *
     * @param player Player to be moved
     * @param finalAbsolutePosition final position of the movement
     */
    private void updatePlayerLapCounter(Player player, int finalAbsolutePosition){
        int playerCurrentPosition = player.getAbsPosition();

        // number of steps related to the laps taken by the player
        int playerCurrentLapsSteps = player.getLapCounter() * lapLength;

        if(
                finalAbsolutePosition > playerCurrentPosition &&
                        playerCurrentPosition < playerCurrentLapsSteps + lapLength &&
                        finalAbsolutePosition >= playerCurrentLapsSteps + lapLength
        ) {
            player.increaseLapCounter();
        }
        else if ( playerCurrentPosition >= playerCurrentLapsSteps &&
                        finalAbsolutePosition < playerCurrentLapsSteps
        ) {
            player.decreaseLapCounter();
        }


    }

    public void setPlayerPosition(ArrayList<Player> players){
        this.players = players;
    }
}
