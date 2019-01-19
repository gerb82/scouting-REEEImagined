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

import java.util.ArrayList;
import java.util.HashMap;

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
    private HashMap<Byte, ScoutingEventDefinition> validEvents = new HashMap<>();
    private MainLogic main;
    private Button backEvent;
    private short playerOffset;

    private class OptionChooser extends MenuItem {

        private boolean game;

        private OptionChooser(boolean game){
            super();
            this.setOnAction(this::handleSelected);
            this.game = game;
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
        backEvent = new Button();
        backEvent.setOnAction(this::back);
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
            self.validEvents.clear();
            for(ScoutingEventDefinition def : (ArrayList<ScoutingEventDefinition>)((Object[])(packet.getContent()))[1]) {
                self.validEvents.put(def.getName(), def);
            }
            self.initialEvents = (byte[]) ((Object[])(packet.getContent()))[2];
            loadMedia("https://" + MainLogic.host + ":4911/" + packet.getContentType());
            packet.ack();
        } catch (ClassCastException e){
            throw new BadPacketException("The packet was poorly formatted.");
        } catch (MediaException e){
            throw new BadPacketException("The media link didn't work.");
        }
    }

    private HashMap<Byte, EventButton> buttons;
    private byte[] initialEvents;
    private ScoutingEvent currentlyProcessing;

    private class EventButton extends Button {
        private ScoutingEventDefinition definition;

        private EventButton(ScoutingEventDefinition definition){
            super();
            this.definition = definition;
            this.setOnAction(event -> generateEvent());
        }

        private void generateEvent(){
            events.setDisable(true);
            try {
                currentlyProcessing.addProgress(definition.getName(), definition.followStamp() ? (short) ((mediaPlayer.getCurrentTime().toMillis() / 100) + playerOffset) : null);
                events.getChildren().clear();
                if (definition.getNextStamps() == null) {
                    eventList.add(currentlyProcessing);
                    currentlyProcessing = new ScoutingEvent();
                    for (int i : initialEvents) {
                        events.getChildren().add(buttons.get(i));
                    }
                } else {
                    for (byte eventDef : definition.getNextStamps()) {
                        events.getChildren().add(buttons.get(validEvents.get(eventDef).getName()));
                    }
                    events.getChildren().add(backEvent);
                }
            } catch (IllegalArgumentException e){
                errored(e.getMessage());
            }
            events.setDisable(false);
        }
    }

    private void back(Event event){
        events.setDisable(true);
        currentlyProcessing.removeLast();
        ScoutingEventDefinition definition = validEvents.get(currentlyProcessing.getLastType());
        events.getChildren().clear();
        for(byte eventDef : definition.getNextStamps()) {
            events.getChildren().add(buttons.get(validEvents.get(eventDef).getName()));
        }
        if(currentlyProcessing.getSize() > 0){
            events.getChildren().add(backEvent);
        }
    }
}
