package gbuiLib.gbfx.popUpListView;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;

public class DeleteableCell<T> extends ListCell<T> {

    public DeleteableCell(){
        setContextMenu(new ContextMenu(new MenuItem("Delete"){{setOnAction(event -> {if(getItem() != null && getIndex() != getListView().getItems().size() - 1) getListView().getItems().remove(DeleteableCell.this); onDelete();});}}));
    }

    public void onDelete(){}
}
