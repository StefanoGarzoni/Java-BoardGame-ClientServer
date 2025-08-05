package it.polimi.ingsw.client.TUI.components.drawable.util;

import it.polimi.ingsw.client.TUI.AnsiColors;
import it.polimi.ingsw.client.TUI.TerminalCell;
import it.polimi.ingsw.client.TUI.exception.UneditedBufferException;
import it.polimi.ingsw.model.Direction;

/**
 * This class offers static methods to draw common shapes or objects on a 2d <code>TerminalCell</code> matrix
 */
public final class DrawUtils {
    private DrawUtils(){}

    /**
     * Draws a string on the input buffer and sends it back
     * @param text the String to print
     * @param buffer the buffer to draw on
     * @param x the starting row
     * @param y the starting column
     * @param foreground foreground color
     * @param background background color
     */
    public static void drawString(String text, TerminalCell[][] buffer, int x, int y, AnsiColors foreground, AnsiColors background){

        //negative lookahead regex, we check that it's not the start of the string (Splitting would return an empty char)
        String[] textArray = text.split("(?!^)");

        for(String s : textArray){
            buffer[x][y].setPixel(background, foreground, s);
            y++;
        }
    }

    /**
     * Draws a string on the input buffer and sends it back with white foreground and black background
     * @param text the String to print
     * @param buffer the buffer to draw on
     * @param x the starting row
     * @param y the starting column
     */
    public static void drawString(String text, TerminalCell[][] buffer, int x, int y){
        drawString(text, buffer, x, y, AnsiColors.WHITE, AnsiColors.BLACK);
    }

    /**
     * Draws a box
     * @param width the width in characters, inclusive of the corners
     * @param height the height in characters, inclusive of the corners
     * @param buffer the buffer to draw on
     * @param foreground the foreground color
     * @param background the background color
     * @param x the x coordinate describing the top left of the box
     * @param y the y coordinate describing the top left of the box
     * @param boxStyle which character set to use between: <ul>
     *                 <li>thick</li>
     *                 <li>thin</li>
     *                 <li>double</li>
     *                </ul>
     */
    public static void drawBox(int width, int height, TerminalCell[][] buffer, AnsiColors foreground, AnsiColors background, int x, int y, BoxStyle boxStyle){
        buffer[x][y].setPixel(background, foreground, boxStyle.topLeftCorner);
        buffer[x][y+width - 1].setPixel(background, foreground, boxStyle.topRightCorner);
        buffer[x+height - 1][y].setPixel(background, foreground, boxStyle.bottomLeftCorner);
        buffer[x+height - 1][y + width - 1].setPixel(background, foreground, boxStyle.bottomRightCorner);

        for(int i = 1; i < width - 1; i++){
            buffer[x][y+i].setPixel(background, foreground, boxStyle.horizontalLine);
            buffer[x + height - 1][y+i].setPixel(background, foreground, boxStyle.horizontalLine);
        }
        for(int i = 1; i < height - 1; i++){
            buffer[x+i][y].setPixel(background, foreground, boxStyle.verticalLine);
            buffer[x+i][y+width-1].setPixel(background, foreground, boxStyle.verticalLine);
        }

    }

    /**
     * Draw a straight (vertical or horizontal) in the received buffer, at specified coordinates with a set color and a cardinal direction
     * @param length the length of the line, inclusive of the start and end characters
     * @param buffer the buffer to draw on
     * @param foreground foreground ascii color
     * @param background background ascii color
     * @param x the row coordinate to draw on
     * @param y the column coordinate to draw on
     * @param lineStyle the character set to use
     * @param dir the direction to draw the line (north, south, west, east)
     */
    public static void drawLine(int length, TerminalCell[][] buffer, AnsiColors foreground, AnsiColors background, int x, int y, BoxStyle lineStyle, Direction dir) throws UneditedBufferException {

        if(dir == Direction.EAST || dir == Direction.WEST){

            int factor = (dir == Direction.EAST) ? 1 : -1;
            for(int i = 0; i < length; i++){
                buffer[x][y + (i * factor) ].setPixel(background, foreground, lineStyle.horizontalLine);
            }
        }
        else if(dir == Direction.NORTH || dir == Direction.SOUTH){
            int factor = (dir == Direction.SOUTH) ? 1 : -1;
            for(int i = 0; i < length; i++){
                buffer[x + (factor * i)][y].setPixel(background, foreground, lineStyle.horizontalLine);
            }
        }
        else {
            throw new UneditedBufferException();
        }

    }

    /**
     * Draw a String of characters with a specified direction
     * @param input the String in the input line
     * @param buffer the buffer to draw on
     * @param x the row coordinate where the string starts
     * @param y the column coordinate where the string starts
     * @param foreground the color of the text
     * @param background the color of the background
     */
    public static void drawText(String input, TerminalCell[][] buffer, int x, int y, AnsiColors foreground, AnsiColors background, Direction dir){
        String[] inputSplit = input.split("(?!^)");
        int factor;
        int index = 0;

        if(dir == Direction.EAST || dir == Direction.WEST){
            factor = (dir == Direction.EAST) ? 1 : -1;

            for(String s : inputSplit){
                buffer[x][y + (factor * index)].setPixel(background, foreground, s);
                index++;
            }
        }
        else if(dir == Direction.NORTH || dir == Direction.SOUTH){
            factor = (dir == Direction.SOUTH) ? 1 : -1;

            for(String s : inputSplit){
                buffer[x + (factor * index)][y].setPixel(background, foreground, s);
                index++;
            }

        }
        else {
            //TODO exception
        }

    }

    /**
     * Fills a buffer with a given value and custom colors
     * @param input the input to fill the buffer
     * @param foreground the color of the text
     * @param background the color of the background
     * @param buffer the buffer to fill
     */
    public static void fillBuffer(String input, AnsiColors foreground, AnsiColors background, TerminalCell[][] buffer){
        for(TerminalCell[] row : buffer){
            for(TerminalCell cell : row){
                cell.setPixel(background, foreground, input);
            }
        }
    }

    /**
     * prints a matrix of cells
     * @param buffer the buffer to print
     * @return the String
     */
    public static String toString(TerminalCell[][] buffer){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < buffer.length; i++){
            for(int j = 0; j < buffer[0].length; j++){
                sb.append(buffer[i][j].getString());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

}
