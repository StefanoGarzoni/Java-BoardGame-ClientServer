package it.polimi.ingsw.client.TUI.components.drawable;

import it.polimi.ingsw.client.TUI.AnsiColors;
import it.polimi.ingsw.client.TUI.TerminalCell;
import it.polimi.ingsw.client.TUI.UnicodeCharacters;
import it.polimi.ingsw.client.TUI.components.Drawable;
import it.polimi.ingsw.model.Cargo.CargoType;
import it.polimi.ingsw.model.ComponentTile.*;
import it.polimi.ingsw.model.Direction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class DrawableTile implements Drawable {
    public final static int COL_COUNT = 11;
    public final static int ROW_COUNT = 5;
    private AnsiColors backgroundColor;
    private TerminalCell[][] tileSprite;
    private final ArrayList<Direction> directions = new ArrayList<>(Arrays.asList(Direction.values()));

    /**
     * Constructor
     * @param tile
     */
    public DrawableTile(ComponentTile tile, AnsiColors background) {
        tileSprite = new TerminalCell[ROW_COUNT][COL_COUNT];

        for (int x = 0; x < ROW_COUNT; x++) {
            for (int y = 0; y < COL_COUNT; y++) {
                tileSprite[x][y] = new TerminalCell(background, AnsiColors.BLACK, " ");
            }
        }

        this.backgroundColor = background;
        chooseTile(tile);

    }

    public DrawableTile(FixedComponentTile fixedTile, AnsiColors background) {
        int currentRotation = fixedTile.getDirection().ordinal();

        tileSprite = new TerminalCell[ROW_COUNT][COL_COUNT];

        for (int x = 0; x < ROW_COUNT; x++) {
            for (int y = 0; y < COL_COUNT; y++) {
                tileSprite[x][y] = new TerminalCell(background, AnsiColors.BLACK, " ");
            }
        }

        this.backgroundColor = background;
        rotateTile(currentRotation);
        chooseTile(fixedTile.getComponentTile());
    }

    /**
     * We rotate the tile by rotating the connectors array {@code directions}
     * @param currentRotation the ordinal of the direction where the defaults nord points
     */
    private void rotateTile(int currentRotation) {
        Collections.rotate(directions, currentRotation);
    }

    /*---------*
     | Generic |
     *---------*/

    //TODO change in favour of drawutils
    private void drawTileCorners(){
        tileSprite[0][0] = new TerminalCell(backgroundColor,AnsiColors.WHITE,UnicodeCharacters.TOP_LEFT_CORNER_BOLD);
        tileSprite[ROW_COUNT-1][0] = new TerminalCell(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.BOTTOM_LEFT_CORNER_BOLD);
        tileSprite[0][COL_COUNT-1] = new TerminalCell(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.TOP_RIGHT_CORNER_BOLD);
        tileSprite[ROW_COUNT-1][COL_COUNT-1] = new TerminalCell(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.BOTTOM_RIGHT_CORNER_BOLD);
    }

    private void drawFlatTileBorder(Direction dir){
        int row;
        int col;
        switch (dir){
            case NORTH:
               row = 0;
               for(col = 1; col < COL_COUNT-1; col++){
                   tileSprite[row][col].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.HORIZONTAL_LINE_BOLD);
               }
               break;
               case EAST:
                col = COL_COUNT - 1;
                for(row = 1; row < ROW_COUNT-1; row++){
                    tileSprite[row][col].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.VERTICAL_LINE_BOLD);
                }
                break;
            case WEST:
                col = 0;
                for(row = 1; row < ROW_COUNT-1; row++){
                    tileSprite[row][col].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.VERTICAL_LINE_BOLD);
                }
                break;
            case SOUTH:
                row = ROW_COUNT - 1;
                for(col = 1; col < COL_COUNT-1; col++){
                    tileSprite[row][col].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.HORIZONTAL_LINE_BOLD);
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid direction");
        }
    }

    private void drawSingleConnector(Direction dir){
        int HALF_COL = (COL_COUNT)/2;
        int HALF_ROW = (ROW_COUNT)/2;

        switch (dir){
            case NORTH:
                tileSprite[0][HALF_COL].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.NORTH_CONNECTOR_BOLD);
                break;
            case EAST:
                tileSprite[HALF_ROW][COL_COUNT - 1].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.EAST_CONNECTOR_BOLD);
                break;
            case WEST:
                tileSprite[HALF_ROW][0].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.WEST_CONNECTOR_BOLD);
                break;
            case SOUTH:
                tileSprite[ROW_COUNT - 1][HALF_COL].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.SOUTH_CONNECTOR_BOLD);
                break;
            default:
                throw new IllegalArgumentException("Invalid direction");
        }
    }

    private void drawDoubleConnectors(Direction dir){
        int HALF_COL = (COL_COUNT)/2;
        int HALF_ROW = (ROW_COUNT)/2;

        switch (dir){
            case NORTH:
                tileSprite[0][HALF_COL - 1].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.NORTH_CONNECTOR_BOLD);
                tileSprite[0][HALF_COL + 1].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.NORTH_CONNECTOR_BOLD);
                break;
            case EAST:
                tileSprite[HALF_ROW - 1][COL_COUNT - 1].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.EAST_CONNECTOR_BOLD);
                tileSprite[HALF_ROW + 1][COL_COUNT - 1].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.EAST_CONNECTOR_BOLD);
                break;
            case WEST:
                tileSprite[HALF_ROW - 1][0].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.WEST_CONNECTOR_BOLD);
                tileSprite[HALF_ROW + 1][0].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.WEST_CONNECTOR_BOLD);
                break;
            case SOUTH:
                tileSprite[ROW_COUNT - 1][HALF_COL - 1].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.SOUTH_CONNECTOR_BOLD);
                tileSprite[ROW_COUNT - 1][HALF_COL + 1].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.SOUTH_CONNECTOR_BOLD);
        }
    }

    private void drawUniversalConnectors(Direction dir){
        int HALF_COL = (COL_COUNT)/2;
        int HALF_ROW = (ROW_COUNT)/2;

        switch (dir){
            case NORTH:
                for(int i = -1; i <= 1; i++){ tileSprite[0][HALF_COL + i].setPixel(AnsiColors.BLACK, AnsiColors.WHITE, UnicodeCharacters.NORTH_CONNECTOR_BOLD); }
                break;
            case EAST:
                for(int i = -1; i <= 1; i++){ tileSprite[HALF_ROW + i][COL_COUNT - 1].setPixel(AnsiColors.BLACK, AnsiColors.WHITE, UnicodeCharacters.EAST_CONNECTOR_BOLD);}
                break;
            case WEST:
                for(int i = -1; i <= 1; i++){ tileSprite[HALF_ROW + i][0].setPixel(AnsiColors.BLACK, AnsiColors.WHITE, UnicodeCharacters.WEST_CONNECTOR_BOLD); }
                break;
            case SOUTH:
                for(int i = -1; i <= 1; i++){ tileSprite[ROW_COUNT - 1][HALF_COL + i].setPixel(AnsiColors.BLACK, AnsiColors.WHITE, UnicodeCharacters.SOUTH_CONNECTOR_BOLD); }
                break;
            default:
                throw new IllegalArgumentException("Invalid direction");
        }
    }

    private void drawConnectionTileBorder(Direction dir, int connectorSize){
        int row;
        int col;
        drawFlatTileBorder(dir);
        switch (connectorSize){
            case 0:
                break;
            case 1:
                drawSingleConnector(dir);
                break;
            case 2:
                drawDoubleConnectors(dir);
                break;
            case 3:
                drawUniversalConnectors(dir);
                break;
            default:
                throw new IllegalArgumentException("Invalid connector size: " +  connectorSize);
        }
    }

    /*--------*
     | Cabins |
     *--------*/

    //TODO player color? Alien sprite? Refactoring
    private void drawCabin(CabinTile cabin){
        int xCabinOffset = 1;
        int yCabinOffset = 2;
        //Cabin corners
        tileSprite[xCabinOffset][yCabinOffset].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.TOP_LEFT_OCTANT);
        tileSprite[ROW_COUNT - 1 - xCabinOffset][yCabinOffset].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.BOTTOM_LEFT_OCTANT);
        tileSprite[xCabinOffset][COL_COUNT - 1 - yCabinOffset].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.TOP_RIGHT_OCTANT);
        tileSprite[ROW_COUNT - 1 - xCabinOffset][COL_COUNT - 1 - yCabinOffset].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.BOTTOM_RIGHT_OCTANT);

        for(int i = 3; i <= COL_COUNT - 3; i++){
            tileSprite[1][i].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.HORIZONTAL_TOP_OCTANT);
            tileSprite[3][i].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.HORIZONTAL_BOTTOM_OCTANT);
        }
        tileSprite[2][2].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.VERTICAL_LEFT_OCTANT);
        tileSprite[2][2].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.VERTICAL_LEFT_OCTANT);
        tileSprite[ROW_COUNT - 2][COL_COUNT - 2].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.BOTTOM_RIGHT_OCTANT);
        tileSprite[ROW_COUNT - 3][COL_COUNT - 2].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.VERTICAL_RIGHT_OCTANT);
        tileSprite[ROW_COUNT - 4][COL_COUNT - 2].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.VERTICAL_LEFT_OCTANT);

        switch(cabin.getAstronautsNumber()){
            case 0:
                break;
            case 1:
                tileSprite[2][6].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.STICK_MAN);
                break;
            case 2:
                tileSprite[2][5].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.STICK_MAN);
                tileSprite[2][7].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.STICK_MAN);
                break;
            case 3:
                for(int i = 5; i <= 7; i++){
                    tileSprite[2][i].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.STICK_MAN);
                }
                break;
        }
    }

    /*-------*
     | Cargo |
     *-------*/

    private void drawCargo(CargoTile cargoTile){
        int cargoInserted = 0;
        ArrayList<CargoType> cargos = cargoTile.getStoredCargos();
        AnsiColors ansiColors;

        if(cargoTile.isSpecial()) ansiColors = AnsiColors.CYAN;
        else ansiColors = AnsiColors.WHITE;

        for(int i = 1; i <= cargoTile.getCargoCapacity(); i++){
            tileSprite[i][5].setPixel(backgroundColor, ansiColors, UnicodeCharacters.CARGO);
        }

        for(CargoType cargo : cargos){
            switch (cargo.getColor()) {
                case "yellow":
                    tileSprite[cargoInserted + 1][5].setPixel(backgroundColor, AnsiColors.YELLOW, UnicodeCharacters.CARGO);
                    cargoInserted++;
                    break;
                case "red":
                    tileSprite[cargoInserted + 1][5].setPixel(backgroundColor, AnsiColors.RED, UnicodeCharacters.CARGO);
                    cargoInserted++;
                    break;
                case "blue":
                    tileSprite[cargoInserted + 1][5].setPixel(backgroundColor, AnsiColors.BLUE, UnicodeCharacters.CARGO);
                    cargoInserted++;
                    break;
                case "green":
                    tileSprite[cargoInserted + 1][5].setPixel(backgroundColor, AnsiColors.GREEN, UnicodeCharacters.CARGO);
                    cargoInserted++;
                    break;
            }
        }
    }

    /*--------*
     | Engine |
     *--------*/

    private void drawEngine(EngineTile engineTile){
        if(engineTile.isDoubleEngine()){
            tileSprite[2][3].setPixel(backgroundColor, AnsiColors.YELLOW, UnicodeCharacters.ENGINE);
            tileSprite[2][7].setPixel(backgroundColor, AnsiColors.YELLOW, UnicodeCharacters.ENGINE);
        }
        else{
            tileSprite[2][5].setPixel(backgroundColor, AnsiColors.YELLOW, UnicodeCharacters.ENGINE);
        }
    }

    /*---------*
     | Cannons |
     *---------*/

    private void drawCannons(CannonTile cannonTile){
        if(cannonTile.isDouble()){
            tileSprite[1][3].setPixel(backgroundColor, AnsiColors.MAGENTA, UnicodeCharacters.CANNON);
            tileSprite[1][7].setPixel(backgroundColor, AnsiColors.MAGENTA, UnicodeCharacters.CANNON);
        }
        else{
            tileSprite[1][5].setPixel(backgroundColor, AnsiColors.MAGENTA, UnicodeCharacters.CANNON);
        }
    }

    /*---------*
     | Shields |
     *---------*/

    private void drawNorthShield(){
        for(int i = 1; i <= COL_COUNT - 2; i++){
            tileSprite[1][i].setPixel(backgroundColor, AnsiColors.GREEN, UnicodeCharacters.HORIZONTAL_TOP_OCTANT);
        }
    }

    private void drawSouthShield(){
        for(int i = 1; i <= COL_COUNT - 2; i++){
            tileSprite[3][i].setPixel(backgroundColor, AnsiColors.GREEN, UnicodeCharacters.HORIZONTAL_BOTTOM_OCTANT);
        }
    }

    private void drawEastShield(){
        for(int i = 1; i <= ROW_COUNT - 2; i++){
            tileSprite[i][9].setPixel(backgroundColor, AnsiColors.GREEN, UnicodeCharacters.VERTICAL_RIGHT_OCTANT);
        }
    }

    private void drawWestShield(){
        for(int i = 1; i <= ROW_COUNT - 2; i++){
            tileSprite[i][1].setPixel(backgroundColor, AnsiColors.GREEN, UnicodeCharacters.VERTICAL_LEFT_OCTANT);
        }
    }

    private void drawShield(Shield shield){

        //FIXME the shield is always towards NW, we can remove the rest
        switch("NW"){
            case "NW":
                drawNorthShield();
                drawWestShield();
                tileSprite[1][1].setPixel(backgroundColor, AnsiColors.GREEN, UnicodeCharacters.TOP_LEFT_OCTANT);
                break;
            case "NE":
                drawNorthShield();
                drawEastShield();
                tileSprite[1][COL_COUNT - 2].setPixel(backgroundColor, AnsiColors.GREEN, UnicodeCharacters.TOP_RIGHT_OCTANT);
                break;
            case "SE":
                drawSouthShield();
                drawEastShield();
                tileSprite[ROW_COUNT - 2][COL_COUNT - 2].setPixel(backgroundColor, AnsiColors.GREEN, UnicodeCharacters.BOTTOM_RIGHT_OCTANT);
                break;
            case "SW":
                drawSouthShield();
                drawWestShield();
                tileSprite[ROW_COUNT - 2][1].setPixel(backgroundColor, AnsiColors.GREEN, UnicodeCharacters.BOTTOM_LEFT_OCTANT);
            default:
                throw new IllegalArgumentException("Invalid direction");
        }
    }

    /*-----------*
     | Batteries |
     *-----------*/
    private void drawBatteries(BatteriesTile batteries){
        final int firstPosition = 3;

        for(int i = 0; i < 2*batteries.getBatteryCapacity(); i+=2) {
            tileSprite[1][firstPosition + i].setPixel(AnsiColors.WHITE, AnsiColors.BLACK, UnicodeCharacters.BATTERY_PLUS);
            tileSprite[2][firstPosition + i].setPixel(AnsiColors.WHITE, AnsiColors.BLACK, UnicodeCharacters.DOUBLE_VERTICAL_LINE);
            tileSprite[3][firstPosition + i].setPixel(AnsiColors.WHITE, AnsiColors.BLACK, UnicodeCharacters.BATTERY_MINUS);
        }

        for(int i = 0; i < 2*batteries.getBatteries(); i+=2){
            tileSprite[1][firstPosition + i].setPixel(AnsiColors.GREEN, AnsiColors.BLACK, UnicodeCharacters.BATTERY_PLUS);
            tileSprite[2][firstPosition + i].setPixel(AnsiColors.GREEN, AnsiColors.BLACK, UnicodeCharacters.DOUBLE_VERTICAL_LINE);
            tileSprite[3][firstPosition + i].setPixel(AnsiColors.GREEN, AnsiColors.BLACK, UnicodeCharacters.BATTERY_MINUS);
        }

    }

    /*----------------*
     | Alien Supports |
     *----------------*/

    private void drawAlienSupport(AlienSupportSystemTile alienSupport){

        for(int i = 3; i <= COL_COUNT - 3; i++){
            tileSprite[1][i].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.HORIZONTAL_TOP_OCTANT);
            tileSprite[3][i].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.HORIZONTAL_BOTTOM_OCTANT);
        }
        tileSprite[2][3].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.VERTICAL_LEFT_OCTANT);
        tileSprite[2][8].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.VERTICAL_RIGHT_OCTANT);

        tileSprite[1][3].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.TOP_LEFT_OCTANT);
        tileSprite[1][7].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.TOP_RIGHT_OCTANT);
        tileSprite[3][3].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.BOTTOM_LEFT_OCTANT);
        tileSprite[3][7].setPixel(backgroundColor, AnsiColors.WHITE, UnicodeCharacters.BOTTOM_RIGHT_OCTANT);


        if(alienSupport.isBrownAlien()) {
            tileSprite[2][5].setPixel(backgroundColor, AnsiColors.GREEN, UnicodeCharacters.SUPPORT_DECO);
        }
        else{
            tileSprite[2][5].setPixel(backgroundColor, AnsiColors.MAGENTA, UnicodeCharacters.SUPPORT_DECO);
        }
    }

    private void drawGenericTile(ComponentTile genericTile){
        drawTileCorners();
        drawConnectionTileBorder(Direction.EAST,genericTile.getConnector(directions.get(1)).getNumberOfConnections());
        drawConnectionTileBorder(Direction.SOUTH,genericTile.getConnector(directions.get(2)).getNumberOfConnections());
        drawConnectionTileBorder(Direction.WEST,genericTile.getConnector(directions.get(3)).getNumberOfConnections());
        drawConnectionTileBorder(Direction.NORTH,genericTile.getConnector(directions.get(0)).getNumberOfConnections());

    }

    private void chooseTile(ComponentTile tile){
        switch (tile) {
            case CabinTile c -> drawCabin(c);
            case CargoTile c -> drawCargo(c);
            case EngineTile c -> drawEngine(c);
            case BatteriesTile c -> drawBatteries(c);
            case Shield c -> drawShield(c);
            case AlienSupportSystemTile c -> drawAlienSupport(c);
            case CannonTile c -> drawCannons(c);
            case StructuralTile c -> drawGenericTile(c);
            default -> throw new IllegalStateException("Unexpected value: " + tile);
        }
        drawGenericTile(tile);
    }


    /**
     * Generic tileSprite
     * @return the cell representation
     */
    @Override
    public TerminalCell[][] draw() {
        return tileSprite;
    }
}
