package it.polimi.ingsw.client;

import it.polimi.ingsw.client.TUI.TUIHandler;
import it.polimi.ingsw.client.TUI.TUIView;
import it.polimi.ingsw.client.states.ClientSetupState;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.util.Scanner;

public class Client {
    ServerProxy serverProxy;
    ViewHandler viewHandler;

    public Client(ViewHandler viewHandler, ServerProxy serverProxy){
        this.viewHandler = viewHandler;
        this.serverProxy = serverProxy;
    }

    public static void main(String[] args) throws NotBoundException, IOException {
        String serverIpAddress = args[0];
        int socketServerPort = Integer.parseInt(args[1]);

        View view;
        ViewHandler viewHandler;
        ServerProxy serverProxy;
        ClientController clientController;

        Scanner scanner = new Scanner(System.in);

        // asks for TUI or GUI
        System.out.println("Select view: 1.TUI 2.GUI");
        int selectedView = scanner.nextInt();
        while(selectedView != 1 && selectedView != 2){
            System.out.println("Reselect a valid view: 1.TUI 2.GUI");
            selectedView = scanner.nextInt();
        }
        view = switch (selectedView){
            case 1: yield new TUIView();
            //case 2: yield new GUIView();
            default: throw new IllegalStateException("Unexpected value: " + selectedView);
        };

        clientController = new ClientController(view);

        viewHandler = switch (selectedView){
            case 1: yield new TUIHandler(clientController);
            case 2: yield new GUIHandler(clientController);
            default: throw new IllegalStateException("Unexpected value: " + selectedView);
        };


        // asks for RMI or Socket communication protocol
        System.out.println("select: 1.RMI 2.Socket");
        int communicationProtocol = scanner.nextInt();
        while(communicationProtocol != 1 && communicationProtocol != 2){
            System.out.println("reselect a valid option");
            System.out.println("Select view: 1.TUI 2.GUI");
            communicationProtocol = scanner.nextInt();
        }
        serverProxy = switch (communicationProtocol) {
            case 1: yield new RMIServerProxy("127.0.0.1", clientController);
            case 2: yield new SocketServerProxy(serverIpAddress, socketServerPort, clientController);
            default: throw new IllegalStateException("Unexpected value: " + selectedView);
        };

        // clientController first setup
        clientController.setServerProxy(serverProxy);
        clientController.setState(new ClientSetupState(clientController));

        // starts setup state
        clientController.getCurrentState().run();
        viewHandler.run();
    }
}
