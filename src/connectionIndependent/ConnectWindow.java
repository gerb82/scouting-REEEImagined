package connectionIndependent;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;


public class ConnectWindow {

    @FXML
    private Text introText;
    @FXML
    private TextField ip;
    @FXML
    private Pane pane;

    private static Connect connectorToRun;
    private static InitMainWindow starter;
    private static ConnectWindow controller;

    public static Scene start(Stage stage, Connect connector, String introText, InitMainWindow starter) throws IOException {
        FXMLLoader loader = new FXMLLoader(ConnectWindow.class.getResource("ConnectWindow.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        controller = loader.getController();
        connectorToRun = connector;
        controller.starter = starter;
        controller.introText.setText(introText);
        controller.introText.setLayoutX((controller.pane.getPrefWidth()-controller.introText.getLayoutBounds().getWidth())/2);
        return scene;
    }

    public interface Connect {
        boolean connect(String address);
    }

    public interface InitMainWindow {
        void init();
    }

    private boolean alreadyInitialized;

    @FXML
    private void connect(){
        String address = ip.getText();
        ip.clear();
        pane.setDisable(true);
        if(connectorToRun.connect(address)){
            if(!alreadyInitialized) {
                starter.init();
                alreadyInitialized = true;
            }
        } else {
            pane.setDisable(false);
        }
    }
}