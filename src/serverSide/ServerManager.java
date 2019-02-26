package serverSide;

import connectionIndependent.ScoutingConnections;
import connectionIndependent.scouted.ScoutedGame;
import connectionIndependent.scouted.ScoutedTeam;
import gbuiLib.GBSockets.GBServerSocket;
import gbuiLib.GBSockets.PacketLogger;
import gbuiLib.gbfx.MediaControl;
import gbuiLib.gbfx.WomboComboBox;
import gbuiLib.gbfx.grid.CheckCell;
import gbuiLib.gbfx.grid.EditGrid;
import gbuiLib.gbfx.grid.GridCell;
import gbuiLib.gbfx.grid.RowController;
import gbuiLib.gbfx.popUpListView.PopUpEditCell;
import gbuiLib.gbfx.popUpListView.PopUpListView;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.bytedeco.javacv.FFmpegFrameGrabber;
//import org.bytedeco.javacv.FFmpegFrameRecorder;
//import org.bytedeco.javacv.Frame;

public class ServerManager {

    private ScoutersManager scouters;
    private DataBaseManager database;
    private File originalVidDirectory = null;

    protected ServerManager() {
        database = new DataBaseManager();
        scouters = new ScoutersManager(database);
        PacketLogger.setDirectory(ScoutingVars.getMainDirectory());
        GBServerSocket socket = new GBServerSocket(4590, null, null, false, "LocalNetwork");
        socket.addConnectionType(ScoutingConnections.SCOUTER.toString(), scouters.getHandler());
        socket.initSelector();
    }

    private void addCompetition(String competition) {
        database.addNewCompetition(competition);
        new File(ScoutingVars.getVideosDirectory(), competition).mkdirs();
    }

    @FXML
    private Button competitions;
    @FXML
    private Button teams;
    @FXML
    private Button games;

    private String inputSanitizer(String input) {
        String out;
        if ((out = input.replaceAll("[^A-Za-z 0-9]+", "")) == "") return null;
        else return out;
    }

    private Short inputSanitizerNumbers(String input) {
        String out;
        if ((out = input.replaceAll("[^0-9]+", "")) == "") return null;
        else return Short.valueOf(out);
    }

