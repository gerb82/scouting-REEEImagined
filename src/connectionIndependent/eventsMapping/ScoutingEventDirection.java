package connectionIndependent.eventsMapping;

import javafx.beans.NamedArg;
import javafx.scene.shape.Line;

public class ScoutingEventDirection extends Line {

    private ScoutingEventUnit start;
    private ScoutingEventUnit end;
    private ScoutingEventTree tree;

    public ScoutingEventDirection(@NamedArg("start") ScoutingEventUnit start, @NamedArg("end") ScoutingEventUnit end, @NamedArg("tree") ScoutingEventTree tree){
        super();
        this.setMouseTransparent(true);
        this.start = start;
        this.end = end;
        this.start.bindToBottom(startXProperty(), startYProperty());
        this.end.bindToTop(endXProperty(), endYProperty());
        this.tree = tree;
        this.tree.addArrow(this);
    }

    public void discard(){
        startXProperty().unbind();
        startYProperty().unbind();
        endXProperty().unbind();
        endYProperty().unbind();
        tree.removeArrow(this);
    }
}
