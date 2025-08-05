//TODO ECCEZIONI
package it.polimi.ingsw.model;
import it.polimi.ingsw.model.Cargo.CargoType;
import it.polimi.ingsw.model.ComponentTile.ComponentTile;
import it.polimi.ingsw.model.ComponentTile.Connector;
import it.polimi.ingsw.model.ComponentTile.FixedComponentTile;

import java.io.Serializable;
import java.util.*;
import java.util.ArrayList;

public class ShipBoard implements Serializable {

    //ATTRIBUTES
    private int totalCrew;
    private Coordinates brownAlien;
    private Coordinates purpleAlien;
    private int totalBatteries;
    private int totalCargoValue;

    private int lostTiles;

    private boolean hasAnyComponent; // settato a True ogni volta che aggiungo un tile alla matrice

    private MatrixDataStructure shipBoardMatrix;
    private ArrayList<ComponentTile> bookedComponentTiles;

//-------------------------------------------------------------------------------------------------------------------------------------------------------
//CONSTRUCTOR
    public ShipBoard(int level) {
        this.totalCrew = 0;
        this.totalBatteries = 0;
        this.totalCargoValue = 0;
        this.lostTiles = 0;

        this.hasAnyComponent = false;

        this.brownAlien = null;
        this.purpleAlien = null;

        this.bookedComponentTiles = new ArrayList<>();
        this.shipBoardMatrix = new MatrixDataStructure(5,7, level);
    }

//-------------------------------------------------------------------------------------------------------------------------------------------------------
// GETTERS

    /**
     * Returns the coordinates of the tiles containing crew and their respective numbers.
     * This calls a function from MatrixDataStructure.
     * @return a map of coordinates and the number of crew members in each tile, -1 if the tile contains a purple alien, -2 if it contains a brown alien.
     */
    public Map<Coordinates, Integer> getCrewPositions(){
        Map<Coordinates,Integer> crewPositions = new HashMap<>(shipBoardMatrix.getTilesContainCrew());
        if(purpleAlien!=null){
            if(crewPositions.containsKey(purpleAlien)){
                crewPositions.replace(purpleAlien, -1);
            }
        }
        if (brownAlien!= null){
            if(crewPositions.containsKey(brownAlien)){
                crewPositions.replace(brownAlien, -2);
            }
        }
        return crewPositions;
    }

    /**
     * Returns a structure containing the coordinates, the number of astronauts in the tile
     * (-1 for purple alien, -2 for brown alien), the file name or an empty string, and the list of connectors.
     *
     * @param GUI true if the GUI requires the file name, false otherwise.
     * @return a map with the structure described above.
     */
    public Map<Coordinates, Map<Integer, Map<String, Connector[]>>> getCrewTilesPosition(boolean GUI){
        Map<Coordinates,Integer> crewPositions = getCrewPositions();
        Map<Coordinates, Map<Integer, Map<String,Connector[]>>> crewTilesPosition = new HashMap<>();

        for(Coordinates c : crewPositions.keySet() ){
            Connector[] connectors = getShipBoardComponent(c).getComponentTile().getConnectorList();
            Map<Integer,  Map<String,Connector[]>> tmp = new HashMap<>();
            Map<String,Connector[]> tmp1 = new HashMap<>();
            if(GUI){
                tmp1.put(getShipBoardComponent(c).getComponentTile().getFileName(), connectors);
            }else{
                tmp1.put("", connectors);
            }
            tmp.put(crewPositions.get(c), tmp1);
            crewTilesPosition.put(c,tmp);
        }
        return crewTilesPosition;
    }

    /**
     * Returns a structure containing the coordinates, the number of batteries in the tile,
     * the file name or an empty string, and the list of connectors.
     *
     *
     * @return a map with the structure described above.
     */
    public Map<Coordinates, Map<Integer, Map<String,Connector[]>>> getBatteriesTilesPosition(){

        Map<Coordinates, Integer> batteriesPosition = new HashMap<>();
        for(Coordinates c : shipBoardMatrix.getBatteriesTilesCoordinates()){
            batteriesPosition.put(c,getShipBoardComponent(c).getComponentTile().getValue());
        }

        Map<Coordinates, Map<Integer, Map<String,Connector[]>>>  batteriesTilesPosition = new HashMap<>();

        for(Coordinates c : batteriesPosition.keySet() ){
            Connector[] connectors = getShipBoardComponent(c).getComponentTile().getConnectorList();
            Map<Integer,  Map<String,Connector[]>> tmp = new HashMap<>();
            Map<String,Connector[]> tmp1 = new HashMap<>();

            tmp1.put(getShipBoardComponent(c).getComponentTile().getFileName(), connectors);

            tmp.put(batteriesPosition.get(c), tmp1);
            batteriesTilesPosition.put(c,tmp);
        }

        return batteriesTilesPosition;
    }

    /**
     * Returns a structure containing the coordinates, an integer indicating whether it is double (2) or not (1),
     * the file name or an empty string, and the list of connectors.
     *
     * @param GUI true if the GUI requires the file name, false otherwise.
     * @return a map with the structure described above.
     */
    public Map<Coordinates, Map<Integer, Map<String,Connector[]>>> getCannonTilesPosition(boolean GUI){
        Map<Coordinates, Map<Integer, Map<String,Connector[]>>> cannonsPosition = new HashMap<>();

        for(Coordinates c : shipBoardMatrix.getCannonsPosition().get("singleCannons")){
            Map<Integer,  Map<String,Connector[]>> tmp = new HashMap<>();
            Connector[] connectors = getShipBoardComponent(c).getComponentTile().getConnectorList();
            Map<String,Connector[]> tmp1 = new HashMap<>();
            if(GUI){
                tmp1.put(getShipBoardComponent(c).getComponentTile().getFileName(), connectors);
            }else{
                tmp1.put("", connectors);
            }
            tmp.put(1, tmp1);
            cannonsPosition.put(c,tmp);
        }

        for(Coordinates c : shipBoardMatrix.getCannonsPosition().get("doubleCannons")){
            Map<Integer,  Map<String,Connector[]>> tmp = new HashMap<>();
            Connector[] connectors = getShipBoardComponent(c).getComponentTile().getConnectorList();
            Map<String,Connector[]> tmp1 = new HashMap<>();
            if(GUI){
                tmp1.put(getShipBoardComponent(c).getComponentTile().getFileName(), connectors);
            }else{
                tmp1.put("", connectors);
            }
            tmp.put(2, tmp1);
            cannonsPosition.put(c,tmp);
        }
        return cannonsPosition;
    }

    /**
     * Returns a structure containing the coordinates, an integer indicating whether it is double (2) or not (1),
     * the file name or an empty string, and the list of connectors.
     *
     * @param GUI true if the GUI requires the file name, false otherwise.
     * @return a map with the structure described above.
     */
    public Map<Coordinates, Map<Integer, Map<String,Connector[]>>> getEngineTilesPosition(boolean GUI){
        Map<Coordinates, Map<Integer, Map<String,Connector[]>>> enginePosition = new HashMap<>();

        for(Coordinates c : shipBoardMatrix.getEnginesPosition().get("singleEngines")){
            Map<Integer,  Map<String,Connector[]>> tmp = new HashMap<>();
            Connector[] connectors = getShipBoardComponent(c).getComponentTile().getConnectorList();
            Map<String,Connector[]> tmp1 = new HashMap<>();
            if(GUI){
                tmp1.put(getShipBoardComponent(c).getComponentTile().getFileName(), connectors);
            }else{
                tmp1.put("", connectors);
            }
            tmp.put(1, tmp1);
            enginePosition.put(c,tmp);
        }

        for(Coordinates c : shipBoardMatrix.getCannonsPosition().get("doubleEngines")){
            Map<Integer,  Map<String,Connector[]>> tmp = new HashMap<>();
            Connector[] connectors = getShipBoardComponent(c).getComponentTile().getConnectorList();
            Map<String,Connector[]> tmp1 = new HashMap<>();
            if(GUI){
                tmp1.put(getShipBoardComponent(c).getComponentTile().getFileName(), connectors);
            }else{
                tmp1.put("", connectors);
            }
            tmp.put(2, tmp1);
            enginePosition.put(c,tmp);
        }
        return enginePosition;
    }

