package it.polimi.ingsw.client.TUI.components.drawable;

import it.polimi.ingsw.client.TUI.AnsiColors;
import it.polimi.ingsw.client.TUI.TerminalCell;
import it.polimi.ingsw.client.TUI.UnicodeCharacters;
import it.polimi.ingsw.client.TUI.components.Drawable;
import it.polimi.ingsw.client.TUI.components.drawable.util.BoxStyle;
import it.polimi.ingsw.client.TUI.components.drawable.util.DrawUtils;
import it.polimi.ingsw.client.TUI.components.drawable.util.Resources;
import it.polimi.ingsw.client.TUI.exception.UneditedBufferException;
import it.polimi.ingsw.model.Cargo.CargoType;
import it.polimi.ingsw.model.Direction;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.generic.*;
import it.polimi.ingsw.model.cards.util.MeteorShot;
import it.polimi.ingsw.model.cards.util.Planet;
import it.polimi.ingsw.model.cards.util.ShotSize;
import it.polimi.ingsw.model.cards.visitor.GenericCardVisitor;

import java.util.ArrayList;

public class DrawableCard implements Drawable, GenericCardVisitor {
    private final TerminalCell[][] cardSprite;
    public static final int COL_COUNT = 11;
    public static final int ROW_COUNT = 8;

    public DrawableCard(Card card) {
        cardSprite = new TerminalCell[ROW_COUNT][COL_COUNT];

        for(int x = 0; x < ROW_COUNT; x++){
            for(int y = 0; y < COL_COUNT; y++){
                cardSprite[x][y] = new TerminalCell();
            }
        }

        drawGenericCard();
        card.accept(this);
    }

    public DrawableCard(int level){
        if(level != 1 && level != 2) throw new IllegalArgumentException();

        cardSprite = new TerminalCell[ROW_COUNT][COL_COUNT];
        for(int x = 0; x < ROW_COUNT; x++){
            for(int y = 0; y < COL_COUNT; y++){
                cardSprite[x][y] = new TerminalCell();
            }
        }

        drawGenericCard();
        drawCardBack(level);
    }

    private void drawGenericCard(){
        DrawUtils.drawBox(COL_COUNT, ROW_COUNT, cardSprite, AnsiColors.WHITE, AnsiColors.BLACK, 0, 0, BoxStyle.THIN);
    }

    /*-----------*
     | Utilities |
     *-----------*/

    /**
     * Draws resource amount and their respective symbol
     * @param res Enum of resource types //TODO
     * @param amount The amount of the resource, a negative amount will be marked red, a positive amount green
     * @param startingX The X coordinate where the list starts
     * @param startingY The Y coordinate where the list ends
     * @param isRequired If the resource is needed but does not get removed, marked cyan
     */
    private void drawResources(Resources res, int amount, int startingX, int startingY, boolean isRequired){
        AnsiColors fgColor;
        if(isRequired) {
            fgColor = AnsiColors.CYAN;
        }else if(amount < 0){
            fgColor = AnsiColors.RED;
            amount = -amount;
        }
        else {
            fgColor = AnsiColors.GREEN;
        }

        //We need to split the value into single digits
        String[] valueArray = String.valueOf(amount).split("(?!^)");

        int cursor;
        for(cursor = 0; cursor < valueArray.length; cursor++){
            cardSprite[startingX][startingY + cursor].setPixel(AnsiColors.BLACK, fgColor, valueArray[cursor]);
        }
        cardSprite[startingX][startingY + cursor].setPixel(AnsiColors.BLACK, fgColor, res.glyph());
    }

    /**
     * Draws a list of cargo with their respective colors
     * @param cargoList the list of cargo to draw on the cards
     * @param startingX the row to start the drawing process on
     * @param startingY the column to start the drawing process on
     */
    private void drawCargos(ArrayList<CargoType> cargoList, int startingX, int startingY){
        int cargoIndex = 1;

        cardSprite[startingX][startingY].setPixel(AnsiColors.BLACK, AnsiColors.WHITE, UnicodeCharacters.BATTERY_PLUS);

        for(CargoType cargo : cargoList){
            AnsiColors color = switch (cargo.getColor()) {
                case "yellow" -> AnsiColors.YELLOW;
                case "red" -> AnsiColors.RED;
                case "green" -> AnsiColors.GREEN;
                case "blue" -> AnsiColors.BLUE;
                default ->  throw new IllegalArgumentException("Invalid color");
            };

            cardSprite[startingX][startingY + cargoIndex].setPixel(AnsiColors.BLACK, color, UnicodeCharacters.CARGO);
            cargoIndex++;
        }
    }

