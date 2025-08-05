package it.polimi.ingsw.model.ComponentTile;

import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.Direction;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

public class FixedComponentTile implements Serializable {
    private Direction direction;
    private ComponentTile componentTile;

    public Direction getDirection() {
        return direction;
    }

    public ComponentTile getComponentTile(){
        return componentTile;
    }

    public FixedComponentTile(ComponentTile componentTile, Direction direction){
        ComponentTile tile;
        switch (componentTile){
            case CargoTile t -> tile = new CargoTile(t);
            case AlienSupportSystemTile t -> tile = new AlienSupportSystemTile(t);
            case BatteriesTile t -> tile = new BatteriesTile(t);
            case CabinTile t -> tile = new CabinTile(t);
            case CannonTile t -> tile = new CannonTile(t);
            case EngineTile t -> tile = new EngineTile(t);
            case StructuralTile t -> tile = new StructuralTile(t);
            case Shield t -> tile = new Shield(t);
            default -> throw new IllegalStateException("Invalid argument");
        }
        this.componentTile= tile;
        this.direction = direction;

    }
    public FixedComponentTile(FixedComponentTile fT){
        ComponentTile t = fT.getComponentTile();
        switch (t){
            case CargoTile p -> this.componentTile = new CargoTile(p);
            case AlienSupportSystemTile p -> this.componentTile = new AlienSupportSystemTile(p);
            case BatteriesTile p -> this.componentTile = new BatteriesTile(p);
            case CabinTile p -> this.componentTile = new CabinTile(p);
            case CannonTile p -> this.componentTile = new CannonTile(p);
            case EngineTile p -> this.componentTile = new EngineTile(p);
            case StructuralTile p -> this.componentTile = new StructuralTile(p);
            case Shield p -> this.componentTile = new Shield(p);
            default -> throw new IllegalStateException("Invalid argument");
        }
        this.direction = fT.direction;

    }

    public boolean isConnectedWith(FixedComponentTile t, Direction d){
        if(t == null){
            return false;
        }
        int tContact = (2+ d.ordinal())%4;
        /*System.out.println(getConnector(d) + " compareTo " + t.getConnector(Direction.values()[tContact]));
        for(int i=0; i<4;i++)
            System.out.println(t.getConnector(Direction.values()[(tContact+i)%4]));*/

        if(this.isCompatibleWith(t, d) && t.getConnector(Direction.values()[(tContact)%4]).getNumberOfConnections()!=0) return this.getConnector(d).matchWith(t.getConnector(Direction.values()[tContact])) ;
        return false;
    }

    public boolean isCompatibleWith(FixedComponentTile t, Direction d) {
        if(t == null)
            return true;

        int tContact = (2+d.ordinal())%4;

//        System.out.println(getConnector(d) + " compareTo " + t.getConnector(Direction.values()[tContact]));
//        for(int i=0; i<4;i++)
//            System.out.println(t.getConnector(Direction.values()[(tContact+i)%4]));

        return this.getConnector(d).matchWith(t.getConnector(Direction.values()[tContact]));
    }

    public Connector getConnector(Direction d){

        int dir = (d.ordinal() - this.getDirection().ordinal()+4)%4;
        return this.componentTile.getConnector(Direction.values()[dir]);
    }

    public Boolean[] checkNearby(FixedComponentTile[] fComponentTile){
        Boolean[] ok = new Boolean[4];

        for(int i=0; i<4; i++){
            if(this.isCompatibleWith(fComponentTile[i], Direction.values()[i]))
                ok[i]=true;
            else
                ok[i]=false;
        }
        return ok;
    }

    public void insertYourSelfInCache(Map<String, ArrayList<Coordinates>> cacheTilesMap, Coordinates coordinates){
        switch (componentTile){
            case CargoTile t -> {
                if (t.isSpecial()){
                    cacheTilesMap.get("specialCargo").add(coordinates);
                }else {
                    cacheTilesMap.get("cargo").add(coordinates);
                }
            }
            case CabinTile t -> cacheTilesMap.get("astronauts").add(coordinates);
            case EngineTile t -> { if(t.isDoubleEngine()){
                cacheTilesMap.get(("doubleEngines")).add(coordinates);
                }else{
                cacheTilesMap.get(("singleEngines")).add(coordinates);}
            }
            case CannonTile t ->{
                if(t.isDouble()){
                    cacheTilesMap.get("doubleCannons").add(coordinates);
                }else{
                    cacheTilesMap.get("singleCannons").add(coordinates);
                }
            }
            case AlienSupportSystemTile t -> cacheTilesMap.get("aliens").add(coordinates);
            case BatteriesTile t -> cacheTilesMap.get("batteries").add(coordinates);
            case Shield t -> cacheTilesMap.get("shields").add(coordinates);
            case StructuralTile t -> {} //TODO is this correct?
            default -> {}
        }
    }

    public String toString(){
        return this.componentTile.toString() + " direction: "+direction;
    }

    public Boolean equals(FixedComponentTile fT){
        Boolean equals = true;
        if(fT.getDirection()!=this.getDirection()) return false;
        ComponentTile t = fT.getComponentTile();
        return switch (t){
            case CargoTile p -> p.equals(this.componentTile);
            case CabinTile p -> p.equals(this.componentTile);
            case CannonTile p -> p.equals(this.componentTile);
            case StructuralTile p -> p.equals(this.componentTile);
            case Shield p -> p.equals(this.componentTile);
            case AlienSupportSystemTile p -> p.equals(this.componentTile);
            case EngineTile p -> p.equals(this.componentTile);
            case BatteriesTile p -> p.equals(this.componentTile);
            default -> false;
        };
    }

}
