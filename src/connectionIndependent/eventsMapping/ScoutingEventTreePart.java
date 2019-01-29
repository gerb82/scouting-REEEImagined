package connectionIndependent.eventsMapping;

import javafx.scene.Node;
import javafx.scene.Parent;

public interface ScoutingEventTreePart {
    static Parent findEventParent(ScoutingEventTreePart part){
        Parent parent = ((Node)part).getParent();
        if(parent == null){
            return null;
        }
        while(true){
            if(parent instanceof ScoutingEventTreePart){
                return parent;
            }
            parent = parent.getParent();
        }
    }
}
