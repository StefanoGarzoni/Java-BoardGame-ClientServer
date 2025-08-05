package it.polimi.ingsw.model.ComponentTile;

import it.polimi.ingsw.model.Direction;

public final class AlienSupportSystemTile extends ComponentTile {
    private final Boolean brownAlien;
    /*
     * Constructs a AlienSupportSystemTile with the specified parameters.
     *
     * @param northConnector
     * @param eastConnector
     * @param southConnector
     * @param westConnector
     * @param there is an alien or not
     */
    public AlienSupportSystemTile(String fileName, Connector northConnector, Connector eastConnector, Connector southConnector, Connector westConnector, Boolean brownAlien ) {
        super(fileName, northConnector, eastConnector, southConnector, westConnector);
        this.brownAlien = brownAlien;
    }


    /* Copy constructor
     * @param AlienSupportSystemTile which is desired to make a copy of
     * */
    public AlienSupportSystemTile(AlienSupportSystemTile alienSupportSystemTile){
        this(
                alienSupportSystemTile.getFileName(),
                alienSupportSystemTile.getConnectorList()[Direction.NORTH.ordinal()],
                alienSupportSystemTile.getConnectorList()[Direction.EAST.ordinal()],
                alienSupportSystemTile.getConnectorList()[Direction.SOUTH.ordinal()],
                alienSupportSystemTile.getConnectorList()[Direction.WEST.ordinal()],
                alienSupportSystemTile.isBrownAlien()
        );
    }

    /*returns true if the engine is a double engine otherwise false
     *
     * @return true if the engine is a double engine otherwise false
     * */
    public Boolean isBrownAlien(){
        return brownAlien;
    }

    /* public void insertYourselfInCache(Map<String, ArrayList<Coordinates>> cacheTileMaps){
        cacheTileMaps.get("aliens").add();
    }*/
    public Boolean equals(AlienSupportSystemTile aT){
        return this.equalsConnections(aT.getConnectorList());
    }

    public Boolean equals(ComponentTile t){
        return switch (t){
            case AlienSupportSystemTile p-> this.equals(p);
            default -> false;
        };
    }

}
