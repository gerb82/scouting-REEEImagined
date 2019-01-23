package connectionIndependent.eventsMapping;

import javafx.beans.NamedArg;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;

public class ScoutingEventLayer extends HBox{

    private ScoutingEventTree tree;

    public ScoutingEventLayer(@NamedArg("tree") ScoutingEventTree tree){
        super();
        this.tree = tree;
    }

    public boolean isEditing(){
        return tree.isEditing();
    }

    public int layerNumber(){
        return tree.getChildren().indexOf(this);
    }

    public ScoutingEventTree getTree() {
        return tree;
    }

    public Paint getDragColor() {
        return tree.getDragColor();
    }

    public Paint getSelectColor() {
        return tree.getSelectColor();
    }

    public Paint getLineAdded() {
        return tree.getLineAdded();
    }

    public Paint getLineRemoved() {
        return tree.getLineRemoved();
    }

    public Paint getDefaultColor() {
        return tree.getDefaultColor();
    }
}
