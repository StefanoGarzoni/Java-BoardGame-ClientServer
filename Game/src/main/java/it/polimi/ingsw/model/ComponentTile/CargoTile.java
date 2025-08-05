package it.polimi.ingsw.model.ComponentTile;

import it.polimi.ingsw.model.Cargo.CargoType;
import it.polimi.ingsw.model.Direction;

import java.util.ArrayList;

public final class CargoTile extends ComponentTile {
    private ArrayList<CargoType> storedCargo;
    private final Boolean isSpecial;
    private final int capacity;

    /*
     * Constructs a CargoTile with the specified parameters.
     *
     * @param northConnector
     * @param eastConnector
     * @param southConnector
     * @param westConnector
     * @param isSpecial             boolean that specifies if the cannon is a doubleCannon
     * @param capacity              int that specifies the capacity of the CargoTile
     */
    public CargoTile(String fileName, Connector northConnector, Connector eastConnector, Connector southConnector, Connector westConnector, Boolean isSpecial, int capacity) {
        super(fileName, northConnector, eastConnector, southConnector, westConnector);
        this.isSpecial = isSpecial;
        this.capacity = capacity;
        storedCargo = new ArrayList<>();
    }

    /* Copy constructor
     * @param CargoTile which is desired to make a copy of
     * */

    public CargoTile(CargoTile cargoTile){
        this(cargoTile.getFileName(),
                cargoTile.getConnectorList()[Direction.NORTH.ordinal()],
                cargoTile.getConnectorList()[Direction.EAST.ordinal()],
                cargoTile.getConnectorList()[Direction.SOUTH.ordinal()],
                cargoTile.getConnectorList()[Direction.WEST.ordinal()],
                cargoTile.isSpecial(),
                cargoTile.getCapacity()
        );

        storedCargo = cargoTile.getStoredCargos();
    }

    /* returns a Boolean that specifies whether this CargoTile is special
     *
     * @return a Boolean that specifies whether this CargoTile is special
     * */
    public Boolean isSpecial(){
        return isSpecial;
    }

    /*returns the capacity of this CargoTile
    *
    * @return The capacity of this CargoTile
    * */
    public int getCargoCapacity(){
        return capacity;
    }

    /*loads the given Cargo type into this CargoTile
    *
    * @param c          The cargo that is desired to be loaded
    * */
    public void loadCargo(CargoType c){
        storedCargo.add(c);
    }

    /*unloads the given Cargo type from this CargoTile
    *
    * @param c          The cargo that is desired to be unloaded
     */
    public boolean unloadCargo(CargoType c){
        CargoType cargoToRemove= null;
        for(var cargo: storedCargo)
            if(c.equals(cargo)) {
                cargoToRemove = cargo;
                break;
            }
        if(cargoToRemove!=null) storedCargo.remove(cargoToRemove);
        else return false;
        return true;
    }

    /*returns the total value of the cargos stored in this CargoTile
    *
    * @return The total value of the cargos stored in this CargoTile
    * */

    public int getCargosValue() {
        int sum = 0;
        for(CargoType c: storedCargo){
            sum += c.getCredits();
        }
        return sum;
    }

    /*returns the list of the stored cargo
    *
    * @return The list of the stored cargo
    * */
    public ArrayList<CargoType> getStoredCargos(){
        return storedCargo;
    }

    public Boolean equals(CargoTile cT){
        return this.equalsConnections(cT.getConnectorList()) && this.isSpecial()==cT.isSpecial() && this.getCapacity()== cT.getCapacity();
    }
    public Boolean equals(ComponentTile t){
        return switch (t){
            case CargoTile p-> this.equals(p);
            default -> false;
        };
    }

}
