package serverSide.code;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utilities.GBLoader.GBDReader;
import utilities.GBSockets.*;
import utilities.GBUILibGlobals;
import utilities.ProgramWideVariable;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;


public class Main extends Application{


    @Override
    public void start(Stage primaryStage) {

    }

    public static GBSocket socket1;
    public static GBSocket socket2;
    public static GBServerSocket server;

    public static void main(String args[]){
        ProgramWideVariable.initializeDefaults();
        PacketLogger.setDirectory(new File("C:\\Users\\User\\Desktop\\socketDebug\\socketLogs"));
        ActionHandler handler = new ActionHandler();
        handler.setHandler("message1", Main::printMessage);
        handler.setHandler("message2", Main::anotherPrintMessage);
        String testConnection1 = "testConnection1";
        String testConnection2 = "testConnection2";
        SelectorManager selector = new SelectorManager();
        GBSocket.SocketConfig config = new GBSocket.SocketConfig();
        config.setHeartBeatDelay(100000000);
        socket1 = new GBSocket(8000, new InetSocketAddress("localhost", 8002), testConnection1, false, selector, handler, config, false);
        socket2 = new GBSocket(8001, new InetSocketAddress("localhost", 8002), testConnection2, false, selector, handler, new GBSocket.SocketConfig(), false);
        server = new GBServerSocket(8002, null, null, false, "testServer");
        server.addConnectionType(testConnection1, handler);
        server.addConnectionType(testConnection2, handler);
        server.initSelector();
        Timer timer = new Timer();
        TimerTask task1 = new TimerTask() {
            @Override
            public void run() {
                if(socket1.startConnection()){
                    System.out.println("socket1 is connected");
                } else{
                    System.out.println("socket 1 failed");
                }
            }
        };
        TimerTask task2 = new TimerTask() {
            @Override
            public void run() {
                if(socket2.startConnection()){
                    System.out.println("socket2 is connected");
                } else {
                    System.out.println("socket2 failed");
                }
            }
        };
        timer.schedule(task1, 0);
        timer.schedule(task2, 0);
    }


    public static void printMessage(ActionHandler.PacketOut packet){

    }

    public static void anotherPrintMessage(ActionHandler.PacketOut packet){

    }
}
