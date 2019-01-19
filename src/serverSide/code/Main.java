package serverSide.code;


import connectionIndependent.ConnectWindow;
import gbuiLib.ProgramWideVariable;
import javafx.application.Application;
import javafx.stage.Stage;
import gbuiLib.GBSockets.*;
import org.apache.catalina.LifecycleException;

import javax.servlet.ServletException;
import java.io.*;


public class Main{

    public static File runningDirectory = new File(System.getProperty("userDir"), "data");

    private static HTTPManager manager;

    public static void main(String args[]) {
        runningDirectory.mkdirs();
        ProgramWideVariable.initializeDefaults(ScoutingVars::initialize);
        Thread thread = new Thread(() -> manager = new HTTPManager());
        thread.start();
        DataBaseManager database = new DataBaseManager();

    }

}
