package serverSide.code;


import gbuiLib.ProgramWideVariable;
import javafx.application.Application;
import javafx.stage.Stage;
import gbuiLib.GBSockets.*;
import org.apache.catalina.LifecycleException;

import javax.servlet.ServletException;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;


public class Main{

    public static File runningDirectory = new File(System.getProperty("userDir"), "data");

    private static HTTPManager manager;

    public static void main(String args[]) {
        runningDirectory.mkdirs();
        ProgramWideVariable.initializeDefaults(ScoutingVars::initialize);
        Thread thread = new Thread(() -> manager = new HTTPManager());
        thread.start();
        ServerManager manager = new ServerManager();
        Thread multiCast = new Thread(() -> {
            try {
                DatagramSocket socket = new DatagramSocket();
                socket.connect(new InetSocketAddress("234.0.0.0", 8000));
                while(true) {
                    socket.send(new DatagramPacket("Hi I'm hosting".getBytes(), 0, 14));
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {}
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
