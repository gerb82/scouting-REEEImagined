package connectionIndependent.scrawings;

import connectionIndependent.scrawings.hitboxes.PossibleHitBox;
import connectionIndependent.scrawings.scrawtypes.DataScraw;
import connectionIndependent.scrawings.scrawtypes.ScrawRecipe;
import gbuiLib.gbfx.popUpListView.DeleteableCell;
import gbuiLib.gbfx.popUpListView.PopUpEditCell;
import gbuiLib.gbfx.popUpListView.PopUpListView;
import javafx.application.Application;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;


public class Main extends Application {
    private File runningDirectory = new File(new File(System.getProperty("userDir"), "data"), "config");

    private void gridUpdate(GridPane grid, DataScraw scraw, ScrawRecipe recipe) {
        grid.getChildren().removeIf(node -> !(node instanceof ComboBox));
        if (recipe == null) return;
        if (scraw.getRecipeName() == null || !scraw.getRecipeName().equals(recipe.getName())) {
            scraw.getData().clear();
            scraw.setRecipeName(recipe.getName());
        }
        int highestID = -1;
        for (Node node : recipe.getChildren()) {
            if (node instanceof PossibleHitBox) {
                highestID = highestID < ((PossibleHitBox) node).getHitboxId() ? ((PossibleHitBox) node).getHitboxId() : highestID;
            }
        }
        for (int i = 0; i <= highestID; i++) {
            final int myNum = i;
            if (scraw.getData().size() <= myNum || scraw.getData().get(myNum) == null) scraw.getData().add(myNum, new ArrayList<>());
            grid.addRow(myNum + 1, new Label("Nodes " + myNum), new TextField(arraySerialize(scraw.getData().get(myNum))) {{
                textProperty().addListener((observable, oldValue, newValue) -> {
                    String checkedInput = newValue.replaceAll("[^0-9,]|,+[^0-9]*,+", "");
                    String[] numbers = checkedInput.split(",");
                    for (String number : numbers) {
                        if (Integer.parseInt(number) > 127 || Integer.parseInt(number) < -128) {
                            ((StringProperty) observable).set(oldValue);
                            return;
                        }
                    }
                    if (!checkedInput.equals(newValue)) {
                        ((StringProperty) observable).set(newValue);
                        return;
                    }
                    ArrayList<Byte> array = scraw.getData().get(myNum);
                    array.clear();
                    for (String number : numbers) {
                        array.add(Byte.valueOf(number));
                    }
                });
            }});
        }
    }

    private String arraySerialize(ArrayList<Byte> array) {
        String result = "";
        boolean first = true;
        for (Byte bite : array) {
            result += (!first ? "," : "") + bite;
            first = false;
        }
        return result;
    }

