package configurator;

import com.sun.deploy.config.Platform;
import connectionIndependent.eventsMapping.ScoutingEventTree;
import connectionIndependent.eventsMapping.ScoutingTreesManager;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Configurator extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private File storageDir = new File("C:\\Users\\Programmer\\Desktop\\javafx\\workspace\\scouting-REEEImagined\\data\\config");

    private ScoutingTreesManager manager;
    @FXML
    private Pane root;

    @FXML
    private TabPane tabs;
    private Tab addTabs;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Editor.fxml"));
        ScoutingTreesManager.initialize(true);
        manager = ScoutingTreesManager.getInstance();
        loader.setController(this);
        primaryStage.setScene(new Scene(loader.load()));
        root.prefWidthProperty().bind(primaryStage.getScene().widthProperty());
        root.prefHeightProperty().bind(primaryStage.getScene().heightProperty());
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
        tabs.getTabs().add(new TreeEditTab("test", false));
    }

    public class TreeEditTab extends Tab {

        private byte treeNumber;
        private ScoutingEventTree tree;

        public TreeEditTab(String name, boolean alliance) {
            super(name);
            ScrollPane scroll = new ScrollPane();
            setContent(scroll);
            scroll.setMaxHeight(Double.MAX_VALUE);
            tree = new ScoutingEventTree();
            tree.setAlliance(alliance);
            manager.registerTree(tree);
            treeNumber = tree.getTreeNumber();
            scroll.setContent(tree);
            scroll.prefWidthProperty().bind(tree.widthProperty());
            scroll.prefHeightProperty().bind(tree.heightProperty());
        }

        public byte getTreeNumber() {
            return treeNumber;
        }
    }
}
