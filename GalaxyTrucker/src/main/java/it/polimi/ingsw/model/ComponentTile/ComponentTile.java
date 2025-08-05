package it.polimi.ingsw.model.ComponentTile;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.Cargo.CargoType;

import java.io.Serializable;
import java.util.ArrayList;

public sealed abstract class ComponentTile
        implements Serializable
        permits AlienSupportSystemTile, BatteriesTile, CargoTile, CannonTile, CabinTile, EngineTile, StructuralTile, Shield
{
    private Connector[] connectorList;
    private String fileName;

    public ComponentTile(String fileName, Connector northConnector, Connector eastConnector, Connector southConnector, Connector westConnector){
        this.fileName = fileName;
        connectorList = new Connector[4];
        this.connectorList[Direction.NORTH.ordinal()] = northConnector;
        this.connectorList[Direction.EAST.ordinal()] = eastConnector;
        this.connectorList[Direction.SOUTH.ordinal()] = southConnector;
        this.connectorList[Direction.WEST.ordinal()] = westConnector;
    }

    /*Copy constructor
    * */
    public ComponentTile(ComponentTile componentTile){
        this.fileName = componentTile.getFileName();
        this.connectorList = new Connector[4];
        this.connectorList[Direction.NORTH.ordinal()] = componentTile.getConnectorList()[Direction.NORTH.ordinal()];
        this.connectorList[Direction.EAST.ordinal()] = componentTile.getConnectorList()[Direction.EAST.ordinal()];
        this.connectorList[Direction.SOUTH.ordinal()] = componentTile.getConnectorList()[Direction.SOUTH.ordinal()];
        this.connectorList[Direction.WEST.ordinal()] = componentTile.getConnectorList()[Direction.WEST.ordinal()];
    }

    public Connector getConnector(Direction d){
         return connectorList[d.ordinal()];
     }

     public Connector[] getConnectorList(){
        Connector[] connectorListCopy = new Connector[4];
        for(int i = 0; i < connectorList.length; i++){
            if(connectorList[i] == null){
                connectorListCopy[i] = new Connector(0);
            }
            else{
                connectorListCopy[i] = connectorList[i];
            }
        }
         return connectorListCopy;
    }

    public boolean isDouble(){
         return switch (this){
             case CannonTile t ->  t.isDoubleCannon();
             case EngineTile t ->  t.isDoubleEngine();
             default -> throw new IllegalStateException("Invalid argument");
         };
    }
     /*public abstract void insertYourSelfInCache(Map<String, ArraysList<Coordinates>> cacheTileMaps)); ???*/

    public boolean isBrown(){
        return switch (this){
            case AlienSupportSystemTile t -> t.isBrownAlien();
            default -> throw new IllegalStateException("Invalid argument");
        };
    }

    public void unload(CargoType c){
        switch (this){
            case CargoTile t -> t.unloadCargo(c);
            default -> throw new IllegalStateException("Invalid argument");
        }
    }

    public void unload(int q){
        switch (this){
            case BatteriesTile t -> t.unloadBattery(q);
            case CabinTile t -> t.loadAstronauts(-q);
            default -> throw new IllegalStateException("Invalid argument");
        }
    }

    public void load(CargoType c){
        switch (this){
            case CargoTile t -> t.loadCargo(c);
            default -> throw new IllegalStateException("Invalid argument");
        }
    }

    public int getValue(){
        return switch (this){
            case CargoTile t -> t.getCargosValue();
            case BatteriesTile t -> t.getBatteries();
            case CabinTile t -> t.getAstronautsNumber();
            default -> throw new IllegalStateException("Invalid argument");
        };
    }

    public int getCapacity(){
        return switch (this){
            case CargoTile t -> t.getCargoCapacity();
            case BatteriesTile t -> t.getBatteryCapacity();
            default -> throw new IllegalStateException("Invalid argument");
        };
    }

    public ArrayList<CargoType> getStored(){
        return switch (this){
            case CargoTile t -> t.getStoredCargos();
            default -> throw new IllegalStateException("Invalid argument");
        };
    }

    public String getFileName(){
        return fileName;
    }

    public Boolean equalsConnections(Connector[] connectorList){
        Boolean ok=false;
        for(int i=0; i<4; i++){
            if(this.connectorList[i]==null) ok = (connectorList[i]==null);
            else ok = this.connectorList[i].equals((connectorList[i]));
            if(!ok){
                break;
            }
        }
        return ok;
    }

    public String toString(){
        return getConnector(Direction.NORTH).toString() +" "+ getConnector(Direction.EAST) +" "+ getConnector(Direction.SOUTH) +" "+ getConnector(Direction.WEST);
    }

}
