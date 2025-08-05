package it.polimi.ingsw.model.ComponentTile;

import it.polimi.ingsw.model.Direction;

import java.io.Serializable;

public final class Shield extends ComponentTile{

    /*
     * Constructs a Shield with the specified parameters.
     *
     * @param northConnector
     * @param eastConnector
     * @param southConnector
     * @param westConnector
     */
    public Shield(String fileName, Connector northConnector, Connector eastConnector, Connector southConnector, Connector westConnector) {
        super(fileName, northConnector, eastConnector, southConnector, westConnector);
    }
    /* Copy constructor
     * @param ShieldTile which is desired to make a copy of
     * */
    public Shield(Shield shield){
        this(
                shield.getFileName(),
                shield.getConnectorList()[Direction.NORTH.ordinal()],
                shield.getConnectorList()[Direction.EAST.ordinal()],
                shield.getConnectorList()[Direction.SOUTH.ordinal()],
                shield.getConnectorList()[Direction.WEST.ordinal()]
        );
    }

    public boolean equals(Shield s){
        return this.equalsConnections(s.getConnectorList());
    }

    public Boolean equals(ComponentTile t){
        return switch (t){
            case Shield p-> this.equals(p);
            default -> false;
        };
    }
}