    /**
     * Returns a structure containing the coordinates, total capacity, whether it is special (1 for special, 0 for normal),
     * the file name or an empty string, the list of connectors, and the list of colors.
     *
     * @param GUI true if the GUI requires the file name, false otherwise.
     * @return a map with the structure described above.
     */
    public Map<Coordinates, Map<Integer[], Map<String, Map<Connector[], String[]>>>> getCargoTilesPosition(boolean GUI){
        Map<Coordinates, Map<Integer[], Map<String, Map<Connector[], String[]>>>> cargoPositions = new HashMap<>();
        int i;

        for(Coordinates c : shipBoardMatrix.getCargosPosition().get("cargo").keySet()){
            Map<Integer[], Map<String, Map<Connector[], String[]>>> tmp = new HashMap<>();
            Map<String, Map<Connector[], String[]>> tmp1 = new HashMap<>();
            Map<Connector[], String[]> tmp2 = new HashMap<>();
            Connector[] connectors = getShipBoardComponent(c).getComponentTile().getConnectorList();
            String[] colors= new String[getShipBoardComponent(c).getComponentTile().getStored().size()];
            i=0;
            for(CargoType ct : getShipBoardComponent(c).getComponentTile().getStored()){ //getStoredCargo????
                colors[i] = ct.getColor();
                i++;
            }
            tmp2.put(connectors,colors);
            if(GUI){
                tmp1.put(getShipBoardComponent(c).getComponentTile().getFileName(), tmp2);
            }else{
                tmp1.put("", tmp2);
            }
            Integer[] tmp3 = new Integer[2];
            tmp3[0] = getShipBoardComponent(c).getComponentTile().getCapacity();
            tmp3[1] = 0;
            tmp.put(tmp3, tmp1);
            cargoPositions.put(c,tmp);
        }

        for(Coordinates c : shipBoardMatrix.getCargosPosition().get("cargoSpecial").keySet()){
            Map<Integer[], Map<String, Map<Connector[], String[]>>> tmp = new HashMap<>();
            Map<String, Map<Connector[], String[]>> tmp1 = new HashMap<>();
            Map<Connector[], String[]> tmp2 = new HashMap<>();
            Connector[] connectors = getShipBoardComponent(c).getComponentTile().getConnectorList();
            String[] colors= new String[getShipBoardComponent(c).getComponentTile().getStored().size()];
            i=0;
            for(CargoType ct : getShipBoardComponent(c).getComponentTile().getStored()){ //getStoredCargo????
                colors[i] = ct.getColor();
                i++;
            }
            tmp2.put(connectors,colors);
            if(GUI){
                tmp1.put(getShipBoardComponent(c).getComponentTile().getFileName(), tmp2);
            }else{
                tmp1.put("", tmp2);
            }
            Integer[] tmp3 = new Integer[2];
            tmp3[0] = getShipBoardComponent(c).getComponentTile().getCapacity();
            tmp3[1] = 1;
            tmp.put(tmp3, tmp1);
            cargoPositions.put(c,tmp);
        }
        return cargoPositions;
    }

    public ArrayList<Coordinates> getTilesBlockedByShipForm(){
        return shipBoardMatrix.getBlockedTiles();
    }

    /**
     * Returns a structure containing the coordinates, an integer indicating whether it is brown (1) or purple (0),
     * the file name or an empty string, and the list of connectors.
     *
     * @param GUI true if the GUI requires the file name, false otherwise.
     * @return a map with the structure described above.
     */
    public Map<Coordinates, Map<Integer, Map<String,Connector[]>>> getAlienLifeSupportTilesPosition(boolean GUI){
        Map<Coordinates, Map<Integer, Map<String,Connector[]>>> alienLifeSupportPositions = new HashMap<>();

        for(Coordinates c: shipBoardMatrix.getAliensLifeSupportPlace()){
            Map<Integer,  Map<String,Connector[]>> tmp = new HashMap<>();
            Map<String, Connector[]> tmp1 = new HashMap<>();
            Connector[] connectors = getShipBoardComponent(c).getComponentTile().getConnectorList();
            if(GUI){
                tmp1.put(getShipBoardComponent(c).getComponentTile().getFileName(), connectors);
            }else{
                tmp1.put("", connectors);
            }
            if(getShipBoardComponent(c).getComponentTile().isBrown()){
                tmp.put(1, tmp1);
            }else{
                tmp.put(0, tmp1);
            }
            alienLifeSupportPositions.put(c,tmp);
        }

        return alienLifeSupportPositions;

    }

    /**
     * Returns a structure containing the coordinates, the file name,
     * the directions covered by the shield, and the connectors list.
     *
     *
     * @return a map with the structure described above.
     */
    public Map<Coordinates,Map<String, Map<Direction[], Connector[]>>> getShieldTilesPosition(){
        Map<Coordinates,Map<String, Map<Direction[], Connector[]>>> shieldTilesPositions = new HashMap<>();
        Map<Coordinates, Direction[]> shields = shipBoardMatrix.getShieldsPosition();
        for(Coordinates c : shields.keySet()){
            Map<Direction[], Connector[]> tmp = new HashMap<>();
            Map<String, Map<Direction[], Connector[]>> tmp1 = new HashMap<>();
            tmp.put(shields.get(c),getShipBoardComponent(c).getComponentTile().getConnectorList());
            tmp1.put(getShipBoardComponent(c).getComponentTile().getFileName(), tmp);
            shieldTilesPositions.put(c,tmp1);
        }
        return shieldTilesPositions;
    }

    /**
     * Returns a structure containing the coordinates, the file name or an empty string,
     * and the connectors list.
     *
     * @param GUI true if the GUI requires the file name, false otherwise.
     * @return a map with the structure described above.
     */
    public Map<Coordinates, Map<String, Connector[]>> getTubesTilesPosition(boolean GUI){
        Map<Coordinates, Map<String, Connector[]>> tubesPositions = new HashMap<>();
        for(Coordinates c : shipBoardMatrix.getTubesPosition()){
            Map<String, Connector[]> tmp = new HashMap<>();
            if(GUI){
                tmp.put(getShipBoardComponent(c).getComponentTile().getFileName(), getShipBoardComponent(c).getComponentTile().getConnectorList());
            }else{
                tmp.put("", getShipBoardComponent(c).getComponentTile().getConnectorList());
            }
            tubesPositions.put(c,tmp);
        }
        return tubesPositions;
    }

    /**
     * Returns the total crew attribute present in the shipboard.
     * @return the total number of crew members.
     */
    public int getTotalCrew() {
        return totalCrew;
    }

    /**
     * Returns the total cargo value attribute present in the shipboard.
     * @return the total cargo value.
     */
    public int getTotalCargoValue(){
        return totalCargoValue;
    }

    /**
     * Returns the total batteries attribute present in the shipboard.
     * @return the total number of batteries.
     */
    public int getTotalBatteries() {
        return totalBatteries;
    }

