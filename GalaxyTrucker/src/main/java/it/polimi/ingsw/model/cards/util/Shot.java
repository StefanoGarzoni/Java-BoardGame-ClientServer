package it.polimi.ingsw.model.cards.util;

import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.Direction;
import it.polimi.ingsw.model.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * Represents a shot thrown at a player
 * @author Francesco Montefusco
 */
public abstract class Shot {
    protected ShotSize size;
    protected Direction direction;


    public Shot(ShotSize size, Direction direction) {
        this.size = size;
        this.direction = direction;
    }
    public ShotSize getSize(){
        return size;
    }
    public Direction getDirection(){
        return direction;
    }

    /**
     * hits the player with a shot
     * @param player the player to hit
     * @param diceRoll the roll
     * @return an arrays of coordinates list (trunks), or {@code null} if it doesn't cause hit
     */
    @Nullable
    public abstract Pair<Coordinates,ArrayList<ArrayList<Coordinates>>> hitTile (Player player, int diceRoll);

}
