package it.polimi.ingsw.client.states;


import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.client.*;

import java.util.ArrayList;

public class ClientSetupState extends ClientState {
    int currentAskedInformationIndex;
    private String playerNickname;
    private int newGamePlayersNum;
    private final int newGameLevel = 2;


    public ClientSetupState(ClientController clientController) {
        super(clientController);
        currentAskedInformationIndex = 0;

        methodsFromViewMap.put("answer", this::handleUserAnswers);
    }

    @Override
    public void run() {
        clientController.getView().showMessage("Enter your nickname");
    }

    @Override
    public void processFromServer(ActionMessage actionMessage){}

    @Override
    public void processFromView(ActionMessage actionMessage){
        try{
            methodsFromViewMap.get(actionMessage.getActionName()).accept(actionMessage);
        }
        catch (NullPointerException e){
            clientController.getView().showMessage("The selected method is not valid in this state");
        }
    }

    /** Handles the answers from the user about nickname and number of player of a new game
     *
     * @param actionMessage containing the player nickname and number of players in a new game
     */
    private void handleUserAnswers(ActionMessage actionMessage){
        String answer = ((ArrayList<String>) actionMessage.getData("answer")).getFirst();

        if(currentAskedInformationIndex == 0){
            playerNickname = answer;
            clientController.setNickname(answer);
            currentAskedInformationIndex++;
            clientController.getView().showMessage("Select number of player 2-4");
        }
        else if(currentAskedInformationIndex == 1) {
            try{
                newGamePlayersNum = Integer.parseInt(answer);
                if (newGamePlayersNum > 4 || newGamePlayersNum < 2){
                    clientController.getView().showErrorMessage("The selected player number is invalid");
                    clientController.getView().showMessage("Select number of player 2-4");
                }
                else {
                    currentAskedInformationIndex++;
                    setUpGame();
                }
            }
            catch(NumberFormatException e){
                clientController.getView().showErrorMessage("You should select a number");
            }
        }
    }

    /** Sets up the next state and starts the connection with the server
     */
    private void setUpGame(){
        try{
            clientController.setState(new ClientLobbyState(clientController));

            boolean successfullyConnection = this.clientController.getServerProxy().connectToServer(playerNickname, newGamePlayersNum, newGameLevel);
            if (successfullyConnection){
                    this.clientController.getCurrentState().run();
            }
            else
                throw new Exception("Connection to server failed");
        }
        catch (Exception e){
            clientController.getView().showMessage(e.getMessage());
            putUserInSetupState();
        }
    }
}
