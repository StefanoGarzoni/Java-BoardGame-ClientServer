package it.polimi.ingsw.model.ComponentTile;

import it.polimi.ingsw.model.Direction;

public final class BatteriesTile extends ComponentTile {
    private final int capacity;
    private int storedQuantity;

    /*
     * Constructs a BatteriesTile with the specified parameters.
     *
     * @param northConnector
     * @param eastConnector
     * @param southConnector
     * @param westConnector
     * @param capacity
     */
    public BatteriesTile(String fileName, Connector northConnector, Connector eastConnector, Connector southConnector, Connector westConnector, int capacity) {
        super(fileName, northConnector, eastConnector, southConnector, westConnector);
        this.capacity=capacity;
        this.storedQuantity=capacity;
    }


    /* Copy constructor
    * @param BatteriesTile which is desired to make a copy of
    * */
    public BatteriesTile(BatteriesTile batteriesTile){
        this(
                batteriesTile.getFileName(),
                batteriesTile.getConnectorList()[Direction.NORTH.ordinal()],
                batteriesTile.getConnectorList()[Direction.EAST.ordinal()],
                batteriesTile.getConnectorList()[Direction.SOUTH.ordinal()],
                batteriesTile.getConnectorList()[Direction.WEST.ordinal()],
                batteriesTile.getBatteryCapacity()
        );
        storedQuantity = batteriesTile.getBatteries();
    }
    /* returns current quantity of battery in this batteries tile
     *
     * @return current quantity of battery in this batteries tile
     */
    public int getBatteries(){
        return storedQuantity;
    }

    /* returns battery capacity this batteries tile
     *
     * @return battery capacity this batteries tile
     */
    public int getBatteryCapacity(){
        return capacity;
    }

    /*increases stored battery by q

    * @param the quantity of battery that is desired to load
     */
    public void loadBattery(int q){
        storedQuantity += q;
    }
    /*decreases stored battery by q

    * @param the quantity of battery that is desired to unload
     */
    public void unloadBattery(int q){
        storedQuantity -=q;
    }

    public Boolean equals(BatteriesTile bT){
        return this.equalsConnections(bT.getConnectorList()) && this.getCapacity()==bT.getCapacity();
    }

    public Boolean equals(ComponentTile t){
        return switch (t){
            case BatteriesTile p-> this.equals(p);
            default -> false;
        };
    }

}
