package serverSide.code;

import connectionIndependent.ScoutingConnections;
import connectionIndependent.scouted.ScoutedGame;
import connectionIndependent.scouted.ScoutedTeam;
import gbuiLib.GBSockets.GBServerSocket;
import gbuiLib.GBSockets.PacketLogger;
import gbuiLib.gbfx.grid.CheckCell;
import gbuiLib.gbfx.grid.EditGrid;
import gbuiLib.gbfx.grid.GridCell;
import gbuiLib.gbfx.grid.RowController;
import gbuiLib.gbfx.popUpListView.PopUpEditCell;
import gbuiLib.gbfx.popUpListView.PopUpListView;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import javafx.util.Pair;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerManager {

    private ScoutersManager scouters;
    private DataBaseManager database;

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
        scouters.addCompetition(competition);
        new File(ScoutingVars.getVideosDirectory(), competition).mkdirs();
    }

    private void refreshGames(ArrayList<Pair<ScoutedGame, Path>> games) throws IOException {
        ArrayList<ScoutedGame> gamesList = new ArrayList<>();
        for (Pair<ScoutedGame, Path> gamePair : games) {
            gamesList.add(gamePair.getKey());
            if (gamePair.getValue() != null) {
                File destination = new File(new File(ScoutingVars.getVideosDirectory(), String.valueOf(gamePair.getKey().getCompetition())), gamePair.getKey().getGame() + ".mp4");
                FFmpegFrameGrabber grabbber = new FFmpegFrameGrabber(gamePair.getValue().toFile());
//                grabbber.setImageScalingFlags();
                        //-i input -c:v libx264 -crf 23 -preset medium -c:a libfdk_aac -vbr 4 \
                //-movflags +faststart -vf scale=-2:720,format=yuv420p $output
            }
        }
        database.refreshGames(gamesList);
//        scouters.addGame(competition, game, teams);
//        FFmpegFrameGrabber ffmpeg = new FFmpegFrameGrabber()
    }

    @FXML
    private void pickCurrentComp(Event event) {
        scouters.pickCurrentCompetition(((MenuItem) event.getTarget()).getText());
    }

    @FXML
    private Button competitions;
    @FXML
    private Button teams;
    @FXML
    private Button games;

    private String inputSanitizer(String input) {
        Pattern pattern = Pattern.compile("[A-Za-z 0-9]+");
        Matcher matcher = pattern.matcher(input);
        if (matcher.matches()) return matcher.group(0);
        return null;
    }

    private Short inputSanitizerNumbers(String input) {
        Pattern pattern = Pattern.compile("[0-9]+");
        Matcher matcher = pattern.matcher(input);
        if (matcher.matches()) return Short.valueOf(matcher.group(0));
        return null;
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
                            map.put(inputSanitizerNumbers(number.getText()), new ScoutedTeam(Short.valueOf(inputSanitizer(number.getText())), inputSanitizer(name.getText()), compList));
                        } catch (NullPointerException | NumberFormatException e) {
                        }
                    }
                    return map;
                }
                return null;
            });
            Optional<HashMap<Short, ScoutedTeam>> result = editTeams.showAndWait();
            result.ifPresent(newTeams -> {
                boolean changed = false;
                for (Short team : initialTeams.keySet()) {
                    try {
                        changed = !newTeams.get(team).equals(initialTeams.get(team)) || changed;
                    } catch (NullPointerException e) {
                        changed = true;
                    }
                }
                if (changed) return;
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
                                    Dialog<ScoutedGame> editor = new Dialog<>();
                                    GridPane pane = new GridPane();
                                    TextField gameName = new TextField();
                                    CheckBox happened = new CheckBox();
                                    TextField team1 = new TextField();
                                    TextField team2 = new TextField();
                                    TextField team3 = new TextField();
                                    TextField team4 = new TextField();
                                    TextField team5 = new TextField();
                                    TextField team6 = new TextField();
                                    TextField blueScore = new TextField();
                                    TextField redScore = new TextField();
                                    TextField blueRP = new TextField();
                                    TextField redRP = new TextField();
                                    TextField mapConfiguration = new TextField();
                                    if (game != null) {
                                        gameName.setText(game.getName());
                                        happened.setSelected(game.didHappen());
                                        team1.setText(game.getTeamNumber1() == null ? "" : String.valueOf(game.getTeamNumber1()));
                                        team2.setText(game.getTeamNumber2() == null ? "" : String.valueOf(game.getTeamNumber2()));
                                        team3.setText(game.getTeamNumber3() == null ? "" : String.valueOf(game.getTeamNumber3()));
                                        team4.setText(game.getTeamNumber4() == null ? "" : String.valueOf(game.getTeamNumber4()));
                                        team5.setText(game.getTeamNumber5() == null ? "" : String.valueOf(game.getTeamNumber5()));
                                        team6.setText(game.getTeamNumber6() == null ? "" : String.valueOf(game.getTeamNumber6()));
                                        if (game.didHappen()) {
                                            blueScore.setText(String.valueOf(game.getBlueAllianceScore()));
                                            redScore.setText(String.valueOf(game.getRedAllianceScore()));
                                            blueRP.setText(String.valueOf(game.getBlueAllianceRP()));
                                            redRP.setText(String.valueOf(game.getRedAllianceRP()));
                                            mapConfiguration.setText(game.getMapConfiguration() == null ? "" : game.getMapConfiguration());
                                        }
                                    }
                                    blueScore.editableProperty().bind(happened.selectedProperty());
                                    redScore.editableProperty().bind(happened.selectedProperty());
                                    blueRP.editableProperty().bind(happened.selectedProperty());
                                    redRP.editableProperty().bind(happened.selectedProperty());
                                    mapConfiguration.editableProperty().bind(happened.selectedProperty());
                                    pane.addRow(0, new Label("Game Name:"), gameName);
                                    pane.addRow(1, new Label("Blue Alliance Team 1:"), team1);
                                    pane.addRow(2, new Label("Blue Alliance Team 2:"), team2);
                                    pane.addRow(3, new Label("Blue Alliance Team 3:"), team3);
                                    pane.addRow(4, new Label("Red Alliance Team 1:"), team4);
                                    pane.addRow(5, new Label("Red Alliance Team 2:"), team5);
                                    pane.addRow(6, new Label("Red Alliance Team 3:"), team6);
                                    pane.addRow(7, new Label("Game Already Completed?"), happened);
                                    pane.addRow(8, new Label("Blue Alliance Score:"), blueScore);
                                    pane.addRow(9, new Label("Red Alliance Score:"), redScore);
                                    pane.addRow(10, new Label("Blue Alliance RP:"), blueRP);
                                    pane.addRow(11, new Label("Red Alliance RP:"), redRP);
                                    pane.addRow(12, new Label("Map Configuration:"), mapConfiguration);
                                    pane.setVgap(10);
                                    pane.setHgap(20);
                                    editor.getDialogPane().setContent(pane);
                                    editor.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
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
                                                                inputSanitizerNumbers(team1.getText()),
                                                                inputSanitizerNumbers(team2.getText()),
                                                                inputSanitizerNumbers(team3.getText()),
                                                                inputSanitizerNumbers(team4.getText()),
                                                                inputSanitizerNumbers(team5.getText()),
                                                                inputSanitizerNumbers(team6.getText())) :
                                                        new ScoutedGame(
                                                                game == null ? (short) list.getItems().indexOf(game) : game.getGame(),
                                                                database.getCompetitionFromName(comp),
                                                                inputSanitizer(gameName.getText()),
                                                                inputSanitizerNumbers(team1.getText()),
                                                                inputSanitizerNumbers(team2.getText()),
                                                                inputSanitizerNumbers(team3.getText()),
                                                                inputSanitizerNumbers(team4.getText()),
                                                                inputSanitizerNumbers(team5.getText()),
                                                                inputSanitizerNumbers(team6.getText())));
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
                ArrayList<ScoutedGame> games = new ArrayList<>();
                for (Tab tab : tabs.getTabs()) {
                    games.addAll(((PopUpListView<ScoutedGame>) tab.getContent()).getItems());
                }
                return games;
            });
            dialog.getDialogPane().setContent(tabs);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.showAndWait();
        });
    }
}
