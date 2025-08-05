package it.polimi.ingsw.model.cards.server;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.model.Cargo.CargoType;
import it.polimi.ingsw.model.ComponentTile.Connector;
import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.Direction;
import it.polimi.ingsw.model.ModelPublisher;
import it.polimi.ingsw.model.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CardNotifier {
    ModelPublisher modelPublisher;

    public CardNotifier(ModelPublisher modelPublisher) {
        this.modelPublisher = modelPublisher;
    }


    public boolean askEngineUsage(Player player){
        Map<Coordinates, Integer> doubleEngines = player.getShipBoard().getPotentialEnginePowerDouble();
        Map<Coordinates, Map<Integer, Map<String, Connector[]>>> batteries = player.getShipBoard().getBatteriesTilesPosition();
        Map<Coordinates, Integer> actualBatteries = new HashMap<>();
        batteries.forEach((coordinates, map) -> {
            map.forEach((integer, map2) -> {
                actualBatteries.put(coordinates, integer);
            });
        });
        boolean enoughResources;

        if(batteries.isEmpty() || doubleEngines.isEmpty()){
            enoughResources = false;
            notEnoughResources(player);
        }
        else {
            enoughResources = true;
            ActionMessage am = new ActionMessage("askEngineUsage", "server");
            am.setReceiver(player.getNickname());
            am.setData("doubleEngines", doubleEngines);
            am.setData("batteries", actualBatteries);
            modelPublisher.notify(am);
        }
        return enoughResources;

    }

    @Deprecated
    public void askEngineUsage(List<Player> players){
        players.forEach(player -> askEngineUsage(player));
    }

    public void sendCurrentShot(int shotIndex, int currentRoll){
        ActionMessage am = new ActionMessage("currentShot", "server");
        am.setData("shotIndex", shotIndex);
        am.setData("currentRoll", currentRoll);
        modelPublisher.notify(am);
    }

    public void sendCurrentShot(int shotIndex, int currentRoll, Player player){
        ActionMessage am = new ActionMessage("currentShot", "server");
        am.setData("shotIndex", shotIndex);
        am.setData("currentRoll", currentRoll);
        am.setReceiver(player.getNickname());
        modelPublisher.notify(am);
    }

    public boolean askCannonsUsage(Player player) {
        Map<Coordinates,Integer> doubleCannons = player.getShipBoard().getPotentialFirePowerDouble();
        Map<Coordinates, Map<Integer, Map<String, Connector[]>>> batteries = player.getShipBoard().getBatteriesTilesPosition();
        boolean enoughResources;

        if(batteries.isEmpty() || doubleCannons.isEmpty()){
            enoughResources = false;
            notEnoughResources(player);
        }
        else {
            ActionMessage am = new ActionMessage("askCannonsUsage", "server");
            am.setReceiver(player.getNickname());
            am.setData("cannons", player.getShipBoard().getPotentialFirePowerDouble());
            am.setData("batteries", player.getShipBoard().getBatteriesTilesPosition());
            modelPublisher.notify(am);
            enoughResources = true;
        }
        return enoughResources;
    }

    @Deprecated
    public void askCannonsUsage(List<Player> players) {
        for(Player player : players){
            askCannonsUsage(player);
        }
    }

    public void askCargoToRemove(Player player, Map<Coordinates, CargoType> cargo){
        ActionMessage am = new ActionMessage("askCargoToRemove", "server");
        am.setReceiver(player.getNickname());
        am.setData("possibleCargos", cargo);
    }

    public void askToLoadCargo(ArrayList<CargoType> cargoList, Player player) {
        ActionMessage am = new ActionMessage("askToLoadCargo", "server");
        am.setData("cargo", cargoList);
        am.setReceiver(player.getNickname());
    }

    public void askCrewToRemove(Player player){
        ActionMessage am = new ActionMessage("askCrewToRemove", "server");
        am.setReceiver(player.getNickname());
        am.setData("cabins", player.getShipBoard().getCrewPositions());
    }

    public void askCrewToRemove(List<Player> players){
        players.forEach(this::askCrewToRemove);
    }

    public void appendCrewCabins(Player player, ActionMessage am){
        am.setData("cabins", player.getShipBoard().getCrewPositions());
    }

    public void appendPlayerPositions(ArrayList<Player> players, ActionMessage am) {
        Map<Player, Integer> positions = new HashMap<>();

        players.forEach(player -> {
            positions.put(player, player.getAbsPosition());
        });

        am.setData("playerPositions", positions);
    }

    public void appendPossibleCargo(Player player, ActionMessage am) {
        am.setData("allCargos", player.getShipBoard().getPossibleCargoPlacement(false)); //TODO ADD TO RMI
        am.setData("specialCargos",player.getShipBoard().getPossibleCargoPlacement(true)); //TODO ADD TO RMI
    }

    public void notifyTrunk(ArrayList<ArrayList<Coordinates>> trunks, Player player){
        ActionMessage am = new ActionMessage("branchingResolve", "server");

        am.setReceiver(player.getNickname());
        am.setData("trunks", trunks);
        modelPublisher.notify(am);
    }

    public boolean sendPossibleShields(Player player) {
        boolean enoughResources;
        Map<Coordinates, Map<String, Map<Direction[], Connector[]>>> shields = player.getShipBoard().getShieldTilesPosition();
        if(shields.isEmpty() || player.getShipBoard().getTotalBatteries() == 0){
            enoughResources = false;
            notEnoughResources(player);
        }
        else{
            enoughResources = true;

            ActionMessage am = new ActionMessage("appendPossibleShields", "server"); //TODO RMI
            am.setReceiver(player.getNickname());
            am.setData("possibleShields", player.getShipBoard().getShieldTilesPosition());
            am.setData("batteries", player.getShipBoard().getBatteriesTilesPosition());
            modelPublisher.notify(am);
        }
        return enoughResources;

    }

    @Deprecated
    public void sendPossibleShields(List<Player> players) {
        players.forEach(this::sendPossibleShields);
    }

    public void appendPossibleShields(Player player, ActionMessage am) {
        am.setData("allShields", player.getShipBoard().getShieldTilesPosition());
    }

    private void notEnoughResources(Player player){
        ActionMessage am = new ActionMessage("notEnoughResources", "server");
        am.setReceiver(player.getNickname());
        modelPublisher.notify(am);
    }


}
