package it.polimi.ingsw.model.Cargo;

public final class BlueCargo implements CargoType {
    public int getCredits(){
        return 2;
    }

    public String getColor(){
        return "blue";
    }

    @Override
    public boolean isSpecial() { return false; }
}
