package connectionIndependent.eventsMapping;

import javafx.beans.NamedArg;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;

public class ScoutingEventTree extends Pane{

    private boolean editing;
    private Paint dragColor;
    private Paint selectColor;
    private Paint lineAdded;
    private Paint lineRemoved;
    private Paint defaultColor;
    private VBox layers;
    private ObservableList<ScoutingEventDirection> arrows;

    public ScoutingEventTree(@NamedArg("editor") boolean editor, @NamedArg("dragColor") Paint dragColor, @NamedArg("selectColor") Paint selectColor, @NamedArg("lineAdded") Paint lineAdded, @NamedArg("lineRemoved") Paint lineRemoved, @NamedArg("defaultColor") Paint defaultColor){
        super();
        setManaged(false);
        this.dragColor = dragColor;
        this.selectColor = selectColor;
        this.defaultColor = defaultColor;
        this.lineAdded = lineAdded;
        this.lineRemoved = lineRemoved;
        layers = new VBox();
        getChildren().add(layers);
        layers.setManaged(true);
        layers.prefWidthProperty().bind(widthProperty());
        layers.prefHeightProperty().bind(heightProperty());
        arrows = FXCollections.observableArrayList();
        arrows.addListener((ListChangeListener<ScoutingEventDirection>) c -> {
            if(c.wasRemoved()){
                getChildren().removeAll(c.getRemoved());
            }
            if(c.wasAdded()){
                getChildren().addAll(c.getAddedSubList());
            }
        });
        editing = editor;
    }

    public ObservableList<ScoutingEventDirection> getArrows(){
        return arrows;
    }

    public ObservableList<Node> getLayers(){
        return layers.getChildren();
    }

    public boolean isEditing(){
        return editing;
    }

    public void addArrow(ScoutingEventDirection direction){
        arrows.add(direction);
        getChildren().add(direction);
    }

    public void removeArrow(ScoutingEventDirection direction){
        arrows.remove(direction);
        getChildren().remove(direction);
    }

    public Paint getDragColor() {
        return dragColor;
    }

    public Paint getSelectColor() {
        return selectColor;
    }

    public Paint getLineAdded() {
        return lineAdded;
    }

    public Paint getLineRemoved() {
        return lineRemoved;
    }

    public Paint getDefaultColor() {
        return defaultColor;
    }
}
