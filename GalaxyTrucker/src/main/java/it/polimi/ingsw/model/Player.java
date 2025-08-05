package it.polimi.ingsw.model;

import it.polimi.ingsw.model.ComponentTile.ComponentTile;

import java.io.Serializable;

public class Player implements Serializable {
    private final String nickname;
    private final char color;

    private int credits;
    private int absPosition;
    private int lapCounter;

    private final ShipBoard shipboard;
    private boolean isInGame;

    private ComponentTile tileInHand;

    public Player(
            String nickname,
            char color,
            ShipBoard shipboard
            ){
        this.nickname = nickname;
        this.color = color;
        this.credits = 0;
        this.absPosition = 0;
        this.lapCounter = 0;
        this.shipboard = shipboard;
        this.tileInHand = null;
        this.isInGame = true;
    }

    public int getAbsPosition(){ return absPosition; }
    public int getLapCounter(){ return lapCounter; }
    public int getCredits(){ return credits; }
    public char getColor() { return color; }
    public String getNickname() { return nickname; /* strings are immutable */ }
    public ShipBoard getShipBoard() { return this.shipboard; }
    public ComponentTile getTileInHand(){ return tileInHand; }
    public void increaseLapCounter(){ lapCounter++; }
    public void decreaseLapCounter(){ lapCounter--; }

    public void setTileInHand(ComponentTile tile){ this.tileInHand = tile; }
    public void setAbsPosition(int positionsNumber){ absPosition = positionsNumber; }
    public void increaseCredits(int creditsNumber) { credits += creditsNumber; }
    public void putOutOfPlay() { isInGame = false; }
    public boolean isInGame(){ return isInGame; }
}
