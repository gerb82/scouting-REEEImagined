package gbuiLib.gbfx;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;

public class WomboComboBox<T> extends ComboBox<T> {

    private ObservableList<T> allOptions;
    private String value = "";
    private int caretPos;

    public WomboComboBox() {
        super();
        setButtonCell(new ListCell<>());
        allOptions = FXCollections.observableArrayList();
        setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.DOWN) {
                return;
            }
            value = isEditable() ? getEditor().getText() : getValue() == null ? "" : getEditor().getText();
            refreshItems();
        });
        focusedProperty().addListener((observable, oldValue, newValue) -> {
            value = isEditable() ? getEditor().getText() : getValue() == null ? "" : getEditor().getText();
            changeEditing(newValue);
        });
        getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            Background background = new Background(new BackgroundFill(isSelectedAnOption(newValue) ? Color.GREEN : Color.RED, CornerRadii.EMPTY, Insets.EMPTY));
            ((StackPane) lookup(".arrow-button")).setBackground(background);
        });
        allOptions.addListener(new ListChangeListener<T>() {
            @Override
            public void onChanged(Change<? extends T> c) {
                refreshItems();
                setEditable(true);
                allOptions.removeListener(this);
            }
        });
    }

    public void refreshItems() {
        hide();
        getItems().clear();
        getItems().addAll(allOptions.filtered(test -> test.toString().contains(value)));
        if (!getItems().isEmpty() && isEditable()) {
            requestLayout();
            show();
        }
    }

    private void changeEditing(boolean editing) {
        if (editing && !isEditable()) {
            setEditable(true);
            getEditor().positionCaret(caretPos);
        } else if (!editing && isEditable()) {
            caretPos = getEditor().getCaretPosition();
            setEditable(false);
            FilteredList<T> finalVal = allOptions.filtered(test -> test.toString().equals(value));
            if (finalVal.size() > 0) {
                getSelectionModel().select(finalVal.get(0));
                setValue(finalVal.get(0));
            }
            getButtonCell().setText(value);
            getButtonCell().setBackground(((StackPane) lookup(".arrow-button")).getBackground());
        }

    }

    private boolean isSelectedAnOption(String value) {
        return !allOptions.filtered(t -> t.toString().equals(String.valueOf(value))).isEmpty();
    }

    public ObservableList<T> getOptions() {
        return allOptions;
    }
}
