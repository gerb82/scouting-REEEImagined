package gbuiLib.gbfx.popUpListView;

import javafx.scene.control.ListCell;
import javafx.scene.input.MouseButton;

public abstract class EditCell<T> extends ListCell<T> {

    @Override
    public void startEdit() {
        super.startEdit();
        editGraphic();
    }

    protected abstract void editGraphic();

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        refreshGraphic(item, empty);
    }

    protected abstract void refreshGraphic(T item, boolean empty);
}
