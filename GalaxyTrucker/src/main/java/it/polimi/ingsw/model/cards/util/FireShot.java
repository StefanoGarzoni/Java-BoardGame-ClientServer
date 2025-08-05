package it.polimi.ingsw.model.cards.util;


import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.Direction;
import it.polimi.ingsw.model.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static it.polimi.ingsw.model.cards.util.ShotSize.SMALL;

public class FireShot extends Shot {


    public FireShot(ShotSize size, Direction direction) {
        super(size, direction);
    }


    @Override
    public Pair<Coordinates, ArrayList<ArrayList<Coordinates>>> hitTile(Player player, int diceRoll) {
        Map<Coordinates, ArrayList<ArrayList<Coordinates>>> result = player.getShipBoard().hitTile(this.direction, diceRoll, this.size == ShotSize.BIG, false)
                .values().iterator().next();
        if(result.isEmpty()){
            return new Pair<>(null, null);
        }
        Coordinates hit = result.keySet().iterator().next();
        ArrayList<ArrayList<Coordinates>> trunks = result.values().iterator().next();
        return new Pair(hit, trunks);
    }
}
