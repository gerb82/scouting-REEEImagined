package gbuiLib.gbfx.grid;

import javafx.scene.Node;
import javafx.scene.layout.GridPane;

public abstract class EditGrid extends GridPane {

    public EditGrid() {
        super();
        initialRow();
        setAdder(new RowController(false));
        add(getAdder(), 0, 1);
    }

    public void removeMyRow(RowController deleter) {
        int rowID = GridPane.getRowIndex(deleter);
        getChildren().removeIf(node -> GridPane.getRowIndex(node) == rowID);
        for (Node node : getChildren().filtered(node -> GridPane.getRowIndex(node) > rowID)) {
            GridPane.setRowIndex(node, GridPane.getRowIndex(node) - 1);
        }
        deleter.setOnAction(null);
    }

    private RowController adder = null;

    public RowController getAdder() {
        return adder;
    }

    public void setAdder(RowController adder) {
        if (this.adder != null) {
            this.adder.deleter = true;
            this.adder.setText("X");
        }
        this.adder = adder;
    }

    public abstract void addNewRow(int rowIndex, String... values);

    protected abstract void initialRow();
}
