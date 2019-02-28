package scouterSide;

import connectionIndependent.scouted.ScoutIdentifier;
import connectionIndependent.scouted.ScoutedGame;
import connectionIndependent.scouted.ScoutingEvent;
import connectionIndependent.scouted.ScoutingEventDefinition;
import gbuiLib.GBSockets.ActionHandler;
import gbuiLib.GBSockets.BadPacketException;
import gbuiLib.GBSockets.PacketLogger;
import gbuiLib.gbfx.MediaControl;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.util.Callback;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class ScouterUI {

    private MediaPlayer mediaPlayer;
    private Media media;
    @FXML
    private Pane mediaView;
    @FXML
    private Pane controlsPanel;
    @FXML
    private Slider volumeSlider;
    @FXML
    private VBox eventsLog;
    @FXML
    private FlowPane events;
    @FXML
    private Group panelsContainer;
    private Parent root;
    private SimpleObjectProperty<ArrayList<String>> competitions = new SimpleObjectProperty<>();
    private SimpleObjectProperty<ArrayList<ScoutedGame>> gamesList = new SimpleObjectProperty<>();
    private HashMap<ScoutedGame, ArrayList<ScoutIdentifier>> identifiers = new HashMap<>();
    private ArrayList<ScoutingEvent> eventList;
    private HashMap<Byte, ScoutingEventDefinition> validEvents = new HashMap<>();
    private MainLogic main;
    private Button backEvent;
    private short playerOffset;
    private SimpleBooleanProperty isScouting = new SimpleBooleanProperty(true);

    public void setRoot(Parent root) {
        this.root = root;
    }

    public ScouterUI() {
        backEvent = new Button("back");
        backEvent.setOnAction(this::back);
        isScouting.addListener((observable, oldValue, newValue) -> {
            SimpleIntegerProperty lastTry = new SimpleIntegerProperty(1);
            main.setShowing(newValue);
            if (!newValue) {
                while (lastTry.get() == 0) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }
                while (lastTry.get() == 1) {
                    Dialog<SimpleIntegerProperty> dialog = new Dialog<>();
                    GridPane grid = new GridPane();
                    Label competition = new Label();
                    Button changeCompetition = new Button("Change Competition");
                    Spinner<Integer> minimumPriority = new Spinner<>(0, Integer.MAX_VALUE, 0);
                    CheckBox showScouted = new CheckBox();
                    changeCompetition.setOnAction(event -> {
                        Dialog<String> compPick = new Dialog<>();
                        ComboBox<String> combo = new ComboBox<>();
                        combo.setValue(competition.getText());
                        ChangeListener<ArrayList<String>> listener = (observable1, oldValue1, newValue1) -> {
                            Platform.runLater(() -> {
                                combo.hide();
                                combo.setItems(FXCollections.observableArrayList(newValue1));
                                if (combo.getItems().size() > 0) combo.show();
                            });
                        };
                        competitions.addListener(listener);
                        GridPane compGrid = new GridPane();
                        Button reload = new Button("reload");
                        reload.setOnAction(event1 -> {
                            try {
                                main.getCompetitions();
                            } catch (BadPacketException e) {
                                errored("Couldn't fetch the competitions");
                            }
                        });
                        compGrid.addRow(0, combo, reload);
                        compGrid.setHgap(40);
                        compPick.getDialogPane().setContent(compGrid);
                        compPick.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
                        compPick.setResultConverter(param -> {
                            competitions.removeListener(listener);
                            competitions.set(null);
                            if (param == ButtonType.OK) return combo.getValue();
                            return null;
                        });
                        Optional<String> out = compPick.showAndWait();
                        out.ifPresent(s -> competition.setText(out.get()));
                    });
                    competition.textProperty().addListener((observable12, oldValue12, newValue12) -> {
                        try {
                            if (newValue12 == null) {
                                gamesList.set(null);
                                return;
                            }
                            main.getGames(newValue12);
                        } catch (BadPacketException e) {
                            errored("Couldn't fetch the games for the competition: " + newValue12);
                        }
                    });
                    ComboBox<ScoutedGame> games = new ComboBox<>();
                    games.setCellFactory(new Callback<ListView<ScoutedGame>, ListCell<ScoutedGame>>() {
                        @Override
                        public ListCell<ScoutedGame> call(ListView<ScoutedGame> param) {
                            return new ListCell<ScoutedGame>() {
                                @Override
                                protected void updateItem(ScoutedGame item, boolean empty) {
                                    super.updateItem(item, empty);
                                    if (item != null) setText(item.getName());
                                }
                            };
                        }
                    });
                    games.setButtonCell(new ListCell<ScoutedGame>() {
                        @Override
                        protected void updateItem(ScoutedGame item, boolean empty) {
                            super.updateItem(item, empty);
                            if (item != null) setText(item.getName());
                        }
                    });
                    Callback<ScoutIdentifier, Boolean> legit = param -> (param.getState() < 2 || (param.getState() == 2 && showScouted.isSelected())) && minimumPriority.getValue() <= ((int) param.getPriority());
                    ComboBox<ScoutIdentifier> identifier = new ComboBox<>();
                    ChangeListener<ArrayList<ScoutedGame>> listener = (observable13, oldValue13, newValue13) -> {
                        Platform.runLater(() -> {
                            games.hide();
                            if (newValue13 == null) {
                                games.setItems(FXCollections.observableArrayList());
                                return;
                            }
                            games.setItems(FXCollections.observableArrayList(newValue13).filtered(scoutedGame -> {
                                for (ScoutIdentifier identifier1 : identifiers.get(scoutedGame)) {
                                    if (legit.call(identifier1)) {
                                        return true;
                                    }
                                }
                                return false;
                            }));
                            identifier.hide();
                            identifier.setItems(FXCollections.observableArrayList());
                        });
                    };
                    gamesList.addListener(listener);
                    showScouted.selectedProperty().addListener((observable15, oldValue15, newValue15) -> {
                        listener.changed(gamesList, gamesList.getValue(), gamesList.getValue());
                        Platform.runLater(() -> {
                            identifier.hide();
                            if(games.getValue() != null) identifier.setItems(FXCollections.observableArrayList(identifiers.get(games.getValue())).filtered(identifier1 -> legit.call(identifier1)));
                            else identifier.setItems(FXCollections.observableArrayList());
                        });
                    });
                    minimumPriority.valueProperty().addListener((observable15, oldValue15, newValue15) -> {
                        listener.changed(gamesList, gamesList.getValue(), gamesList.getValue());
                        Platform.runLater(() -> {
                            identifier.hide();
                            if(games.getValue() != null) identifier.setItems(FXCollections.observableArrayList(identifiers.get(games.getValue())).filtered(identifier1 -> legit.call(identifier1)));
                            else identifier.setItems(FXCollections.observableArrayList());
                        });
                    });
                    games.valueProperty().addListener((observable14, oldValue14, newValue14) -> {
                        Platform.runLater(() -> {
                            identifier.hide();
                            if(games.getValue() != null) identifier.setItems(FXCollections.observableArrayList(identifiers.get(games.getValue())).filtered(identifier1 -> legit.call(identifier1)));
                            else identifier.setItems(FXCollections.observableArrayList());
                        });
                    });
                    int rowcount = 0;
                    grid.addRow(rowcount++, competition, changeCompetition);
                    grid.addRow(rowcount++, new Label("Show already scouted games"), showScouted);
                    grid.addRow(rowcount++, new Label("The minimum priority for games"), minimumPriority);
                    grid.addRow(rowcount++, new Label("Select a game"), games);
                    grid.addRow(rowcount++, new Label("Select a team/alliance"), identifier);
                    grid.setVgap(20);
                    grid.setHgap(50);
                    dialog.getDialogPane().setContent(grid);
                    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
                    dialog.getDialogPane().lookupButton(ButtonType.OK).addEventFilter(ActionEvent.ACTION, event -> {
                        if (identifier.getValue() == null)
                            event.consume();
                    });
                    dialog.setResultConverter(param -> {
                        try {
                            SimpleIntegerProperty state = new SimpleIntegerProperty(0);
                            main.loadGame(identifier.getValue()).addListener((observable16, oldValue16, newValue16) -> {
                                if (newValue16 == PacketLogger.PacketStatus.ACKED)
                                    state.set(2);
                                if (newValue16 == PacketLogger.PacketStatus.SEND_ERRORED || newValue16 == PacketLogger.PacketStatus.TIMED_OUT)
                                    state.set(1);
                            });
                            return state;
                        } catch (BadPacketException e) {
                            throw new Error("Something messed up");
                        }
                    });
                    Optional<SimpleIntegerProperty> scouting = dialog.showAndWait();
                    if (scouting.isPresent()) {
                        lastTry.set(0);
                        scouting.get().addListener((observable17, oldValue17, newValue17) -> lastTry.set(newValue17.intValue()));
                    }
                }
            }
        });
    }

    public void init() {
        isScouting.set(false);
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
        mediaControl.setFitToWidth(700);
        mediaControl.setFitToHeight(500);
        mediaControl.setPrefHeight(500);
        mediaControl.setPrefWidth(700);
        mediaView.getChildren().add(mediaControl);
        SplitPane.setResizableWithParent(mediaControl, false);
    }

    private void disable() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
        root.setDisable(true);
    }

    public void scoutOver(ActionHandler.PacketOut packet) throws BadPacketException {
        mediaPlayer = null;
        media = null;
        mediaUrl = null;
        gamesList.set(null);
        identifiers.clear();
        packet.ack();
    }

    public void competitions(ActionHandler.PacketOut packet) throws BadPacketException {
        competitions.set((ArrayList<String>) packet.getContent());
        packet.ack();
    }

    public void games(ActionHandler.PacketOut packet) throws BadPacketException {
        ArrayList<ScoutedGame> games = (ArrayList<ScoutedGame>) ((Object[]) packet.getContent())[0];
        for (ScoutIdentifier identifier : (ArrayList<ScoutIdentifier>) ((Object[]) packet.getContent())[1]) {
            ScoutedGame game = games.get(identifier.getGame());
            if (identifiers.get(game) == null) {
                identifiers.put(game, new ArrayList<>());
            }
            identifiers.get(game).add(identifier);
        }
        gamesList.set(games);
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

    protected void packetErrored(ActionHandler.PacketOut packet){
        errored("A" + packet.getPacketType() + " request failed because of: " + packet.getContent());
    }

    public void loadNewView(ActionHandler.PacketOut packet) throws BadPacketException {
        try {
            events.getChildren().clear();
            validEvents.clear();
            buttons.clear();
            for (ScoutingEventDefinition def : (ArrayList<ScoutingEventDefinition>) ((Object[]) (packet.getContent()))[0]) {
                validEvents.put(def.getName(), def);
                buttons.put(def.getName(), new EventButton(def));
            }
            ArrayList<ScoutingEventDefinition> initialDefs = (ArrayList<ScoutingEventDefinition>) ((Object[]) (packet.getContent()))[1];
            initialEvents = new byte[initialDefs.size()];
            int i = 0;
            for (ScoutingEventDefinition def : initialDefs) {
                initialEvents[i++] = def.getName();
            }
            eventList = (ArrayList<ScoutingEvent>) ((Object[]) (packet.getContent()))[2];
            Platform.runLater(() -> {
                for (Byte bite : initialEvents) {
                    try {
                        events.getChildren().add(buttons.get(bite));
                    } catch (MediaException e) {
                        errored("The media link didn't work.");
                    }
                }
            });
            playerOffset = (Short) ((Object[]) packet.getContent())[3];
            mediaUrl = "http://" + main.host + ":4911/" + packet.getContentType().replace(" ", "+");
            System.out.println(mediaUrl);
            Platform.runLater(() -> loadMedia());
            isScouting.set(true);
            packet.ack();
        } catch (ClassCastException e) {
            e.printStackTrace();
            throw new BadPacketException("The packet was poorly formatted.");
        }
    }

    private HashMap<Byte, EventButton> buttons = new HashMap<>();
    private byte[] initialEvents;
    private ScoutingEvent currentlyProcessing = new ScoutingEvent();

    @FXML
    private void cancel(Event event) {
        try {
            main.cancelScout().addListener(this::statusChangeListener);
            isScouting.set(false);
        } catch (BadPacketException e) {
            errored("Couldn't cancel the game");
        }
    }

    @FXML
    private void submit(Event event) {
        if (currentlyProcessing.getStamps().isEmpty()) {
            try {
                main.submitScout(eventList).addListener(this::statusChangeListener);
                isScouting.set(false);
            } catch (BadPacketException e) {
                errored("Couldn't send the game");
            }
        } else {
            errored("Complete the current chain before you submit the game");
        }
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
            try {
                currentlyProcessing.addProgress(definition.getName(), definition.followStamp() ? (short) Math.max(0, (mediaPlayer.getCurrentTime().toMillis() / 100) - (playerOffset / 100)) : null);
                 events.getChildren().clear();
                if (definition.getNextStamps() == null) {
                    eventList.add(currentlyProcessing);
                    currentlyProcessing = new ScoutingEvent();
                    for (Byte bite : initialEvents) {
                        events.getChildren().add(buttons.get(bite));
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
        }
    }

    private void back(Event event) {
        events.setDisable(true);
        currentlyProcessing.removeLast();
        if (currentlyProcessing.getSize() == 0) {
            events.getChildren().clear();
            for (byte eventDef : initialEvents) {
                events.getChildren().add(buttons.get(validEvents.get(eventDef).getName()));
            }
        } else {
            ScoutingEventDefinition definition = validEvents.get(currentlyProcessing.getType());
            events.getChildren().clear();
            for (byte eventDef : definition.getNextStamps()) {
                events.getChildren().add(buttons.get(validEvents.get(eventDef).getName()));
            }
            if (currentlyProcessing.getSize() > 0) {
                events.getChildren().add(backEvent);
            }
        }
        events.setDisable(false);
    }
}