    public void initialize() {
        teams.setOnAction(event -> {
            Dialog<HashMap<Short, ScoutedTeam>> editTeams = new Dialog<>();
            editTeams.setTitle("Edit the teams list");
            ScrollPane scroll = new ScrollPane();
            ArrayList<String> competitions = database.getCompetitionsList();
            EditGrid grid = new EditGrid() {
                @Override
                public void addNewRow(int rowIndex, String... values) {
                    if (values.length < competitions.size() + 2) {
                        values = new String[competitions.size() + 2];
                        values[0] = "";
                        values[1] = "";
                        for (int i = 2; i < competitions.size(); i++) {
                            values[i] = "false";
                        }
                    }
                    addRow(rowIndex, new GridCell(values[0], 120, "name"), new GridCell(values[1], 60, "value"));
                    int i = 2;
                    for (String comp : competitions) {
                        addRow(rowIndex, new CheckCell(comp, Boolean.valueOf(values[i++])));
                    }
                    setAdder(new RowController(false));
                    addRow(++rowIndex, getAdder());
                }

                @Override
                protected void initialRow() {
                    Label label = new Label("");
                    Label name = new Label("Name");
                    name.setMinWidth(120);
                    Label number = new Label("Number");
                    number.setMinWidth(60);
                    addRow(0, label, name, number);
                    for (String comp : competitions) {
                        addRow(0, new Label(comp));
                    }
                    getChildren().remove(label);
                }
            };
            grid.setHgap(20);
            grid.setVgap(5);
            grid.setPadding(new Insets(20, 20, 20, 30));
            grid.setManaged(true);
            scroll.setPrefViewportHeight(400);
            scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
            scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scroll.setContent(grid);
            scroll.setManaged(true);
            HashMap<Short, ScoutedTeam> initialTeams = new HashMap<>();
            int rowCount = 1;
            for (ScoutedTeam team : database.getTeamsList()) {
                initialTeams.put(team.getNumber(), team);
                String[] array = new String[competitions.size() + 2];
                array[0] = team.getName();
                array[1] = String.valueOf(team.getNumber());
                int i = 2;
                for (String comp : competitions) {
                    array[i++] = String.valueOf(team.getCompetitions().contains(comp));
                }
                grid.addNewRow(rowCount++, array);
            }
            editTeams.getDialogPane().setContent(scroll);
            editTeams.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            editTeams.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    HashMap<Short, ScoutedTeam> map = new HashMap<>();
                    for (Node node : grid.getChildren().filtered(node -> {
                        if (node instanceof GridCell) {
                            return ((GridCell) node).getType().equals("value");
                        }
                        return false;
                    })) {
                        GridCell number = (GridCell) node;
                        GridCell name = (GridCell) grid.getChildren().filtered(cell -> {
                            if (cell instanceof GridCell) {
                                return GridPane.getRowIndex(cell) == GridPane.getRowIndex(number) && GridPane.getColumnIndex(cell) == 1;
                            }
                            return false;
                        }).get(0);
                        ArrayList<String> compList = new ArrayList<>();
                        for (Node cell : grid.getChildren().filtered(cell -> {
                            if (cell instanceof CheckCell) {
                                return GridPane.getRowIndex(cell) == GridPane.getRowIndex(number);
                            }
                            return false;
                        })) {
                            CheckCell box = (CheckCell) cell;
                            if (box.isSelected()) compList.add(box.getType());
                        }
                        try {
                            map.put(inputSanitizerNumbers(number.getText()), new ScoutedTeam(inputSanitizerNumbers(number.getText()), inputSanitizer(name.getText()), compList));
                        } catch (NullPointerException | NumberFormatException e) {
                        }
                    }
                    return map;
                }
                return null;
            });
            Optional<HashMap<Short, ScoutedTeam>> result = editTeams.showAndWait();
            result.ifPresent(newTeams -> {
                Set<Short> initialSet = initialTeams.keySet();
                Set<Short> newSet = newTeams.keySet();
                ArrayList<ScoutedTeam> newlyAdded = new ArrayList<>();
                ArrayList<ScoutedTeam> removed = new ArrayList<>();
                for (Short shot : newSet) {
                    newlyAdded.add(newTeams.get(shot));
                }
                initialSet.removeAll(newSet);
                for (Short shot : initialSet) {
                    removed.add(initialTeams.get(shot));
                }
                database.teamsChanged(newlyAdded, removed);
            });
        });
        competitions.setOnAction(event -> {
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("New Competition Dialog");
            dialog.setContentText("New competition's name:");
            BorderPane pane = new BorderPane();
            ListView<String> competitions = new ListView<>(FXCollections.observableArrayList(database.getCompetitionsList()));
            TextField newComp = new TextField();
            pane.setTop(newComp);
            pane.setCenter(competitions);
            pane.setPrefHeight(200);
            dialog.getDialogPane().setContent(pane);
            dialog.setResultConverter(param -> {
                String result = newComp.getText();
                return inputSanitizer(result);
            });
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(s -> addCompetition(s));
        });
        games.setOnAction(event -> {
            Dialog<ArrayList<ScoutedGame>> dialog = new Dialog<>();
            dialog.setTitle("Game Config Dialog");
            TabPane tabs = new TabPane();
            for (String comp : database.getCompetitionsList()) {
                Tab tab = new Tab(comp);
                PopUpListView<ScoutedGame> listView = new PopUpListView<ScoutedGame>() {
                    @Override
                    protected Callback<ListView<ScoutedGame>, ListCell<ScoutedGame>> customCellFactory() {
                        return list -> new PopUpEditCell<ScoutedGame>() {
                            @Override
                            protected Callback<ScoutedGame, Dialog<ScoutedGame>> createDialog() {
                                return game -> {
                                    ArrayList<ScoutedTeam> teams = database.getAllTeamsForCompetition(comp);
                                    Dialog<ScoutedGame> editor = new Dialog<>();
                                    GridPane pane = new GridPane();
                                    TextField gameName = new TextField();
                                    CheckBox happened = new CheckBox();
                                    WomboComboBox<ScoutedTeam> team1 = new WomboComboBox<>();
                                    WomboComboBox<ScoutedTeam> team2 = new WomboComboBox<>();
                                    WomboComboBox<ScoutedTeam> team3 = new WomboComboBox<>();
                                    WomboComboBox<ScoutedTeam> team4 = new WomboComboBox<>();
                                    WomboComboBox<ScoutedTeam> team5 = new WomboComboBox<>();
                                    WomboComboBox<ScoutedTeam> team6 = new WomboComboBox<>();
                                    team1.getOptions().addAll(teams);
                                    team2.getOptions().addAll(teams);
                                    team3.getOptions().addAll(teams);
                                    team4.getOptions().addAll(teams);
                                    team5.getOptions().addAll(teams);
                                    team6.getOptions().addAll(teams);
                                    TextField blueScore = new TextField();
                                    TextField redScore = new TextField();
                                    TextField blueRP = new TextField();
                                    TextField redRP = new TextField();
                                    TextField mapConfiguration = new TextField();
                                    Label currentVideoOffset = new Label("No Video Available");
                                    Button videoEdit = new Button("Edit Game Video");
                                    SimpleObjectProperty<Short> videoOffset = new SimpleObjectProperty<>();
                                    videoOffset.addListener((observable, oldValue, newValue) -> currentVideoOffset.setText("Video offset: " + videoOffset.get() / 1000 + "." + (videoOffset.get() - ((videoOffset.get() / 1000) * 1000))));
                                    happened.selectedProperty().addListener((observable, oldValue, newValue) -> {
                                        blueScore.setEditable(newValue);
                                        redScore.setEditable(newValue);
                                        blueRP.setEditable(newValue);
                                        redRP.setEditable(newValue);
                                        mapConfiguration.setEditable(newValue);
                                        videoEdit.setDisable(!newValue);
                                    });
                                    videoEdit.setOnAction(clicked -> {
                                        Dialog<Short> videoDialog = new Dialog<>();
                                        Media video;
                                        MediaPlayer player;
                                        MediaControl media;
                                        File destination = new File(new File(ScoutingVars.getVideosDirectory(), comp), (game == null ? (short) list.getItems().indexOf(game) : game.getGame()) + ".mp4");
                                        if (!destination.exists()) {
                                            FileChooser originalVideoPath = new FileChooser();
                                            originalVideoPath.setInitialDirectory(originalVidDirectory != null ? originalVidDirectory : null);
                                            File path = originalVideoPath.showOpenDialog(editor.getDialogPane().getScene().getWindow());
                                            if (path == null) return;
                                            try {
//                                                FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(path);
//                                                FfmpegVideo test = new FfmpegVideo();
//                                                test.load("test", new ImageStorage<>());
//                                                grabber.setFrameRate(23);
//                                                FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(new FileOutputStream(destination), 700, 500, 2);
//                                                recorder.setFrameRate(23);
//                                                recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
//                                                recorder.setImageWidth(700);
//                                                recorder.setImageHeight(500);
//                                                recorder.setVideoCodec(13);
//                                                recorder.setFrameRate(30);
//                                                recorder.setFormat("mp4");
//                                                recorder.setVideoCodec(13);
//                                                recorder.setAudioCodecName("libfdk_aac");
//                                                recorder.setVideoBitrate(4);
//                                                Frame frame;
//                                                long t = 0;
//                                                try {
//                                                    recorder.start();
//                                                    grabber.start();
//                                                    while (true) {
//                                                        try {
//                                                            t += 1000/recorder.getFrameRate();
//                                                            if (t > recorder.getTimestamp()) {
//                                                                recorder.setTimestamp(t);
//                                                                grabber.setTimestamp(t);
//                                                            }
//                                                            frame = grabber.grab();
//                                                            if (frame == null) {
//                                                                System.out.println("!!! Failed cvQueryFrame");
//                                                                break;
//                                                            }
//                                                            recorder.record(frame);
//                                                        } catch (Exception e) {
//                                                    }
//                                                    recorder.stop();
//                                                    recorder.release();
//                                                    grabber.stop();
//                                                    grabber.release();
//                                                } catch (Exception e) {
//                                                    e.printStackTrace();
//                                                }
                                                Files.copy(path.toPath(), destination.toPath());
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                                return;
                                            }
                                        }
                                        try {
                                            video = new Media(destination.toURI().toURL().toString());
                                        } catch (MalformedURLException e) {
                                            return;
                                        }
                                        player = new MediaPlayer(video);
                                        media = new MediaControl(player);
                                        media.setFitToHeight(500);
                                        media.setFitToWidth(700);
                                        videoDialog.getDialogPane().setContent(media);
                                        ButtonType fromEnd = new ButtonType("From End", ButtonBar.ButtonData.YES);
                                        ButtonType fromStart = new ButtonType("From Start", ButtonBar.ButtonData.NO);
                                        videoDialog.getDialogPane().getButtonTypes().addAll(fromEnd, fromStart, ButtonType.CANCEL);
                                        videoDialog.setResultConverter(param -> {
                                            if (param.getButtonData() == ButtonBar.ButtonData.NO)
                                                return ((Double) player.getCurrentTime().toMillis()).shortValue();
                                            if (param.getButtonData() == ButtonBar.ButtonData.YES)
                                                return (short) (150000 - ((Double) player.getCurrentTime().toMillis()).shortValue());
                                            return null;
                                        });
                                        Optional<Short> result = videoDialog.showAndWait();
                                        result.ifPresent(videoOffset::set);
                                    });
                                    if (game != null) {
                                        gameName.setText(game.getName());
                                        happened.setSelected(game.didHappen());
                                        class TeamSelector implements Callback<Short, ScoutedTeam> {
                                            @Override
                                            public ScoutedTeam call(Short param) {
                                                for (ScoutedTeam team : teams) {
                                                    if (param != null) if (team.getNumber() == param) return team;
                                                }
                                                return null;
                                            }
                                        }
                                        team1.setValue(new TeamSelector().call(game.getTeamNumber1()));
                                        team2.setValue(new TeamSelector().call(game.getTeamNumber2()));
                                        team3.setValue(new TeamSelector().call(game.getTeamNumber3()));
                                        team4.setValue(new TeamSelector().call(game.getTeamNumber4()));
                                        team5.setValue(new TeamSelector().call(game.getTeamNumber5()));
                                        team6.setValue(new TeamSelector().call(game.getTeamNumber6()));
                                        if (game.didHappen()) {
                                            blueScore.setText(String.valueOf(game.getBlueAllianceScore()));
                                            redScore.setText(String.valueOf(game.getRedAllianceScore()));
                                            blueRP.setText(String.valueOf(game.getBlueAllianceRP()));
                                            redRP.setText(String.valueOf(game.getRedAllianceRP()));
                                            mapConfiguration.setText(game.getMapConfiguration() == null ? "" : game.getMapConfiguration());
                                            videoOffset.set(game.getVideoOffset());
                                        }
                                    }
                                    int rowCount = 0;
                                    pane.addRow(rowCount++, new Label("Game Name:"), gameName);
                                    pane.addRow(rowCount++, new Label("Blue Alliance Team 1:"), team1);
                                    pane.addRow(rowCount++, new Label("Blue Alliance Team 2:"), team2);
                                    pane.addRow(rowCount++, new Label("Blue Alliance Team 3:"), team3);
                                    pane.addRow(rowCount++, new Label("Red Alliance Team 1:"), team4);
                                    pane.addRow(rowCount++, new Label("Red Alliance Team 2:"), team5);
                                    pane.addRow(rowCount++, new Label("Red Alliance Team 3:"), team6);
                                    if (!happened.isSelected())
                                        pane.addRow(rowCount++, new Label("Game Already Completed?"), happened);
                                    pane.addRow(rowCount++, new Label("Blue Alliance Score:"), blueScore);
                                    pane.addRow(rowCount++, new Label("Red Alliance Score:"), redScore);
                                    pane.addRow(rowCount++, new Label("Blue Alliance RP:"), blueRP);
                                    pane.addRow(rowCount++, new Label("Red Alliance RP:"), redRP);
                                    pane.addRow(rowCount++, new Label("Map Configuration:"), mapConfiguration);
                                    pane.addRow(rowCount++, new Label("Video"), currentVideoOffset, videoEdit);
                                    pane.setVgap(10);
                                    pane.setHgap(20);
                                    editor.getDialogPane().setContent(pane);
                                    editor.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
                                    editor.getDialogPane().lookupButton(ButtonType.OK).addEventFilter(ActionEvent.ACTION, okEvent -> {
                                        if (inputSanitizer(gameName.getText()).equals("") ||
                                                (happened.isSelected() && (
                                                        inputSanitizerNumbers(blueScore.getText()).equals("") ||
                                                                inputSanitizerNumbers(redScore.getText()).equals("") ||
                                                                inputSanitizerNumbers(blueRP.getText()).equals("") ||
                                                                inputSanitizerNumbers(redRP.getText()).equals("") ||
                                                                videoOffset.get() == null)))
                                            okEvent.consume();
                                    });
                                    editor.setResultConverter(param -> {
                                        if (param == ButtonType.OK) {
                                            try {
                                                return (happened.isSelected() ?
                                                        new ScoutedGame(
                                                                game == null ? (short) list.getItems().indexOf(game) : game.getGame(),
                                                                database.getCompetitionFromName(comp),
                                                                inputSanitizer(gameName.getText()),
                                                                inputSanitizerNumbers(blueScore.getText()),
                                                                inputSanitizerNumbers(redScore.getText()),
                                                                inputSanitizerNumbers(blueRP.getText()).byteValue(),
                                                                inputSanitizerNumbers(redRP.getText()).byteValue(),
                                                                inputSanitizer(mapConfiguration.getText()),
                                                                team1.getValueInItems() == null ? null : team1.getValueInItems().getNumber(),
                                                                team2.getValueInItems() == null ? null : team2.getValueInItems().getNumber(),
                                                                team3.getValueInItems() == null ? null : team3.getValueInItems().getNumber(),
                                                                team4.getValueInItems() == null ? null : team4.getValueInItems().getNumber(),
                                                                team5.getValueInItems() == null ? null : team5.getValueInItems().getNumber(),
                                                                team6.getValueInItems() == null ? null : team6.getValueInItems().getNumber(),
                                                                videoOffset.get()) :
                                                        new ScoutedGame(
                                                                game == null ? (short) list.getItems().indexOf(game) : game.getGame(),
                                                                database.getCompetitionFromName(comp),
                                                                inputSanitizer(gameName.getText()),
                                                                team1.getValueInItems() == null ? null : team1.getValueInItems().getNumber(),
                                                                team2.getValueInItems() == null ? null : team2.getValueInItems().getNumber(),
                                                                team3.getValueInItems() == null ? null : team3.getValueInItems().getNumber(),
                                                                team4.getValueInItems() == null ? null : team4.getValueInItems().getNumber(),
                                                                team5.getValueInItems() == null ? null : team5.getValueInItems().getNumber(),
                                                                team6.getValueInItems() == null ? null : team6.getValueInItems().getNumber()));
                                            } catch (NullPointerException e) {
                                                return null;
                                            }
                                        }
                                        return null;
                                    });
                                    return editor;
                                };
                            }

                            @Override
                            protected void refreshGraphic(ScoutedGame item, boolean empty) {
                                if (item != null) {
                                    setText(item.getName());
                                } else if (this.getItem() == null && getIndex() == list.getItems().size() - 1) {
                                    setText("Add Game");
                                }
                            }
                        };
                    }
                };
                listView.getItems().addAll(database.getGamesList(comp, false));
                listView.getItems().add(null);
                tab.setContent(listView);
                tabs.getTabs().add(tab);
            }
            dialog.setResultConverter(param -> {
                if (param == ButtonType.OK) {
                    ArrayList<ScoutedGame> games = new ArrayList<>();
                    for (Tab tab : tabs.getTabs()) {
                        games.addAll(((PopUpListView<ScoutedGame>) tab.getContent()).getItems().filtered(scoutedGame -> scoutedGame != null));
                    }
                    return games;
                }
                return null;
            });
            dialog.getDialogPane().setContent(tabs);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            Optional<ArrayList<ScoutedGame>> finalGames = dialog.showAndWait();
            finalGames.ifPresent(games -> database.refreshGames(games));
        });
    }
}
