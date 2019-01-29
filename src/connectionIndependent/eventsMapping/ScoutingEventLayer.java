package connectionIndependent.eventsMapping;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class ScoutingEventLayer extends Pane implements ScoutingEventTreePart{

    private ScoutingEventTree tree;
    private HBox units;

    public ScoutingEventLayer(){
        super();
        parentProperty().addListener((observable, oldValue, newValue) -> tree = (ScoutingEventTree) ScoutingEventTreePart.findEventParent(this));
        setManaged(true);
        units = new HBox();
        units.setAlignment(Pos.CENTER);
        units.setManaged(true);
        getChildren().add(units);
        units.prefHeightProperty().bind(heightProperty());
        units.prefWidthProperty().bind(widthProperty());
        this.units.setSpacing(100);
        setWidth(600);
        setHeight(200);
    }

    public ObservableList<Node> getUnits() {
        return units.getChildren();
    }

    public boolean isEditing(){
        return tree.isEditing();
    }

    public int layerNumber(){
        return tree.getLayers().indexOf(this);
    }

    public ScoutingEventTree getTree() {
        return tree;
    }
}
