package it.polimi.ingsw.model.clientModel;

import it.polimi.ingsw.model.ComponentTile.CabinTile;
import it.polimi.ingsw.model.ComponentTile.ComponentTile;
import it.polimi.ingsw.model.ComponentTile.FixedComponentTile;
import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.Direction;
import it.polimi.ingsw.model.ShipBoard;

import java.util.ArrayList;
import java.util.Optional;

public class ViewShipBoard {
    private final String nickname;
    private final char color;
    private final FixedComponentTile[][] shipBoardMatrix ;
    int points;

    private int deckIndexPlayerIsWaitingFor;
    private ComponentTile tileInHand;
    private final ArrayList<ComponentTile> bookedTiles = new ArrayList<>();
    private boolean hasEndedBuilding;

    int discardedTileNum;
    private Coordinates purpleAlien;
    private Coordinates brownAlien;

    private int absPosition;
    private int lapCounter;

    public ViewShipBoard(String nickname, char color) {
        this.nickname = nickname;
        this.color = color;
        this.shipBoardMatrix = new FixedComponentTile[5][7];
        this.discardedTileNum = 0;
        this.purpleAlien = null;
        this.brownAlien = null;
        hasEndedBuilding = false;
        deckIndexPlayerIsWaitingFor = -1;
        this.points =0;
    }

    public FixedComponentTile[][] getShipBoardMatrix(){
        return this.shipBoardMatrix;
    }

    public int getPoints(){
        return this.points;
    }

    public ComponentTile getTileInHand(){
        return this.tileInHand;
    }

    public ArrayList<ComponentTile> getBookedTiles(){
        return this.bookedTiles;
    }

    /** Gets and removes a ComponentTile from booked tiles, based of its fileName
     *
     * @param fileName of the file to be returned
     * @return covered ComponentTile with requested filename, null if not exists
     */
    public ComponentTile takeBookedTile(String fileName){
        Optional<ComponentTile> selectedTile = bookedTiles.stream().filter((tile) -> tile.getFileName().equals(fileName)).findFirst();

        if(selectedTile.isPresent())
            bookedTiles.removeIf((tile) -> selectedTile.get().getFileName().equals(tile.getFileName()));

        return selectedTile.orElse(null);
    }

    /** Returns the index of the deck the player has asked the server to view
     *
     * @return index of the waited deck, -1 if the player isn't waiting for a deck
     */
    public int getDeckIndexPlayerIsWaitingFor() { return deckIndexPlayerIsWaitingFor; }

    /** Sets the index of the deck the player has asked the server to view
     *
     * @param index index of the deck the player is waiting for
     */
    public void setDeckIndexPlayerIsWaitingFor(int index) { deckIndexPlayerIsWaitingFor = index; }

    public boolean getPlayerHasEndedBuilding(){ return hasEndedBuilding; }
    public void setPlayerHasEndedBuilding(){ hasEndedBuilding = true; }

    public String getNickname() { return nickname; }
    public void bookTile(ComponentTile componentTile){
        this.bookedTiles.add(componentTile);
    }
    public void setTileInHand(ComponentTile componentTile){
        this.tileInHand = componentTile;
    }

    public void fixComponentTile(ComponentTile tile, Coordinates coordinates, Direction direction) {
        FixedComponentTile fixedTile = new FixedComponentTile(tile, direction);
        shipBoardMatrix[coordinates.getRow()][coordinates.getCol()] = fixedTile;
    }

    public void removeComponentTile(Coordinates coordinates){
        shipBoardMatrix[coordinates.getRow()][coordinates.getCol()] = null;
    }

    public void setBrownAlien(Coordinates coordinates){
        this.brownAlien = coordinates;
    }

    public void setPurpleAlien(Coordinates coordinates){
        this.purpleAlien = coordinates;
    }

    /** Removes a tile from the ViewShipboard
     *
     * @param coordinates of the tile to be removed
     */
    public void remove(Coordinates coordinates){
        this.shipBoardMatrix[coordinates.getRow()][coordinates.getCol()] = null;
    }

    /** Updated points of the player related to this shipboard
     *
     * @param points number of player's shipboard
     */
    public void setPoints(int points){
        this.points = points;
    }

    /** Updates the content of ViewShipboard, copying tiles from a server's Shipboard
     *
     * @param shipBoard to be copied into this ViewShipboard
     */
    public void updateViewShipBoard(ShipBoard shipBoard){
        // copying fixed tiles
        int numRows = 5;
        int numCols = 7;

        for(int i = 0; i < numRows; i++ ){
            for(int j = 0; j < numCols; j++){
                Coordinates coordinates = new Coordinates(i, j);
                shipBoardMatrix[i][j] = shipBoard.getShipBoardComponent(coordinates);
            }
        }

        // copying other information
        discardedTileNum = shipBoard.getLostTilesNumber();

        // copying aliens
        purpleAlien = shipBoard.getPurpleAlienCoord();
        brownAlien = shipBoard.getBrownAlienCoord();
    }

    /** Sets the absolute position in the game of the player related to this shipboard
     *
     * @param newAbsPosition player's absolute position
     */
    public void setAbsPosition(int newAbsPosition) { this.absPosition = newAbsPosition; }
    public int getAbsPosition() { return this.absPosition; }

    public char getColor(){
        return color;
    }

}
