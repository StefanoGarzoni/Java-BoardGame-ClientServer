package it.polimi.ingsw.model.Cargo;

import it.polimi.ingsw.model.Cargo.CargoType;

public final class GreenCargo implements CargoType {
    public int getCredits(){
        return 1;
    }

    public String getColor(){
        return "green";
    }

    @Override
    public boolean isSpecial() { return false; }
}