    private Tab dataScraws(boolean blueAlliance) {
        Tab dataScraws = new Tab(blueAlliance ? "Blue Alliance" : "Red Alliance");
        SplitPane split = new SplitPane();
        ListView<DataScraw> dScraws = new ListView<DataScraw>();
        dScraws.setEditable(true);
        dScraws.getItems().add(null);
        dScraws.setCellFactory(param -> new DeleteableCell<DataScraw>() {
            @Override
            protected void updateItem(DataScraw item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    setText("#" + item.getScrawNumber() + " (" + item.getRecipeName() + ")");
                } else if (this.getItem() == null && getIndex() == getListView().getItems().size() - 1) {
                    setText("Add Recipe Preset");
                }
            }

            @Override
            public void onDelete() {
                super.onDelete();
                (blueAlliance ? ScrawingsManager.getInstance().getBlueScrawsMap() : ScrawingsManager.getInstance().getRedScrawsMap()).remove(getItem().getScrawNumber(), getItem());
            }

            @Override
            public void startEdit() {
                super.startEdit();
                if (getItem() == null && ScrawingsManager.getInstance().getRecipesList().size() > 0) {
                    commitEdit(new DataScraw(ScrawingsManager.getInstance().getRecipesList().get(0), DataScraw.bytesArray(), (byte) 0));
                    ScrawingsManager.getInstance().registerScraw(getItem(), blueAlliance);
                    getListView().getItems().add(null);
                } else {
                    cancelEdit();
                }
            }
        });
        ScrollPane preview = new ScrollPane() {{
            setDisable(true);
            setOpacity(1);
        }};
        GridPane values = new GridPane();
        SplitPane editor = new SplitPane();
        editor.setOrientation(Orientation.VERTICAL);
        editor.getItems().addAll(values, preview);
        ComboBox<ScrawRecipe> type = new ComboBox<>();
        type.setCellFactory(param -> new ListCell<ScrawRecipe>() {
            @Override
            protected void updateItem(ScrawRecipe item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    setText(item.getName());
                }
            }
        });
        type.setButtonCell(new ListCell<ScrawRecipe>() {
            @Override
            protected void updateItem(ScrawRecipe item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    setText(item.getName());
                }
            }
        });
        dScraws.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;
            if (newValue.getRecipeName() == null) return;
            type.getSelectionModel().select(ScrawingsManager.getInstance().getRecipesList().filtered(scrawRecipe -> newValue.getRecipeName().equals(scrawRecipe.getName())).get(0));
            preview.setContent(type.getSelectionModel().getSelectedItem());
            gridUpdate(values, newValue, type.getSelectionModel().getSelectedItem());
        });
        dataScraws.tabPaneProperty().addListener((observable, oldValue, newValue) -> dataScraws.getTabPane().getSelectionModel().selectedItemProperty().addListener((observable1, oldValue1, newValue1) -> {
            if (!newValue.equals(dataScraws)) {
                type.getItems().clear();
                type.getItems().addAll(ScrawingsManager.getInstance().getRecipesList());
            }
        }));
        dScraws.getItems().addListener((ListChangeListener<? super DataScraw>) c -> {
            while (c.next()) {
                if (c.wasPermutated()) continue;
                dScraws.getItems().sort((o1, o2) -> {
                    if (o1 == null) return 1;
                    if (o2 == null) return -1;
                    return o1.getScrawNumber() - o2.getScrawNumber();
                });
            }
        });
        type.getItems().addAll(ScrawingsManager.getInstance().getRecipesList());
        dScraws.getItems().addAll((blueAlliance ? ScrawingsManager.getInstance().getBlueScrawsMap() : ScrawingsManager.getInstance().getRedScrawsMap()).values());
        values.addRow(0, type);
        type.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            gridUpdate(values, dScraws.getSelectionModel().getSelectedItem(), newValue);
            preview.setContent(newValue);
        });
        split.getItems().addAll(dScraws, editor);
        dataScraws.setContent(split);
        return dataScraws;
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        TabPane types = new TabPane();
        Tab recipes = new Tab("Recipes");
        ScrawingsManager.initialize(true);
        ScrawingsManager.getInstance().loadDirectory(runningDirectory);

        types.getTabs().addAll(recipes, dataScraws(true), dataScraws(false));
        VBox box = new VBox();
        TextField recipeName = new TextField() {{
            setDisable(true);
        }};
        SplitPane split = new SplitPane();
        PopUpListView scraws = new PopUpListView<ScrawRecipe>() {
            @Override
            protected Callback<ListView<ScrawRecipe>, ListCell<ScrawRecipe>> customCellFactory() {
                return param -> new PopUpEditCell<ScrawRecipe>() {
                    @Override
                    protected Callback<ScrawRecipe, Dialog<ScrawRecipe>> createDialog() {
                        return param1 -> ScrawingsManager.getInstance().startScrawEdit(param1 == null ? ScrawingsManager.getInstance().createScraw() : param1);
                    }

                    @Override
                    protected void refreshGraphic(ScrawRecipe item, boolean empty) {
                        if (item != null) {
                            setText(item.getName());
                        } else if (this.getItem() == null && getIndex() == getListView().getItems().size() - 1) {
                            setText("Add Recipe");
                        }
                    }

                    @Override
                    public void onDelete() {
                        super.onDelete();
                        ScrawingsManager.getInstance().getRecipesList().remove(getItem());
                    }
                };
            }
        };
        ScrollPane preview = new ScrollPane() {{
            setDisable(true);
            setOpacity(1);
        }};
        scraws.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            recipeName.setDisable(newValue == null);
            recipeName.setText(newValue == null ? "" : ((ScrawRecipe) newValue).getName());
            if (newValue == null) return;
            preview.setContent((ScrawRecipe) newValue);
        });
        scraws.getItems().addAll(ScrawingsManager.getInstance().getRecipesList());
        scraws.getItems().add(null);
        recipeName.textProperty().addListener((observable, oldValue, newValue) -> {
            ScrawRecipe recipe = (ScrawRecipe) scraws.getSelectionModel().getSelectedItem();
            if (recipe == null) return;
            recipe.setName(newValue);
            scraws.getItems().set(scraws.getItems().indexOf(recipe), recipe);
        });
        box.getChildren().addAll(recipeName, scraws);
        split.getItems().addAll(box, preview);
        recipes.setContent(split);

        Scene scene = new Scene(types, 700, 500);
        primaryStage.setOnCloseRequest(event -> {
            try {
                ScrawingsManager.getInstance().prepareDirectory(runningDirectory);
                ScrawingsManager.getInstance().saveScrawsToDirectory(runningDirectory);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
