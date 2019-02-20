package connectionIndependent.eventsMapping;

import javafx.beans.NamedArg;
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
        start.exiting.put(end, this);
        end.arriving.put(start, this);
    }

    public ScoutingEventUnit getStart() {
        return start;
    }

    public ScoutingEventUnit getEnd() {
        return end;
    }

    public String toFXML(){
        return String.format("<ScoutingEventDirection start=\"$%s\" end=\"$%s\"/>", ScoutingEventUnit.unitID(start.getUnitID()), ScoutingEventUnit.unitID(end.getUnitID()));
    }
}
