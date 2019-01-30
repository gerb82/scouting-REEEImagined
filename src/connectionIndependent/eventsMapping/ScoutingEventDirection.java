package connectionIndependent.eventsMapping;

import javafx.beans.NamedArg;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.shape.Line;

public class ScoutingEventDirection extends Line implements ScoutingEventTreePart {

    private ScoutingEventUnit start;
    private ScoutingEventUnit end;

    public ScoutingEventDirection(@NamedArg("start") ScoutingEventUnit start, @NamedArg("end") ScoutingEventUnit end){
        super();
        this.setStroke(ScoutingTreesManager.getInstance().getArrowColor());
        this.setMouseTransparent(true);
        this.start = start;
        this.end = end;
    }

    public ScoutingEventUnit getStart() {
        return start;
    }

    public ScoutingEventUnit getEnd() {
        return end;
    }
}