    /*----------*
     | Concrete |
     *----------*/

    public void visit(PlanetCard card){
        int currPlanetInserted = 0;
        int cargoDefaultColumn = 3;

        for(Planet planet : card.getPlanets())
        {
            int cargoIndex = 0;
            currPlanetInserted++;

            cardSprite[currPlanetInserted][1].setPixel(AnsiColors.BLACK, AnsiColors.WHITE, String.valueOf(currPlanetInserted));
            cardSprite[currPlanetInserted][2].setPixel(AnsiColors.BLACK, AnsiColors.WHITE, UnicodeCharacters.DOT);

            drawCargos(planet.getCargoList(), currPlanetInserted, cargoDefaultColumn);
        }
        DrawUtils.drawString("planet", cardSprite, 0, 1);
        drawResources(Resources.FLIGHT_DAYS, card.getFlightDayLoss(), ROW_COUNT - 2, 7, false);
    }

    public void visit(EnslaversCard card){

        drawResources(Resources.CANNON, card.getRequiredFirePower(), 1, 1, true);
        drawResources(Resources.CREW, card.getCrewPenalty(), 1, 8, false);
        drawResources(Resources.FLIGHT_DAYS, card.getFlightDayLoss(), 2, 1, false);
        drawResources(Resources.CREDITS, card.getCreditPrize(), 5, 4, false);
        DrawUtils.drawString("enslavers", cardSprite, 0, 1);
    }

    public void visit(MeteorSwarmCard card){
        int northRow = 1;
        int southRow = ROW_COUNT - 2;
        int westColumn = 1;
        int eastColumn = COL_COUNT - 2;

        int northIndex = 1;
        int southIndex = 1;
        int westIndex = 2;
        int eastIndex = 2;

        for(MeteorShot meteor : card.getMeteorList()){
            String meteorGlyph;
            switch (meteor.getDirection()){
                case NORTH:
                    meteorGlyph = (meteor.getSize() == ShotSize.BIG) ? "N" : "n";
                    cardSprite[northRow][northIndex].setPixel(AnsiColors.BLACK, AnsiColors.RED, meteorGlyph);
                    northIndex++;
                    break;
                case WEST:
                    meteorGlyph = (meteor.getSize() == ShotSize.BIG) ? "W" : "w";
                    cardSprite[westIndex][westColumn].setPixel(AnsiColors.BLACK, AnsiColors.RED, meteorGlyph);
                    westIndex++;
                    break;
                case EAST:
                    meteorGlyph = (meteor.getSize() == ShotSize.BIG) ? "E" : "e";
                    cardSprite[eastIndex][eastColumn].setPixel(AnsiColors.BLACK, AnsiColors.RED, meteorGlyph);
                    eastIndex++;
                    break;
                case SOUTH:
                    meteorGlyph = (meteor.getSize() == ShotSize.BIG) ? "S" : "s";
                    cardSprite[southRow][southIndex].setPixel(AnsiColors.BLACK, AnsiColors.RED, meteorGlyph);
                    southIndex++;
                    break;
            }
        }
        DrawUtils.drawString("meteors", cardSprite, 0, 1);
    }

    public void visit(AbandonedShipCard card){
        drawResources(Resources.CREW, card.getCrewLoss(), 2, 1, false);
        drawResources(Resources.FLIGHT_DAYS, card.getFlightDayLoss(), 3, 1, false);
        drawResources(Resources.CREDITS, card.getCreditReward(), 4, 1, false);
        DrawUtils.drawString("ab. ship", cardSprite, 0, 1);
    }

    public void visit(AbandonedStationCard card){
        drawResources(Resources.CREW, card.getRequiredCrew(), 2, 1, true);
        drawCargos(card.getCargoList(), 3 ,1);
        drawResources(Resources.FLIGHT_DAYS, card.getFlightDayLoss(), 4, 1, false);
        DrawUtils.drawString("ab. statn", cardSprite, 0, 1);

    }

    public void visit(SmugglersCard card){
        drawResources(Resources.CANNON, card.getRequiredFirePower(), 2, 1, true);
        drawResources(Resources.CARGO, card.getCargoPenalty(), 2, 8, false);
        drawResources(Resources.FLIGHT_DAYS, card.getFlightDayLoss(), 4, 8, false);
        drawCargos(card.getCargoPrize(), 5, 1);
        DrawUtils.drawString("smugglers", cardSprite, 0, 1);
    }