    /**
     * Returns the coordinates of the brown alien present in the shipboard.
     * @return the coordinates of the brown alien.
     */
    public Coordinates getBrownAlienCoord() {
        return brownAlien;
    }

    /**
     * Returns the coordinates of the purple alien present in the shipboard.
     * @return the coordinates of the purple alien.
     */
    public Coordinates getPurpleAlienCoord() {
        return purpleAlien;
    }

    /**
     * Returns the number of lost tiles attribute present in the shipboard.
     * @return the number of lost tiles.
     */
    public int getLostTilesNumber() {
        return lostTiles;
    }

    /**
     * Returns a fixed component, i.e., the element at the given coordinates in the matrix.
     * This calls a function from MatrixDataStructure.
     * @param coordinates the coordinates of the component.
     * @return the fixed component tile at the given coordinates.
     */
    public FixedComponentTile getShipBoardComponent(Coordinates coordinates) {
        return shipBoardMatrix.getTile(coordinates);
    }

    /**
     * Returns the coordinates of cargo tiles and the remaining space in each tile.
     * This calls a function from MatrixDataStructure.
     * @param isSpecial whether the cargo is special or not.
     * @return a map of coordinates and the remaining space in each cargo tile.
     */
    public Map<Coordinates, Integer> getPossibleCargoPlacement(Boolean isSpecial){
        return shipBoardMatrix.getPossibleCargoPlacementOnBoard(isSpecial);
    }

    /**
     * Returns the coordinates of the cells in the matrix that have not yet been filled with a tile (usable).
     * @return a list of coordinates of possible placements.
     */
    public ArrayList<Coordinates> getPossiblePlacement(){
        return shipBoardMatrix.getPossiblePlacementOnBoard();
    }

    /**
     * Returns the firepower value considering only single cannons.
     * This calls a function from MatrixDataStructure.
     * @return the firepower value of single cannons.
     */
    public double getPotentialFirePowerSingle(){
        int alienFactor = 0;
        if(purpleAlien != null){alienFactor+=2;}
        return shipBoardMatrix.calculateFirePowerSingle() + alienFactor;
    }

    /**
     * Returns the coordinates and respective values of each double cannon.
     * This calls a function from MatrixDataStructure.
     * @return a map of coordinates and the count of double cannons.
     */
    public Map<Coordinates,Integer> getPotentialFirePowerDouble(){
        return shipBoardMatrix.calculatePotentialFirePowerDouble();
    }

    /**
     * Returns the engine power value considering only single engines.
     * This calls a function from MatrixDataStructure.
     * @return the engine power value of single engines.
     */
    public double getPotentialEnginePowerSingle(){
        int alienFactor = 0;
        if(brownAlien != null){alienFactor+=2;}
        return shipBoardMatrix.calculateEnginePowerSingle() + alienFactor;
    }

    /**
     * Returns the coordinates and respective values of each double engine.
     * This calls a function from MatrixDataStructure.
     * @return a map of coordinates and the count of double engines.
     */
    public Map<Coordinates,Integer> getPotentialEnginePowerDouble(){
        return shipBoardMatrix.calculatePotentialEnginePowerDouble();
    }

    /**
     * Returns the possible placements for brown and purple aliens.
     * ArrayList index 0: coordinates for possible brown alien placements.
     * ArrayList index 1: coordinates for possible purple alien placements.
     * @return a list of lists containing coordinates for alien placements.
     */
    public ArrayList<ArrayList<Coordinates>> getPossibleAliensPosition(){

        //preparation of variables to contain the values to be returned
        ArrayList<ArrayList<Coordinates>> possibleAliensPositions = new ArrayList<>();
        ArrayList<Coordinates> brownAliens = new ArrayList<>();
        ArrayList<Coordinates> purpleAliens = new ArrayList<>();

        //get all the coordinates of life supports from matrixDataStructure
        ArrayList<Coordinates> aliensPositions = shipBoardMatrix.getAliensLifeSupportPlace();

        //for each tile that is a cabin, check if it is attached to a life support for aliens
        for(Coordinates c1 : shipBoardMatrix.getTilesContainCrew().keySet()){
            for(Coordinates c2 : aliensPositions){
                if(c1.getRow() == c2.getRow() && c1.getCol() == c2.getCol() + 1 ||
                        c1.getRow() == c2.getRow() && c1.getCol() == c2.getCol() - 1 ||
                        c1.getRow() == c2.getRow() + 1 && c1.getCol() == c2.getCol() ||
                        c1.getRow() == c2.getRow() - 1 && c1.getCol() == c2.getCol()){
                    if(getShipBoardComponent(c2).getComponentTile().isBrown()){
                        brownAliens.add(c1);
                    }else{
                        purpleAliens.add(c1);
                    }
                }
            }
        }

        //prepare the output
        possibleAliensPositions.add(brownAliens);
        possibleAliensPositions.add(purpleAliens);

        return possibleAliensPositions;
    }

    /**
     * Returns a list of tiles that have been booked by the user.
     * @return a list of booked component tiles.
     */
    public ArrayList<ComponentTile> getBookedComponents(){
        return new ArrayList<>(bookedComponentTiles);
    }

    /**
     * Given a cargo value, returns all possible cargo tiles that could be chosen to lose.
     * This calls a function from MatrixDataStructure.
     * @param value the cargo value to consider.
     * @return a map of cargo types and their respective list of coordinates.
     */
    public Map<String, ArrayList<Coordinates>> getTilesContainsCargoWithMostValue(int value){
        return shipBoardMatrix.getMostValueCargoTiles(value);
    }

    /**
     * Returns whether the shipboard hasAanyComponent attribute value.
     * @return true if any component is placed, false otherwise.
     */
    public boolean hasAnyComponent() {
        return hasAnyComponent;
    }


//-------------------------------------------------------------------------------------------------------------------------------------------------------
// INCREASE and DECREASE

    /**
     * Decreases the crew attribute in the shipboard and also updates the value in the tile itself
     * (calls a function from MatrixDataStructure).
     * @param coordinates the coordinates of the cabin.
     * @param num the number of crew members to decrease.
     */
    public void decreaseCrewInCabin(Coordinates coordinates, int num) {
        if(totalCrew - num >= 0){
            totalCrew -= num;
            shipBoardMatrix.decreaseCrewInTile(coordinates,num);
        }//else
        //eccezione?
    }

    /**
     * Decreases the total batteries attribute in the shipboard and also updates the value in the tile itself
     * (calls a function from MatrixDataStructure).
     * @param coordinates the coordinates of the tile.
     * @param batteries the number of batteries to decrease.
     */
    public void decreaseTotalBatteries(Coordinates coordinates, int batteries) {
        if(totalBatteries - batteries >= 0) {
            totalBatteries -= batteries;
            shipBoardMatrix.decreaseBatteriesInTile(coordinates, batteries);
        }//else
        //eccezione?
    }

    /**
     * Increases the lost tiles attribute in the shipboard.
     */
    public void increaseLostTiles() {
        lostTiles++;
    }

    /**
     * Increases or decreases (based on the boolean parameter) the cargo value in the shipboard and updates
     * the value in the tile itself (calls a function from MatrixDataStructure).
     * @param coordinates the coordinates of the tile.
     * @param cargotype the type of cargo.
     * @param decrease true to decrease the cargo value, false to increase it.
     */
    public void increaseCargo(Coordinates coordinates, CargoType cargotype, boolean decrease) {
        if(decrease){
            totalCargoValue -= cargotype.getCredits();
        } else {
            totalCargoValue += cargotype.getCredits();
        }
        shipBoardMatrix.increaseCargoInTile(coordinates, cargotype, decrease);
    }


//-------------------------------------------------------------------------------------------------------------------------------------------------------
// FUNCTIONAL METHODS

