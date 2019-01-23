package connectionIndependent.eventsMapping;

import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

public class Pivot<T> extends Rectangle {

    private T value;

    public Pivot(T value){
        this.value = value;
    }

    public T getValue(){
        return value;
    }
}
