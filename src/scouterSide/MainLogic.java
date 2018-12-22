package scouterSide;

import connectionIndependent.ConnectWindow;
import connectionIndependent.ScoutingConnections;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utilities.GBSockets.ActionHandler;
import utilities.GBSockets.GBSocket;
import utilities.GBSockets.PacketLogger;
import utilities.GBSockets.SelectorManager;
import utilities.ProgramWideVariable;

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


    // connect to server


    public static void main(String[] args) {
        launch(args);
        socket.close();
    }

    // sync up with game





    // manage UI
}
