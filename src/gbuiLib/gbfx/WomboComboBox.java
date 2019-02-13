package gbuiLib.gbfx;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.function.Predicate;

public class WomboComboBox<T> extends ComboBox {

    private ObservableList<T> allOptions;
    private String value;
    private int caretPos;

    public WomboComboBox() {
        super();
        setButtonCell(new ListCell<T>());
        allOptions = FXCollections.observableArrayList();
        setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.DOWN) {
                return;
            }
            value = isEditable() ? getEditor().getText() : getValue() == null ? "" : String.valueOf(getValue());
            changeEditing(event.getCode() != KeyCode.ENTER);
            refreshItems();
        });
        getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            Background background = new Background(new BackgroundFill(isSelectedAnOption(newValue) ? Color.GREEN : Color.RED, CornerRadii.EMPTY, Insets.EMPTY));
            ((StackPane) lookup(".arrow-button")).setBackground(background);
        });
    }

    public void refreshItems(){
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
            Platform.runLater(() -> {
                getSelectionModel().select(value);
                setValue(value);
                getButtonCell().setText(value);
                getButtonCell().setBackground(((StackPane) lookup(".arrow-button")).getBackground());
            });
        }
    }

    private boolean isSelectedAnOption(String value) {
        return !allOptions.filtered(t -> t.toString().equals(String.valueOf(value))).isEmpty();
    }

    public ObservableList<T> getOptions() {
        return allOptions;
    }
}
