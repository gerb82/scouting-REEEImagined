package scouterSide;

import connectionIndependent.ConnectWindow;
import connectionIndependent.ScoutingConnections;
import connectionIndependent.ScoutingEvent;
import connectionIndependent.ScoutingPackets;
import gbuiLib.GBSockets.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import gbuiLib.ProgramWideVariable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;

public class MainLogic extends Application{

    private Stage stage;
    private static GBSocket socket;
    private Scene connectionWindow;
    protected static String host;

    @Override
    public void start(Stage primaryStage) throws Exception {
        ProgramWideVariable.initializeDefaults();
        PacketLogger.setDirectory();
        stage = primaryStage;
        ActionHandler handler = new ActionHandler();
        handler.setHandler(ScoutingPackets.SCOUTER_LOADGAME.toString(), ScouterUI::loadNewView);
        SelectorManager selector = new SelectorManager();
        String testConnection1 = ScoutingConnections.SCOUTER.toString();
        socket = new GBSocket(null, testConnection1, false, selector, handler, new GBSocket.SocketConfig(), false);
        connectionWindow = ConnectWindow.start(primaryStage, this::connect, "server side", this::initMainWindow);
    }

    public boolean connect(String address){
        try {
            String[] splitAddress = address.split(":");
            assert (splitAddress.length == 2);
            socket.setAddress(new InetSocketAddress(splitAddress[0], Integer.valueOf(splitAddress[1])));
            host = splitAddress[0];
            return socket.startConnection();
        } catch (IllegalAccessException e) {
            throw new Error("This error should not happen, the connection window's button was clicked when the socket was already connected");
        } catch (AssertionError e){
            return false;
        }
    }

    public void initMainWindow(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MainScreen.fxml"));
            stage.setScene(new Scene(loader.load()));
            ScouterUI controller = loader.getController();
            controller.setMain(this);
            stage.setFullScreen(true);
        } catch (IOException e) {
            throw new Error("MainScreen.fxml file for scouter side could not be loaded");
        }
    }

    private int currentGame;
    private String currentTeam;

    // connect to server

    public PacketLogger.ObservablePacketStatus loadGame(int gameCount, String teamIdentifier) throws BadPacketException {
        currentTeam = teamIdentifier;
        currentGame = gameCount;
        return loadGame();
    }

    public PacketLogger.ObservablePacketStatus loadGame() throws BadPacketException {
        return socket.sendAsPacket(currentGame, currentTeam, ScoutingPackets.SCOUTER_LOADGAME.toString(), true);
    }

    public static void main(String[] args) {
        launch(args);
        socket.close();
    }

    // sync up with games





    // manage UI
}
