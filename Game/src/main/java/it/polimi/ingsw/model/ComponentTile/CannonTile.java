package it.polimi.ingsw.model.ComponentTile;

import it.polimi.ingsw.model.Direction;

public final class CannonTile extends ComponentTile {
    private final Boolean doubleCannon;

    /*
     * Constructs a CannonTile with the specified parameters.
     *
     * @param northConnector
     * @param eastConnector
     * @param southConnector
     * @param westConnector
     * @param doubleCannon          boolean that specifies if the cannon is a doubleCannon
     */

    public CannonTile(String fileName, Connector northConnector, Connector eastConnector, Connector southConnector, Connector westConnector, boolean doubleCannon) {
        super(fileName, northConnector, eastConnector, southConnector, westConnector);
        this.doubleCannon = doubleCannon;
    }

    /* Copy constructor
     * @param CannonTile which is desired to make a copy of
     * */
    public CannonTile(CannonTile cannonTile){
        this(
                cannonTile.getFileName(),
                cannonTile.getConnectorList()[Direction.NORTH.ordinal()],
                cannonTile.getConnectorList()[Direction.EAST.ordinal()],
                cannonTile.getConnectorList()[Direction.SOUTH.ordinal()],
                cannonTile.getConnectorList()[Direction.WEST.ordinal()],
                cannonTile.isDoubleCannon()
        );
    }

    /* returns a Boolean that specifies whether the cannon is double
    *
    * @return a Boolean that specifies whether the cannon is double
    * */
    Boolean isDoubleCannon(){
        return doubleCannon;
    }
    public Boolean equals(CannonTile cT){
        return this.equalsConnections(cT.getConnectorList()) && this.isDouble()==cT.isDouble();
    }
    public Boolean equals(ComponentTile t){
        return switch (t){
            case CannonTile p-> this.equals(p);
            default -> false;
        };
    }

}
