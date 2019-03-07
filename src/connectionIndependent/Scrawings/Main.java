package connectionIndependent.Scrawings;

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
        Scene scene = new Scene(pane, 700, 243);
        ScrawingsManager.initialize(true);
        Scraw scraw = ScrawingsManager.getInstance().createScraw();
        pane.getChildren().add(scraw);
        ScrawingsManager.getInstance().startScrawEdit(scraw);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
