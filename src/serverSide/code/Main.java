package serverSide.code;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utilities.LoaderUtil;

public class Main extends Application{


    @Override
    public void start(Stage primaryStage) {
        FXMLLoader loader = LoaderUtil.set(MainController.class);
        Parent root = loader.getRoot();
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String args[]){
        launch(args);
    }
}
