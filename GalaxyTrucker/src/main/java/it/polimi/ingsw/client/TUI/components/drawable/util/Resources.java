package it.polimi.ingsw.client.TUI.components.drawable.util;

import it.polimi.ingsw.client.TUI.UnicodeCharacters;

public enum Resources {
    CANNON(UnicodeCharacters.CANNON),
    ENGINE(UnicodeCharacters.ENGINE),
    CREW(UnicodeCharacters.STICK_MAN),
    CREDITS(UnicodeCharacters.CREDIT),
    FLIGHT_DAYS(UnicodeCharacters.FLIGHT_DAYS),
    CARGO(UnicodeCharacters.CARGO);

    private final String Symbol;
    Resources(String symbol) {
        this.Symbol = symbol;
    }

    public String glyph(){
        return Symbol;
    }
}