    /**
     * Simulates the impact of a shot on the shipboard, determining the effects of the hit and the tiles affected.
     *
     * The method evaluates whether the shield can be activated, whether the shot bounces, or whether it misses the ship entirely.
     * It also identifies the tiles from which two branches diverge, if applicable.
     *
     * @param direction The direction from which the shot is coming (NORTH, SOUTH, EAST, or WEST).
     * @param place The column (for NORTH/SOUTH) or row (for EAST/WEST) where the shot is aimed.
     * @param isBigShot True if the shot is a "big shot" (ignores shields and bounces), false otherwise.
     * @param canBounce True if the shot can bounce off tiles with no connections, false otherwise.
     * @return A map containing:
     *         - A boolean array of size 3:
     *           - [0]: True if the shield can be activated, false otherwise.
     *           - [1]: True if the shot bounces, false otherwise.
     *           - [2]: True if the shot misses the ship entirely, false otherwise.
     *         - A Map with the coordinates that has been shot (ora the last checked if it hit out of ship) as a key
     *              And as a value null if no branch occurs or an arraylist contains the arraylist that contain all the coordinates of that branch
     *
     *
     * Note: If branching occurs, the caller must handle the branch selection and invoke the `delBranch` method accordingly.
     */
    public Map<boolean[], Map<Coordinates, ArrayList<ArrayList<Coordinates>>>> hitTile (Direction direction, int place, boolean isBigShot, boolean canBounce){

        Coordinates[] coordinates = new Coordinates[2];
        coordinates[0] = null;
        coordinates[1] = null;
        Map<boolean[], Map<Coordinates, ArrayList<ArrayList<Coordinates>> >> returnMap = new HashMap<>();
        boolean[] returnValues = {false, false, false};
        boolean canActivateShield = false;
        boolean hitOutShip = true;
        boolean bounce = false;
        Coordinates tmp = new Coordinates(0,0);

        // check if the shot is out of the ship board
        if(
                ((direction.equals(Direction.NORTH) || direction.equals(Direction.SOUTH)) && (place < 0 || place > 6))
                || ((direction.equals(Direction.EAST) || direction.equals(Direction.WEST) ) && (place < 0 || place > 4))
        ){
            returnValues[2] = true;
            returnMap.put(returnValues, new HashMap<>());
            return returnMap;
        }


        if(direction.equals(Direction.NORTH)){
            tmp.setCol(place);
            for(int i=0; i<5; i++){
                tmp.setRow(i);
                if(getShipBoardComponent(tmp)!=null){
                    hitOutShip = false;
                    if(canBounce){
                        if(getShipBoardComponent(tmp).getComponentTile().getConnectorList()[0].getNumberOfConnections()==0){
                            bounce = true;
                        }
                    }
                    if(!isBigShot && !bounce){
                        //potrebbe essere da rivedere la funzione della matrix
                        Map<Coordinates, Direction[]> shields = shipBoardMatrix.getShieldsPosition();
                        for(Coordinates c : shields.keySet()){
                            if(shields.get(c)[0].equals(Direction.NORTH) || shields.get(c)[1].equals(Direction.NORTH)){
                                canActivateShield = true;
                            }
                        }
                    }
                    break;
                }
            }
        }
        else if(direction.equals(Direction.SOUTH)){
            tmp.setCol(place);
            for(int i=4; i>=0; i--){
                tmp.setRow(i);
                if(getShipBoardComponent(tmp)!=null){
                    hitOutShip = false;
                    if(canBounce){
                        if(getShipBoardComponent(tmp).getComponentTile().getConnectorList()[2].getNumberOfConnections()==0){
                            bounce = true;
                        }
                    }
                    if(!isBigShot && !bounce){
                        //potrebbe essere da rivedere la funzione della matrix
                        Map<Coordinates, Direction[]> shields = shipBoardMatrix.getShieldsPosition();
                        for(Coordinates c : shields.keySet()){
                            if(shields.get(c)[0].equals(Direction.SOUTH) || shields.get(c)[1].equals(Direction.SOUTH)){
                                canActivateShield = true;
                            }
                        }
                    }
                    break;
                }
            }

        }
        else if(direction.equals(Direction.EAST)){
            tmp.setRow(place);
            for(int i=6; i>=0; i--){
                tmp.setCol(i);
                if(getShipBoardComponent(tmp)!=null){
                    hitOutShip = false;
                    if(canBounce){
                        if(getShipBoardComponent(tmp).getComponentTile().getConnectorList()[1].getNumberOfConnections()==0){
                            bounce = true;
                        }
                    }
                    if(!isBigShot && !bounce){
                        //potrebbe essere da rivedere la funzione della matrix
                        Map<Coordinates, Direction[]> shields = shipBoardMatrix.getShieldsPosition();
                        for(Coordinates c : shields.keySet()){
                            if(shields.get(c)[0].equals(Direction.EAST) || shields.get(c)[1].equals(Direction.EAST)){
                                canActivateShield = true;
                            }
                        }
                    }
                    break;
                }
            }
        }
        else{
            tmp.setRow(place);
            for(int i=0; i<7; i++){
                tmp.setCol(i);
                if(getShipBoardComponent(tmp)!=null){
                    hitOutShip = false;
                    if(canBounce){
                        if(getShipBoardComponent(tmp).getComponentTile().getConnectorList()[3].getNumberOfConnections()==0){
                            bounce = true;
                        }
                    }
                    if(!isBigShot && !bounce){
                        //potrebbe essere da rivedere la funzione della matrix
                        Map<Coordinates, Direction[]> shields = shipBoardMatrix.getShieldsPosition();
                        for(Coordinates c : shields.keySet()){
                            if(shields.get(c)[0].equals(Direction.WEST) || shields.get(c)[1].equals(Direction.WEST)){
                                canActivateShield = true;
                            }
                        }
                    }
                    break;
                }
            }
        }

        returnValues[0]=canActivateShield;
        returnValues[1]=bounce;
        returnValues[2]=hitOutShip;


        if((!canActivateShield && !bounce && !hitOutShip)|| (isBigShot && !hitOutShip)){

                this.removeComponentTileAt(tmp);
                this.increaseLostTiles();
                Map<Coordinates, ArrayList<ArrayList<Coordinates>>> map = new HashMap<>();
                ArrayList<ArrayList<Coordinates>> arr = this.findConnectedGroups();
                if(arr.size()>1){
                    map.put(tmp, arr);
                }
                else{
                    map.put(tmp, null);
                }

                returnMap.put(returnValues, map);

        }else{
            Map<Coordinates, ArrayList<ArrayList<Coordinates>>> map = new HashMap<>();
            map.put(tmp, null);
            returnMap.put(returnValues, map);
        }

        return returnMap;
    }

    /**
     * Sets the hasAnyComponent attribute to true and creates a FixedComponent by placing the ComponentTile
     * in the matrix at the given coordinates and direction (calls a function from MatrixDataStructure).
     *
     * @param coordinates the coordinates where the component will be placed.
     * @param component the component tile to be placed.
     * @param direction the direction of the component tile.
     */
    public void placeBoardComponent(Coordinates coordinates, ComponentTile component, Direction direction) {
        hasAnyComponent = true;
        FixedComponentTile fixedComponent = new FixedComponentTile(component, direction);
        shipBoardMatrix.placeComponentOnBoard(coordinates, fixedComponent); // Can this object creation be done in a smarter way?
    }

    /**
     * Checks the entire matrix and returns an ArrayList containing ArrayLists of coordinates
     * of adjacent tiles with incompatible connectors.
     *
     * @return a list of lists containing pairs of coordinates with incompatible connectors.
     */