    public void visit(PiratesCard card){
        drawResources(Resources.CANNON, card.getRequiredFirePower(), 2, 1, true);
        drawResources(Resources.FLIGHT_DAYS, card.getFlightDayLoss(), 3, 1, false);
        drawResources(Resources.CREDITS, card.getCreditPrize(), 4, 1, false);
        DrawUtils.drawString("pirates", cardSprite, 0, 1);
    }

    public void visit(StardustCard stardustCard){
        DrawUtils.drawBox(5, 3, cardSprite, AnsiColors.WHITE, AnsiColors.BLACK, 1, 3, BoxStyle.THICK);
        cardSprite[2][7].setPixel(AnsiColors.BLACK, AnsiColors.WHITE, UnicodeCharacters.EAST_CONNECTOR_BOLD);
        cardSprite[2][3].setPixel(AnsiColors.BLACK, AnsiColors.WHITE, UnicodeCharacters.WEST_CONNECTOR_BOLD);
        DrawUtils.drawString("stardust", cardSprite, 0, 1);
    }

    public void visit(EpidemicCard card){
        DrawUtils.drawBox(8, 5, cardSprite, AnsiColors.WHITE, AnsiColors.BLACK, 1, 1, BoxStyle.THIN);
        try {
            DrawUtils.drawLine(7, cardSprite, AnsiColors.WHITE, AnsiColors.BLACK, 3, 2, BoxStyle.DOUBLE, Direction.EAST);
        } catch (UneditedBufferException e) {
            throw new RuntimeException(e);
        }
        cardSprite[3][1].setPixel(AnsiColors.BLACK, AnsiColors.WHITE, UnicodeCharacters.MIXED_DBL_EAST_CONNECTOR);
        cardSprite[3][9].setPixel(AnsiColors.BLACK, AnsiColors.WHITE, UnicodeCharacters.MIXED_DBL_WEST_CONNECTOR);
        cardSprite[3][5].setPixel(AnsiColors.BLACK, AnsiColors.WHITE, UnicodeCharacters.MIXED_DBL_HORIZONTAL_CROSS);
        cardSprite[2][5].setPixel(AnsiColors.BLACK, AnsiColors.RED, UnicodeCharacters.ALIEN);
        cardSprite[4][4].setPixel(AnsiColors.BLACK, AnsiColors.WHITE, UnicodeCharacters.STICK_MAN);
        cardSprite[4][6].setPixel(AnsiColors.BLACK, AnsiColors.RED, UnicodeCharacters.STICK_MAN);
        DrawUtils.drawString("epidemic", cardSprite, 0, 1);
    }

    public void visit(OpenSpaceCard card){
        cardSprite[3][3].setPixel(AnsiColors.BLACK, AnsiColors.WHITE, UnicodeCharacters.ENGINE);
        cardSprite[3][6].setPixel(AnsiColors.BLACK, AnsiColors.WHITE, UnicodeCharacters.FLIGHT_DAYS);
        DrawUtils.drawString("op. space", cardSprite, 0, 1);
    }

    //TODO Manage different penalties?
    public void visit(WarZoneCard card){
        AnsiColors white = AnsiColors.WHITE;
        AnsiColors black = AnsiColors.BLACK;
        AnsiColors red = AnsiColors.RED;

        cardSprite[1][1].setPixel(black, white, UnicodeCharacters.CANNON);
        cardSprite[2][1].setPixel(black, white, UnicodeCharacters.ENGINE);
        cardSprite[3][1].setPixel(black, white, UnicodeCharacters.STICK_MAN);


        for(int i = 0; i <= 2; i++) {
            cardSprite[1+i][2].setPixel(black, white, "<");
            DrawUtils.drawString("warzone", cardSprite, 0, 1);
        }
    }

    private void drawCardBack(int level){
        if(level == 1){
            DrawUtils.drawBox(3, 5, cardSprite, AnsiColors.WHITE, AnsiColors.BLACK, 1, 4, BoxStyle.THIN);
        }
        else if(level == 2){
            DrawUtils.drawBox(3, 5, cardSprite, AnsiColors.WHITE, AnsiColors.BLACK, 1, 2, BoxStyle.THIN);
            DrawUtils.drawBox(3, 5, cardSprite, AnsiColors.WHITE, AnsiColors.BLACK, 1, 6, BoxStyle.THIN);
        }
        else {
            throw new IllegalArgumentException("level must be 1 or 2");
        }
    }



    @Override
    public TerminalCell[][] draw() {
        return cardSprite;
    }
}
