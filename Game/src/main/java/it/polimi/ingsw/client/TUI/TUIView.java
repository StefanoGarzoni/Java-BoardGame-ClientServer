package it.polimi.ingsw.client.TUI;

import it.polimi.ingsw.client.TUI.components.drawable.DrawableCard;
import it.polimi.ingsw.client.TUI.components.drawable.DrawableFlightBoard;
import it.polimi.ingsw.client.TUI.components.drawable.DrawableTile;
import it.polimi.ingsw.client.TUI.components.drawable.DrawableTimer;
import it.polimi.ingsw.client.TUI.components.drawable.util.DrawUtils;
import it.polimi.ingsw.client.View;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.ComponentTile.ComponentTile;
import it.polimi.ingsw.model.ComponentTile.FixedComponentTile;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.client.ClientCard;
import it.polimi.ingsw.model.clientModel.ClientComponentTilesBunch;
import it.polimi.ingsw.model.clientModel.ClientDeck;
import it.polimi.ingsw.model.clientModel.ViewFlightBoard;
import it.polimi.ingsw.model.clientModel.ViewShipBoard;

import java.util.*;

public class TUIView implements View {
    private final int screenWidth = 150;
    private final int screenHeight = 50;
    private final int SHIPBOARD_TOP_LEFT_ROW = 0;
    private final int SHIPBOARD_TOP_LEFT_COL = 0;
    private final int SHIPBOARD_ROWS = 25;
    private final int SHIPBOARD_COLUMNS = 77;
    private final int TIMER_TOP_LEFT_ROW = 2;
    private final int TIMER_TOP_LEFT_COL = screenWidth - 15;
    private final int MESSAGE_TOP_LEFT_ROW = screenHeight - 1;
    private final int MESSAGE_TOP_LEFT_COL = 0;
    private final int MESSAGE_WIDTH = screenWidth;
    private final int MESSAGES_TO_KEEP = 5;
    private final int TILE_IN_HAND_TOP_LEFT_ROW = (2*(screenHeight - DrawableTile.ROW_COUNT))/3;
    private final int TILE_IN_HAND_TOP_LEFT_COL = (screenWidth - DrawableTile.COL_COUNT)/2;

    private MessageList messages;
    private TerminalBuffer buffer = new TerminalBuffer(screenWidth, screenHeight);
    private Scanner scanner = new Scanner(System.in);
    private Player associatedPlayer;
    private ViewFlightBoard viewFlightBoard;
    private DrawableFlightBoard dfl;
    private boolean firstFlightBoard = true;

    //We might need os specific configuration?
    public TUIView() {
        messages = new MessageList();
    }

    @Override
    public void showCreateGame() {
        DrawUtils.drawString("Create game", buffer.getScreen(), 0, 0);
    }

    @Override
    public void showLobby(ArrayList<Player> players) {
        buffer.clearFrame();

        int counter = 0;
        for (Player player : players) {
            DrawUtils.drawString(player.getNickname(), buffer.getScreen(), counter, 0);
            counter++;
        }
    }

    //Change to ClientComponentTilesBunch and add tile index
    @Override
    public void showTileBunch(ClientComponentTilesBunch clientComponentTilesBunch) {
        buffer.clearFrame();

        DrawUtils.drawString("Select a tile:", buffer.getScreen(), 0, 0);

        List<ComponentTile> uncoveredTiles = clientComponentTilesBunch.getAllUncoveredTiles();
        int counter = 0;
        int rowCounter = 0;
        int colCounter = 0;
        int rowFactor = 6; //each tile is 5 row tall, and an extra for the coordinates
        int colFactor = 11; //each tile is 11 columns wide
        int maxTilesInRow = 9;
        int maxTilesInCol = 4;

        //TODO unique id?
        for (ComponentTile componentTile : uncoveredTiles) {
            if(colCounter % maxTilesInCol == 0) {
                colCounter = 0;
                rowCounter++;
            }
            if(rowCounter % maxTilesInRow == 0) {
                //TODO exception
            }
            DrawableTile drawableTile = new DrawableTile(componentTile, AnsiColors.BLACK);
            DrawUtils.drawString(Integer.toString(counter), buffer.getScreen(), rowFactor * rowCounter, colFactor * colCounter);
            buffer.addSprite(drawableTile.draw(),  rowFactor * rowCounter + 1, colFactor * colCounter);
            colCounter++;
            counter++;
        }
        buffer.printBuffer();

    }

