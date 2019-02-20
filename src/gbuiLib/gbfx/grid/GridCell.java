package gbuiLib.gbfx.grid;

import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;

public class GridCell extends TextField {

    public GridCell(String text, String propertyType) {
        super(text);
        setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
        editableProperty().bind(focusedProperty());
        GridPane.setHgrow(this, Priority.ALWAYS);
        GridPane.setVgrow(this, Priority.ALWAYS);
        getProperties().put("CellType", propertyType);
    }

    public GridCell(String text, double width, String propertyType) {
        this(text, propertyType);
        setMaxWidth(width);
    }

    public String getType() {
        return (String) getProperties().get("CellType");
    }
}