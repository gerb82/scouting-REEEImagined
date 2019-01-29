package connectionIndependent.eventsMapping;

import javafx.beans.DefaultProperty;
import javafx.beans.NamedArg;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.List;

public class ScoutingEventTree extends Pane implements ScoutingEventTreePart{

    private VBox layers;
    private Pivot<ScoutingEventTree> anchor = null;
    private ScoutingEventUnit linkStarter = null;
    private boolean linkExit = false;
    private byte treeNumber = 1;

    public ScoutingEventUnit getLinkStarter() {
        return linkStarter;
    }

    public void setLinkStarter(ScoutingEventUnit linkStarter) {
        this.linkStarter = linkStarter;
    }

    public boolean isLinkExit() {
        return linkExit;
    }

    public void setLinkExit(boolean linkExit) {
        this.linkExit = linkExit;
    }

    public ScoutingEventTree(){
        super();
        setManaged(false);
        layers = new VBox(30);
        layers.setBackground(new Background(new BackgroundFill(Color.GREEN, null, null)));
        layers.setManaged(true);
        if(ScoutingTreesManager.getInstance().isEditing()) {
            getChildren().add(layers);
        } else {
            anchor = new Pivot<>(this);
            getChildren().addAll(layers, anchor);
        }
        layers.prefWidthProperty().bind(widthProperty());
        layers.prefHeightProperty().bind(heightProperty());
        layers.setLayoutX(0);
        layers.setLayoutY(0);
        setWidth(1000);
        setHeight(3000);
        ScoutingTreesManager.getInstance().addTree(this);
    }

    public ObservableList<Node> getLayers(){
        return layers.getChildren();
    }

    public List<Node> getArrows() {
        return getChildren().subList(anchor == null ? 2 : 3, getChildren().size());
    }

    public byte getTreeNumber() {
        return treeNumber;
    }

    public void setTreeNumber(byte treeNumber) {
        this.treeNumber = treeNumber;
    }
}
