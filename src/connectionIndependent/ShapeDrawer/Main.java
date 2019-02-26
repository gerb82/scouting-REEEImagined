package connectionIndependent.ShapeDrawer;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


public class Main extends Application {
    private static Pane pane = new Pane();


    public static Pane getPane() {
        return pane;
    }

    @Override
    public void start(Stage primaryStage){
        pane.setManaged(true);
        Scene scene = new Scene(pane, 625, 500);
        scene.heightProperty().addListener((observable, oldValue, newValue) -> pane.requestLayout());
        scene.widthProperty().addListener((observable, oldValue, newValue) -> pane.requestLayout());
        Editor.initialize(true);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