    //TODO : nuovo flusso di controllo-> check con una funzione che cannoni e pistole vadano bene, passa poi al vecchio flusso da checkCorrectStructure()
    public ArrayList<Coordinates> checkEngineOrientation(){
        ArrayList<Coordinates> tilesRemoved = new ArrayList<>();
        for(Coordinates c : shipBoardMatrix.getEnginesPosition().get("singleEngines")){
           if(!(getShipBoardComponent(c).getDirection().equals(Direction.NORTH))){
               //removeComponentTileAt(c);
               //System.out.println("in1");
               //System.out.println(c.toString());
               tilesRemoved.add(c);
           }
        }

        for(Coordinates c : shipBoardMatrix.getEnginesPosition().get("doubleEngines")){
            if(!(getShipBoardComponent(c).getDirection().equals(Direction.NORTH))){
                //removeComponentTileAt(c);
                //System.out.println("in2");
                //System.out.println(c.toString());
                tilesRemoved.add(c);
            }
        }

        return tilesRemoved;
    }

    public ArrayList<ArrayList<Coordinates>> checkCorrectStructure() {
        // Prepare structures for output and support variables for the method
        ArrayList<ArrayList<Coordinates>> mainList = new ArrayList<>();
        int i, j, k;
        FixedComponentTile[] fixedTilesNearBy = new FixedComponentTile[4];
        Boolean[] tileResponse;
        Coordinates coordinates = new Coordinates(0, 0);

        // Traverse the entire matrix with these two loops
        for (i = 0; i < 5; i++) {
            for (j = 0; j < 7; j++) {
                coordinates.setRow(i);
                coordinates.setCol(j);

                // Prepare the array to pass to checkNearby
                if (getShipBoardComponent(coordinates) != null) {

                    // North Direction
                    if (i - 1 >= 0) {
                        coordinates.setRow(i - 1);
                        if (getShipBoardComponent(coordinates) != null) {
                            fixedTilesNearBy[Direction.NORTH.ordinal()] = getShipBoardComponent(coordinates);
                        } else {
                            fixedTilesNearBy[Direction.NORTH.ordinal()] = null;
                        }
                    } else {
                        fixedTilesNearBy[Direction.NORTH.ordinal()] = null;
                    }
                    coordinates.setRow(i);

                    // East Direction
                    if (j + 1 < 7) {
                        coordinates.setCol(j + 1);
                        if (getShipBoardComponent(coordinates) != null) {
                            fixedTilesNearBy[Direction.EAST.ordinal()] = getShipBoardComponent(coordinates);
                        } else {
                            fixedTilesNearBy[Direction.EAST.ordinal()] = null;
                        }
                    } else {
                        fixedTilesNearBy[Direction.EAST.ordinal()] = null;
                    }
                    coordinates.setCol(j);

                    // South Direction
                    if (i + 1 < 5) {
                        coordinates.setRow(i + 1);
                        if (getShipBoardComponent(coordinates) != null) {
                            fixedTilesNearBy[Direction.SOUTH.ordinal()] = getShipBoardComponent(coordinates);
                        } else {
                            fixedTilesNearBy[Direction.SOUTH.ordinal()] = null;
                        }
                    } else {
                        fixedTilesNearBy[Direction.SOUTH.ordinal()] = null;
                    }
                    coordinates.setRow(i);

                    // West Direction
                    if (j - 1 >= 0) {
                        coordinates.setCol(j - 1);
                        if (getShipBoardComponent(coordinates) != null) {
                            fixedTilesNearBy[Direction.WEST.ordinal()] = getShipBoardComponent(coordinates);
                        } else {
                            fixedTilesNearBy[Direction.WEST.ordinal()] = null;
                        }
                    } else {
                        fixedTilesNearBy[Direction.WEST.ordinal()] = null;
                    }
                    coordinates.setCol(j);
                    // Pass the prepared array of fixedTiles and receive a boolean array
                    tileResponse = getShipBoardComponent(coordinates).checkNearby(fixedTilesNearBy);

                    // Traverse the boolean array
                    for (k = 0; k < 4; k++) {

                        // The connection at position k in the array is not valid | Order: [North, East, South, West]
                        if (!tileResponse[k]) {

                            // Create an array to store the pairs that are not valid
                            ArrayList<Coordinates> coordinatesList = new ArrayList<>();

                            switch (k) {

                                case 0: // The connection to the north is not valid
                                    coordinates.setRow(i - 1);
                                    coordinatesList.add(new Coordinates(coordinates.getRow(), coordinates.getCol()));
                                    coordinates.setRow(i);
                                    coordinatesList.add(new Coordinates(coordinates.getRow(), coordinates.getCol()));
                                    mainList.add(coordinatesList);
                                    break;
                                case 1: // The connection to the east is not valid
                                    coordinates.setCol(j + 1);
                                    coordinatesList.add(new Coordinates(coordinates.getRow(), coordinates.getCol()));
                                    coordinates.setCol(j);
                                    coordinatesList.add(new Coordinates(coordinates.getRow(), coordinates.getCol()));
                                    mainList.add(coordinatesList);
                                    break;
                                case 2: // The connection to the south is not valid
                                    coordinates.setRow(i + 1);
                                    coordinatesList.add(new Coordinates(coordinates.getRow(), coordinates.getCol()));
                                    coordinates.setRow(i);
                                    coordinatesList.add(new Coordinates(coordinates.getRow(), coordinates.getCol()));
                                    mainList.add(coordinatesList);
                                    break;
                                case 3: // The connection to the west is not valid
                                    coordinates.setCol(j - 1);
                                    coordinatesList.add(new Coordinates(coordinates.getRow(), coordinates.getCol()));
                                    coordinates.setCol(j);
                                    coordinatesList.add(new Coordinates(coordinates.getRow(), coordinates.getCol()));
                                    mainList.add(coordinatesList);
                                    break;
                            }
                        }
                    }
                }
            }
        }

        //Controllo spazio tra cannoni e pistole:
        boolean ctrl = false;
        Coordinates tmp = new Coordinates(0,0);
        ArrayList<Coordinates> coordinatesList2;


        for(Coordinates c : shipBoardMatrix.getEnginesPosition().get("singleEngines")){
            tmp.setCol(c.getCol());
            tmp.setRow(c.getRow()+1);
            if(tmp.getRow()<5 && getShipBoardComponent(tmp) != null){
                coordinatesList2 = new ArrayList<>();
                coordinatesList2.add(new Coordinates(c.getRow(), c.getCol()));
                coordinatesList2.add(new Coordinates(tmp.getRow(), tmp.getCol()));
                mainList.add(coordinatesList2);
            }
        }

        for(Coordinates c : shipBoardMatrix.getEnginesPosition().get("doubleEngines")){
            tmp.setCol(c.getCol());
            tmp.setRow(c.getRow()+1);
            if(tmp.getRow()<5 && getShipBoardComponent(tmp) != null){
                coordinatesList2 = new ArrayList<>();
                coordinatesList2.add(new Coordinates(c.getRow(), c.getCol()));
                coordinatesList2.add(new Coordinates(tmp.getRow(), tmp.getCol()));
                mainList.add(coordinatesList2);
            }
        }

        for(Coordinates c : shipBoardMatrix.getCannonsPosition().get("singleCannons")){
            if(getShipBoardComponent(c).getDirection().equals(Direction.NORTH)){
                tmp.setCol(c.getCol());
                tmp.setRow(c.getRow()-1);
                if(tmp.getRow()>=0 && getShipBoardComponent(tmp) != null){
                    ctrl = true;
                }
            }
            else if(getShipBoardComponent(c).getDirection().equals(Direction.EAST)){
                tmp.setCol(c.getCol()+1);
                tmp.setRow(c.getRow());
                if(tmp.getCol()<7 && getShipBoardComponent(tmp) != null){
                    ctrl = true;
                }
            }
            else if(getShipBoardComponent(c).getDirection().equals(Direction.SOUTH)){
                tmp.setCol(c.getCol());
                tmp.setRow(c.getRow()+1);
                if(tmp.getRow()<5 && getShipBoardComponent(tmp) != null){
                    ctrl = true;
                }
            }
            else{
                tmp.setCol(c.getCol()-1);
                tmp.setRow(c.getRow());
                if(tmp.getCol()>=0 && getShipBoardComponent(tmp) != null){
                    ctrl = true;
                }
            }

            if(ctrl){
                ctrl = false;
                ArrayList<Coordinates> coordinatesList3 = new ArrayList<>();
                coordinatesList3.add(new Coordinates(c.getRow(), c.getCol()));
                coordinatesList3.add(new Coordinates(tmp.getRow(), tmp.getCol()));
                mainList.add(coordinatesList3);
            }
        }

        for(Coordinates c : shipBoardMatrix.getCannonsPosition().get("doubleCannons")){
            if(getShipBoardComponent(c).getDirection().equals(Direction.NORTH)){
                tmp.setCol(c.getCol());
                tmp.setRow(c.getRow()-1);
                if(tmp.getRow()>=0 && getShipBoardComponent(tmp) != null){
                    ctrl = true;
                }
            }
            else if(getShipBoardComponent(c).getDirection().equals(Direction.EAST)){
                tmp.setCol(c.getCol()+1);
                tmp.setRow(c.getRow());
                if(tmp.getCol()<7 && getShipBoardComponent(tmp) != null){
                    ctrl = true;
                }
            }
            else if(getShipBoardComponent(c).getDirection().equals(Direction.SOUTH)){
                tmp.setCol(c.getCol());
                tmp.setRow(c.getRow()+1);
                if(tmp.getRow()<5 && getShipBoardComponent(tmp) != null){
                    ctrl = true;
                }
            }
            else{
                tmp.setCol(c.getCol()-1);
                tmp.setRow(c.getRow());
                if(tmp.getCol()>=0 && getShipBoardComponent(tmp) != null){
                    ctrl = true;
                }
            }

            if(ctrl){
                ctrl = false;
                ArrayList<Coordinates> coordinatesList4 = new ArrayList<>();
                coordinatesList4.add(new Coordinates(c.getRow(), c.getCol()));
                coordinatesList4.add(new Coordinates(tmp.getRow(), tmp.getCol()));
                mainList.add(coordinatesList4);
            }
        }

        for (i=0; i<mainList.size(); i++){
            for (j=i+1; j<mainList.size(); j++){
                if(mainList.get(i).get(0).equals(mainList.get(j).get(0)) && mainList.get(i).get(1).equals(mainList.get(j).get(1)) ||
                    mainList.get(i).get(0).equals(mainList.get(j).get(1)) && mainList.get(i).get(1).equals(mainList.get(j).get(0)) ){
                    mainList.remove(j);
                    j--;
                }
            }
        }



        return mainList;
    }

