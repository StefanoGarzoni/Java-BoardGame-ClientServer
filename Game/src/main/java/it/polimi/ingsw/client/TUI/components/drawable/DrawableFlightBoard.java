package it.polimi.ingsw.client.TUI.components.drawable;

import it.polimi.ingsw.client.TUI.AnsiColors;
import it.polimi.ingsw.client.TUI.TerminalCell;
import it.polimi.ingsw.client.TUI.UnicodeCharacters;
import it.polimi.ingsw.client.TUI.components.Drawable;
import it.polimi.ingsw.client.TUI.components.drawable.util.BoxStyle;
import it.polimi.ingsw.client.TUI.components.drawable.util.DrawUtils;
import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.Direction;
import it.polimi.ingsw.model.clientModel.ViewFlightBoard;

import java.util.ArrayList;
import java.util.Map;

public class DrawableFlightBoard implements Drawable {
    TerminalCell[][] flightBoardSprite;
    public static int ROW_COUNT = 12;
    public static int COL_COUNT = 71;
    private int COL_FACTOR = 6;
    private int ROW_FACTOR = 3;
    private final int lengthSecondLevel = 24;
    private final int lengthFirstLevel = 0;
    ArrayList<Coordinates> positions = new ArrayList();

    public DrawableFlightBoard(ViewFlightBoard viewFlightBoard){
        flightBoardSprite = new TerminalCell[ROW_COUNT][COL_COUNT];

        for(int row=0;row<ROW_COUNT;row++){
            for(int col=0;col<COL_COUNT;col++){
                flightBoardSprite[row][col] = new TerminalCell();
            }
        }

        this.drawSecondLevelBoard(viewFlightBoard.getPlayerPositions());
    }

    private void setupSecondLevelBoard(){
        int rowLength = 4;
        int columnLength = 10;
        for(int topCounter = 0; topCounter < columnLength; topCounter++) positions.add(new Coordinates(0, topCounter));
        for(int rightCounter = 0; rightCounter < rowLength; rightCounter++) positions.add(new Coordinates(rightCounter, columnLength - 1));
        for(int bottomCounter = columnLength - 1; bottomCounter >= 0; bottomCounter--) positions.add(new Coordinates(rowLength - 1, bottomCounter));
        for(int leftCounter = rowLength - 1; leftCounter >= 0; leftCounter--) positions.add(new Coordinates(leftCounter, 0));
    }

    /**
     * Draw the frame of the position cell
     * @param connectionIn this is the input direction, since cells are ordered
     * @param connectionOut this is the output direction, since cells are ordered
     * @param x the row coordinate to start drawing
     * @param y the column coordinate to start drawing
     */
    private void drawPositionFrame(Direction connectionIn, Direction connectionOut, int x, int y){
        DrawUtils.drawBox(6, 3, flightBoardSprite, AnsiColors.WHITE, AnsiColors.BLACK, x, y, BoxStyle.THIN);
        switch (connectionOut){
            case NORTH:
                flightBoardSprite[x][y + 2].setPixel(AnsiColors.BLACK, AnsiColors.WHITE, UnicodeCharacters.NORTH_CONNECTOR_THIN);
                break;
            case WEST:
                flightBoardSprite[x+1][y].setPixel(AnsiColors.BLACK, AnsiColors.WHITE, UnicodeCharacters.WEST_CONNECTOR_THIN);
                break;
            case SOUTH:
                flightBoardSprite[x+2][y+2].setPixel(AnsiColors.BLACK, AnsiColors.WHITE, UnicodeCharacters.SOUTH_CONNECTOR_THIN);
                break;
            case EAST:
                flightBoardSprite[x+1][y+5].setPixel(AnsiColors.BLACK, AnsiColors.WHITE, UnicodeCharacters.EAST_CONNECTOR_THIN);
                break;
        }

        switch (connectionIn){
            case NORTH:
                flightBoardSprite[x][y + 2].setPixel(AnsiColors.BLACK, AnsiColors.WHITE, UnicodeCharacters.NORTH_ARROW);
                break;
            case WEST:
                flightBoardSprite[x+1][y].setPixel(AnsiColors.BLACK, AnsiColors.WHITE, UnicodeCharacters.WEST_ARROW);
                break;
            case SOUTH:
                flightBoardSprite[x+2][y+2].setPixel(AnsiColors.BLACK, AnsiColors.WHITE, UnicodeCharacters.SOUTH_ARROW);
                break;
            case EAST:
                flightBoardSprite[x+1][y+5].setPixel(AnsiColors.BLACK, AnsiColors.WHITE, UnicodeCharacters.EAST_ARROW);
                break;
        }
    }

