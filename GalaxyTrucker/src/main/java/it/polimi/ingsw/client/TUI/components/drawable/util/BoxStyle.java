package it.polimi.ingsw.client.TUI.components.drawable.util;


import it.polimi.ingsw.client.TUI.UnicodeCharacters;

/**
 * This class defines character sets to draw boxes, allowing multiple styles to be chosen and swapped quickly
 */
public class BoxStyle {
    public final String topLeftCorner, topRightCorner, bottomLeftCorner, bottomRightCorner, verticalLine, horizontalLine;

    private BoxStyle(String topLeftCorner, String topRightCorner, String bottomLeftCorner, String bottomRightCorner, String verticalLine, String horizontalLine) {
        this.topLeftCorner = topLeftCorner;
        this.topRightCorner = topRightCorner;
        this.bottomLeftCorner = bottomLeftCorner;
        this.bottomRightCorner = bottomRightCorner;
        this.verticalLine = verticalLine;
        this.horizontalLine = horizontalLine;
    }

    public static final BoxStyle THIN = new BoxStyle(
            UnicodeCharacters.TOP_LEFT_CORNER_LIGHT,
            UnicodeCharacters.TOP_RIGHT_CORNER_LIGHT,
            UnicodeCharacters.BOTTOM_LEFT_CORNER_LIGHT,
            UnicodeCharacters.BOTTOM_RIGHT_CORNER_LIGHT,
            UnicodeCharacters.VERTICAL_LINE_THIN,
            UnicodeCharacters.HORIZONTAL_LINE_THIN
            );

    public static final BoxStyle THICK = new BoxStyle(
            UnicodeCharacters.TOP_LEFT_CORNER_BOLD,
            UnicodeCharacters.TOP_RIGHT_CORNER_BOLD,
            UnicodeCharacters.BOTTOM_LEFT_CORNER_BOLD,
            UnicodeCharacters.BOTTOM_RIGHT_CORNER_BOLD,
            UnicodeCharacters.VERTICAL_LINE_BOLD,
            UnicodeCharacters.HORIZONTAL_LINE_BOLD
    );

    public static final BoxStyle DOUBLE = new BoxStyle(
            UnicodeCharacters.DOUBLE_TOP_LEFT_CORNER,
            UnicodeCharacters.DOUBLE_TOP_RIGHT_CORNER,
            UnicodeCharacters.DOUBLE_BOTTOM_RIGHT_CORNER,
            UnicodeCharacters.DOUBLE_BOTTOM_LEFT_CORNER,
            UnicodeCharacters.DOUBLE_VERTICAL_LINE,
            UnicodeCharacters.DOUBLE_HORIZONTAL_LINE
    );

}
