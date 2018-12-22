package scouterSide;

import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Slider;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

public class ScouterUI {

    private MediaPlayer mediaPlayer;
    private Media media;
    @FXML private MediaView mediaView;
    @FXML private Pane controlsPanel;
    @FXML private MenuButton gameSelect;
    @FXML private MenuButton teamSelect;
    @FXML private Slider volumeSlider;
    @FXML private VBox eventsLog;
    @FXML private FlowPane events;

    private ScouterUI(){
    }

    @FXML
    private void back10(){
        movePlayerTime(-10);
    }

    @FXML
    private void back5(){
        movePlayerTime(-5);
    }

    @FXML
    private void forward5(){
        movePlayerTime(5);
    }

    @FXML
    private void forward10(){
        movePlayerTime(10);
    }

    private void movePlayerTime(int dif){
        mediaPlayer.seek(mediaPlayer.getCurrentTime().add(Duration.seconds(dif)));
    }

    @FXML
    private void loadGame(){

    }

    @FXML
    private void reload(){

    }
}