    /**
     * Draw a position with a player on it
     * @param connectionIn this is the input direction
     * @param connectionOut this is the output direction
     * @param x the row coordinate to start drawing
     * @param y the column coordinate to start drawing
     * @param playerColor the color of the player to draw
     */
    private void drawPosition(Direction connectionIn, Direction connectionOut, int x, int y, Character playerColor){
        drawPositionFrame(connectionIn, connectionOut, x, y);
        AnsiColors color = AnsiColors.BLACK;

        switch (playerColor){
            case 'Y' -> color = AnsiColors.YELLOW;
            case 'R' -> color = AnsiColors.RED;
            case 'G' -> color = AnsiColors.GREEN;
            case 'B' -> color = AnsiColors.BLUE;
        }

        flightBoardSprite[x+1][y].setPixel(AnsiColors.BLACK, color, UnicodeCharacters.FULL_BLOCK);
        flightBoardSprite[x+1][y+1].setPixel(AnsiColors.BLACK, color, UnicodeCharacters.FULL_BLOCK);
        flightBoardSprite[x+1][y+2].setPixel(AnsiColors.BLACK, color, UnicodeCharacters.BLOCK_RIGHT_TRIANGLE);
    }

    /**
     * Draw an empty position
     * @param connectionIn input connection direction
     * @param connectionOut output connection direction
     * @param x the row coordinate to start drawing
     * @param y the column coordinate to start drawing
     */
    private void drawPosition(Direction connectionIn, Direction connectionOut, int x, int y){
        drawPositionFrame(connectionIn,connectionOut,x,y);
    }

    /**
     * Draw a position with a starting position index
     * @param connectionIn input connection direction
     * @param connectionOut output connection direction
     * @param x the row coordinate to start drawing
     * @param y the column coordinate to start drawing
     * @param startingIndex the positional index
     */
    private void drawPosition(Direction connectionIn, Direction connectionOut, int x, int y, int startingIndex){
        drawPositionFrame(connectionIn, connectionOut, x, y);
        flightBoardSprite[x + 1][y + 3].setPixel(AnsiColors.BLACK, AnsiColors.WHITE, String.valueOf(startingIndex));
    }

    /**
     * Converts a player position to the x,y coordinates of the corresponding flightboard cell
     * @param playerPosition the player position on the flightboard
     * @param gameLevel the game level (1 or 2)
     * @return the xy coordinates of the cell on the flightboard, they have to be multiplied by the x and y factors
     */
    private Coordinates convertPosition(int playerPosition, int gameLevel){
        int length = (gameLevel==2) ? 24 : 0;
        return positions.get(Math.floorMod(playerPosition, length));
    }

    /*
    *-*-*-*-*-*-*-*-*-*
    *                 *
    *                 *
    *-*-*-*-*-*-*-*-*-*
     */


