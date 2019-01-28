package connectionIndependent.eventsMapping;

import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

public class ScoutingEventTree extends Pane{

    private boolean editing;
    private SimpleObjectProperty<Paint> dragColor;
    private SimpleObjectProperty<Paint> selectColor;
    private SimpleObjectProperty<Paint> lineAdded;
    private SimpleObjectProperty<Paint> lineRemoved;
    private SimpleObjectProperty<Paint> defaultColor;
    private VBox layers;
    private Pivot<ScoutingEventTree> anchor;
    private ScoutingEventUnit linkStarter = null;
    private boolean linkExit;

    public ScoutingEventUnit getLinkStarter() {
        return linkStarter;
    }

    public void setLinkStarter(ScoutingEventUnit linkStarter) {
        this.linkStarter = linkStarter;
    }

    public boolean isLinkExit() {
        return linkExit;
    }

    public void setLinkExit(boolean linkExit) {
        this.linkExit = linkExit;
    }

    public ScoutingEventTree(@NamedArg("editor") boolean editor, @NamedArg("dragColor") Paint dragColor, @NamedArg("selectColor") Paint selectColor, @NamedArg("lineAdded") Paint lineAdded, @NamedArg("lineRemoved") Paint lineRemoved, @NamedArg("defaultColor") Paint defaultColor, @NamedArg("spacing") double spacing){
        super();
        this.dragColor = new SimpleObjectProperty<>(dragColor);
        this.selectColor = new SimpleObjectProperty<>(selectColor);
        this.defaultColor = new SimpleObjectProperty<>(defaultColor);
        this.lineAdded = new SimpleObjectProperty<>(lineAdded);
        this.lineRemoved = new SimpleObjectProperty<>(lineRemoved);
        layers = new VBox(spacing);
        layers.setBackground(new Background(new BackgroundFill(Color.GREEN, null, null)));
        layers.setManaged(true);
        if(editor) {
            anchor = new Pivot<>(this);
            getChildren().addAll(layers, anchor);
        } else {
            getChildren().add(layers);
        }
        layers.prefWidthProperty().bind(widthProperty());
        layers.prefHeightProperty().bind(heightProperty());
        layers.setLayoutX(0);
        layers.setLayoutY(0);
        editing = editor;
    }

    public ObservableList<Node> getLayers(){
        return layers.getChildren();
    }

    public boolean isEditing(){
        return editing;
    }

    public void addArrow(ScoutingEventDirection direction){
        getChildren().add(direction);
    }

    public void removeArrow(ScoutingEventDirection direction){
        getChildren().remove(direction);
    }

    public Paint getDragColor() {
        return dragColor.get();
    }

    public Paint getSelectColor() {
        return selectColor.get();
    }

    public Paint getLineAdded() {
        return lineAdded.get();
    }

    public Paint getLineRemoved() {
        return lineRemoved.get();
    }

    public Paint getDefaultColor() {
        return defaultColor.get();
    }

    public void setDragColor(Paint dragColor) {
        this.dragColor.set(dragColor);
    }

    public void setSelectColor(Paint selectColor) {
        this.selectColor.set(selectColor);
    }

    public void setLineAdded(Paint lineAdded) {
        this.lineAdded.set(lineAdded);
    }

    public void setLineRemoved(Paint lineRemoved) {
        this.lineRemoved.set(lineRemoved);
    }

    public void setDefaultColor(Paint defaultColor) {
        this.defaultColor.set(defaultColor);
    }

    public SimpleObjectProperty<Paint> dragColorProperty() {
        return dragColor;
    }

    public SimpleObjectProperty<Paint> selectColorProperty() {
        return selectColor;
    }

    public SimpleObjectProperty<Paint> lineAddedProperty() {
        return lineAdded;
    }

    public SimpleObjectProperty<Paint> lineRemovedProperty() {
        return lineRemoved;
    }

    public SimpleObjectProperty<Paint> defaultColorProperty() {
        return defaultColor;
    }
}