    @Override
    public void showTileInHand(ComponentTile componentTile) {

        DrawableTile drawableTile = new DrawableTile(componentTile, AnsiColors.BLACK);
        buffer.addSprite(drawableTile.draw(), (2*(buffer.getHeight() - DrawableTile.ROW_COUNT))/3, (buffer.getWidth() - DrawableTile.COL_COUNT)/2);
        buffer.printBuffer();
    }

    private void showBookedTile(ArrayList<ComponentTile> bookedTiles) {
        int bookedTileCounter = 0;
        int bookedTileColumn = 80;
        int tileColumnFactor = 11;
        int bookedTileRow = 1;
        ArrayList<DrawableTile> drawableTiles = new ArrayList<>();
        for (ComponentTile componentTile : bookedTiles) {
            DrawableTile drawableTile =  new DrawableTile(componentTile, AnsiColors.BLACK);
            drawableTiles.add(drawableTile);
        }
        for(DrawableTile drawableTile : drawableTiles){
            buffer.addSprite(drawableTile.draw(), bookedTileRow, bookedTileColumn + bookedTileCounter * tileColumnFactor);
            DrawUtils.drawString(Integer.toString(bookedTileCounter), buffer.getScreen() ,bookedTileRow - 1,
                    bookedTileColumn + bookedTileCounter * tileColumnFactor + tileColumnFactor/2, AnsiColors.BLACK, AnsiColors.BLUE);
            bookedTileCounter++;
        }
    }

    //Pass player or shipboard?
    @Override
    public void showShipboard(ViewShipBoard shipBoard){
        clearShipBoard();
        int rowFactor = 5;
        int colFactor = 11;

        //FIXME
        FixedComponentTile[][] shipBoardMatrix = shipBoard.getShipBoardMatrix();
        showBookedTile(shipBoard.getBookedTiles());

        for(int i = 0; i < shipBoardMatrix.length; i++){
            for(int j = 0; j < shipBoardMatrix[i].length; j++){
                if(i == 0) {
                    DrawUtils.drawString(String.valueOf(j), buffer.getScreen(), i, j * colFactor + colFactor / 2 + 1);
                }
                if(j == 0) {
                    DrawUtils.drawString(String.valueOf(i), buffer.getScreen(), i * rowFactor + rowFactor/2 + 1, j);
                }
                if(shipBoardMatrix[i][j] == null){continue;}
                DrawableTile drawableTile = new DrawableTile(shipBoardMatrix[i][j], AnsiColors.BLACK);
                buffer.addSprite(drawableTile.draw(), rowFactor * i + 1, colFactor * j + 1);
            }
        }
        buffer.printBuffer();
    }

    @Override
    public void showDeck(ClientDeck deck) {
        //Who takes the deck?
        int counter = 0;
        int widthFactor = 15;
        int padding = 1;

        //FIXME
        for(ClientCard card : deck.getAllClientCards()){
            DrawableCard drawableCard = new DrawableCard(card.getCardData());
            buffer.addSprite(drawableCard.draw(), screenHeight - DrawableCard.ROW_COUNT - padding, screenWidth/2 - counter * widthFactor);
            counter++;
        }
        buffer.printBuffer();
    }

    @Override
    public void showHourGlass(long time) {
        DrawableTimer drawableTimer = new DrawableTimer(time);
        buffer.addSprite(drawableTimer.draw(), 2,screenWidth - 15);
        buffer.printBuffer();
    }

    @Override
    public void showAlienPlacements() {

    }

    @Override
    public void showBatteries() {
    }

    @Override
    public void showCrew() {

    }

    @Override
    public void showCargo() {

    }

    @Override
    public void showFlightBoard(ViewFlightBoard viewFlightBoard) {
        if(firstFlightBoard){
            buffer.clearFrame();
            firstFlightBoard = false;
        }
        dfl = new DrawableFlightBoard(viewFlightBoard);
        drawCoveredDeck();
        drawPlayers(viewFlightBoard.getPlayerNames());
        buffer.addSprite(dfl.draw(), 26, 40);
        buffer.printBuffer();
    }

    @Override
    public void showFlightBoard(){
        dfl.draw();
        drawCoveredDeck();
        buffer.addSprite(dfl.draw(), 26, 40);
        buffer.printBuffer();
    }

