package it.polimi.ingsw.model.cards.util;

import it.polimi.ingsw.model.Cargo.CargoType;
import it.polimi.ingsw.model.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * Planet class
 * @author Francesco Montefusco
 */
public class Planet {
    private ArrayList<CargoType> cargoList;
    private boolean isFree;
    private Player landedPlayer;

    public Planet(ArrayList<CargoType> cargoList){
        this.cargoList = cargoList;
        isFree = true;
        landedPlayer = null;
    }

    /**
     *
     * @return Returns the list of cargos available on the planet
     */
    public ArrayList<CargoType> getCargoList(){
        return cargoList;
    }

    /**
     *
     * @return Returns if the planet was occupied
     */
    public boolean isFree(){
        return isFree;
    }

    /**
     * Exception raised if the player tries to occupy a planet that a player has chosen to land on
     * @deprecated
     * @param player the player to occupy the planet
     * @throws PlanetOccupiedException If a player tries to land on an already occupied planet
     */
    public void land(Player player) throws PlanetOccupiedException{
        if(!isFree)  {
            throw new PlanetOccupiedException();
        }

        landedPlayer = player;
        isFree = false;
    }

    @Nullable
    public Player getPlayer(){
        return landedPlayer;
    }



}
