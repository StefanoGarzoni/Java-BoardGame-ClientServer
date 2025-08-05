package it.polimi.ingsw.model;

import java.io.Serializable;
import java.util.Objects;

public class Coordinates implements Serializable {
    private int row;
    private int col;

    //costruttore
    public Coordinates(int row, int col) {
        this.row = row;
        this.col = col;
    }

    //getters
    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    //setters
    public void setRow(int row) {
        this.row = row;
    }

    public void setCol(int col) {
        this.col = col;
    }

    //data una coordinata controlla se puntano alla stessa cella
    @Override
    public boolean equals(Object object){
        Coordinates coordinates = (Coordinates) object;
        return this.row == coordinates.getRow() && this.col == coordinates.getCol();
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    @Override
    public String toString() {
        return "("+row+";"+col+")";
    }

    /**
     * Creates a Coordinates object from a String in the form of row,col
     * @param arg String in the form of row,col
     * @return Coordinates object
     */
    public static Coordinates fromString(String arg){
        if(arg.matches("\\d+,\\d+")) {
            return new Coordinates(Integer.parseInt(arg.split(",")[0]), Integer.parseInt(arg.split(",")[1]));
        }
        else if(arg.matches("\\(\\d+;\\d+\\)")) {
            String inner = arg.substring(1, arg.length()-1);
            String[] temp = inner.split(";");
            return new Coordinates(Integer.parseInt(temp[0]), Integer.parseInt(temp[1]));
        }
        else{
            throw new IllegalArgumentException("Invalid argument " + arg + ", should be in the form of row,col or (row,col)");
        }
    }


}
