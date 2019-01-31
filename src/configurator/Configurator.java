package configurator;

import com.sun.deploy.config.Platform;
import connectionIndependent.eventsMapping.ScoutingEventTree;
import connectionIndependent.eventsMapping.ScoutingTreesManager;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Configurator extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Editor.fxml"));
        ScoutingTreesManager.initialize(false);
        loader.setController(ScoutingTreesManager.getInstance());
        primaryStage.setScene(new Scene(loader.load()));
        ((Pane)loader.getRoot()).prefWidthProperty().bind(primaryStage.getScene().widthProperty());
        ((Pane)loader.getRoot()).prefHeightProperty().bind(primaryStage.getScene().heightProperty());
        primaryStage.show();
        ScoutingTreesManager.getInstance().start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println(ScoutingTreesManager.getInstance().treeAsFXML((byte) 1));
        }));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
