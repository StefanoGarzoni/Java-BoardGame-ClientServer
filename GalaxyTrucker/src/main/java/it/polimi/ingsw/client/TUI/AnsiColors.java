package it.polimi.ingsw.client.TUI;

import org.fusesource.jansi.Ansi;

/**
 * represents the 3/4 bit terminal colors following <a href="https://en.wikipedia.org/wiki/ANSI_escape_code#3-bit_and_4-bit">ANSI codes</a>
 */
public enum AnsiColors {
    BLACK,
    RED,
    GREEN,
    YELLOW,
    BLUE,
    MAGENTA,
    CYAN,
    WHITE,
    DEFAULT; //default represents the standard color for the terminal as defined by the user config, OS or terminal config


    /**
     * Maps from this enum to jansi enum, allows to opt out of jansi if needed
     * @return the corresponding color
     * @see Ansi
     */
    public Ansi.Color getColorCode() {
        return switch (this) {
            case BLACK -> Ansi.Color.BLACK;
            case RED -> Ansi.Color.RED;
            case GREEN -> Ansi.Color.GREEN;
            case YELLOW -> Ansi.Color.YELLOW;
            case BLUE -> Ansi.Color.BLUE;
            case MAGENTA -> Ansi.Color.MAGENTA;
            case CYAN -> Ansi.Color.CYAN;
            case WHITE -> Ansi.Color.WHITE;
            default -> Ansi.Color.DEFAULT;
        };
    }
}
