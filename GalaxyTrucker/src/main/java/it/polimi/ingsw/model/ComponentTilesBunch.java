package it.polimi.ingsw.model;

import it.polimi.ingsw.model.ComponentTile.ComponentTile;
import it.polimi.ingsw.model.FileUploaders.TilesLoader;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class ComponentTilesBunch implements Serializable {
    private List<ComponentTile> coveredBunch;
    private List<ComponentTile> uncoveredBunch;
    private List<ComponentTile> takenTiles;

    /** Sets all class attributes and loads all component tiles from disk
     */
    public ComponentTilesBunch(){
        String tilesFilePath = "gameAssets/tiles.json";

        try {
            coveredBunch = TilesLoader.uploadAllTiles(tilesFilePath);
        }
        catch (FileNotFoundException e){
            //FIXME: cosa fare in questo caso?? Non deve permettere l'avanzamento degli stati
            e.printStackTrace();
        }

        takenTiles = new ArrayList<>();
        uncoveredBunch = new ArrayList<>();
    }

    /** Returns a random ComponentTile from covered ones
     *
     * @return random ComponentTile
     */
    public ComponentTile getRandomFromCovered(){
        Random ran = new Random();
        ComponentTile tile = coveredBunch.get(ran.nextInt(coveredBunch.size()));
        coveredBunch.remove(tile);
        takenTiles.add(tile);
        return tile;
    }

    public List<ComponentTile> getAllUncovered(){return new ArrayList<>(uncoveredBunch);}
    public List<ComponentTile> getAllCovered(){ return new ArrayList<>(coveredBunch); }

    /** Removes a tile from taken tiles
     *
     * @param fileName of the tile to be removed
     */
    public void remove(String fileName){
        if(fileName != null){
            Iterator<ComponentTile> iterator = takenTiles.iterator();
            while(iterator.hasNext()){
                if(iterator.next().getFileName().equals(fileName)) {
                    iterator.remove();
                    break;
                }
                ComponentTile tile = iterator.next();
            }
            //eccezione
        }
        else{
            System.out.println("Removing from tileBunch a tile which filename is null");
        }
    }

    /** Puts the selected tile among uncovered ones
     *
     * @param ct tile to uncover
     */
    public void setTileAsUncovered(ComponentTile ct){
        if(!uncoveredBunch.contains(ct) && takenTiles.contains(ct)){
            takenTiles.remove(ct);
            uncoveredBunch.add(ct);
        }
    }

    /** Returns a tile from uncovered ones and removes it
     *
     * @param fileName ot the tile to be returned
     * @return ComponentTile if the fileName is present, otherwise null
     */
    public ComponentTile takeUncoveredTile(String fileName){
        for(ComponentTile ct : uncoveredBunch){
            if(fileName.equals(ct.getFileName())){
                uncoveredBunch.remove(ct);
                takenTiles.add(ct);
                return ct;
            }
        }
        return null;
    }
}

