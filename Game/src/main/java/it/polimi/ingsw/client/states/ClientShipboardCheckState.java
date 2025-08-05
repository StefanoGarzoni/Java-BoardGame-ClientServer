package it.polimi.ingsw.client.states;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.client.ClientController;
import it.polimi.ingsw.model.Coordinates;
import it.polimi.ingsw.model.ShipBoard;
import it.polimi.ingsw.model.clientModel.ViewShipBoard;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ClientShipboardCheckState extends ClientState{

    private ArrayList<ArrayList<Coordinates>> wrongCouples;
    private ArrayList<Coordinates> wrongEngines;
    private ArrayList<ArrayList<Coordinates>> groups;

    private int groupsSize;
    private int currentGroup;

    private ArrayList<Coordinates> tilesToRemove;

    private Boolean askedTileToDelete;
    private Boolean receivedGroups;
    private int nthCouple;
    private int totCouple;
    private Boolean askedGroupToKeep;

    /* service methods */

    ClientShipboardCheckState(ClientController clientController){
        super(clientController);
        this.askedTileToDelete = false;
        this.receivedGroups = false;
        this.tilesToRemove = new ArrayList<>();
        this.nthCouple = 0;
    }

    @Override
    public void run() {
        this.methodsFromServerMap.put("chooseWhichGroupsKeep", this::chooseWhichGroupsKeep);
        this.methodsFromServerMap.put("wrongTilesCouplings", this:: ReceiveWrongTilesCoupling);
        this.methodsFromServerMap.put("tilesBranchesRemoved", this::removeFromServer);
        this.methodsFromServerMap.put("distributeResourcesState", this::nextState);
        this.methodsFromServerMap.put("yourShipboardIsPerfect", this::communicatePerfectShipBoard);

        this.methodsFromViewMap.put("selectTileToRemove", this::selectTileToRemove);
        this.methodsFromViewMap.put("keepCurrentGroup", this::selectIfKeepCurrentGroup);
        this.methodsFromViewMap.put("showShipBoard", this::showShipBoard);

        clientController.getView().showMessage("Building has ended! It's time to check your shipboard!");
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
            try {
                methodsFromViewMap.get(actionMessage.getActionName()).accept(actionMessage);
            } catch (NullPointerException e) {
                clientController.getView().showMessage("The selected method is not valid in this state");
            }
        }
    }

    private void removeFromServer(ActionMessage actionMessage){
        ViewShipBoard playerShipboard = this.clientController.getViewFlightBoard().getViewShipBoard(actionMessage.getReceiver());

        ShipBoard shipBoard = (ShipBoard) actionMessage.getData("shipBoard");
        playerShipboard.updateViewShipBoard(shipBoard);

        int playerAbsolutePosition = (int) actionMessage.getData("playerAbsolutePosition");
        playerShipboard.setAbsPosition(playerAbsolutePosition);

        if(actionMessage.getReceiver().equals(clientController.getNickname())){
            clientController.getView().showMessage("This is you shipboard after a check. Waits for other players!");
            clientController.getView().showShipboard(playerShipboard);
        }
        else{
            clientController.getView().showMessage(actionMessage.getReceiver()+" ended up checking his shipboard");
        }
    }

    private void sendToServerProxy(ActionMessage actionMessage){
        this.clientController.getServerProxy().send(actionMessage);
    }

    private void showShipBoard(ActionMessage request){
        String playerName = (String) request.getData("playerName");
        if(playerName.isBlank()) clientController.getView()
                .showShipboard(clientController.getViewFlightBoard().getViewShipBoard(clientController.getNickname()));
        else clientController.getView().showShipboard(clientController.getViewFlightBoard().getViewShipBoard(playerName));
    }

    private void nextState(ActionMessage actionMessage){
        this.clientController.setState(new ClientDistributeResourcesState(this.clientController
        ));
        clientController.getCurrentState().run();
    }

    /* wrong tiles request to client */

    /** Handles the request of the server to correct the current shipboard
     *
     * @param actionMessage containing wrong engines' coordinates and wrong couples
     */
    private void ReceiveWrongTilesCoupling(ActionMessage actionMessage){
        if(actionMessage.getReceiver().equals(clientController.getNickname())){
            wrongEngines = (ArrayList<Coordinates>) actionMessage.getData("wrongEngines");
            wrongCouples = (ArrayList<ArrayList<Coordinates>>) actionMessage.getData("couplings");

            if(wrongEngines != null && !wrongEngines.isEmpty())
                showAllWrongEngines();

            if(wrongCouples != null && !wrongCouples.isEmpty()){
                totCouple = wrongCouples.size();
                askTileToDelete();
            }
            else{
                sendTilesToRemove();
            }
        }
    }

    private void showAllWrongEngines(){
        clientController.getView().showMessage("These are engines that will be removed because are not pointing to south");

        clientController.getView().showShipboard(clientController.getViewFlightBoard().getViewShipBoard(clientController.getNickname()));
        clientController.getView().showHighlightedTiles(wrongEngines);
    }

    private void askTileToDelete() {
        ArrayList<Coordinates> wrongCouple = wrongCouples.get(nthCouple);
        this.clientController.getView().showMessage("Select between " + wrongCouple.get(0) +" and "+ wrongCouple.get(1)+" to remove the "+nthCouple+"/"+(totCouple-1)+" wrong couple");
        this.askedTileToDelete = true;
    }

    /** Handles the answer of the player when asked to solve a wrong connection
     *
     * @param actionMessage containing the player's answer
     */
    private void selectTileToRemove(ActionMessage actionMessage) {
        if(askedTileToDelete) {
                Coordinates coordinates = (Coordinates) actionMessage.getData("coordinates");
                if (wrongCouples.get(nthCouple).contains(coordinates)) {
                    tilesToRemove.add(coordinates);
                    nthCouple++;
                    askedTileToDelete = false;
                } else{
                    clientController.getView().showErrorMessage("You can't delete that tile");
                }

                if (nthCouple < totCouple)
                    askTileToDelete();
                else
                    sendTilesToRemove();
        }
        else
            clientController.getView().showErrorMessage("It's not the moment to delete");

    }

    private void sendTilesToRemove(){
        tilesToRemove.addAll(this.wrongEngines);

        ActionMessage actionMessage = new ActionMessage("playerChoice", clientController.getNickname());
        actionMessage.setData("choicesToRemove", this.tilesToRemove);
        sendToServerProxy(actionMessage);
    }

    private void communicatePerfectShipBoard(ActionMessage actionMessage){
        if(actionMessage.getReceiver().equals(clientController.getNickname())){
            clientController.getView().showMessage("You shipboard is perfect! Waits for other players");
        }
    }

    /* shipboard branching handling */

    /** Handles the message from the server containing all shipboard branches that was created during check state
     *
     * @param actionMessage containing shipboard's branches
     */
    private void chooseWhichGroupsKeep(ActionMessage actionMessage){
        if(actionMessage.getReceiver().equals(clientController.getNickname())){
            groups = (ArrayList<ArrayList<Coordinates>>) actionMessage.getData("groups");

            groupsSize = groups.size();
            currentGroup = 0;
            receivedGroups = true;
            askIfKeepCurrentGroup();
        }
    }

    /** Shows a branch among all shipboards branches, so the player can choose whether keep it or loose it
     */
    private void askIfKeepCurrentGroup(){
        clientController.getView().showShipboard(clientController.getViewFlightBoard().getViewShipBoard(clientController.getNickname()));
        this.clientController.getView().showHighlightedTiles(groups.get(currentGroup));

        this.clientController.getView().showMessage("Do you want yo keep the current "+currentGroup+"/"+(groupsSize)+" branch [y/n]?");

        StringBuilder stringBuilder = new StringBuilder();
        for (var c: groups.get(currentGroup))
            stringBuilder.append(c.toString()+" ");
        System.out.println(stringBuilder);

        askedGroupToKeep = true;
    }

    /** Handles user's choice for the current branch
     *
     * @param actionMessage message from player, with his choice
     */
    private void selectIfKeepCurrentGroup(ActionMessage actionMessage){
        String confirmation = (String) actionMessage.getData("answer");
        if (confirmation.equals("y"))
            sendGroupToKeep(currentGroup);
        else {
            if(currentGroup < groupsSize - 1)
                currentGroup++;
            else
                currentGroup = 0;   // if the player hasn't chosen a group

            askIfKeepCurrentGroup();
        }
    }

    /** Sends the branch to keep (chosen by the player) to the server
     *
     * @param i index of the branch to keep
     */
    private void sendGroupToKeep(int i){
        ActionMessage groupToKeep = new ActionMessage("groupsToRemove", clientController.getNickname());
        groupToKeep.setData("numberToKeep", i);
        this.sendToServerProxy(groupToKeep);
    }

}
