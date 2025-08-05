package it.polimi.ingsw.client.TUI;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.client.ClientController;
import it.polimi.ingsw.client.ViewHandler;
import it.polimi.ingsw.model.Cargo.*;
import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.Direction;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

public class TUIHandler implements ViewHandler {

    ClientController controller;
    private HashMap<String, Consumer<String[]>> actions;
    private boolean isAlive;


    public TUIHandler(ClientController controller){
        this.controller = controller;
        isAlive = true;

        actions = new HashMap<>();

        /*----------*
         | Generics |
         *----------*/

        //showAllCommands
        actions.put("showAllCommands", this::showAllCommands);
        actions.put("help", this::showAllCommands);
        actions.put("h", this::showAllCommands);

        //leaveGame
        actions.put("leaveGame", this::leaveGame);

        //showShipboard playername
        actions.put("showShipBoard", this::showShipBoard);
        actions.put("sb", this::showShipBoard);

        //showFlightboard
        actions.put("showFlightBoard", this::showFlightBoard);
        actions.put("fb", this::showFlightBoard);

        /*-------------------*
         | ShipBoardBuilding |
         *-------------------*/

        //viewDeck decknum
        actions.put("viewDeck", this::viewDeck);

        //viewTimeLeft
        actions.put("viewTimeLeft", this::viewTimeLeft);
        actions.put("time", this::viewTimeLeft);

        //poseDeck
        actions.put("poseDeck", this::poseDeck);

        //getCoveredTile
        actions.put("getCoveredTile", this::getCoveredTile);
        actions.put("ctile", this::getCoveredTile);

        //getUncoveredTile {int}
        actions.put("getUncoveredTile", this::getUncoveredTile);

        //bookTile
        actions.put("bookTile", this::bookTile);

        //poseTile
        actions.put("poseTile", this::poseTile);

        //placeDrawnTileAt x,y N/S/W/S
        actions.put("placeDrawnTileAt", this::placeDrawnTileAt);
        actions.put("dplace", this::placeDrawnTileAt);

        //placeBookedTileAt x,y N/S/W/S
        actions.put("placeBookedTileAt", this::placeBookedTileAt);
        actions.put("bplace", this::placeBookedTileAt);

        //flipHourGlass
        actions.put("flipHourGlass", this::flipHourGlass);
        actions.put("flip", this::flipHourGlass);

        //endBuilding
        actions.put("endBuilding", this::endBuilding);

        //showTiles
        actions.put("showAllTiles", this::showAllTiles);
        actions.put("atiles", this::showAllTiles);

        /*--------------*
         | PlayingState |
         *--------------*/
        // stop a card
        actions.put("stopCard", this::stopCard);

        //loadCargo color x,y
        actions.put("loadCargo", this::loadCargo);

        //unloadCargo color x,y
        actions.put("unloadCargo", this::unloadCargo);

        //activateDoubleCannon xCannon,yCannon,xBattery,yBattery
        actions.put("activateDoubleCannon", this::activateDoubleCannon);
        actions.put("dcan", this::activateDoubleCannon);

        //activateShield x,y
        actions.put("activateShield", this::activateShield);
        actions.put("shield", this::activateShield);

        //activateDoubleEngine x,y
        actions.put("activateDoubleEngine", this::activateDoubleEngine);
        actions.put("deng", this::activateDoubleEngine);

        //landOn planetindex
        actions.put("landOn", this::landOn);

        //drawCard
        actions.put("drawCard", this::drawCard);

        /*---------------*
         |  CheckState   |
         *---------------*/

        //selectTileToRemove row,col
        actions.put("selectTileToRemove", this::selectTileToRemove);
        actions.put("rtile", this::selectTileToRemove);

        //keepCurrentGroup y/n
        actions.put("keepCurrentGroup", this::keepCurrentGroup);

        /*---------------------*
         | DistributeResources |
         *---------------------*/

        //setBrownAlien y/n [row,col]
        actions.put("placeBrownAlien",  this::placeBrownAlien);

        //setPurpleAlien y/n [row,col]
        actions.put("placePurpleAlien", this::placePurpleAlien);

    }


