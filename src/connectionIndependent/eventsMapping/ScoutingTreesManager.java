package connectionIndependent.eventsMapping;

import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public final class ScoutingTreesManager {

    private static ScoutingTreesManager treesManager = null;

    public static void initialize(boolean edit) {
        if (treesManager == null) {
            treesManager = new ScoutingTreesManager(edit);
            ScoutingEventUnit.manager = treesManager;
        }
    }

    public static ScoutingTreesManager getInstance() {
        return treesManager;
    }

    public ArrayList<ScoutingEventTree> loadDirectory(File directoryPath) throws IOException {
        try {
            ArrayList<ScoutingEventTree> output = new ArrayList<>();
            for (File file : directoryPath.listFiles(pathname -> pathname.getAbsolutePath().endsWith(".fxml"))) {
                FXMLLoader loader = new FXMLLoader(file.toURI().toURL());
                loader.setController(this);
                output.add(loader.load());
            }
            return output;
        } catch (IOException e) {
            throw new IOException("Failed to load the trees directory!", e);
        }
    }


    private Paint dragColor = Color.BLACK;
    private Paint selectColor = Color.BLUE;
    private Paint lineAdded = Color.FORESTGREEN;
    private Paint lineRemoved = Color.RED;
    private Paint defaultColor = Color.GRAY;
    private Paint arrowColor = Color.BLACK;
    private boolean editing;
    private HashMap<Byte, ScoutingEventTree> treesMap = new HashMap<>();

    private ScoutingTreesManager(boolean editor) {
        editing = editor;
    }

    public ScoutingEventTree getTree(byte number) {
        return treesMap.get(number);
    }

    public void initialize(){
        for(ScoutingEventTree tree : treesMap.values()){
            registerTree(tree);
        }
    }

    public void addTree(ScoutingEventTree tree){
        byte treeNumber = tree.getTreeNumber();
        while (treesMap.keySet().contains(treeNumber)) {
            treeNumber++;
        }
        tree.setTreeNumber(treeNumber);
        treesMap.put(treeNumber, tree);
    }

    public void registerTree(ScoutingEventTree tree) {
        byte treeNumber = tree.getTreeNumber();
        for (Node layer : tree.getLayers()) {
            if(layer instanceof ScoutingEventLayer) {
                ((ScoutingEventLayer) layer).setTreeNumber(treeNumber);
                ((ScoutingEventLayer) layer).setPrefHeight(200);
                ((ScoutingEventLayer) layer).setPrefWidth(1000);
                for (Node node : ((ScoutingEventLayer) layer).getUnits()) {
                    ScoutingEventUnit unit = (ScoutingEventUnit) node;
                    unit.setPrefWidth(200);
                    unit.setPrefHeight(200);
                }
            }
        }
    }


    public Paint getDragColor() {
        return dragColor;
    }

    public void setDragColor(Paint dragColor) {
        this.dragColor = dragColor;
    }

    public Paint getSelectColor() {
        return selectColor;
    }

    public void setSelectColor(Paint selectColor) {
        this.selectColor = selectColor;
    }

    public Paint getLineAdded() {
        return lineAdded;
    }

    public void setLineAdded(Paint lineAdded) {
        this.lineAdded = lineAdded;
    }

    public Paint getLineRemoved() {
        return lineRemoved;
    }

    public void setLineRemoved(Paint lineRemoved) {
        this.lineRemoved = lineRemoved;
    }

    public Paint getDefaultColor() {
        return defaultColor;
    }

    public void setDefaultColor(Paint defaultColor) {
        this.defaultColor = defaultColor;
    }

    public Paint getArrowColor() {
        return arrowColor;
    }

    public void setArrowColor(Paint arrowColor) {
        this.arrowColor = arrowColor;
    }

    public boolean isEditing() {
        return editing;
    }
}
