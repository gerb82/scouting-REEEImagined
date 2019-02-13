package scouterSide;

import connectionIndependent.scouted.ScoutingEvent;
import connectionIndependent.scouted.ScoutingEventDefinition;
import gbuiLib.GBSockets.ActionHandler;
import gbuiLib.GBSockets.BadPacketException;
import gbuiLib.GBSockets.PacketLogger;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Parent;
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
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;

public class ScouterUI {

    private MediaPlayer mediaPlayer;
    private Media media;
    @FXML
    private Pane mediaView;
    @FXML
    private Pane controlsPanel;
    @FXML
    private Pane loadControlsPanel;
    @FXML
    private MenuButton competitionSelect;
    @FXML
    private MenuButton gameSelect;
    @FXML
    private MenuButton teamSelect;
    @FXML
    private Slider volumeSlider;
    @FXML
    private VBox eventsLog;
    @FXML
    private FlowPane events;
    @FXML
    private Group panelsContainer;
    private Parent root;
    private short currentGameNumber;
    private String currentTeamIdentifier;
    private String currentCompetition;
    private ArrayList<ScoutingEvent> eventList;
    private HashMap<Byte, ScoutingEventDefinition> validEvents = new HashMap<>();
    private MainLogic main;
    private Button backEvent;
    private short playerOffset;

    private class OptionChooser extends MenuItem {

        private MenuButton parent;

        private OptionChooser(String text, MenuButton parent) {
            super();
            setText(text);
            this.parent = parent;
            this.setOnAction(this::handleSelected);
        }

        private void handleSelected(Event event) {
            try {
                if (this.parent.equals(competitionSelect)) {
                    currentCompetition = this.getText();
                    competitionSelect.setText(currentCompetition);
                    main.getGames(this.getText());
                    gameSelect.getItems().clear();
                    teamSelect.getItems().clear();
                } else if (this.parent.equals(gameSelect)) {
                    currentGameNumber = Short.parseShort(this.getText());
                    main.getTeams(currentCompetition, currentGameNumber);
                    gameSelect.setText(this.getText());
                    teamSelect.getItems().clear();
                } else {
                    currentTeamIdentifier = this.getText();
                    teamSelect.setText(this.getText());
                }
            } catch (BadPacketException e) {
                errored("Couldn't build the packet to get the data!");
            }
        }

        @Override
        protected void finalize() throws Throwable {
            this.setOnAction(null);
            super.finalize();
        }
    }

    @FXML
    private void refreshCompetitions(Event event) {
        try {
            competitionSelect.getItems().clear();
            gameSelect.getItems().clear();
            teamSelect.getItems().clear();
            main.getCompetitions();
        } catch (BadPacketException e) {
            errored("Couldn't get the competitions");
        }
    }

    public void setRoot(Parent root) {
        this.root = root;
    }

    public ScouterUI() {
        backEvent = new Button();
        backEvent.setOnAction(this::back);
    }

    @FXML
    private void back10(Event event) {
        movePlayerTime(-10);
    }

    @FXML
    private void back5(Event event) {
        movePlayerTime(-5);
    }

    @FXML
    private void forward5(Event event) {
        movePlayerTime(5);
    }

    @FXML
    private void forward10(Event event) {
        movePlayerTime(10);
    }

    private void movePlayerTime(int dif) {
        mediaPlayer.seek(mediaPlayer.getCurrentTime().add(Duration.seconds(dif)));
    }

    private String mediaUrl;

    @FXML
    private void loadMedia() {
        media = new Media(mediaUrl);
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setAutoPlay(true);

        MediaControl mediaControl = new MediaControl(mediaPlayer);
        mediaView.getChildren().add(mediaControl);
        mediaControl.prefWidthProperty().bind(mediaView.widthProperty());
        mediaControl.prefHeightProperty().bind(mediaView.heightProperty());
    }