    public void answer(String[] args){
        ActionMessage am = new ActionMessage("answer", "tui");

        am.setData("answer", new ArrayList<>(List.of(args)));

        controller.getCurrentState().processFromView(am);
    }

    //We should move this to a separate thread
    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        String input;
        while (true) {
            input = scanner.nextLine();
            String[] args = input.split(" ");
            if (actions.containsKey(args[0])) {
                try {
                    actions.get(args[0]).accept(args);
                } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
                    controller.getView().showErrorMessage("illegal argument");
                }
            } else {
                answer(args);
            }
        }
    }

    //placeBrownAlien y/n [row,col]
    private void placeBrownAlien(String[] args){
        ActionMessage am = new ActionMessage("placeBrownAlien", "tui");
        if(args[1].equals("y")){
            am.setData("confirmation", "y");
            am.setData("coordinates", Coordinates.fromString(args[2]));
        }
        if(args[1].equals("n")){
            am.setData("confirmation", "n");
            am.setData("coordinates", Coordinates.fromString(args[2]));
        }
        controller.getCurrentState().processFromView(am);
    }

    //placePurpleAlien y/n [row,col]
    private void placePurpleAlien(String[] args){
        ActionMessage am = new ActionMessage("placePurpleAlien", "tui");
        if(args[1].equals("y")){
            am.setData("confirmation", "y");
            am.setData("coordinates", Coordinates.fromString(args[2]));
        }
        else{
            am.setData("confirmation", "n");
            am.setData("coordinates", Coordinates.fromString(args[2]));
        }

        controller.getCurrentState().processFromView(am);
    }

    //showShipBoard playerName
    private void showShipBoard(String[] args){
        ActionMessage am = new ActionMessage("showShipBoard", "tui");
        if(args.length == 1) {
            am.setData("playerName", "");
        }
        else am.setData("playerName", args[1]);
        controller.getCurrentState().processFromView(am);
    }


    private void showFlightBoard(String[] args){
        ActionMessage am = new ActionMessage("showFlightBoard", "tui");
        controller.getCurrentState().processFromView(am);
    }

    private void flipHourGlass(String[] args){
        ActionMessage am = new ActionMessage("flipHourGlass", "tui");
        controller.getCurrentState().processFromView(am);
    }

    private void loadCargo(String[] args){
        ActionMessage am = new ActionMessage("loadCargo", "tui");
        switch (args[1]) {
            case "blue" -> am.setData("color", new BlueCargo());
            case "green" -> am.setData("color", new GreenCargo());
            case "yellow" -> am.setData("color", new YellowCargo());
            case "red" -> am.setData("color", new RedCargo());
            default -> throw new IllegalArgumentException();
        }

        am.setData("coordinates", Coordinates.fromString(args[1]));
        controller.getCurrentState().processFromView(am);
    }

    private void unloadCargo(String[] args){
        ActionMessage am = new ActionMessage("unloadCargo", "tui");
        switch (args[1]) {
            case "blue" -> am.setData("color", new BlueCargo());
            case "green" -> am.setData("color", new GreenCargo());
            case "yellow" -> am.setData("color", new YellowCargo());
            case "red" -> am.setData("color", new RedCargo());
        }
        am.setData("coordinates", Coordinates.fromString(args[2]));
        controller.getCurrentState().processFromView(am);
    }

    private void activateDoubleCannon(String[] args){
        ActionMessage am = new ActionMessage("activateDoubleCannon", "tui");
        am.setData("coordinatesCannon", Coordinates.fromString(args[1]));
        am.setData("coordinatesBattery", Coordinates.fromString(args[2]));
        controller.getCurrentState().processFromView(am);
    }

    private void activateShield(String[] args){
        ActionMessage am = new ActionMessage("activateShield", "tui");
        am.setData("coordinatesShield", Coordinates.fromString(args[1]));
        controller.getCurrentState().processFromView(am);
    }

    private void activateDoubleEngine(String[] args){
        ActionMessage am = new ActionMessage("activateDoubleEngine", "tui");
        am.setData("coordinatesEngine", Coordinates.fromString(args[1]));
        am.setData("coordinatesBattery", Coordinates.fromString(args[2]));
        controller.getCurrentState().processFromView(am);
    }

    private void viewDeck(String[] args){
        ActionMessage am = new ActionMessage("viewDeck", "tui");
        am.setData("deckIndex", Integer.parseInt(args[1]));
        controller.getCurrentState().processFromView(am);
    }

    private void getCoveredTile(String[] args){
        ActionMessage am = new ActionMessage("getCoveredTile", "tui");
        controller.getCurrentState().processFromView(am);
    }

    private void showAllTiles(String[] args){
        ActionMessage am = new ActionMessage("showAllTiles", "tui");
        controller.getCurrentState().processFromView(am);
    }

    private void placeDrawnTileAt(String[] args){
        ActionMessage am = new ActionMessage("placeDrawnTileAt", "tui");
        am.setData("coordinates", Coordinates.fromString(args[1]));
        Direction direction = switch(args[2]) {
            case "N" -> Direction.NORTH;
            case "S" -> Direction.SOUTH;
            case "E" -> Direction.EAST;
            case "W" -> Direction.WEST;
            default -> throw new IllegalArgumentException();
        };
        am.setData("direction", direction);
        controller.getCurrentState().processFromView(am);
    }

    private void placeBookedTileAt(String[] args){
        ActionMessage am = new ActionMessage("placeBookedTileAt", "tui");
        am.setData("coordinates", Coordinates.fromString(args[1]));
        Direction direction = switch(args[2]) {
            case "N" -> Direction.NORTH;
            case "S" -> Direction.SOUTH;
            case "E" -> Direction.EAST;
            case "W" -> Direction.WEST;
            default -> throw new IllegalArgumentException();
        };
        am.setData("direction", direction);
        am.setData("bookedTileIndex", Integer.parseInt(args[3]));
        controller.getCurrentState().processFromView(am);
    }

    private void poseTile(String[] args){
        ActionMessage am = new ActionMessage("poseTile", "tui");
        controller.getCurrentState().processFromView(am);
    }

    private void showAllCommands(String[] args){
        ActionMessage am = new ActionMessage("showAllCommands", "tui");
        controller.getCurrentState().processFromView(am);
    }

    private void endBuilding(String[] args){
        ActionMessage am = new ActionMessage("endBuilding", "tui");
        controller.getCurrentState().processFromView(am);
    }

    private void viewTimeLeft(String[] args){
        ActionMessage am = new ActionMessage("viewTimeLeft", "tui");
        controller.getCurrentState().processFromView(am);
    }

    private void poseDeck(String[] args){
        ActionMessage am = new ActionMessage("poseDeck", "tui");
        controller.getCurrentState().processFromView(am);
    }

    private void getUncoveredTile(String[] args){
        ActionMessage am = new ActionMessage("getUncoveredTile", "tui");
        am.setData("tileIndex", Integer.parseInt(args[1]));
        controller.getCurrentState().processFromView(am);
    }

    private void leaveGame(String[] args){
        ActionMessage am = new ActionMessage("leaveGame", "tui");
        controller.getCurrentState().processFromView(am);
    }

    private void landOn(String[] args){
        ActionMessage am = new ActionMessage("landOn", "tui");
        am.setData("planetIndex", Integer.parseInt(args[1]));
        controller.getCurrentState().processFromView(am);
    }

    private void bookTile(String[] args){
        ActionMessage am = new ActionMessage("bookTile", "tui");
        controller.getCurrentState().processFromView(am);
    }

    private void selectTileToRemove(String[] args) {
        ActionMessage am = new ActionMessage("selectTileToRemove", "tui");
        am.setData("coordinates", Coordinates.fromString(args[1]));
        controller.getCurrentState().processFromView(am);
    }

    private void keepCurrentGroup(String[] args) {
        ActionMessage am = new ActionMessage("keepCurrentGroup", "tui");
        am.setData("answer", args[1]);
        controller.getCurrentState().processFromView(am);
    }

    private void drawCard(String[] args){
        ActionMessage am = new ActionMessage("drawCard", "tui");
        controller.getCurrentState().processFromView(am);
    }

    private void stopCard(String[] args){
        ActionMessage am = new ActionMessage("stopCard", "tui");
        controller.getCurrentState().processFromView(am);
    }


    /**
     * kills the handler loop, freeing stdout
     */
    public void killHandler(){
        this.isAlive = false;
    }
}
