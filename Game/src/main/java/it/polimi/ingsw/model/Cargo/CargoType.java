package it.polimi.ingsw.model.Cargo;

import java.io.Serializable;

public sealed interface CargoType
    extends Serializable
        permits GreenCargo, BlueCargo, YellowCargo, RedCargo
{
    int getCredits();
    String getColor();
    boolean isSpecial();

    default boolean equals(CargoType cargoType){
        return this.getCredits() == cargoType.getCredits();
    }
}

