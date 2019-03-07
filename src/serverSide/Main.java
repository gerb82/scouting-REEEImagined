package serverSide;


import connectionIndependent.eventsMapping.ScoutingTreesManager;
import gbuiLib.ProgramWideVariable;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;


/* TODO:
    game priorities - getters and setters (getters done, setters are irrelevant for now)
    ffmpeg - ON HOLD
 */
public class Main extends Application {

    public static File runningDirectory = new File(System.getProperty("userDir"), "data");

    private static HTTPManager manager;

    public static void main(String args[]) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        runningDirectory.mkdirs();
        ProgramWideVariable.initializeDefaults(ScoutingVars::initialize);
        ScoutingTreesManager.initialize(false);
        Thread thread = new Thread(() -> manager = new HTTPManager());
        thread.start();
        ServerManager manager = new ServerManager();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ControlPanel.fxml"));
        loader.setController(manager);
        primaryStage.setScene(new Scene(loader.load()));
        primaryStage.show();
        Thread multiCast = new Thread(() -> {
            try {
                DatagramSocket socket = new DatagramSocket();
                socket.connect(new InetSocketAddress("234.0.0.0", 8000));
                while (true) {
                    try {
                        socket.send(new DatagramPacket("Hi I'm hosting".getBytes(), 0, 14));
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                        }
                    } catch (SocketException e) {
                    }
                }
            } catch (SocketException e) {
                throw new Error(e);
            } catch (IOException e) {
                throw new Error(e);
            }
        });
        multiCast.start();
    }
}
