package configurator;

import connectionIndependent.eventsMapping.ScoutingEventTree;
import connectionIndependent.eventsMapping.ScoutingTreesManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

public class Configurator extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private File storageDir = new File(new File(System.getProperty("userDir"), "data"), "config");

    private Stage stage;
    private ScoutingTreesManager manager;
    @FXML
    private Pane root;

    @FXML
    private TabPane tabs;

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Editor.fxml"));
        ScoutingTreesManager.initialize(true);
        manager = ScoutingTreesManager.getInstance();
        loader.setController(this);
        primaryStage.setScene(new Scene(loader.load()));
        root.prefWidthProperty().bind(primaryStage.getScene().widthProperty());
        root.prefHeightProperty().bind(primaryStage.getScene().heightProperty());
        for(Pair<String,ScoutingEventTree> treeTab : manager.loadDirectory(storageDir)){
            tabs.getTabs().add(new TreeEditTab(treeTab.getKey(), treeTab.getValue()));
        }
        if(tabs.getTabs().size() == 0){
            Platform.runLater(this::addNewTab);
        }
        primaryStage.show();
    }

    @FXML
    private void save() {
        manager.prepareDirectory(storageDir);
        for (Tab tad : tabs.getTabs()) {
            TreeEditTab tab = (TreeEditTab) tad;
            File file = new File(storageDir, tab.getText() + ".tree");
            try {
                file.createNewFile();
                try (FileOutputStream stream = new FileOutputStream(file)) {
                    stream.write(manager.treeAsFXML(tab.getTreeNumber()).getBytes());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void addNewTab(){
        Dialog<Pair<String, Boolean>> newTab = new Dialog<>();
        newTab.setTitle("New Tab");
        newTab.setHeaderText("Create a new tab");
        ButtonType loginButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        newTab.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        TextField fileName = new TextField();
        fileName.setPromptText("name");
        CheckBox alliance = new CheckBox();
        grid.add(new Label("Tree name:"), 0, 0);
        grid.add(fileName, 1, 0);
        grid.add(new Label("Is alliance event:"), 0, 1);
        grid.add(alliance, 1, 1);
        newTab.getDialogPane().setContent(grid);

        newTab.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(fileName.getText(), alliance.isSelected());
            }
            return null;
        });

        Optional<Pair<String, Boolean>> result = newTab.showAndWait();

        tabs.getTabs().add(new TreeEditTab(result.get().getKey()));
    }

    public class TreeEditTab extends Tab {

        private class CustomScrollPane extends ScrollPane{
            @Override
            public void setHeight(double value) {
                super.setHeight(value);
            }
        }
        private byte treeNumber;
        private ScoutingEventTree tree;
        private CustomScrollPane scroll;

        public TreeEditTab(String name, ScoutingEventTree tree){
            super(name);
            scroll = new CustomScrollPane();
            scroll.setManaged(true);
            setContent(scroll);
            scroll.setMaxHeight(Double.MAX_VALUE);
            setStyle("-fx-background-color: " + "yellow");
            this.tree = tree;
            treeNumber = tree.getTreeNumber();
            scroll.setMaxHeight(Double.MAX_VALUE);
            scroll.maxWidthProperty().bind(tabs.widthProperty());
            tree.prefWidthProperty().bind(scroll.widthProperty());
            scroll.setContent(tree);
        }

        public TreeEditTab(String name) {
            this(name, new ScoutingEventTree());
            manager.registerTree(tree);
            treeNumber = tree.getTreeNumber();
        }

        public byte getTreeNumber() {
            return treeNumber;
        }
    }
}
