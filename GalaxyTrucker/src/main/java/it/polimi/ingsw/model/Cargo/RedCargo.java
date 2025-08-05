package it.polimi.ingsw.model.Cargo;

import it.polimi.ingsw.model.Cargo.CargoType;

public final class RedCargo implements CargoType {
    public int getCredits(){
        return 4;
    }

    public String getColor(){
        return "red";
    }

    @Override
    public boolean isSpecial() { return true; }
}
