package connectionIndependent.Scrawings;

import connectionIndependent.Scrawings.hitboxes.MyCircGroup;
import connectionIndependent.Scrawings.hitboxes.MyPolyGroup;
import connectionIndependent.Scrawings.scrawtypes.ScrawRecipe;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;


public class Main extends Application {
    private static Pane pane = new Pane();

    private File runningDirectory = new File(new File(System.getProperty("userDir"), "data"), "config");

    public static Pane getPane() {
        return pane;
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        Scene scene = new Scene(pane, 700, 243);
        ScrawingsManager.initialize(true);
        RemoteLoader.setLoadDirectory(runningDirectory.getPath());
        ScrawingsManager.getInstance().loadDirectory(runningDirectory, null);
        ScrawRecipe scraw = ScrawingsManager.getInstance().createScraw();
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
