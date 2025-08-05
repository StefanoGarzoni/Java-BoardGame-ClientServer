package it.polimi.ingsw.model.cards.util;

import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.Direction;
import it.polimi.ingsw.model.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MeteorShot extends Shot {


    public MeteorShot(ShotSize size, Direction direction) {
        super(size, direction);
    }

    @Override
    public Pair<Coordinates, ArrayList<ArrayList<Coordinates>>> hitTile(Player player, int diceRoll) {
        Map<Coordinates,ArrayList<ArrayList<Coordinates>>> map = player.getShipBoard()
                .hitTile(direction, diceRoll, this.size == ShotSize.BIG, this.size == ShotSize.SMALL)
                .values().iterator().next();
        if(map.isEmpty()) {
            return new Pair<>(null, null);
        }
        ArrayList<ArrayList<Coordinates>> coordinates = map.values().iterator().next();
        Coordinates brokenTile = map.keySet().iterator().next();
        return new Pair<>(brokenTile,coordinates);
    }
}
