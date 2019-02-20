package gbuiLib.gbfx.grid;

import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;

public class RowController extends Button {

    protected boolean deleter;

    public RowController(boolean deleter) {
        super(deleter ? "X" : "+");
        this.deleter = deleter;
        setOnAction(event -> {
            EditGrid parent = (EditGrid) getParent();
            if (this.deleter) {
                parent.removeMyRow(this);
            } else {
                this.deleter = true;
                setText("X");
                parent.addNewRow(GridPane.getRowIndex(this));
            }
        });
    }
}
