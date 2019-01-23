package connectionIndependent.eventsMapping;

import javafx.beans.NamedArg;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;

public class ScoutingEventTree extends VBox{

    private boolean editing;
    private Paint dragColor;
    private Paint selectColor;
    private Paint lineAdded;
    private Paint lineRemoved;
    private Paint defaultColor;
    private ObservableList<ScoutingEventDirection> arrows;

    public ScoutingEventTree(@NamedArg("editor") boolean editor, @NamedArg("dragColor") Paint dragColor, @NamedArg("selectColor") Paint selectColor, @NamedArg("lineAdded") Paint lineAdded, @NamedArg("lineRemoved") Paint lineRemoved, @NamedArg("defaultColor") Paint defaultColor){
        super();
        this.dragColor = dragColor;
        this.selectColor = selectColor;
        this.defaultColor = defaultColor;
        this.lineAdded = lineAdded;
        this.lineRemoved = lineRemoved;
        getChildren().add(new ScoutingEventLayer(this));
        editing = editor;
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
