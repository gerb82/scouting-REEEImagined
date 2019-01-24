package configurator;

import connectionIndependent.eventsMapping.ScoutingEventTree;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Configurator extends Application {

    @FXML
    private ScoutingEventTree mainTree;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Editor.fxml"));
        loader.setController(this);
        primaryStage.setScene(new Scene(loader.load()));
        primaryStage.show();
        System.out.println(mainTree.getWidth() + " " + mainTree.getHeight());
    }

    public static void main(String[] args) {
        launch(args);
    }
}