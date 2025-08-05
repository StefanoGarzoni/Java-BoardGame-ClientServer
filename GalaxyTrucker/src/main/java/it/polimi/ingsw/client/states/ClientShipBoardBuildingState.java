package it.polimi.ingsw.client.states;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.client.ClientController;
import it.polimi.ingsw.model.ComponentTile.ComponentTile;
import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.Direction;
import it.polimi.ingsw.model.clientModel.ClientComponentTilesBunch;
import it.polimi.ingsw.model.clientModel.ViewFlightBoard;
import it.polimi.ingsw.model.clientModel.ViewShipBoard;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientShipBoardBuildingState extends ClientState{
    ArrayList<String> availableCommandsWhenFinishedBuilding = new ArrayList<>();

    private Instant currentHourGlassFlipDeadline = null;
    private boolean isLastHourGlassFlip = false;

    ArrayList<Coordinates> unfixableCoordinates = new ArrayList<>();

    public ClientShipBoardBuildingState(ClientController clientController){
        super(clientController);
        setupUnfixableCoordinates();

        // setting available commands when finished building
        availableCommandsWhenFinishedBuilding.add("viewTimeLeft");
        availableCommandsWhenFinishedBuilding.add("flipHourGlass");
        availableCommandsWhenFinishedBuilding.add("showShipBoard");
        availableCommandsWhenFinishedBuilding.add("showAllCommands");

        // setting available methods from view in this state
        methodsFromViewMap.put("viewDeck", this::viewDeck);
        methodsFromViewMap.put("viewTimeLeft", this::viewTimeLeft);
        methodsFromViewMap.put("poseDeck", this::poseDeck);
        methodsFromViewMap.put("getCoveredTile", this::getCoveredTile);
        methodsFromViewMap.put("getUncoveredTile", this::getUncoveredTile);
        methodsFromViewMap.put("bookTile", this::bookTile);
        methodsFromViewMap.put("poseTile", this::poseTile);
        methodsFromViewMap.put("placeDrawnTileAt", this::placeDrawnTileAt);
        methodsFromViewMap.put("placeBookedTileAt", this::placeBookedTileAt);
        methodsFromViewMap.put("flipHourGlass", this::flipHourGlass);
        methodsFromViewMap.put("endBuilding", this::playerFinished);
        methodsFromViewMap.put("showAllTiles", this::showAllTile);
        methodsFromViewMap.put("showShipBoard", this::showShipBoard);

        methodsFromServerMap.put("tileIsBooked", this::setTileAsBooked);    // sets a tile as booked for a player
        methodsFromServerMap.put("tileIsFree", this::setTileAsFree);    // a player freed a tile
        methodsFromServerMap.put("sendTileCovered", this::showCoveredTile);     // a player asked for a booked tile
        methodsFromServerMap.put("tileUncoveredTaken", this::showUncoveredTile);     // a player asked for an uncovered tile
        methodsFromServerMap.put("tileIsPlaced", this::placeTile);     // a player placed a tile
        methodsFromServerMap.put("deckIsViewing", this::setDeckAsHidden);     // a player asked for a deck
        methodsFromServerMap.put("deckReleased", this::setDeckAsAvailable);     // a player released a deck
        methodsFromServerMap.put("playerIsDoneConformation", this::setPlayerAsDone);     // a player released a deck
        methodsFromServerMap.put("canFlipHG", this::hourGlassFlippingPermission);
        methodsFromServerMap.put("shipBoardChecksState", this::endShipBoardBuildingState);
    }

    /** Setups all coordinates that are not fixable in the shipboard
     */
    private void setupUnfixableCoordinates(){
        unfixableCoordinates.add(new Coordinates(0, 0));
        unfixableCoordinates.add(new Coordinates(0, 1));
        unfixableCoordinates.add(new Coordinates(0, 3));
        unfixableCoordinates.add(new Coordinates(0, 5));
        unfixableCoordinates.add(new Coordinates(0, 6));
        unfixableCoordinates.add(new Coordinates(1, 0));
        unfixableCoordinates.add(new Coordinates(0, 6));
        unfixableCoordinates.add(new Coordinates(4, 3));
    }

    /** Checks if a coordinates can contain a new tile
     *
     * @param coordinates to check if available for a tile placing
     * @return true if a can can be fixed to these coordinates, false otherwise
     */
    private boolean canTileBeFixedAt(Coordinates coordinates){
        return coordinates.getRow() >= 0 && coordinates.getRow() < 5
                && coordinates.getCol() >= 0 && coordinates.getCol() < 7
                && !unfixableCoordinates.contains(coordinates)
                && clientController.getViewFlightBoard().getViewShipBoard(clientController.getNickname())
                    .getShipBoardMatrix()[coordinates.getRow()][coordinates.getCol()] == null;
    }

    @Override
    public void run() {
        clientController.getView().showMessage("Shipboard building has started! Select and fix your tiles!");
        showAllCommands();
    }

    @Override
    public void processFromServer(ActionMessage actionMessage) {
        synchronized (clientController) {
            try {
                methodsFromServerMap.get(actionMessage.getActionName()).accept(actionMessage);
            } catch (NullPointerException e) {
                e.printStackTrace(System.out);
            }
        }
    }

    @Override
    public void processFromView(ActionMessage actionMessage) {
        synchronized (clientController) {
            boolean hasEndedBuilding = clientController.getViewFlightBoard().getViewShipBoard(clientController.getNickname()).getPlayerHasEndedBuilding();

            boolean buildingTimeHasEnded = currentHourGlassFlipDeadline != null
                    && currentHourGlassFlipDeadline.isBefore(Instant.now())
                    && isLastHourGlassFlip;

            if (
                    (hasEndedBuilding || buildingTimeHasEnded) && !availableCommandsWhenFinishedBuilding.contains(actionMessage.getActionName())
            ) {
                clientController.getView().showErrorMessage("You have finished building you shipboard");
                return;
            }

            try {
                methodsFromViewMap.get(actionMessage.getActionName()).accept(actionMessage); //FIXME does not throw NPE
            } catch (Exception e) {
                clientController.getView().showErrorMessage("The " + actionMessage.getActionName() + " method is not valid in this state");
            }
        }
    }

    // ---------- Messages from View ----------

    private void viewTimeLeft(ActionMessage request){
        long secondLeft = Duration.between(Instant.now(), currentHourGlassFlipDeadline).getSeconds();

        clientController.getView().showHourGlass(secondLeft);
    }

    private void viewDeck(ActionMessage request){
        ViewFlightBoard flightBoard = clientController.getViewFlightBoard();

        int selectedDeckIndex = request.getInt("deckIndex");

        int decksNumber = flightBoard.getDecksNumber();
        if(selectedDeckIndex < 0 || selectedDeckIndex >= decksNumber ){
            clientController.getView().showErrorMessage("The selected deck's index is not valid");
            return;
        }

        boolean isSelectedDeckFree = flightBoard.isDeckFree(selectedDeckIndex);
        if(!isSelectedDeckFree){   // if another player is already watching the deck
            clientController.getView().showMessage("Another player is viewing this deck");
        }
        else{
            String playerNickname = clientController.getNickname();
            flightBoard.getViewShipBoard(playerNickname).setDeckIndexPlayerIsWaitingFor(selectedDeckIndex);

            // contacts the server to reserve the selected deck
            ActionMessage messageToServer = new ActionMessage("viewDeck", clientController.getNickname());
            messageToServer.setData("deckNumber", selectedDeckIndex);
            clientController.getServerProxy().send(messageToServer);
            //clientController.getView().showDeck(flightBoard.getDeck(selectedDeckIndex));
        }
    }

    private void poseDeck(ActionMessage request){
        int selectedDeckIndex = clientController.getViewFlightBoard().getDeckThatIsViewing(clientController.getNickname());

        if(selectedDeckIndex < 0){
            clientController.getView().showErrorMessage("You aren't viewing a deck");
        }
        else{
            // contacts the server to free the selected deck
            ActionMessage messageToServer = new ActionMessage("poseDeck", clientController.getNickname());
            messageToServer.setData("deckNumber", selectedDeckIndex);
            clientController.getServerProxy().send(messageToServer);
        }
    }

    private void getCoveredTile(ActionMessage request){
        boolean playerHasTileInHand = clientController.getViewFlightBoard().getViewShipBoard(clientController.getNickname()).getTileInHand() != null;

        if(!playerHasTileInHand){
            ActionMessage messageToServer = new ActionMessage("getCoveredTile", clientController.getNickname());
            clientController.getServerProxy().send(messageToServer);
        }
        else{
            clientController.getView().showMessage("You have already a tile in hand. Use it before drawing a new tile.");
        }
    }

    private void getUncoveredTile(ActionMessage request){
        boolean playerHasTileInHand = clientController.getViewFlightBoard().getViewShipBoard(clientController.getNickname()).getTileInHand() != null;

        if(!playerHasTileInHand){
            int tileIndex = request.getInt("tileIndex");
            boolean isTileUncovered = clientController.getViewFlightBoard().getClientTilesBunch().isTileUncovered(tileIndex);

            if(!isTileUncovered){
                clientController.getView().showErrorMessage("The selected tile is not uncovered");
                return;
            }

            String tileFileName = clientController.getViewFlightBoard().getClientTilesBunch().getFileNameOfTileAtIndex(tileIndex, false);

            ActionMessage messageToServer = new ActionMessage("getUncoveredTile", clientController.getNickname());
            messageToServer.setData("fileName", tileFileName);
            clientController.getServerProxy().send(messageToServer);
        }
        else{
            clientController.getView().showMessage("You have already a tile in hand. Use it before drawing a new tile.");
        }
    }

    private void bookTile(ActionMessage request){
        String nickname = clientController.getNickname();
        ViewShipBoard playerShipboard = clientController.getViewFlightBoard().getViewShipBoard(nickname);

        ComponentTile tileInHand = playerShipboard.getTileInHand();
        boolean hasTileInHand = tileInHand != null;
        boolean canBookTiles = playerShipboard.getBookedTiles().size() < 2;

        if(!hasTileInHand){
            clientController.getView().showErrorMessage("You have not a tile in hand");
            return;
        }
        else if(!canBookTiles){
            clientController.getView().showErrorMessage("You have already booked 2 tiles");
            clientController.getView().showTileInHand(tileInHand);
            return;
        }

        ActionMessage messageToServer = new ActionMessage("bookTile", clientController.getNickname());
        clientController.getServerProxy().send(messageToServer);
    }

    private void poseTile(ActionMessage request){
        String nickname = clientController.getNickname();
        boolean hasTileInHand = clientController.getViewFlightBoard().getViewShipBoard(nickname).getTileInHand() != null;
        if(!hasTileInHand){
            clientController.getView().showErrorMessage("You have not a tile in hand");
            return;
        }

        ActionMessage messageToServer = new ActionMessage("poseTile", clientController.getNickname());
        clientController.getServerProxy().send(messageToServer);
    }

    private void placeDrawnTileAt(ActionMessage request){
        ViewShipBoard shipBoard = clientController.getViewFlightBoard().getViewShipBoard(clientController.getNickname());

        Coordinates coordinates = (Coordinates) request.getData("coordinates");

        if(canTileBeFixedAt(coordinates)){
            Direction direction = (Direction) request.getData("direction");

            boolean hasPlayerTileInHand = shipBoard.getTileInHand() != null;
            if(!hasPlayerTileInHand){
                clientController.getView().showErrorMessage("You can't place the selected tile");
                return;
            }

            String tileFileName = shipBoard.getTileInHand().getFileName();

            sendTilePlacementToServer(tileFileName, direction, coordinates);
        }
        else{
            clientController.getView().showErrorMessage("You can't place at that position");
            clientController.getView().showTileInHand(clientController.getViewFlightBoard().getViewShipBoard(clientController.getNickname()).getTileInHand());
        }
    }

    private void placeBookedTileAt(ActionMessage request){
        ViewShipBoard shipBoard = clientController.getViewFlightBoard().getViewShipBoard(clientController.getNickname());

        Coordinates coordinates = (Coordinates) request.getData("coordinates");

        if(canTileBeFixedAt(coordinates)){
            int bookedTileIndex = (int) request.getData("bookedTileIndex");
            Direction direction = (Direction) request.getData("direction");

            boolean hasPlayerTileAmongBooked = bookedTileIndex < shipBoard.getBookedTiles().size();
            if(!hasPlayerTileAmongBooked){
                clientController.getView().showErrorMessage("You can't place the selected tile");
                return;
            }

            String tileFileName = shipBoard.getBookedTiles().get(bookedTileIndex).getFileName();

            sendTilePlacementToServer(tileFileName, direction, coordinates);
        }
        else
            clientController.getView().showErrorMessage("You can't place at that position");
    }

    private void sendTilePlacementToServer(String tileFileName, Direction direction, Coordinates coordinates){
        ActionMessage messageToServer = new ActionMessage("placeTileAt", clientController.getNickname());
        messageToServer.setData("coordinates", coordinates);
        messageToServer.setData("direction", direction);
        messageToServer.setData("fileName", tileFileName);
        clientController.getServerProxy().send(messageToServer);
    }

    private void playerFinished(ActionMessage request){
        ViewShipBoard shipBoard = clientController.getViewFlightBoard().getViewShipBoard(clientController.getNickname());

        if(!shipBoard.getPlayerHasEndedBuilding()){
            ActionMessage messageToServer = new ActionMessage("playerIsDone", clientController.getNickname());
            clientController.getServerProxy().send(messageToServer);

            shipBoard.setPlayerHasEndedBuilding();
        }
        else{
            clientController.getView().showErrorMessage("You have already ended building your shipboard");
        }
    }

    private void flipHourGlass(ActionMessage request){
        ActionMessage messageToServer = new ActionMessage("flipHourGlass", clientController.getNickname());
        clientController.getServerProxy().send(messageToServer);
    }

    private void showAllTile(ActionMessage request){
        clientController.getView().showTileBunch(clientController.getViewFlightBoard().getClientTilesBunch());
    }

    private void showShipBoard(ActionMessage request){
        String playerName = (String) request.getData("playerName");
        if(playerName.isBlank()) clientController.getView()
                .showShipboard(clientController.getViewFlightBoard().getViewShipBoard(clientController.getNickname()));
        else clientController.getView().showShipboard(clientController.getViewFlightBoard().getViewShipBoard(playerName));
    }

    // ---------- Messages from Server ----------

    private void setTileAsBooked(ActionMessage actionMessage){
        String fileName = (String) actionMessage.getData("fileName");
        String playerNickname = actionMessage.getReceiver();

        ViewShipBoard playerShipBoard = clientController.getViewFlightBoard().getViewShipBoard(playerNickname);

        ComponentTile selectedTile = playerShipBoard.getTileInHand();
        if(selectedTile != null){
            playerShipBoard.setTileInHand(null);
            playerShipBoard.bookTile(selectedTile);

            if(playerNickname.equals(clientController.getNickname())){
                clientController.getView().showShipboard(playerShipBoard);  // view update
            }
        }
    }

    private void setTileAsFree(ActionMessage actionMessage){
        String fileName = (String) actionMessage.getData("fileName");
        String playerNickname = actionMessage.getReceiver();

        ViewShipBoard playerShipBoard = clientController.getViewFlightBoard().getViewShipBoard(playerNickname);
        ClientComponentTilesBunch tilesBunch = clientController.getViewFlightBoard().getClientTilesBunch();

        ComponentTile tile = playerShipBoard.getTileInHand();
        if(tile != null){
            tilesBunch.restoreUncoveredTile(tile);
            playerShipBoard.setTileInHand(null);

            if(playerNickname.equals(clientController.getNickname())){
                clientController.getView().showShipboard(playerShipBoard);  // view update
            }
        }
    }

    private void showCoveredTile(ActionMessage actionMessage){
        String fileName = (String) actionMessage.getData("fileName");
        String playerNickname = actionMessage.getReceiver();

        ViewShipBoard playerShipBoard = clientController.getViewFlightBoard().getViewShipBoard(playerNickname);

        ComponentTile tile = clientController.getViewFlightBoard().getClientTilesBunch().takeCoveredTile(fileName);
        if(tile != null){
            playerShipBoard.setTileInHand(tile);

            if(playerNickname.equals(clientController.getNickname())){
                clientController.getView().showShipboard(playerShipBoard);
                clientController.getView().showTileInHand(tile);  // view update
            }
        }
    }

    private void showUncoveredTile(ActionMessage actionMessage){
        String fileName = (String) actionMessage.getData("fileName");
        String playerNickname = actionMessage.getReceiver();

        ViewShipBoard playerShipBoard = clientController.getViewFlightBoard().getViewShipBoard(playerNickname);

        ComponentTile tile = clientController.getViewFlightBoard().getClientTilesBunch().takeUncoveredTile(fileName);
        if(tile != null){
            playerShipBoard.setTileInHand(tile);

            if(playerNickname.equals(clientController.getNickname())){
                clientController.getView().showShipboard(playerShipBoard);  // view update
                clientController.getView().showTileInHand(tile);  // view update
            }
        }
    }

    private void placeTile(ActionMessage actionMessage){
        String playerNickname = actionMessage.getReceiver();

        String fileName = (String) actionMessage.getData("fileName");
        Coordinates coordinates = (Coordinates) actionMessage.getData("coordinates");
        Direction direction = (Direction) actionMessage.getData("direction");

        ViewShipBoard playerShipBoard = clientController.getViewFlightBoard().getViewShipBoard(playerNickname);

        ComponentTile tileInHand = playerShipBoard.getTileInHand();
        boolean tileIsInHand = false;
        if(tileInHand != null)
            tileIsInHand = tileInHand.getFileName().equals(fileName);
        boolean tileIsBooked = playerShipBoard.getBookedTiles().stream().anyMatch((tile)->tile.getFileName().equals(fileName));

        // seeking for tile to place
        ComponentTile tile = null;
        if(tileIsInHand){
            tile = playerShipBoard.getTileInHand();
            playerShipBoard.setTileInHand(null);
        }
        else if(tileIsBooked){
            tile = playerShipBoard.takeBookedTile(fileName);
        }

        if(tile != null){
            playerShipBoard.fixComponentTile(tile, coordinates, direction);

            if(playerNickname.equals(clientController.getNickname())){
                clientController.getView().showShipboard(playerShipBoard);  // view update
            }
        }
    }

    private void setDeckAsHidden(ActionMessage actionMessage){
        String playerNickname = (String) actionMessage.getData("player");
        Integer deckIndex =  actionMessage.getInt("deckNumber");

        ViewFlightBoard flightBoard = clientController.getViewFlightBoard();

        flightBoard.setWhoIsViewingDecks(deckIndex, playerNickname);

        // to communicate the user that the deck he required is now being viewed by another player
        ViewShipBoard thisClientShipboard = clientController.getViewFlightBoard().getViewShipBoard(clientController.getNickname());
        int deckIndexPlayerIsWaitingFor = thisClientShipboard.getDeckIndexPlayerIsWaitingFor();

        if(playerNickname.equals(clientController.getNickname())){
            clientController.getView().showDeck(flightBoard.getDeck(deckIndex));
            thisClientShipboard.setDeckIndexPlayerIsWaitingFor(-1);
        }
        else if(deckIndex == deckIndexPlayerIsWaitingFor){
            clientController.getView().showMessage("Another player is currently viewing the deck you asked for");
            thisClientShipboard.setDeckIndexPlayerIsWaitingFor(-1);
        }
    }

    private void setDeckAsAvailable(ActionMessage actionMessage){
        String playerNickname = (String) actionMessage.getData("player");
        Integer deckIndex =  actionMessage.getInt("deckNumber");

        clientController.getViewFlightBoard().setWhoIsViewingDecks(deckIndex, null);

        if(playerNickname.equals(clientController.getNickname())){
            ViewShipBoard shipBoard = clientController.getViewFlightBoard().getViewShipBoard(playerNickname);
            clientController.getView().showShipboard(shipBoard);
        }
    }

    private void setPlayerAsDone(ActionMessage actionMessage){
        String playerNickname = (String) actionMessage.getData("player");

        clientController.getViewFlightBoard().getViewShipBoard(playerNickname).setPlayerHasEndedBuilding();

        if(playerNickname.equals(clientController.getNickname())){
            clientController.getView().showMessage("You have ended up building your shipboard. Wait for other player ending, too");
        }
    }

    private void hourGlassFlippingPermission(ActionMessage actionMessage) {
        Boolean isPermitted = (Boolean) actionMessage.getData("permit");

        if(isPermitted){
            Instant startingServerTimerTimestamp = (Instant) actionMessage.getData("startingTimerTimestamp");
            currentHourGlassFlipDeadline = startingServerTimerTimestamp.plusSeconds(90);

            // TODO: code that shows timer progress at each second
            // int timerClientRetard = (int) Duration.between(Instant.now(), startingServerTimerTimestamp).getSeconds();
            // int lastingHourglassSeconds = 90 - timerClientRetard;
            // startTimerInView(lastingHourglassSeconds);

            isLastHourGlassFlip = (Boolean) actionMessage.getData("lastTime");
            if(isLastHourGlassFlip){
                clientController.getView().showMessage("This is last hourglass flip! 90 seconds remain!");
            }
            else {
                clientController.getView().showMessage("HourGlass has been flipped!");
            }
        }
    }

    private void endShipBoardBuildingState(ActionMessage actionMessage){
        //TODO: show a messages to client (dynamic countdown?)

        clientController.setState(new ClientShipboardCheckState(clientController));
        clientController.getCurrentState().run();
    }

    private void startTimerInView(int seconds){
        ViewFlightBoard flightBoard = clientController.getViewFlightBoard();
        flightBoard.setTimer(seconds);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(
                () -> {
                    int currentSecondsLeft = flightBoard.getTimer();
                    if(currentSecondsLeft > 0) {
                        clientController.getView().showHourGlass((long) currentSecondsLeft);
                        flightBoard.setTimer(currentSecondsLeft - 1);
                    }
                    else{
                        scheduler.shutdown();
                    }
                },
                0,
                1,
                TimeUnit.SECONDS
        );
    }
}
