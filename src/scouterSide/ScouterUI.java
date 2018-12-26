package scouterSide;

import connectionIndependent.ScoutingEvent;
import connectionIndependent.ScoutingEventDefinition;
import gbuiLib.GBSockets.ActionHandler;
import gbuiLib.GBSockets.BadPacketException;
import gbuiLib.GBSockets.PacketLogger;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import serverSide.code.Main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    private static ScouterUI self;
    private int currentGameNumber;
    private String currentTeamIdentifier;
    private boolean isConnected;
    private ArrayList<ScoutingEvent> eventList;
    private ArrayList<ScoutingEventDefinition> validEvents;
    private MainLogic main;

    private class OptionChooser extends MenuItem {

        private boolean game;

        private OptionChooser(boolean game){
            super();
            this.setOnAction(this::handleSelected);
        }

        private void handleSelected(Event event){
            if(game){
                currentGameNumber = Integer.valueOf(this.getText());
            } else {
                currentTeamIdentifier = this.getText();
            }
        }

        @Override
        protected void finalize() throws Throwable {
            this.setOnAction(null);
            super.finalize();
        }
    }

    private ScouterUI(){
        self = this;
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
        loader(true);
    }

    private static void loadMedia(String url){
        self.media = new Media(url);
        self.mediaPlayer = new MediaPlayer(self.media);
        self.mediaView.setMediaPlayer(self.mediaPlayer);
    }

    @FXML
    private void reload(){
        loader(false);
    }

    private void loader(boolean forceReload){
        try {
            PacketLogger.ObservablePacketStatus status;
            if(forceReload) {
                status = main.loadGame(currentGameNumber, currentTeamIdentifier);
            } else {
                status = main.loadGame();
            }
            status.addListener(this::statusChangeListener);
        } catch (BadPacketException e) {
            errored("Couldn't reload the game");
        } catch (IllegalStateException e){
            errored("We aren't connected");
        }
    }

    public void setMain(MainLogic main){
        this.main = main;
    }

    private void statusChangeListener(ObservableValue<? extends PacketLogger.PacketStatus> observable, PacketLogger.PacketStatus newValue, PacketLogger.PacketStatus oldValue){
        if(newValue.equals(PacketLogger.PacketStatus.SEND_ERRORED)){
            observable.removeListener(this::statusChangeListener);
            errored("The load packet errored.");
        } else if(newValue.equals(PacketLogger.PacketStatus.TIMED_OUT)) {
            observable.removeListener(this::statusChangeListener);
            errored("the load request timed out.");
        } else if(newValue.equals(PacketLogger.PacketStatus.ACKED)){
            observable.removeListener(this::statusChangeListener);
        }
    }

    private void errored(String why){

    }

    public static void loadNewView(ActionHandler.PacketOut packet) throws BadPacketException {
        try {
            self.eventList = (ArrayList<ScoutingEvent>)((Object[])(packet.getContent()))[0];
            self.validEvents = (ArrayList<ScoutingEventDefinition>)((Object[])(packet.getContent()))[1];
            loadMedia(packet.getContentType());
            packet.ack();
        } catch (ClassCastException e){
            throw new BadPacketException("The packet was poorly formatted.");
        } catch (MediaException e){
            throw new BadPacketException("The media link didn't work.");
        }
    }

    private HashMap<Integer, EventButton> buttons;
    private int[] initialEvents;
    private ScoutingEvent currentlyProcessing;

    private class EventButton extends Button {
        private ScoutingEventDefinition definition;

        private EventButton(ScoutingEventDefinition definition){
            super();
            this.definition = definition;
        }

        private void GenerateEvent(){
            if(currentlyProcessing == null){
                currentlyProcessing = new ScoutingEvent(definition.getName(), (int) (mediaPlayer.getCurrentTime().toMillis() / 100));
            } else {
                currentlyProcessing.setContained(new ScoutingEvent(definition.getName(), (int) (mediaPlayer.getCurrentTime().toMillis()/100)));

            }

            if(definition.getContained() == null){
                ScoutingEvent event = new ScoutingEvent(currentlyProcessing);
                eventList.add(event);
                currentlyProcessing = null;
            }
            if(definition.getContained() == null){
                if(currentlyProcessing != null){
                    ScoutingEvent event = new ScoutingEvent(currentlyProcessing);
                    eventList.add(event);
                    currentlyProcessing = null;
                }
            }
        }
    }
}
