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
        ConnectWindow.start(primaryStage, this::connect, "server side", this::initMainWindow);
    }

    public boolean connect(String address){
        try {
            String[] splitAddress = address.split(":");
            socket.setAddress(new InetSocketAddress(splitAddress[0], Integer.valueOf(splitAddress[1])));
            return socket.startConnection();
        } catch (IllegalAccessException e) {
            throw new Error("This error should not happen, the connection window's button was clicked when the socket was already connected");
        } catch (RuntimeException e){
            return false;
        }
    }

    public void initMainWindow(Stage stage){
        try {
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("MainScreen.fxml"))));
            stage.setFullScreen(true);
        } catch (IOException e) {
            throw new Error("MainScreen.fxml file for scouter side could not be loaded");
        }
    }

    private static int currentGame;
    private static String currentTeam;

    // connect to server

    public static PacketLogger.ObservablePacketStatus loadGame(int gameCount, String teamIdentifier) throws BadPacketException {
        currentTeam = teamIdentifier;
        currentGame = gameCount;
        return loadGame();
    }

    public static PacketLogger.ObservablePacketStatus loadGame() throws BadPacketException {
        return socket.sendAsPacket(currentGame, currentTeam, ScoutingPackets.SCOUTER_LOADGAME.toString(), true);
    }

    public static void main(String[] args) {
        launch(args);
        socket.close();
    }

    // sync up with game





    // manage UI
}
