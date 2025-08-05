//TODO ECCEZIONI
package it.polimi.ingsw.model;
import it.polimi.ingsw.model.Cargo.CargoType;
import it.polimi.ingsw.model.ComponentTile.CargoTile;
import it.polimi.ingsw.model.ComponentTile.FixedComponentTile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MatrixDataStructure implements Serializable {

    //ATTRIBUTES
    private FixedComponentTile[][] matrix;
    private ArrayList<Coordinates> blockedTiles;

    //CACHE KEYS:
    // astronauts, batteries, singleEngines, doubleEngines, singleCannons, doubleCannons, cargo, cargoSpecial, aliens, shields
    private Map<String, ArrayList<Coordinates>> cacheTilesMap;

//-------------------------------------------------------------------------------------------------------------------------------------------------------
//CONNSTRUCTOR

    /**
     * Constructor for the MatrixDataStructure class.
     * Initializes the matrix, cache, and blocked tiles based on the given level.
     *
     * @param row    the number of rows in the matrix.
     * @param column the number of columns in the matrix.
     * @param level  the level of the game, which determines blocked tiles.
     */
    public  MatrixDataStructure(int row, int column, int level) {
        this.matrix = new FixedComponentTile[row][column];
        //volendo potremmo passare anche un ArrayList di stringhe al costruttore per creare i campi della cache
        this.cacheTilesMap = new HashMap<>();
        this.cacheTilesMap.put("astronauts", new ArrayList<Coordinates>());
        this.cacheTilesMap.put("batteries", new ArrayList<Coordinates>());
        this.cacheTilesMap.put("singleEngines", new ArrayList<Coordinates>());
        this.cacheTilesMap.put("doubleEngines", new ArrayList<Coordinates>());
        this.cacheTilesMap.put("singleCannons", new ArrayList<Coordinates>());
        this.cacheTilesMap.put("doubleCannons", new ArrayList<Coordinates>());
        this.cacheTilesMap.put("cargo", new ArrayList<Coordinates>());
        this.cacheTilesMap.put("specialCargo", new ArrayList<Coordinates>());
        this.cacheTilesMap.put("aliens", new ArrayList<Coordinates>());
        this.cacheTilesMap.put("shields", new ArrayList<Coordinates>());

        blockedTiles = new ArrayList<>();
        blockedTiles.add(new Coordinates(0, 0));
        blockedTiles.add(new Coordinates(0, 1));
        blockedTiles.add(new Coordinates(1, 0));
        blockedTiles.add(new Coordinates(0, 5));
        blockedTiles.add(new Coordinates(0, 6));
        blockedTiles.add(new Coordinates(1, 6));
        blockedTiles.add(new Coordinates(4, 3));

        if(level == 1){

            blockedTiles.add(new Coordinates(0, 2));
            blockedTiles.add(new Coordinates(0, 4));
            blockedTiles.add(new Coordinates(1, 1));
            blockedTiles.add(new Coordinates(1, 5));
            blockedTiles.add(new Coordinates(2, 0));
            blockedTiles.add(new Coordinates(2, 6));
            blockedTiles.add(new Coordinates(3, 0));
            blockedTiles.add(new Coordinates(3, 6));
            blockedTiles.add(new Coordinates(4, 0));
            blockedTiles.add(new Coordinates(4, 6));

        }else if(level != 2){
            //eccezione
            throw new RuntimeException();
        }
    }

//-------------------------------------------------------------------------------------------------------------------------------------------------------
//GETTERS

    /**
     * @return an ArrayList containing the coordinates of the alien life support systems.
     */
    public ArrayList<Coordinates> getAliensLifeSupportPlace(){
        ArrayList<Coordinates> aliensPositions = new ArrayList<>();
        for(Coordinates coordinates : cacheTilesMap.get("aliens")){
            if(matrix[coordinates.getRow()][coordinates.getCol()]!=null){
                aliensPositions.add(coordinates);
            }
        }
        return aliensPositions;
    }

    /**
     * @return an ArrayList containing the coordinates of the tiles on the edge of the ship.
     */
    public ArrayList<Coordinates> getBorderTiles(){

        // Prepare the return structure and support variables for processing
        ArrayList<Coordinates> borderTilesCoordinates = new ArrayList<>();
        Coordinates tmp =new Coordinates(0,0);
        int i,j;
        boolean ctrl=true;

        // Search for edge tiles starting from the top
        for(i=0; i<5; i++){
            for(j=0; j<7; j++){
                if(matrix[i][j]!= null){
                    borderTilesCoordinates.add(new Coordinates(i,j));
                    break;
                }
            }
        }

        // From now on, before adding a tile to the ArrayList to be returned, check that it is not already present
        // (potentially, it can be encountered on the edge starting from different positions)

        // Search for edge tiles starting from the right
        for(i=6; i>=0; i--){
            for(j=4; j>=0; j--){
                if(matrix[j][i]!= null){
                    tmp.setRow(j);
                    tmp.setCol(i);
                    for(Coordinates c : borderTilesCoordinates){
                        if(c.equals(tmp)){
                            ctrl=false;
                        }
                    }
                    if(ctrl){
                        borderTilesCoordinates.add(new Coordinates(tmp.getRow(), tmp.getCol()));
                        ctrl=true;
                    }
                    break;
                }
            }
        }

        // Search for edge tiles starting from the bottom
        for(i=4; i>=0; i--){
            for(j=6; j>=0; j--){
                if(matrix[i][j]!= null){
                    tmp.setRow(i);
                    tmp.setCol(j);
                    for(Coordinates c : borderTilesCoordinates){
                        if(c.equals(tmp)){
                            ctrl=false;
                        }
                    }
                    if(ctrl){
                        borderTilesCoordinates.add(new Coordinates(tmp.getRow(), tmp.getCol()));
                        ctrl=true;
                    }
                    break;
                }
            }
        }

        // Search for edge tiles starting from the left
        for(i=6; i>=0; i--) {
            for (j=0; j<5; j++) {
                if (matrix[j][i] != null) {
                    tmp.setRow(j);
                    tmp.setCol(i);
                    for (Coordinates c : borderTilesCoordinates) {
                        if (c.equals(tmp)) {
                            ctrl = false;
                        }
                    }
                    if (ctrl) {
                        borderTilesCoordinates.add(new Coordinates(tmp.getRow(), tmp.getCol()));
                        ctrl = true;
                    }
                    break;
                }
            }
        }

        return borderTilesCoordinates;
    }

    /**
     * @param coordinates the coordiantes of the tile,
     * @return the FixedComponentTile located in the matrix at those coordinates.
     */
    public FixedComponentTile getTile(Coordinates coordinates) {
        return this.matrix[coordinates.getRow()][coordinates.getCol()];
    }

    /**
     * Returns all possible cells that are still empty (occupiable).
     * [SEE NOTES IN SHIPBOARD]
     *
     * @return an ArrayList of Coordinates representing the empty cells.
     */
    public ArrayList<Coordinates> getPossiblePlacementOnBoard() {
        ArrayList<Coordinates> possiblePlacements = new ArrayList<>();
        for (int i=0; i<5; i++){
            for(int j=0; j<7; j++){
                if (this.matrix[i][j] == null) {
                    possiblePlacements.add(new Coordinates(i, j));
                }
            }
        }
        return possiblePlacements; // Placeholder logic
    }

    /**
     * Returns the coordinates of the tiles that can host cargo and how much space they have.
     *
     * @param isSpecial a boolean indicating whether to check for special cargo tiles.
     * @return a Map where the keys are Coordinates of the tiles and the values are the remaining capacity.
     */
    public Map<Coordinates, Integer> getPossibleCargoPlacementOnBoard(boolean isSpecial) {
        Map<Coordinates, Integer> possibleCargoPlacements = new HashMap<>();
        int capacity = 0;
        int size = 0;
        for (Coordinates coordinates : cacheTilesMap.get(isSpecial ? "specialCargo" : "cargo")){
            if(matrix[coordinates.getRow()][coordinates.getCol()]!=null){
                capacity = matrix[coordinates.getRow()][coordinates.getCol()].getComponentTile().getCapacity();
                size = matrix[coordinates.getRow()][coordinates.getCol()].getComponentTile().getStored().size();
            }
            if(matrix[coordinates.getRow()][coordinates.getCol()] != null && size < capacity ) {
                possibleCargoPlacements.put(coordinates,(capacity-size));
            }
        }
        return possibleCargoPlacements; // Placeholder logic
    }

    /**
     * Returns the list of coordinates of the tiles that host astronauts and how many they are hosting.
     *
     * @return a Map where the keys are Coordinates of the tiles and the values are the number of astronauts hosted.
     */
    public Map<Coordinates, Integer> getTilesContainCrew() {
        Map<Coordinates, Integer> crewPositions = new HashMap<>();
        for (Coordinates coordinates : cacheTilesMap.get("astronauts")){
            if(getTile(coordinates)!=null)
                crewPositions.put(coordinates, (matrix[coordinates.getRow()][coordinates.getCol()].getComponentTile().getValue()));
        }
        return crewPositions;
    }

    /**
     * @return a map contains the list of coordinates of the tiles that contain batteries and how many they are hosting.
     */
    public Map<Coordinates, Integer> getTilesContainBatteries() {
        Map<Coordinates, Integer> batteriesPositions = new HashMap<>();
        for (Coordinates coordinates : cacheTilesMap.get("batteries")){
            if(getTile(coordinates)!=null)
                batteriesPositions.put(coordinates, (matrix[coordinates.getRow()][coordinates.getCol()].getComponentTile().getValue()));
        }
        return batteriesPositions;
    }

    /**
     * @return a map contains the list of coordinates of the tiles that host cargo and the value of cargo they are hosting (both special and non-special).
     */
    public Map<Coordinates, Integer> getTilesContainCargo(){
        Map<Coordinates, Integer> cargoPositions = new HashMap<>();
        int tot=0;
        System.out.println("normal ora");
        for(Coordinates coordinates : cacheTilesMap.get("cargo")){
            System.out.println("normale");
            if(getTile(coordinates)!=null){
                tot=0;
                CargoTile cargoTile = (CargoTile)(matrix[coordinates.getRow()][coordinates.getCol()].getComponentTile());
                System.out.println(cargoTile.getStoredCargos().size()+" size1");
                for(CargoType c : cargoTile.getStoredCargos()){
                    System.out.println("calcolo");
                    tot+= c.getCredits();
                    System.out.println(tot);
                }
                cargoPositions.put(coordinates, tot);
            }

        }
        System.out.println("special ora");
        for(Coordinates coordinates : cacheTilesMap.get("specialCargo")){
            if(getTile(coordinates)!=null){
                System.out.println("special");
                tot=0;
                CargoTile cargoTile = (CargoTile)(matrix[coordinates.getRow()][coordinates.getCol()].getComponentTile());
                System.out.println(cargoTile.getStoredCargos().size()+" size2");
                for(CargoType c : cargoTile.getStoredCargos()){
                    System.out.println("calcolo2");
                    tot+= c.getCredits();
                    System.out.println(tot);
                }
                cargoPositions.put(coordinates, tot);

            }
        }
        return cargoPositions;
    }

    /**
     * @return a map contains cargoColor and a list of the coordinates of the tiles that contain the cargo with the highest value.
     */
    public Map<String, ArrayList<Coordinates>> getMostValueCargoTiles(int numCargoLost) {

        // Prepare return structures and support variables for processing
        boolean end = false;
        int currentValueSearched = 4;
        Map<String, ArrayList<Coordinates>> cargoPositions = new HashMap<>();
        cargoPositions.put("Red",new ArrayList<Coordinates>());
        cargoPositions.put("Yellow",new ArrayList<Coordinates>());
        cargoPositions.put("Green",new ArrayList<Coordinates>());
        cargoPositions.put("Blue",new ArrayList<Coordinates>());

        // First search for all special cargo (must reach numCargoLost) | in any case, search in order of value from highest to lowest
        for(int i = 0; i<4 && end == false ; i++){
            for (Coordinates coordinates : cacheTilesMap.get("specialCargo")){
                for (CargoType cargotype : matrix[coordinates.getRow()][coordinates.getCol()].getComponentTile().getStored()){
                    if(currentValueSearched == cargotype.getCredits()){
                        switch (i){
                            case 0 : cargoPositions.get("Red").add(coordinates); break;
                            case 1 : cargoPositions.get("Yellow").add(coordinates); break;
                            case 2 : cargoPositions.get("Green").add(coordinates); break;
                            case 3 : cargoPositions.get("Blue").add(coordinates); break;
                        }
                        numCargoLost--;
                    }
                    //numCargoLost -= currentValueSearched;
                    if(numCargoLost <= 0){
                        end = true;
                    }
                }
            }

            if(i>0){
                // If not finished, also search in normal cargo
                for (Coordinates coordinates : cacheTilesMap.get("cargo")){
                    for (CargoType cargotype : matrix[coordinates.getRow()][coordinates.getCol()].getComponentTile().getStored()){
                        if(currentValueSearched == cargotype.getCredits()){
                            switch (i){
                                //case 0 : cargoPositions.get("Red").add(coordinates); break;
                                case 1 : cargoPositions.get("Yellow").add(coordinates); break;
                                case 2 : cargoPositions.get("Green").add(coordinates); break;
                                case 3 : cargoPositions.get("Blue").add(coordinates); break;
                            }
                            numCargoLost--;
                        }
                        if(numCargoLost <= 0){
                            end = true;
                        }
                    }
                }
            }
            if(end == false){
                currentValueSearched--;
            }
        }
        return cargoPositions;
    }

    /**
     * Returns the coordinates of the tiles that can host batteries.
     *
     * @return an ArrayList containing the coordinates of battery tiles.
     */
    public ArrayList<Coordinates> getBatteriesTilesCoordinates(){
        ArrayList<Coordinates> batteriesPositions = new ArrayList<>();
        for(Coordinates c : cacheTilesMap.get("batteries")){
            if(getTile(c)!=null){
                batteriesPositions.add(c);
            }
        }
        return batteriesPositions;
    }

    /**
     * Returns the positions of cannons (single and double) on the board.
     *
     * @return a map where the keys are "singleCannons" and "doubleCannons" and the values are their respective coordinates.
     */
    public Map<String, ArrayList<Coordinates>> getCannonsPosition(){
        Map<String, ArrayList<Coordinates>> cannonPositions = new HashMap<>();
        ArrayList<Coordinates> singleCannons = new ArrayList<Coordinates>();
        ArrayList<Coordinates> doubleCannons = new ArrayList<Coordinates>();

        for(Coordinates c : cacheTilesMap.get("doubleCannons")) {
            if(getTile(c)!=null){
                doubleCannons.add(c);
            }

        }
        for(Coordinates c : cacheTilesMap.get("singleCannons"))
        {
            if(getTile(c)!=null){
                singleCannons.add(c);
            }

        }

        cannonPositions.put("singleCannons", singleCannons);
        cannonPositions.put("doubleCannons", doubleCannons);
        return cannonPositions;
    }

    /**
     * Returns the positions of engines (single and double) on the board.
     *
     * @return a map where the keys are "singleEngines" and "doubleEngines" and the values are their respective coordinates.
     */
    public Map<String, ArrayList<Coordinates>> getEnginesPosition(){
        Map<String, ArrayList<Coordinates>> enginePositions = new HashMap<>();
        ArrayList<Coordinates> singleEngines = new ArrayList<Coordinates>();
        ArrayList<Coordinates> doubleEngines = new ArrayList<Coordinates>();

        for(Coordinates c : cacheTilesMap.get("doubleEngines")) {
            if(getTile(c)!=null){
                doubleEngines.add(c);
            }

        }
        for(Coordinates c : cacheTilesMap.get("singleEngines"))
        {
            if(getTile(c)!=null){
                singleEngines.add(c);
            }

        }

        enginePositions.put("singleEngines",singleEngines);
        enginePositions.put("doubleEngines",doubleEngines);

        return enginePositions;
    }

    /**
     * Returns the positions of cargo tiles (both normal and special) along with their values.
     *
     * @return a map where the keys are "cargo" and "cargoSpecial" and the values are maps of coordinates and their respective values.
     */
    public Map<String, Map<Coordinates, Integer>> getCargosPosition(){
        Map<String, Map<Coordinates, Integer>> cargoPositions = new HashMap<>();
        Map<Coordinates, Integer> cargo = new HashMap<>();
        for(Coordinates coordinates : cacheTilesMap.get("cargo")){
            if(getTile(coordinates)!=null)
                cargo.put(coordinates, matrix[coordinates.getRow()][coordinates.getCol()].getComponentTile().getValue());
        }
        cargoPositions.put("cargo",cargo);
        Map<Coordinates, Integer> cargoSpecial = new HashMap<>();
        for(Coordinates coordinates : cacheTilesMap.get("specialCargo")){
            if(getTile(coordinates)!=null)
                cargoSpecial.put(coordinates, matrix[coordinates.getRow()][coordinates.getCol()].getComponentTile().getValue());
        }
        cargoPositions.put("cargoSpecial",cargoSpecial);
        return cargoPositions;
    }

    /**
     * Returns the coordinates of blocked tiles on the board.
     *
     * @return an ArrayList containing the coordinates of blocked tiles.
     */
    public ArrayList<Coordinates> getBlockedTiles(){
        return new ArrayList<>(blockedTiles);
    }

    //TODO: Bisogna discutere se dividere questo metodo in 2: uno ritorna gli scudi in coordinate e uno da le direzioni.
    // Dipende da cosa serve/vuole la GUI
    /**
     * Returns the positions of shields on the board along with the directions they cover.
     * NOTE: for convention, the directions of the shield are the direction of the tile and the next right direction
     * @return a map where the keys are the coordinates of shields and the values are arrays of directions covered.
     */
    public Map<Coordinates, Direction[]> getShieldsPosition(){
        Map<Coordinates, Direction[]> shieldPositions = new HashMap<>();

        for(Coordinates c : cacheTilesMap.get("shields")){
            if(getTile(c).getDirection() == Direction.NORTH){
                shieldPositions.put(c, new Direction[]{Direction.NORTH, Direction.EAST});
            }
            else if(getTile(c).getDirection() == Direction.EAST){
                shieldPositions.put(c, new Direction[]{Direction.EAST, Direction.SOUTH});
            }
            else if(getTile(c).getDirection() == Direction.SOUTH){
                shieldPositions.put(c, new Direction[]{Direction.SOUTH, Direction.WEST});
            }
            else if(getTile(c).getDirection() == Direction.WEST){
                shieldPositions.put(c, new Direction[]{Direction.WEST, Direction.NORTH});
            }
        }
        return shieldPositions;
    }

    /**
     * Returns the coordinates of the tiles that are tubes.
     *
     * @return an ArrayList containing the coordinates of tiles that are tubes.
     */
    public ArrayList<Coordinates> getTubesPosition(){
        ArrayList<Coordinates> nullPositions = new ArrayList<>();
        int i, j;
        for(i=0; i<7; i++){
            for(j=0; j<7; j++){
                Coordinates coordinates = new Coordinates(i,j);
                if( getTile(coordinates)!=null &&
                    !cacheTilesMap.get("astronauts").contains(coordinates) &&
                    !cacheTilesMap.get("batteries").contains(coordinates) &&
                    !cacheTilesMap.get("singleEngines").contains(coordinates) &&
                    !cacheTilesMap.get("doubleEngines").contains(coordinates) &&
                    !cacheTilesMap.get("singleCannons").contains(coordinates) &&
                    !cacheTilesMap.get("doubleCannons").contains(coordinates) &&
                    !cacheTilesMap.get("cargo").contains(coordinates) &&
                    !cacheTilesMap.get("specialCargo").contains(coordinates) &&
                    !cacheTilesMap.get("shields").contains(coordinates) &&
                    !cacheTilesMap.get("aliens").contains(coordinates))
                {
                    nullPositions.add(coordinates);
                }
            }
        }
        return nullPositions;
    }

//-------------------------------------------------------------------------------------------------------------------------------------------------------
// INCREASE AND DECREASE

    /**
     * Decreases the crew in the specified tile by the given number.
     *
     * @param coordinates the coordinates of the tile.
     * @param num the number of crew members to decrease.
     */
    public void decreaseCrewInTile(Coordinates coordinates, int num) {
        if(matrix[coordinates.getRow()][coordinates.getCol()].getComponentTile().getValue() - num >= 0)
            matrix[coordinates.getRow()][coordinates.getCol()].getComponentTile().unload(num);
        // else
        // exception?
    }

    /**
     * Decreases the batteries in the specified tile by the given number.
     *
     * @param coordinates the coordinates of the tile.
     * @param num the number of batteries to decrease.
     */
    public void decreaseBatteriesInTile(Coordinates coordinates, int num) {
        if(matrix[coordinates.getRow()][coordinates.getCol()].getComponentTile().getValue() - num >= 0)
            matrix[coordinates.getRow()][coordinates.getCol()].getComponentTile().unload(num);
        // else
        // exception?
    }

    /**
     * Increases or decreases the cargo in the specified tile based on the given parameters.
     *
     * @param coordinates the coordinates of the tile.
     * @param cargoType the type of cargo to add or remove.
     * @param decrease a boolean indicating whether to decrease (true) or increase (false) the cargo.
     */
    public void increaseCargoInTile(Coordinates coordinates, CargoType cargoType, boolean decrease) {
        //if()
        try {
            if(decrease) {
                matrix[coordinates.getRow()][coordinates.getCol()].getComponentTile().getStored().remove(cargoType);
            } else {
                matrix[coordinates.getRow()][coordinates.getCol()].getComponentTile().getStored().add(cargoType);
            }
        } catch (Exception e) {
            // throw new RuntimeException(e); // exceptions?
        }
    }


//-------------------------------------------------------------------------------------------------------------------------------------------------------
// FUNCTIONAL METHODS

    /**
     * Returns the value of single cannons (firepower).
     *
     * @return the total firepower of single cannons.
     */
    public double calculateFirePowerSingle() {
        double tot = 0.0;
        for (Coordinates coordinates : cacheTilesMap.get("singleCannons")) {
            if(matrix[coordinates.getRow()][coordinates.getCol()]!=null){
                if (matrix[coordinates.getRow()][coordinates.getCol()].getDirection().equals(Direction.NORTH)) {
                    tot += 1.0;
                } else {
                    tot += 0.5;
                }
            }

        }
        return tot;
    }

    /**
     * Returns the coordinates and the respective value of double cannons.
     *
     * @return a map where the keys are the coordinates of double cannons and the values are their firepower.
     */
    public Map<Coordinates, Integer> calculatePotentialFirePowerDouble() {
        Map<Coordinates, Integer> potentialFirePower = new HashMap<>();
        for (Coordinates coordinates : cacheTilesMap.get("doubleCannons")) {
            if(matrix[coordinates.getRow()][coordinates.getCol()]!=null){
                if (matrix[coordinates.getRow()][coordinates.getCol()].getDirection().equals(Direction.NORTH)) {
                    potentialFirePower.put(coordinates, 2);
                } else {
                    potentialFirePower.put(coordinates, 1);
                }
            }
        }
        return potentialFirePower;
    }

    /**
     * Returns the value of single engines.
     *
     * @return the total power of single engines.
     */
    public double calculateEnginePowerSingle() {
        double tot = 0.0;

        for (Coordinates coordinates : cacheTilesMap.get("singleEngines")) {
            if(matrix[coordinates.getRow()][coordinates.getCol()]!=null){
                if (matrix[coordinates.getRow()][coordinates.getCol()].getDirection().equals(Direction.NORTH)) {
                    tot += 1.0;
                } else {
                    tot += 0.5;
                }
            }
        }
        return tot;
    }

    /**
     * Returns the coordinates and the respective value of double engines.
     *
     * @return a map where the keys are the coordinates of double engines and the values are their power.
     */
    public Map<Coordinates, Integer> calculatePotentialEnginePowerDouble() {
        Map<Coordinates, Integer> potentialEnginePower = new HashMap<>();
        for (Coordinates coordinates : cacheTilesMap.get("doubleEngines")) {
            if(matrix[coordinates.getRow()][coordinates.getCol()]!=null) {
                potentialEnginePower.put(coordinates, 2);
            }
        }

        return potentialEnginePower;
    }

    /**
     * Replaces the astronauts in a cabin with an alien (placement is implicit by saving the coordinates in shipboard and removing the astronauts).
     *
     * @param coordinates the coordinates of the cabin to replace the crew with an alien.
     */
    public void replaceCrewWithAlienAt(Coordinates coordinates){
        if(cacheTilesMap.get("astronauts").contains(coordinates)){
            for(Coordinates coord : cacheTilesMap.get("astronauts")){
                if(matrix[coordinates.getRow()][coordinates.getCol()]!=null) {
                    if (coord.equals(coordinates)) {
                        matrix[coord.getRow()][coord.getCol()].getComponentTile().unload(2);
                        // perhaps the cabin should have a method place alien?
                    }
                }
            }
        }
    }

    /**
     * Saves the given fixed component in the matrix at the specified coordinates.
     *
     * @param coordinates the coordinates where the component will be placed.
     * @param fixedComponent the fixed component to place on the board.
     */
    public void placeComponentOnBoard(Coordinates coordinates, FixedComponentTile fixedComponent) {
        if(!blockedTiles.contains(coordinates)){
            this.matrix[coordinates.getRow()][coordinates.getCol()] = fixedComponent;
            fixedComponent.insertYourSelfInCache(cacheTilesMap, coordinates);
        }else{
            //eccezione
            throw new RuntimeException();
        }

    }


//-------------------------------------------------------------------------------------------------------------------------------------------------------
//REMOVE

    /**
     * Sets the cell at the given coordinates to null.
     *
     * @param coordinates the coordinates of the cell to remove.
     */
    public void removeTile(Coordinates coordinates) {
        matrix[coordinates.getRow()][coordinates.getCol()] = null;
    }


//-------------------------------------------------------------------------------------------------------------------------------------------------------
//NOTE

    //TODO? è possibile fare che se una tile cabin contiene un aliene il suo valore è settato a -1?

//-------------------------------------------------------------------------------------------------------------------------------------------------------
//metodi aggiunti da Ste Hong
/*
    public FixedComponentTile[][] getMatrix() {
        return matrix;
    }

*/

}