    /**
     * draw a flightBoard of second level (24 cells)
     * @param playerLocations the locations of the players
     */
    private void drawSecondLevelBoard(Map<Character, Integer> playerLocations){
        int colIndex = 0;
        int colFactor = 6;
        int rowIndex = 0;
        int rowFactor = 3;

        setupSecondLevelBoard();

        //TOP-LEFT
        for(colIndex++; colIndex < 9; colIndex++){
            drawPosition(Direction.WEST,Direction.EAST,rowIndex * rowFactor, colIndex * colFactor);
        }
        //TOP-RIGHT
        drawPosition(Direction.WEST,Direction.SOUTH,rowIndex * rowFactor, colIndex * colFactor);
        for(rowIndex++; rowIndex < 3; rowIndex++){
            drawPosition(Direction.NORTH,Direction.SOUTH,rowIndex * rowFactor, colIndex * colFactor);
        }
        //BOTTOM-RIGHT
        drawPosition(Direction.NORTH,Direction.WEST,rowIndex * rowFactor, colIndex * colFactor);
        for(colIndex--; colIndex >0; colIndex--){
            drawPosition(Direction.EAST,Direction.WEST,rowIndex * rowFactor, colIndex * colFactor);
        }
        //BOTTOM-LEFT
        drawPosition(Direction.EAST, Direction.NORTH,rowIndex * rowFactor, colIndex * colFactor);
        for(rowIndex--; rowIndex > 0; rowIndex--){
            drawPosition(Direction.SOUTH,Direction.NORTH,rowIndex * rowFactor, colIndex * colFactor);
        }

        drawPosition(Direction.WEST,Direction.EAST,0 * rowFactor ,0 * colFactor, 4);
        drawPosition(Direction.WEST,Direction.EAST, 0 * rowFactor , 1 * colFactor , 3);
        drawPosition(Direction.WEST,Direction.EAST, 0 * rowFactor , 4 * colFactor , 2);
        drawPosition(Direction.WEST,Direction.EAST, 0 * rowFactor , 7 * colFactor , 1);


        for(Map.Entry<Character, Integer> entry : playerLocations.entrySet()){
            entry.getKey();
        }

        for(Map.Entry<Character, Integer> entry : playerLocations.entrySet()){
            Coordinates position = convertPosition(entry.getValue(), 2);
            //TODO check position for correct orientation?
            drawPosition(Direction.NORTH, Direction.SOUTH, position.getRow() * rowFactor, position.getCol() * colFactor, entry.getKey());

        }

        drawSecondLevelCreditPrizes();
    }

    private void drawSecondLevelCreditPrizes(){
        int row = ROW_FACTOR+1;
        int col = COL_FACTOR+5;
        //FIXME
        DrawUtils.drawString("CREDIT VALUES", flightBoardSprite, row - 1, col-1, AnsiColors.WHITE, AnsiColors.BLACK);
        DrawUtils.drawString("1°: 8 6 4 2", flightBoardSprite, row, col, AnsiColors.WHITE, AnsiColors.BLACK);
        DrawUtils.drawString("Best Ship: 4", flightBoardSprite, row+1, col, AnsiColors.WHITE, AnsiColors.BLACK);
        DrawUtils.drawString("Cargos: ", flightBoardSprite, row+2, col, AnsiColors.WHITE, AnsiColors.BLACK);
        DrawUtils.drawString("4", flightBoardSprite, row+2, col + 7, AnsiColors.BLACK, AnsiColors.RED);
        DrawUtils.drawString("3", flightBoardSprite, row+2, col + 9, AnsiColors.BLACK, AnsiColors.YELLOW);
        DrawUtils.drawString("2", flightBoardSprite, row+2, col + 11, AnsiColors.BLACK, AnsiColors.GREEN);
        DrawUtils.drawString("1", flightBoardSprite, row+2, col + 13, AnsiColors.BLACK, AnsiColors.BLUE);
        DrawUtils.drawString("Broken Tiles: -1", flightBoardSprite,row+3, col, AnsiColors.WHITE, AnsiColors.BLACK);
    }

    @Override
    public TerminalCell[][] draw() {
        return flightBoardSprite;
    }
}