    @Override
    public void showHighlightedTiles(ArrayList<Coordinates> highlightedTiles) {
        int rowFactor = 5;
        int colFactor = 11;
        int rowAmount = 5;
        int colAmount = 7;

        for(int i = 1; i < rowAmount; i++){
            for(int j = 1; j < colAmount; j++){
                if(highlightedTiles.contains(new Coordinates(i, j))){
                    for(int k = 1; k < rowFactor; k++){
                        for(int l = 1; l < colFactor; l++){
                            buffer.getScreen()[i * rowFactor + k][j * colFactor + l].setBackgroundColor(AnsiColors.BLUE);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void showCard(Card cardData) {
        DrawableCard card = new DrawableCard(cardData);
        buffer.addSprite(card.draw(), 27, 15);
        buffer.printBuffer();
    }

    @Override
    public void showBrokenTile() {

    }

    @Override
    public void showMessage(String message) {
        message = prepareString(message);
        messages.addMessage(message);
        buffer.addMessage(messages.getMessages(MESSAGES_TO_KEEP));
        buffer.printBuffer();
        clearMessage();
    }

    @Override
    public void showMessage(List<String> messages){
        buffer.clearFrame();
        int i = 0;
        for(String string : messages){
            string = prepareString(string);
            DrawUtils.drawString(string, buffer.getScreen(), i, 0);
            i++;
            if(i >= 50) break;
        }
        buffer.printBuffer();
    }

    @Override
    public void showErrorMessage(String message) {
        message = prepareString(message);
        messages.addErrorMessage(message);
        DrawUtils.drawString(message, buffer.getScreen(), buffer.getHeight() - 1, 0, AnsiColors.RED, AnsiColors.BLACK);
        buffer.printBuffer();
        clearMessage();
    }

    //TODO
    @Override
    public void showDices() {
    }

    @Override
    public void showScoreBoard(ViewShipBoard[] viewShipBoards) {
        buffer.clearFrame();
        int counter = 0;
        for(ViewShipBoard viewShipBoard : viewShipBoards){
            DrawUtils.drawString(viewShipBoard.getNickname() + ": " + String.valueOf(viewShipBoard.getPoints()),
                    buffer.getScreen(), counter, 0);
        }
        buffer.printBuffer();
    }

    @Override
    public void showDisconnect() {
    }

    private void drawPlayers(Map<String, Character> playerNames){
        int topLeftRow = 27;
        int topLeftCol = 112;
        int rowCounter = 1;
        DrawUtils.drawString("PLAYER LIST:", buffer.getScreen(), topLeftRow, topLeftCol - 1);
        for(Map.Entry<String, Character> entry : playerNames.entrySet()){
            AnsiColors color = switch (entry.getValue()){
                case 'R' -> AnsiColors.RED;
                case 'Y' -> AnsiColors.YELLOW;
                case 'B' -> AnsiColors.BLUE;
                case 'G' -> AnsiColors.GREEN;
                default -> AnsiColors.WHITE;
            };
            DrawUtils.drawString(entry.getKey(), buffer.getScreen(), topLeftRow + rowCounter, topLeftCol, AnsiColors.BLACK, color);
            rowCounter++;
        }
    }

    public void drawCoveredDeck(){
        DrawableCard card = new DrawableCard(1);
        buffer.addSprite(card.draw(), 27, 3);
        buffer.printBuffer();
    }

    public void clearShipBoard(){
        buffer.clearArea(SHIPBOARD_TOP_LEFT_ROW, SHIPBOARD_TOP_LEFT_COL, SHIPBOARD_ROWS, SHIPBOARD_COLUMNS);
    }

    public void clearTimer(){
        buffer.clearArea(TIMER_TOP_LEFT_ROW, TIMER_TOP_LEFT_COL, DrawableTimer.ROW_COUNT, DrawableTimer.COL_COUNT);
    }

    public void clearMessage(){
        buffer.clearArea(MESSAGE_TOP_LEFT_ROW-MESSAGES_TO_KEEP, MESSAGE_TOP_LEFT_COL, MESSAGE_TOP_LEFT_ROW, MESSAGE_WIDTH);
    }

    private void clearTileInHand(){
        buffer.clearArea(TILE_IN_HAND_TOP_LEFT_ROW, TILE_IN_HAND_TOP_LEFT_COL,
                TILE_IN_HAND_TOP_LEFT_ROW + DrawableTile.ROW_COUNT, TILE_IN_HAND_TOP_LEFT_COL + DrawableTile.COL_COUNT);
    }

    private String prepareString(String string){
        if(string == null) return "";
        if(string.length() > MESSAGE_WIDTH - 3){
            string = string.substring(0, MESSAGE_WIDTH - 3) + "...";
        }
        return string;
    }


}
