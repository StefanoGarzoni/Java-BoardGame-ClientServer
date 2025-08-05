package it.polimi.ingsw.model.clientModel;

import it.polimi.ingsw.model.ComponentTile.ComponentTile;
import it.polimi.ingsw.model.FileUploaders.TilesLoader;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClientComponentTilesBunch {
    private final List<ComponentTile> coveredBunch;
    private final List<ComponentTile> uncoveredBunch;

    public ClientComponentTilesBunch()  throws FileNotFoundException {
        String tilesFilePath = "gameAssets/tiles.json";

        uncoveredBunch = new ArrayList<>();
        coveredBunch = TilesLoader.uploadAllTiles(tilesFilePath);
    }

    /** Gets and removes a ComponentTile from covered tiles, based of its fileName
     *
     * @param fileName of the file to be returned
     * @return covered ComponentTile with requested filename, null if not exists
     */
    public ComponentTile takeCoveredTile(String fileName){
        Optional<ComponentTile> selectedTile = coveredBunch.stream().filter((tile) -> tile.getFileName().equals(fileName)).findFirst();

        if(selectedTile.isPresent())
            coveredBunch.removeIf((tile) -> selectedTile.get().getFileName().equals(tile.getFileName()));

        return selectedTile.orElse(null);
    }

    /** Gets and removes a ComponentTile from uncovered tiles, based of its fileName
     *
     * @param fileName of the file to be returned
     * @return uncovered ComponentTile with requested filename, null if not exists
     */
    public ComponentTile takeUncoveredTile(String fileName){
        Optional<ComponentTile> selectedTile = uncoveredBunch.stream().filter((tile) -> tile.getFileName().equals(fileName)).findFirst();

        if(selectedTile.isPresent())
            uncoveredBunch.removeIf((tile) -> selectedTile.get().getFileName().equals(tile.getFileName()));

        return selectedTile.orElse(null);
    }

    public boolean isTileUncovered(int tileIndex){
        return tileIndex < uncoveredBunch.size() && tileIndex >= 0;
    }
    public boolean isTileCovered(int tileIndex){
        return tileIndex < coveredBunch.size() && tileIndex >= 0;
    }

    /** Returns a fileName of a file, given his index in one of the tiles lists
     *
     * @param tileIndex index of the tile to be returned the name
     * @param isCovered if true, the file name will be searched among covered tiles. Otherwise, among uncovered.
     * @return filename of the selected tile
     */
    public String getFileNameOfTileAtIndex(int tileIndex, boolean isCovered){
        if(isCovered && isTileCovered(tileIndex))
            return coveredBunch.get(tileIndex).getFileName();
        else if(!isCovered && isTileUncovered(tileIndex))
            return uncoveredBunch.get(tileIndex).getFileName();
        else
            return null;
    }

    /** Re-introduces a ComponentTile in the tiles bunch. Following game's rules, it will be uncovered.
     *
     * @param tile component tile to be inserted among uncovered tiles
     */
    public void restoreUncoveredTile(ComponentTile tile){
        uncoveredBunch.add(tile);
    }

    public List<ComponentTile> getAllUncoveredTiles(){
        return uncoveredBunch;
    }
}
