package it.polimi.ingsw.client.TUI;

/**
 * Class to standardize useful Unicode String for the TUI
 */
public final class UnicodeCharacters {

    private UnicodeCharacters() {

    }

    /*---------------------*
     | Box drawing unicode |
     *---------------------*/
    //BOLD
    public static final String TOP_LEFT_CORNER_BOLD = "┏";
    public static final String TOP_RIGHT_CORNER_BOLD = "┓";
    public static final String BOTTOM_LEFT_CORNER_BOLD = "┗";
    public static final String BOTTOM_RIGHT_CORNER_BOLD = "┛";
    public static final String VERTICAL_LINE_BOLD = "┃";
    public static final String HORIZONTAL_LINE_BOLD = "━";
    public static final String NORTH_CONNECTOR_BOLD = "┻";
    public static final String SOUTH_CONNECTOR_BOLD = "┳";
    public static final String WEST_CONNECTOR_BOLD = "┫";
    public static final String EAST_CONNECTOR_BOLD = "┣";
    public static final String CROSS_BOLD = "╋";

    //LIGHT
    public static final String TOP_LEFT_CORNER_LIGHT = "┌";
    public static final String TOP_RIGHT_CORNER_LIGHT = "┐";
    public static final String BOTTOM_LEFT_CORNER_LIGHT = "└";
    public static final String BOTTOM_RIGHT_CORNER_LIGHT = "┘";
    public static final String VERTICAL_LINE_THIN = "│";
    public static final String HORIZONTAL_LINE_THIN = "─";
    public static final String NORTH_CONNECTOR_THIN = "┴";
    public static final String SOUTH_CONNECTOR_THIN = "┬";
    public static final String WEST_CONNECTOR_THIN = "┤";
    public static final String EAST_CONNECTOR_THIN = "├";
    public static final String CROSS_THIN = "┼";

    //DASHED
    public static final String HORIZONTAL_LINE_DASHED_BOLD = "╍";
    public static final String VERTICAL_LINE_DASHED_BOLD = "╏";
    public static final String HORIZONTAL_LINE_DASHED_THIN = "╌";
    public static final String VERTICAL_LINE_DASHED_THIN = "╎";

    //DOUBLE
    public static final String DOUBLE_HORIZONTAL_LINE = "═";
    public static final String DOUBLE_VERTICAL_LINE = "║";
    public static final String DOUBLE_TOP_LEFT_CORNER = "╔";
    public static final String DOUBLE_TOP_RIGHT_CORNER = "╗";
    public static final String DOUBLE_NORTH_CONNECTOR = "╩";
    public static final String DOUBLE_SOUTH_CONNECTOR = "╦";
    public static final String DOUBLE_EAST_CONNECTOR = "╠";
    public static final String DOUBLE_WEST_CONNECTOR = "╣";
    public static final String DOUBLE_BOTTOM_RIGHT_CORNER = "╝";
    public static final String DOUBLE_BOTTOM_LEFT_CORNER = "╚";

    //MIXED
    public static final String MIXED_DBL_EAST_CONNECTOR = "╞";
    public static final String MIXED_DBL_WEST_CONNECTOR = "╡";
    public static final String MIXED_DBL_HORIZONTAL_CROSS = "╪";


    /*----------
    //RESOURCES
     -----------*/
    public static final String STICK_MAN = "Ω"; //🯅
    public static final String ALIEN = "Ψ"; //𐁃
    public static final String CARGO = "▮";
    public static final String CANNON = "\uD833\uDC61";
    public static final String ENGINE = "\uD833\uDC57";
    public static final String LIFE_SUPPORT = "\uD833\uDDFB";

    /*------
    |OCTANTS|
    ---------
    An octant is a set of 8 different "pixels", we can combine them to form shapes in a 4x2 matrix
    1 2
    3 4
    5 6
    7 8
     */
    public static final String TOP_LEFT_OCTANT = "▛"; //123457
    public static final String TOP_RIGHT_OCTANT = "▜"; //123468
    public static final String BOTTOM_LEFT_OCTANT = "▙"; //135678
    public static final String BOTTOM_RIGHT_OCTANT = "▟"; //245678
    public static final String HORIZONTAL_TOP_OCTANT = "\uD83E\uDF82"; //12
    public static final String HORIZONTAL_BOTTOM_OCTANT = "▂";
    public static final String VERTICAL_RIGHT_OCTANT = "▐";
    public static final String VERTICAL_LEFT_OCTANT = "▌";


    /*------------------*
     | Other structural |
     *------------------*/
    public static final String CABIN_WINDOW = "☐";
    public static final String SUPPORT_DECO = "\uD833\uDC78";
    public static final String BATTERY_PLUS = "+";
    public static final String BATTERY_MINUS = "|";
    public static final String FLIGHT_DAYS = "⏃";
    public static final String NORTH_ARROW = "V";
    public static final String WEST_ARROW = ">";
    public static final String EAST_ARROW = "<";
    public static final String SOUTH_ARROW = "↑";

    /*------*
    | ASCII |
    *-------*/
    public static final String DOT = ".";
    public static final String CREDIT = "¢";
    public static final String MINUS = "-";

    /*-------*
     | Block |
     *-------*/
    public static final String FULL_BLOCK = "█";
    public static final String BLOCK_RIGHT_TRIANGLE = "\uD83E\uDF6C";

}
