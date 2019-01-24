package connectionIndependent.eventsMapping;

import javafx.scene.shape.Rectangle;

public class Pivot<T> extends Rectangle {

    private T value;

    public Pivot(T value){
        this.value = value;
        setWidth(20);
        setHeight(20);
    }

    public T getValue(){
        return value;
    }
}
