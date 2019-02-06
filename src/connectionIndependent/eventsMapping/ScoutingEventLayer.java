package connectionIndependent.eventsMapping;

import javafx.beans.DefaultProperty;
import javafx.beans.NamedArg;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class ScoutingEventLayer extends Pane implements ScoutingEventTreePart {

    private Pivot<ScoutingEventLayer> anchor;
    private Pivot<ScoutingEventLayer> adder;
    private Pivot<ScoutingEventLayer> remover;
    private Pane units;
    private byte treeNumber;

    public ScoutingEventLayer() {
        super();
        VBox.setVgrow(this, Priority.ALWAYS);
        setBackground(new Background(new BackgroundFill(Color.BLUE, null, null)));
        setManaged(true);
        units = new HBox();
        units.setManaged(true);
        setMinWidth(1000);
        setMinHeight(200);

        if (ScoutingTreesManager.getInstance().isEditing()) {
            anchor = new Pivot<>(this);
            adder = new Pivot<>(this);
            remover = new Pivot<>(this);
            getChildren().addAll(anchor, adder, remover, units);
            anchor.setLayoutX(0);
            anchor.setLayoutY((getHeight()-remover.getHeight())/2);
            adder.setLayoutX(0);
            adder.setLayoutY(getHeight() - adder.getHeight());
            remover.setLayoutX(0);
            remover.setLayoutY(0);
            remover.setOnMouseClicked(this::remove);
            adder.setOnMouseClicked(this::addNewUnit);
            units.setPrefWidth(getWidth() - anchor.getWidth());
            units.setLayoutX(anchor.getWidth());
        } else {
            getChildren().addAll(units);
            units.setLayoutX(0);
            units.prefWidthProperty().bind(widthProperty());
        }
        units.setLayoutY(0);
        units.prefHeightProperty().bind(heightProperty());

        layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
            if(ScoutingTreesManager.getInstance().isEditing()) {
                anchor.setLayoutX(0);
                anchor.setLayoutY((getHeight()-remover.getHeight())/2);
                adder.setLayoutX(0);
                adder.setLayoutY(getHeight() - adder.getHeight());
                remover.setLayoutX(0);
                remover.setLayoutY(0);
                units.setPrefWidth(newValue.getWidth() - anchor.getWidth());
                units.setLayoutX(anchor.getWidth());
            } else {
                units.setLayoutX(0);
            }
            units.setLayoutY(0);
        });
    }

    public void addNewUnit(Event event) {
        if(getTree().getLayers().indexOf(this) == 0){
            if(units.getChildren().size() != 0) return;
        }
        ScoutingEventUnit unit = new ScoutingEventUnit();
        getUnits().add(unit);
    }

    public void remove(Event event){
        for(Node node : getUnits()){
            ((ScoutingEventUnit)node).removeFromLayer();
        }
        getUnits().removeAll(getUnits());
        getTree().getLayers().remove(this);
    }

    public ObservableList<Node> getUnits() {
        return units.getChildren();
    }

    public int layerNumber() {
        return getTree().getLayers().indexOf(this);
    }

    public ScoutingEventTree getTree() {
        return ScoutingTreesManager.getInstance().getTree(treeNumber);
    }

    public double getUnitWidth() {
        return units.getWidth();
    }

    public void setTreeNumber(byte treeNumber) {
        this.treeNumber = treeNumber;
    }

    public byte getTreeNumber() {
        return treeNumber;
    }

    public String toFXML(String units){
        return String.format("<ScoutingEventLayer treeNumber=\"%d\">%n<units>%n%s</units>%n</ScoutingEventLayer>", treeNumber, units);
    }
}
