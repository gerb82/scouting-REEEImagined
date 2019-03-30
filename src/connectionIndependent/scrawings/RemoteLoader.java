package connectionIndependent.scrawings;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public class RemoteLoader {

    private static String directoryLink;
    private static HashMap<String, Node> nodes = new HashMap<>();
    private static ObservableList<String> fileLocs = FXCollections.observableArrayList();

    public static void setLoadDirectory(String directory) {
        directoryLink = directory;
    }

    public void initialize() throws IOException {
        for (String scraw : fileLocs) {
            nodes.put(scraw, FXMLLoader.load(new URL(directoryLink += File.separator + scraw)));
        }
    }

    public RemoteLoader() {
        fileLocs.clear();
        nodes.clear();
    }

    public static HashMap<String, Node> getNodes() {
        return nodes;
    }

    public ObservableList<String> getFileLocs() {
        return fileLocs;
    }
}
