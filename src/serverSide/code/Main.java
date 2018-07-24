package serverSide.code;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utilities.ConsoleManager;
import utilities.GBLoader.ClassToNamesMap;
import utilities.LoaderUtil;

import java.io.File;


public class Main extends Application{


    @Override
    public void start(Stage primaryStage) {
        ConsoleManager.enableConsole(Main.class, false);
        FXMLLoader loader = LoaderUtil.set(MainController.class);
        Parent root = loader.getRoot();
        Scene scene = new Scene(root, 600, 400);
        ClassToNamesMap.addToDictionary(new File((LoaderUtil.pathMaker(Main.class, "GBDConfig")).getPath().replace("%20", " ")));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String args[]){
        launch(args);
    }
}
