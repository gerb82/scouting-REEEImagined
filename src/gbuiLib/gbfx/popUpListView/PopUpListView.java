package gbuiLib.gbfx.popUpListView;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public abstract class PopUpListView<T> extends ListView<T> {

    public PopUpListView() {
        setEditable(true);
        setCellFactory(customCellFactory());
    }

    protected abstract Callback<ListView<T>, ListCell<T>> customCellFactory();
}
