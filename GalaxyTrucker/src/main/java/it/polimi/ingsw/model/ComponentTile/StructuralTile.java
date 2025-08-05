package it.polimi.ingsw.model.ComponentTile;

import it.polimi.ingsw.model.Direction;

public final class StructuralTile extends ComponentTile{

    /*
     * Constructs a Structural with the specified parameters.
     *
     * @param northConnector
     * @param eastConnector
     * @param southConnector
     * @param westConnector
     */
    public StructuralTile(String fileName, Connector northConnector, Connector eastConnector, Connector southConnector, Connector westConnector) {
        super(fileName, northConnector, eastConnector, southConnector, westConnector);
    }

    /* Copy constructor
     * @param ShieldTile which is desired to make a copy of
     * */
    public  StructuralTile(StructuralTile structuralTile){
        this(
                structuralTile.getFileName(),
                structuralTile.getConnectorList()[Direction.NORTH.ordinal()],
                structuralTile.getConnectorList()[Direction.EAST.ordinal()],
                structuralTile.getConnectorList()[Direction.SOUTH.ordinal()],
                structuralTile.getConnectorList()[Direction.WEST.ordinal()]
        );
    }
    public Boolean equals(StructuralTile sT){
        return this.equalsConnections(sT.getConnectorList());
    }

    public Boolean equals(ComponentTile t){
        return switch (t){
            case StructuralTile p-> this.equals(p);
            default -> false;
        };
    }
}
