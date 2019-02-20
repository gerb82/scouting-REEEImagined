package gbuiLib.gbfx.grid;

import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class CheckCell extends CheckBox {

    public CheckCell(String propertyType, boolean selected) {
        super();
        setSelected(selected);
        GridPane.setHgrow(this, Priority.ALWAYS);
        GridPane.setVgrow(this, Priority.ALWAYS);
        getProperties().put("CheckType", propertyType);
    }

    public String getType() {
        return (String) getProperties().get("CheckType");
    }
}