    private void disable() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
        root.setDisable(true);
    }

    @FXML
    private void load(Event event) {
        try {
            disable();
            PacketLogger.ObservablePacketStatus status;
            status = main.loadGame(currentCompetition, currentGameNumber, currentTeamIdentifier);
            status.addListener(this::statusChangeListener);
        } catch (BadPacketException e) {
            errored("Couldn't reload the game");
        } catch (IllegalStateException e) {
            errored("We aren't connected");
        }
    }

    public void scoutOver(ActionHandler.PacketOut packet) throws BadPacketException {
        mediaPlayer = null;
        media = null;
        mediaUrl = null;
        setActivePane(false);
        packet.ack();
    }

    public void competitions(ActionHandler.PacketOut packet) throws BadPacketException {
        if (competitionSelect.getItems().isEmpty()) {
            Platform.runLater(() -> {
                String[] competitions = (String[]) packet.getContent();
                for (String comp : competitions) {
                    competitionSelect.getItems().add(new OptionChooser(comp, competitionSelect));
                }
                currentCompetition = packet.getContentType();
            });
        }
        packet.ack();
    }

    public void games(ActionHandler.PacketOut packet) throws BadPacketException {
        if (packet.getContentType().equals(currentCompetition) && gameSelect.getItems().isEmpty()) {
            Platform.runLater(() -> {
                Short[] games = (Short[]) packet.getContent();
                for (Short game : games) {
                    if (game != null) {
                        gameSelect.getItems().add(new OptionChooser(game.toString(), gameSelect));
                    }
                }
            });
        }
        packet.ack();
    }

    public void teams(ActionHandler.PacketOut packet) throws BadPacketException {
        if (Short.valueOf(packet.getContentType()) == currentGameNumber && teamSelect.getItems().isEmpty()) {
            Platform.runLater(() -> {
                String[] teams = (String[]) packet.getContent();
                for (String team : teams) {
                    if (team != null) {
                        teamSelect.getItems().add(new OptionChooser(team, teamSelect));
                    }
                }
            });
        }
        packet.ack();
    }

    public void setMain(MainLogic main) {
        this.main = main;
    }

    private void statusChangeListener(ObservableValue<? extends PacketLogger.PacketStatus> observable, PacketLogger.PacketStatus newValue, PacketLogger.PacketStatus oldValue) {
        if (newValue.equals(PacketLogger.PacketStatus.SEND_ERRORED)) {
            observable.removeListener(this::statusChangeListener);
            errored("The packet errored.");
            root.setDisable(false);
        } else if (newValue.equals(PacketLogger.PacketStatus.TIMED_OUT)) {
            observable.removeListener(this::statusChangeListener);
            errored("the packet timed out.");
            root.setDisable(false);
        } else if (newValue.equals(PacketLogger.PacketStatus.ACKED)) {
            observable.removeListener(this::statusChangeListener);
            root.setDisable(false);
        }
    }

    private void errored(String why) {
        System.out.println(why);
    }

    public void loadNewView(ActionHandler.PacketOut packet) throws BadPacketException {
        try {
            eventList = (ArrayList<ScoutingEvent>) ((Object[]) (packet.getContent()))[0];
            validEvents.clear();
            buttons.clear();
            for (ScoutingEventDefinition def : (ArrayList<ScoutingEventDefinition>) ((Object[]) (packet.getContent()))[1]) {
                validEvents.put(def.getName(), def);
                buttons.put(def.getName(), new EventButton(def));
            }
            initialEvents = (byte[]) ((Object[]) (packet.getContent()))[2];
//            playerOffset = (short) ((Object[])packet.getContent())[3];
            mediaUrl = "http://" + main.host + ":4911/" + packet.getContentType();
            System.out.println(mediaUrl);
            Platform.runLater(() -> {
                loadMedia();
                setActivePane(true);
            });
            packet.ack();
        } catch (ClassCastException e) {
            e.printStackTrace();
            throw new BadPacketException("The packet was poorly formatted.");
        } catch (MediaException e) {
            throw new BadPacketException("The media link didn't work.");
        }
    }

    private HashMap<Byte, EventButton> buttons = new HashMap<>();
    private byte[] initialEvents;
    private ScoutingEvent currentlyProcessing;

    @FXML
    private void cancel(Event event) {
        try {
            main.cancelScout().addListener(this::statusChangeListener);
            disable();
        } catch (BadPacketException e) {
            errored("Couldn't cancel the game");
        }
    }

    @FXML
    private void submit(Event event) {
        if (currentlyProcessing == null) {
            try {
                main.submitScout(eventList).addListener(this::statusChangeListener);
                disable();
            } catch (BadPacketException e) {
                errored("Couldn't send the game");
            }
        } else {
            errored("Complete the current chain before you submit the game");
        }
    }

    public void setActivePane(boolean scouting) {
        panelsContainer.getChildren().clear();
        panelsContainer.getChildren().add(scouting ? controlsPanel : loadControlsPanel);
    }

    private class EventButton extends Button {
        private ScoutingEventDefinition definition;

        private EventButton(ScoutingEventDefinition definition) {
            super();
            this.definition = definition;
            setText(definition.getTextName());
            this.setOnAction(event -> generateEvent());
        }

        private void generateEvent() {
            events.setDisable(true);
            try {
                currentlyProcessing.addProgress(definition.getName(), definition.followStamp() ? (short) ((mediaPlayer.getCurrentTime().toMillis() / 100)/* + playerOffset */) : null);
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
            } catch (IllegalArgumentException e) {
                errored(e.getMessage());
            }
            events.setDisable(false);
        }
    }

    private void back(Event event) {
        events.setDisable(true);
        currentlyProcessing.removeLast();
        ScoutingEventDefinition definition = validEvents.get(currentlyProcessing.getLastType());
        events.getChildren().clear();
        for (byte eventDef : definition.getNextStamps()) {
            events.getChildren().add(buttons.get(validEvents.get(eventDef).getName()));
        }
        if (currentlyProcessing.getSize() > 0) {
            events.getChildren().add(backEvent);
        }
    }
}
