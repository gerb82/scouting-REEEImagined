package gbuiLib.gbfx.popUpListView;

import javafx.scene.control.Dialog;
import javafx.util.Callback;

import java.util.Optional;

public abstract class PopUpEditCell<T> extends EditCell<T> {

    @Override
    protected void editGraphic() {
        if (this.getItem() != null) {
            Optional<T> result = createDialog().call(this.getItem()).showAndWait();
            if (result.isPresent()) commitEdit(result.get());
            else cancelEdit();
        } else if (this.getItem() == null && this.getIndex() == this.getListView().getItems().size() - 1) {
            Optional<T> result = createDialog().call(this.getItem()).showAndWait();
            if (result.isPresent()) {
                commitEdit(result.get());
                getListView().getItems().add(null);
            } else cancelEdit();
        } else {
            cancelEdit();
        }
    }

    protected abstract Callback<T, Dialog<T>> createDialog();
}
