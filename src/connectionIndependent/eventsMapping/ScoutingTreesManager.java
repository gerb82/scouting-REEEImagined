package connectionIndependent.eventsMapping;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.Pair;

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

    public void prepareDirectory(File directoryPath) {
        directoryPath.mkdirs();
        for (File file : directoryPath.listFiles(pathname -> pathname.getAbsolutePath().endsWith(".tree"))) {
            file.delete();
        }
    }

    public boolean mustOverride(File directoryPath) {
        return directoryPath.listFiles(pathname -> pathname.getAbsolutePath().endsWith(".tree")).length == 0;
    }

    public ArrayList<Pair<String, ScoutingEventTree>> loadDirectory(File directoryPath) throws IOException {
        try {
            ArrayList<Pair<String,ScoutingEventTree>> output = new ArrayList<>();
            for (File file : directoryPath.listFiles(pathname -> pathname.getAbsolutePath().endsWith(".tree"))) {
                FXMLLoader loader = new FXMLLoader(file.toURI().toURL());
                loader.setController(this);
                ScoutingEventTree tree = loader.load();
                registerTree(tree);
                output.add(new Pair<>(file.getName().split("\\.")[0], tree));
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

    public void addTree(ScoutingEventTree tree) {
        byte treeNumber = tree.getTreeNumber();
        while (treeNumber == -1 || treesMap.keySet().contains(treeNumber)) {
            treeNumber++;
        }
        tree.setTreeNumber(treeNumber);
    }

    public void registerTree(ScoutingEventTree tree) {
        byte treeNumber = tree.getTreeNumber();
        treesMap.put(treeNumber, tree);
        for (Node layer : tree.getLayers()) {
            if (layer instanceof ScoutingEventLayer) {
                ((ScoutingEventLayer) layer).setTreeNumber(treeNumber);
                for (Node node : ((ScoutingEventLayer) layer).getUnits()) {
                    ScoutingEventUnit unit = (ScoutingEventUnit) node;
                    unit.init();
                }
            }
        }
        if (isEditing()) {
            tree.initButton();
        }
        tree.requestLayout();
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

    private byte lastUnitID = Byte.MIN_VALUE;

    public String treeAsFXML(byte treeNumber) {
        ScoutingEventTree tree = treesMap.get(treeNumber);
        String arrows = "";
        String layers = "";
        boolean first = true;
        for (Node layer : tree.getLayers()) {
            if (layer instanceof ScoutingEventLayer) {
                if (first) {
                    assert (((ScoutingEventLayer) layer).getUnits().size() == 1);
                    first = false;
                }
                String units = "";
                for (Node unit : ((ScoutingEventLayer) layer).getUnits()) {
                    ((ScoutingEventUnit) unit).setUnitID(lastUnitID++);
                    units += ((ScoutingEventUnit) unit).toFXML() + System.lineSeparator();
                }
                layers += ((ScoutingEventLayer) layer).toFXML(units) + System.lineSeparator();
            }
        }
        for (Node direction : tree.getArrows()) {
            arrows += ((ScoutingEventDirection) direction).toFXML() + System.lineSeparator();
        }
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator() +
                "<?import connectionIndependent.eventsMapping.ScoutingEventTree?>" + System.lineSeparator() +
                "<?import connectionIndependent.eventsMapping.ScoutingEventLayer?>" + System.lineSeparator() +
                "<?import connectionIndependent.eventsMapping.ScoutingEventUnit?>" + System.lineSeparator() +
                "<?import connectionIndependent.eventsMapping.ScoutingEventDirection?>" + System.lineSeparator() +
                tree.toFXML("xmlns=\"http://javafx.com/javafx\" xmlns:fx=\"http://javafx.com/fxml\"", arrows, layers);
    }
}
