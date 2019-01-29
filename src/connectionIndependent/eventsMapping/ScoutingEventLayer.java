package connectionIndependent.eventsMapping;

import javafx.beans.DefaultProperty;
import javafx.beans.NamedArg;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class ScoutingEventLayer extends Pane implements ScoutingEventTreePart{

    private Pivot<ScoutingEventLayer> anchor;
    private Pane units;
    private byte treeNumber;

    public ScoutingEventLayer(){
        super();
        setBackground(new Background(new BackgroundFill(Color.BLUE, null, null)));
        setManaged(true);
        anchor = new Pivot<>(this);
        units = new HBox();
        units.setManaged(true);
        getChildren().addAll(anchor, units);
        anchor.setLayoutX(0);
        anchor.setLayoutY(0);
        setWidth(1000);
        setHeight(200);
        units.setPrefWidth(getWidth()-anchor.getWidth());
        units.setPrefHeight(getHeight());
        units.setLayoutX(anchor.getWidth());
        units.setMinHeight(200);
        units.setLayoutY(0);
        layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
            anchor.setLayoutX(0);
            anchor.setLayoutY(0);
            units.setPrefWidth(newValue.getWidth()-anchor.getWidth());
            units.setPrefHeight(newValue.getHeight());
            units.setLayoutX(anchor.getWidth());
            units.setLayoutY(0);
        });
    }

    public ObservableList<Node> getUnits() {
        return units.getChildren();
    }

    public int layerNumber(){
        return getTree().getLayers().indexOf(this);
    }

    public ScoutingEventTree getTree() {
        return ScoutingTreesManager.getInstance().getTree(treeNumber);
    }

    public double getUnitWidth(){
        return units.getWidth();
    }

    public void setTreeNumber(byte treeNumber) {
        this.treeNumber = treeNumber;
    }

    public byte getTreeNumber() {
        return treeNumber;
    }
}
