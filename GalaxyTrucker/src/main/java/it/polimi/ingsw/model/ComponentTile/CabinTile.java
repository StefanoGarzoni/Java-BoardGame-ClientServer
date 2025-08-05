package it.polimi.ingsw.model.ComponentTile;

import it.polimi.ingsw.model.Direction;

public final class CabinTile extends ComponentTile {
    private int astronautsNumber;

    /*
     * Constructs a CabinTile with the specified parameters.
     *
     * @param northConnector
     * @param eastConnector
     * @param southConnector
     * @param westConnector
     */
    public CabinTile(String fileName, Connector northConnector, Connector eastConnector, Connector southConnector, Connector westConnector) {
        super(fileName, northConnector, eastConnector, southConnector, westConnector);
        astronautsNumber = 3;
    }

    /* Copy constructor

     * @param cabinTile which is desired to make a copy of
     * */
    public  CabinTile(CabinTile cabinTile){
        this(
                cabinTile.getFileName(),
                cabinTile.getConnectorList()[Direction.NORTH.ordinal()],
                cabinTile.getConnectorList()[Direction.EAST.ordinal()],
                cabinTile.getConnectorList()[Direction.SOUTH.ordinal()],
                cabinTile.getConnectorList()[Direction.WEST.ordinal()]
        );
        astronautsNumber = cabinTile.getAstronautsNumber();
    }
    /* returns the number of the astronauts in this CabinTile
    *
    *  @return The number of the astronauts in this CabinTile
    * */
    public int getAstronautsNumber() {
        return astronautsNumber;
    }
    /* increases the number of astronauts in this CabinTile
    *
    * @param num        is the number of astronauts that is desired to increase by
    * */
    public void loadAstronauts(int num){
        astronautsNumber += num;
    }

    /* decreases the number of astronauts in this CabinTile
    *
    * @param num        is the number of astronauts that is desired to decrease by
     */

    public Boolean equals(CabinTile cT){
        return this.equalsConnections(cT.getConnectorList());
    }

    public Boolean equals(ComponentTile t){
        return switch (t){
            case CabinTile p-> this.equals(p);
            default -> false;
        };
    }
    public void empty(){
        this.astronautsNumber=0;
    }
}