    /**
     * Searches for an indirect path connecting two adjacent tiles. If found, returns true.
     *
     * @param current the current tile coordinates.
     * @param previous the previous tile coordinates.
     * @param goalTile the goal tile coordinates.
     * @return true if a path is found, false otherwise.
     */
    public boolean findPath(Coordinates current, Coordinates previous, Coordinates goalTile) {

        // If the current cell is null, the method must terminate
        if (current == null) {
            return false;
        }

        // If the goal tile is found, return true
        if (current.getRow() == goalTile.getRow() && current.getCol() == goalTile.getCol()) {
            return true;
        }

        // Prepare data and structures for processing
        ArrayList<Coordinates> nextTiles = new ArrayList<>();
        Coordinates tmp = new Coordinates(current.getRow(), current.getCol());
        int[] pathDirections = new int[3];

        // Save the next directions to expand (all except the one from which it arrives)
        if (current.getRow() == previous.getRow()) {
            if (current.getCol() > previous.getCol()) {
                pathDirections[0] = 0;
                pathDirections[1] = 1;
                pathDirections[2] = 2;
            } else if (current.getCol() < previous.getCol()) {
                pathDirections[0] = 0;
                pathDirections[1] = 2;
                pathDirections[2] = 3;
            }
        } else {
            if (current.getRow() > previous.getRow()) {
                pathDirections[0] = 1;
                pathDirections[1] = 2;
                pathDirections[2] = 3;
            } else {
                pathDirections[0] = 0;
                pathDirections[1] = 1;
                pathDirections[2] = 3;
            }
        }

        // Create attributes to pass to the functions called at the end
        for (int direction : pathDirections) {
            switch (direction) {
                case 0: // Expand to the north
                    if (current.getRow() - 1 >= 0) {
                        tmp.setCol(current.getCol());
                        tmp.setRow(current.getRow() - 1);
                        if (getShipBoardComponent(current).isConnectedWith(getShipBoardComponent(tmp), Direction.NORTH))
                            nextTiles.add(new Coordinates(tmp.getRow(), tmp.getCol()));
                        else
                            nextTiles.add(null);
                    } else {
                        nextTiles.add(null);
                    }
                    break;

                case 1: // Expand to the east
                    if (current.getCol() + 1 < 7) {
                        tmp.setCol(current.getCol() + 1);
                        tmp.setRow(current.getRow());
                        if (getShipBoardComponent(current).isConnectedWith(getShipBoardComponent(tmp), Direction.EAST))
                            nextTiles.add(new Coordinates(tmp.getRow(), tmp.getCol()));
                        else
                            nextTiles.add(null);
                    } else {
                        nextTiles.add(null);
                    }
                    break;

                case 2: // Expand to the south
                    if (current.getRow() + 1 < 5) {
                        tmp.setCol(current.getCol());
                        tmp.setRow(tmp.getRow() + 1);
                        if (getShipBoardComponent(current).isConnectedWith(getShipBoardComponent(tmp), Direction.SOUTH))
                            nextTiles.add(new Coordinates(tmp.getRow(), tmp.getCol()));
                        else
                            nextTiles.add(null);
                    } else {
                        nextTiles.add(null);
                    }
                    break;

                case 3: // Expand to the west
                    if (current.getCol() - 1 >= 0) {
                        tmp.setCol(current.getCol() - 1);
                        tmp.setRow(current.getRow());
                        if (getShipBoardComponent(current).isConnectedWith(getShipBoardComponent(tmp), Direction.WEST))
                            nextTiles.add(new Coordinates(tmp.getRow(), tmp.getCol()));
                        else
                            nextTiles.add(null);
                    } else {
                        nextTiles.add(null);
                    }
                    break;
            }
        }
        // Call new functions with all branches (one for each direction to expand)
        return findPath(nextTiles.get(0), current, goalTile) || findPath(nextTiles.get(1), current, goalTile) || findPath(nextTiles.get(2), current, goalTile);
    }

