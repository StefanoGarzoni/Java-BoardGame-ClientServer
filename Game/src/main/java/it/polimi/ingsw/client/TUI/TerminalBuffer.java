package it.polimi.ingsw.client.TUI;

import it.polimi.ingsw.client.TUI.components.drawable.util.DrawUtils;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.util.Arrays;
import java.util.List;

/**
 * This class prepares the next frame to be drawn on screen
 */
public class TerminalBuffer {
    private TerminalCell[][] screen;
    private final int HEIGHT;
    private final int WIDTH;


    TerminalBuffer(int width, int height){
        this.WIDTH = width;
        this.HEIGHT = height;
        screen = new TerminalCell[height][width];
        for(int i = 0; i < height; i++){
            for(int j = 0; j < width; j++){
                screen[i][j] = new TerminalCell(AnsiColors.BLACK,AnsiColors.RED, " ");
            }
        }
    }

    public TerminalCell[][] getScreen(){
        return screen;
    }

    /**
     * sets the whole buffer to whitespaces
     */
    public void clearFrame(){
        for(TerminalCell[] row : screen){
            for(TerminalCell cell : row){
                cell.setPixel(AnsiColors.BLACK,AnsiColors.RED, " ");
            }
        }
    }

    /**
     * Prints the buffer content
     */
    public void printBuffer(){
        if(!AnsiConsole.isInstalled()){
            System.setProperty("jansi.mode", "force");
            AnsiConsole.systemInstall();
        }
        for(TerminalCell[] row : screen){
            StringBuilder sb = new StringBuilder();
            for(TerminalCell tc : row){
                sb.append(tc.getPixel());
            }
            System.out.println(sb.toString());
        }
        //TODO do we need to do this?
        AnsiConsole.systemUninstall();
    }

    /**
     * Adds a sprite to the terminal
     * @param sprite a 2d array of cells to copy from
     * @param x the x coordinate of the top left corner of the location to insert the sprite into
     * @param y the y coordinate of the top left corner of the location to insert the sprite into
     */
    public void addSprite(TerminalCell[][] sprite, int x, int y){
        for(int i = 0; i < sprite.length; i++){
            for(int j = 0; j < sprite[i].length; j++){
                screen[x+i][y+j] = sprite[i][j];
            }
        }
    }

    /**
     * Adds a sprite to the top left corner of the terminal
     * @param sprite the 2d array of cells to copy from
     */
    public void addSprite(TerminalCell[][] sprite){
        addSprite(sprite, 0, 0);
    }

    /**
     * Adds a message to the buffer
     * @param messages the message to print
     */
    public void addMessage(List<String> messages){
        int messageAmounts = 5;

        for(int i = 1; i <= messageAmounts && i - 1 < messages.size(); i++ ){
            DrawUtils.drawString(messages.get(i-1), screen, screen.length - i, 0);
        }
    }

    /**
     * Adds an error message to the buffer
     * @param messages the message to print
     */
    public void addErrorMessage(List<String> messages){
        int messageAmounts = 5;

        for(int i = 1; i <= messageAmounts && i - 1 < messages.size(); i++ ){
            DrawUtils.drawString(messages.get(i-1), screen, screen.length - i, 0, AnsiColors.BLACK, AnsiColors.RED);
        }
    }

    public void addMessage(){

    }

    public int getHeight(){
        return HEIGHT;
    }

    public int getWidth(){
        return WIDTH;
    }

    public void clearArea(int row1, int col1, int row2, int col2){
        for(int i = row1; i <= row2; i++){
            for(int j = col1; j < col2; j++){
                screen[i][j].setPixel(AnsiColors.BLACK, AnsiColors.BLACK, " ");
            }
        }
    }

}
