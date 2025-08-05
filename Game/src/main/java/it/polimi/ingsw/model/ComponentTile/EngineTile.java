package it.polimi.ingsw.model.ComponentTile;

import it.polimi.ingsw.model.Direction;

import java.io.Serializable;

public final class EngineTile extends ComponentTile {
    private final Boolean doubleEngine;

    /*
     * Constructs a BatteriesTile with the specified parameters.
     *
     * @param northConnector
     * @param eastConnector
     * @param southConnector
     * @param westConnector
     * @param capacity
     */
    public EngineTile(String fileName, Connector northConnector, Connector eastConnector, Connector southConnector, Connector westConnector, Boolean doubleEngine) {
        super(fileName, northConnector, eastConnector, southConnector, westConnector);
        this.doubleEngine = doubleEngine;
    }
    /* Copy constructor
     * @param EngineTile which is desired to make a copy of
     * */
    public EngineTile(EngineTile engineTile){
        this(
                engineTile.getFileName(),
                engineTile.getConnectorList()[Direction.NORTH.ordinal()],
                engineTile.getConnectorList()[Direction.EAST.ordinal()],
                engineTile.getConnectorList()[Direction.SOUTH.ordinal()],
                engineTile.getConnectorList()[Direction.WEST.ordinal()],
                engineTile.isDoubleEngine()
        );
    }


    /* returns a Boolean that specifies whether this doubleEngine is double
     *
     * @return a Boolean that specifies whether this doubleEngine is double
     * */
    public boolean isDoubleEngine(){
        return doubleEngine;
    }

    public Boolean equals(EngineTile eT){
        return this.equalsConnections(eT.getConnectorList()) && this.isDoubleEngine()==eT.isDoubleEngine();
    }
    public Boolean equals(ComponentTile t){
        return switch (t){
            case EngineTile p-> this.equals(p);
            default -> false;
        };
    }
}