    /**
     * Places an alien at the specified coordinates. It sets the coordinates in the ShipBoard attribute
     * and also updates the cabin (actual tile) by calling a function from MatrixDataStructure.
     *
     * @param coordinates the coordinates where the alien will be placed.
     */
    public void placeAlienAt(Coordinates coordinates){
        ArrayList<Coordinates> aliensPositions = shipBoardMatrix.getAliensLifeSupportPlace();
        boolean found = false;

        // Check that the passed coordinates are usable for an alien and at the same time
        // determine the color of the alien
        for(Coordinates c : aliensPositions){
            if(c.getRow() == coordinates.getRow() && c.getCol() == coordinates.getCol() + 1 ||
                    c.getRow() == coordinates.getRow() && c.getCol() == coordinates.getCol() - 1 ||
                    c.getRow() == coordinates.getRow() + 1 && c.getCol() == coordinates.getCol() ||
                    c.getRow() == coordinates.getRow() - 1 && c.getCol() == coordinates.getCol()){
                if(getShipBoardComponent(c).getComponentTile().isBrown()){
                    brownAlien = coordinates;
                    found = true;
                    break;
                }else{
                    purpleAlien = coordinates;
                    found = true;
                    break;
                }
            }
        }
        if(found){
            shipBoardMatrix.replaceCrewWithAlienAt(coordinates);
            //totalCrew-=2;
        }
        //else
        //exception
    }

    /**
     * Calculates the total crew on the shipboard by summing up the crew values from the matrix cache.
     * This method should be called after the alien placement procedure.
     */
    public int calculateTotalCrew(){
        for(Integer number : shipBoardMatrix.getTilesContainCrew().values()){
            this.totalCrew += number;
        }
        return this.totalCrew;
    }

    /**
     * Calculates the starting total batteries on the shipboard by summing up the battery values
     * from the matrix cache.
     */
    public int calculateStartingTotalBatteries(){
        for(Integer value : shipBoardMatrix.getTilesContainBatteries().values()){
            this.totalBatteries += value;
        }
        return this.totalBatteries;
    }

    /**
     * Calculates the total cargo value on the shipboard by summing up the cargo values
     * from the matrix cache.
     */
    public void calculateTotalCargoValue(){
        this.totalCargoValue = 0;
        for(Integer value : shipBoardMatrix.getTilesContainCargo().values()){
            this.totalCargoValue += value;
        }
    }

    /**
     * Adds a ComponentTile to the list of components booked by the user on their shipboard.
     * A maximum of 2 components can be booked.
     *
     * @param tile the component tile to be booked.
     */
    public void addBookedComponent(ComponentTile tile){
        if(bookedComponentTiles.size()<=2 && tile!=null){
            bookedComponentTiles.add(tile);
        }else{
            //exception
            return;
        }
    }

    /**
     * Removes a component from the list of booked components (the user wants to use it).
     *
     * @param fileName the component tile to be removed from the booked list.
     * @return true if the tile was booked else false
     */
    public ComponentTile useBookedComponent(String fileName){
        for(ComponentTile ct : bookedComponentTiles){
            if(fileName.equals(ct.getFileName())){
                bookedComponentTiles.remove(ct);
                return ct;
            }
        }
        return null;
    }

    /**
     * Counts the number of exposed connectors on the shipboard.
     * It checks all tiles on the "border of the ship" placed in the matrix and counts the exposed connectors
     * in all directions.
     *
     * @return the number of exposed connectors.
     */
    public int countExposedConnectors(){

        // Prepare structures for the return and support variables for processing
        int counter=0;
        Coordinates coordinates = new Coordinates(0,0);
        Coordinates tmpCoords = new Coordinates(0,0);
        FixedComponentTile fixTmpComponent;

        // Among all the tiles "on the edge of the ship" placed in the matrix, check those that have exposed connectors in all their directions
        for(int i = 0; i<5;i++){
            for(int j = 0; j<7; j++) {
                coordinates.setRow(i);
                coordinates.setCol(j);
                if (this.getShipBoardComponent(coordinates) != null) {
                    //NORTH
                    tmpCoords.setCol(coordinates.getCol());
                    tmpCoords.setRow(coordinates.getRow() - 1);

                    // If there is nothing to its north
                    if (tmpCoords.getRow() < 0 || getShipBoardComponent(tmpCoords) == null) {
                        fixTmpComponent = getShipBoardComponent(coordinates);
                        if (!(fixTmpComponent.getComponentTile().getConnectorList()[Direction.NORTH.ordinal()] == null) &&
                                fixTmpComponent.getComponentTile().getConnectorList()[Direction.NORTH.ordinal()].getNumberOfConnections() != 0) {
                            counter++;
                        }
                    }

                    //EAST
                    tmpCoords.setCol(coordinates.getCol() + 1);
                    tmpCoords.setRow(coordinates.getRow());

                    // If there is nothing to its east
                    if (tmpCoords.getCol() >= 7 || getShipBoardComponent(tmpCoords) == null) {
                        fixTmpComponent = getShipBoardComponent(coordinates);
                        if (!(fixTmpComponent.getComponentTile().getConnectorList()[Direction.EAST.ordinal()] == null) &&
                                fixTmpComponent.getComponentTile().getConnectorList()[Direction.EAST.ordinal()].getNumberOfConnections() != 0) {
                            counter++;
                        }
                    }

                    //SOUTH
                    tmpCoords.setCol(coordinates.getCol());
                    tmpCoords.setRow(coordinates.getRow() + 1);

                    // If there is nothing to its south
                    if (tmpCoords.getRow() >= 5 || getShipBoardComponent(tmpCoords) == null) {
                        fixTmpComponent = getShipBoardComponent(coordinates);
                        if (!(fixTmpComponent.getComponentTile().getConnectorList()[Direction.SOUTH.ordinal()] == null) &&
                                fixTmpComponent.getComponentTile().getConnectorList()[Direction.SOUTH.ordinal()].getNumberOfConnections() != 0) {
                            counter++;
                        }
                    }

                    //WEST
                    tmpCoords.setCol(coordinates.getCol() - 1);
                    tmpCoords.setRow(coordinates.getRow());

                    // If there is nothing to its west
                    if (tmpCoords.getCol() < 0 || getShipBoardComponent(tmpCoords) == null) {
                        fixTmpComponent = getShipBoardComponent(coordinates);
                        if (!(fixTmpComponent.getComponentTile().getConnectorList()[Direction.WEST.ordinal()] == null) &&
                                fixTmpComponent.getComponentTile().getConnectorList()[Direction.WEST.ordinal()].getNumberOfConnections() != 0) {
                            counter++;
                        }
                    }
                }
            }
        }

        return counter;
    }


    public ArrayList<ArrayList<Coordinates>> findConnectedGroups() {

        final int ROWS = 5, COLS = 7;        // evita “numeri magici”
        boolean[][] visited = new boolean[ROWS][COLS];
        ArrayList<ArrayList<Coordinates>> groups = new ArrayList<>();

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {

                Coordinates start = new Coordinates(r, c);

                // ①  partiamo solo da celle occupate non ancora viste
                if (getShipBoardComponent(start) != null && !visited[r][c]) {

                    ArrayList<Coordinates> group = new ArrayList<>();
                    Deque<Coordinates> stack   = new ArrayDeque<>();
                    stack.push(start);
                    visited[r][c] = true;

                    while (!stack.isEmpty()) {
                        Coordinates cur = stack.pop();
                        group.add(cur);

                        for (int nr = 0; nr < ROWS; nr++) {
                            for (int nc = 0; nc < COLS; nc++) {

                                if (!visited[nr][nc]) {
                                    Coordinates next = new Coordinates(nr, nc);

                                    if (getShipBoardComponent(next) != null){

                                        Direction d = Direction.NORTH;
                                        Boolean ctrl = true;
                                        if(next.getCol()==cur.getCol() && next.getRow()==cur.getRow()+1){
                                            d = Direction.SOUTH;
                                        }
                                        else if(next.getCol()==cur.getCol() +1 && next.getRow()==cur.getRow()){
                                            d = Direction.EAST;
                                        }
                                        else if(next.getCol()==cur.getCol() -1 && next.getRow()==cur.getRow()){
                                            d = Direction.WEST;
                                        }
                                        else if(next.getCol()==cur.getCol() && next.getRow()==cur.getRow()-1){
                                            d = Direction.NORTH;
                                        }else{
                                            ctrl=false;
                                        }
                                        System.out.println("aaaaaaaaaaaaaaaaaaaaaaaa");
                                        System.out.println(d.ordinal());
                                        System.out.println(cur.toString());
                                        System.out.println(next.toString());
                                        System.out.println(ctrl);
                                        System.out.println(getShipBoardComponent(cur).isConnectedWith(getShipBoardComponent(next),d));
                                        System.out.println("aaaaaaaaaaaaaaaaaaaaaaaa");
                                        if(ctrl && getShipBoardComponent(cur).isConnectedWith(getShipBoardComponent(next),d))
                                        {

                                            visited[nr][nc] = true;
                                            stack.push(next);
                                        }
                                    }else{
                                        visited[nr][nc] = true;
                                    }
                                }
                            }
                        }
                    }
                    groups.add(group);

                    System.out.println("gruppo completo\n");// gruppo completato
                    for(Coordinates c2: group){
                        System.out.println(c2.toString());

                    }
                    System.out.println("\n");
                }
            }
        }
        return groups;
    }


