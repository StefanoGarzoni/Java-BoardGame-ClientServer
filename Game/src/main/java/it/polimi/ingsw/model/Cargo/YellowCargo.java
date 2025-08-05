package it.polimi.ingsw.model.Cargo;

import it.polimi.ingsw.model.Cargo.CargoType;

public final class YellowCargo implements CargoType {
    public int getCredits(){
        return 3;
    }

    public String getColor(){
        return "yellow";
    }

    @Override
    public boolean isSpecial() { return false; }
}
