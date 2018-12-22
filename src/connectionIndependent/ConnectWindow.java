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

    private static connect connectorToRun;
    private static InitMainWindow starter;
    private static Stage stage;
    private static ConnectWindow controller;

    public static void start(Stage stage, connect connector, String introText, InitMainWindow starter) throws IOException {
        FXMLLoader loader = new FXMLLoader(ConnectWindow.class.getResource("ConnectWindow.fxml"));
        stage.setScene(new Scene(loader.load()));
        connectorToRun = connector;
        controller.starter = starter;
        controller.stage = stage;
        controller.introText.setText(introText);
        stage.setResizable(false);
        stage.show();
        controller.introText.setLayoutX((controller.pane.getPrefWidth()-controller.introText.getLayoutBounds().getWidth())/2);
    }

    public interface connect{
        boolean connect(String address);
    }

    public interface InitMainWindow {
        void init(Stage stage);
    }

    @FXML
    private void connect(){
        pane.setDisable(true);
        if(connectorToRun.connect(ip.getText())){
            starter.init(stage);
        } else {
            pane.setDisable(false);
        }
    }
}