//-------------------------------------------------------------------------------------------------------------------------------------------------------
//REMOVE

    /**
     * Removes (sets to null) the cell in the matrix at the given coordinates
     * (calls a function from MatrixDataStructure).
     *
     * @param coordinates the coordinates of the cell to remove.
     */
    public void removeComponentTileAt(Coordinates coordinates) {
        shipBoardMatrix.removeTile(coordinates);
    }

    /**
     * Given two coordinates: one to keep and one to remove, checks if there are branches
     * connected to the cell. If so, it removes the entire branch; otherwise, it removes
     * only the specified cell.
     *
     * @param coordinatesToRemove the coordinates of the cell to remove.
     * @param coordinatesToKeep the coordinates of the cell to keep that is adiacent to the cell to remove.
     */
    public void removeTileAndCheckCorrectPositions(Coordinates coordinatesToRemove, Coordinates coordinatesToKeep) {

        boolean ctrl = findPath(coordinatesToRemove, coordinatesToKeep, coordinatesToKeep);

        if(ctrl){
            removeComponentTileAt(coordinatesToRemove);
        }else{
            delBranch(coordinatesToRemove, coordinatesToKeep);
        }

    }

    /**
     * Recursive function that, given the starting coordinates, removes the entire branch
     * (the first "previous" passed is not removed).
     *
     * @param current the current coordinates being processed.
     * @param previous the previous coordinates to avoid removing.
     */

    public void delBranch(Coordinates current, Coordinates previous) {

        // If current is null, the method must terminate
        if (current == null) {
            removeComponentTileAt(previous);
            System.out.println("rimuovo "+ previous);
            return;
        }

        // Prepare data and structures for processing
        ArrayList<Coordinates> nextTiles = new ArrayList<>();
        Coordinates tmp = new Coordinates(current.getRow(), current.getCol());
        int[] pathDirections = new int[3];

        // Save the next directions to expand (all except the one from which it arrives)
        if (current.getRow() == previous.getRow()) {
            if (current.getCol() > previous.getCol()) {
                //System.out.println("tutte tranne ovest");
                pathDirections[0] = 0;
                pathDirections[1] = 1;
                pathDirections[2] = 2;
            } else if (current.getCol() < previous.getCol()) {
                //System.out.println("tutte tranne est");
                pathDirections[0] = 0;
                pathDirections[1] = 2;
                pathDirections[2] = 3;
            }
        } else {
            if (current.getRow() > previous.getRow()) {
                //System.out.println("tutte tranne nord");
                pathDirections[0] = 1;
                pathDirections[1] = 2;
                pathDirections[2] = 3;
            } else {
                //System.out.println("tutte tranne sud");
                pathDirections[0] = 0;
                pathDirections[1] = 1;
                pathDirections[2] = 3;
            }
        }

        // Create attributes to pass to the functions called at the end
        for (int direction : pathDirections) {
            switch (direction) {
                case 0: // Expand to the north
                    //System.out.println("provo a nord");
                    if (current.getRow() - 1 >= 0) {
                        tmp.setRow(current.getRow() - 1);
                        tmp.setCol(current.getCol());
                        //System.out.println("dentro");
                        if (getShipBoardComponent(current)!=null && getShipBoardComponent(current).isConnectedWith(getShipBoardComponent(tmp), Direction.NORTH)){
                            nextTiles.add(new Coordinates(tmp.getRow(), tmp.getCol()));
                            //System.out.println("aggiunto");
                        }
                        else{
                            nextTiles.add(null);
                        }
                    } else {
                        nextTiles.add(null);
                    }
                    break;

                case 1: // Expand to the east
                    //System.out.println("provo a est");
                    if (current.getCol() + 1 < 7) {
                        tmp.setCol(current.getCol() + 1);
                        tmp.setRow(current.getRow());
                        if (getShipBoardComponent(current)!=null && getShipBoardComponent(current).isConnectedWith(getShipBoardComponent(tmp), Direction.EAST)) {
                            nextTiles.add(new Coordinates(tmp.getRow(), tmp.getCol()));
                        }else {
                            nextTiles.add(null);
                        }
                    } else {
                        nextTiles.add(null);
                    }
                    break;

                case 2: // Expand to the south
                    //System.out.println("provo a sud");
                    if (current.getRow() + 1 < 5) {
                        tmp.setRow(current.getRow() + 1);
                        tmp.setCol(current.getCol());
                        if (getShipBoardComponent(current)!=null && getShipBoardComponent(current).isConnectedWith(getShipBoardComponent(tmp), Direction.SOUTH)) {
                            nextTiles.add(new Coordinates(tmp.getRow(), tmp.getCol()));
                        }else{
                            nextTiles.add(null);
                        }
                    } else {
                        nextTiles.add(null);
                    }
                    break;

                case 3: // Expand to the west
                    //System.out.println("provo a ovest");
                    if (current.getCol() - 1 >= 0) {
                        tmp.setCol(current.getCol() - 1);
                        tmp.setRow(current.getRow());
                        if (getShipBoardComponent(current)!=null && getShipBoardComponent(current).isConnectedWith(getShipBoardComponent(tmp), Direction.WEST)) {
                            nextTiles.add(new Coordinates(tmp.getRow(), tmp.getCol()));
                        }else{
                            nextTiles.add(null);
                        }
                    } else {
                        nextTiles.add(null);
                    }
                    break;
            }
        }

        // If there is nothing nearby, remove the cell
        if((nextTiles.get(0) == null && nextTiles.get(1) == null && nextTiles.get(2) == null)) {
            //System.out.println("what?");
            removeComponentTileAt(current);
            System.out.println("elimino "+current);
        }else {
            //System.out.println("richiamo");
            // Call new functions for all branches
            delBranch(nextTiles.get(0), current);
            delBranch(nextTiles.get(1), current);
            delBranch(nextTiles.get(2), current);
            System.out.println("elimino "+ current);
            removeComponentTileAt(current);
        }
    }

//-------------------------------------------------------------------------------------------------------------------------------------------------------
//NOTE
    //TODO non teniamo mai traccia del fatto che in base al livello del gioco la nave può avere una forma base diversa

//-------------------------------------------------------------------------------------------------------------------------------------------------------
// metodi aggiunti da Ste Hong


}

