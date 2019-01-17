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

    public static File runningDirectory = new File("C:\\Users\\Programmer\\Desktop\\testVids");

    private static HTTPManager manager;

    public static void main(String args[]) {
        ProgramWideVariable.initializeDefaults(ScoutingVars::initialize);
        Thread thread = new Thread(){
            @Override
            public void run() {
                manager = new HTTPManager();
            }
        };
        thread.start();
        DataBaseManager database = new DataBaseManager();

    }

}
