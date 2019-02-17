package serverSide.code;

import connectionIndependent.ScoutingConnections;
import connectionIndependent.scouted.ScoutedTeam;
import gbuiLib.GBSockets.GBServerSocket;
import gbuiLib.GBSockets.PacketLogger;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
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

    private void addGame(String competition, short game, Short[] teams, String mapConfig, Path originalVideo) throws IOException {
//        database.addGame(game, competition, mapConfig, teams);
        scouters.addGame(competition, game, teams);
        File video = new File(new File(ScoutingVars.getVideosDirectory(), competition), game + ".mp4");
        Files.move(originalVideo, video.toPath());
    }

    private void addCompetition(String competition) {
        database.addNewCompetition(competition);
        scouters.addCompetition(competition);
        new File(ScoutingVars.getVideosDirectory(), competition).mkdirs();
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

    private String inputSanitizerNumbers(String input) {
        Pattern pattern = Pattern.compile("[0-9]+");
        Matcher matcher = pattern.matcher(input);
        if (matcher.matches()) return matcher.group(0);
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
                            map.put(Short.valueOf(inputSanitizerNumbers(number.getText())), new ScoutedTeam(Short.valueOf(inputSanitizer(number.getText())), inputSanitizer(name.getText()), compList));
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
                    } catch (NullPointerException e){
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

    }

    private static class GridCell extends TextField {

        public GridCell(String text, String propertyType) {
            super(text);
            setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
            editableProperty().bind(focusedProperty());
            GridPane.setHgrow(this, Priority.ALWAYS);
            GridPane.setVgrow(this, Priority.ALWAYS);
            getProperties().put("CellType", propertyType);
        }

        public GridCell(String text, double width, String propertyType) {
            this(text, propertyType);
            setMaxWidth(width);
        }

        public String getType() {
            return (String) getProperties().get("CellType");
        }
    }

    private static class CheckCell extends CheckBox {

        public CheckCell(String propertyType, boolean selected) {
            super();
            setSelected(selected);
            GridPane.setHgrow(this, Priority.ALWAYS);
            GridPane.setVgrow(this, Priority.ALWAYS);
            getProperties().put("CheckType", propertyType);
        }

        public String getType() {
            return (String) getProperties().get("CheckType");
        }
    }

    private static class RowController extends Button {

        private boolean deleter;

        public RowController(boolean deleter) {
            super(deleter ? "X" : "+");
            this.deleter = deleter;
            setOnAction(event -> {
                EditGrid parent = (EditGrid) getParent();
                if (this.deleter) {
                    parent.removeMyRow(this);
                } else {
                    this.deleter = true;
                    setText("X");
                    parent.addNewRow(GridPane.getRowIndex(this));
                }
            });
        }
    }

    private static abstract class EditGrid extends GridPane {

        public EditGrid() {
            super();
            initialRow();
            setAdder(new RowController(false));
            add(getAdder(), 0, 1);
        }

        public void removeMyRow(RowController deleter) {
            int rowID = GridPane.getRowIndex(deleter);
            getChildren().removeIf(node -> GridPane.getRowIndex(node) == rowID);
            for (Node node : getChildren().filtered(node -> GridPane.getRowIndex(node) > rowID)) {
                GridPane.setRowIndex(node, GridPane.getRowIndex(node) - 1);
            }
            deleter.setOnAction(null);
        }

        private RowController adder = null;

        public RowController getAdder() {
            return adder;
        }

        public void setAdder(RowController adder) {
            if (this.adder != null) {
                this.adder.deleter = true;
                this.adder.setText("X");
            }
            this.adder = adder;
        }

        public abstract void addNewRow(int rowIndex, String... values);

        protected abstract void initialRow();
    }
}
