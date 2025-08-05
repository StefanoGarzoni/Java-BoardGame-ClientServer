package it.polimi.ingsw.client.TUI;

import org.fusesource.jansi.Ansi;

/**
 * Representation of a pixel in the TUI
 */
public class TerminalCell{
    private AnsiColors backgroundColor;
    private AnsiColors foregroundColor;
    private String pixelContent;

    public TerminalCell(){
        this.backgroundColor = AnsiColors.BLACK;
        this.foregroundColor = AnsiColors.BLACK;
        this.pixelContent = " ";
    }

    public TerminalCell(AnsiColors backgroundColor, AnsiColors foregroundColor, String pixelContent){
        this.backgroundColor = backgroundColor;
        this.foregroundColor = foregroundColor;
        this.pixelContent = pixelContent;
    }

    /**
     * @return the string that represents the pixel following ANSI standards, comprehends FG and BG Color
     */
    public String getPixel(){
        //sets fg color, sets bg color, appends content, resets colors, and converts to string
        return Ansi.ansi().fg(foregroundColor.getColorCode()).bg(backgroundColor.getColorCode()).a(pixelContent).reset().toString();
    }

    /**
     * Sets (or resets) the pixel with given parameters
     * @param backgroundColor the color of the background of the terminal pixel
     * @param foregroundColor the color of the character of the terminal pixel
     * @param pixelContent the character of the pixel
     */
    public void setPixel(AnsiColors backgroundColor, AnsiColors foregroundColor, String pixelContent){
        this.backgroundColor = backgroundColor;
        this.foregroundColor = foregroundColor;
        this.pixelContent = pixelContent;
    }

    public void setBackgroundColor(AnsiColors backgroundColor){
        this.backgroundColor = backgroundColor;
    }

    public void setForegroundColor(AnsiColors foregroundColor){
        this.foregroundColor = foregroundColor;
    }

    public String getString(){
        return this.pixelContent;
    }

}
