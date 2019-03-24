package connectionIndependent.eventsMapping;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.List;

public class ScoutingEventTree extends Pane implements ScoutingEventTreePart{

    private VBox layers;
    private Pivot<ScoutingEventTree> anchor = null;
    private Button addLayer;
    private ScoutingEventUnit linkStarter = null;
    private boolean linkExit = false;
    private byte treeNumber = -1;

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

    public ScoutingEventTree(){
        super();
        setManaged(true);
        layers = new VBox(30);
        layers.setBackground(new Background(new BackgroundFill(Color.GREEN, null, null)));
        layers.setManaged(true);
        if(ScoutingTreesManager.getInstance().isEditing()) {
            addLayer = new Button("add new layer");
            getChildren().add(layers);
            VBox.setVgrow(addLayer, Priority.ALWAYS);
            addLayer.prefWidthProperty().bind(layers.widthProperty());
            addLayer.setOnMouseClicked(this::addLayer);
            addLayer.setPrefHeight(200);
        } else {
            anchor = new Pivot<>(this);
            getChildren().addAll(layers, anchor);
        }
        layers.prefWidthProperty().bind(widthProperty());
        setMaxWidth(Double.MAX_VALUE);
        layers.setLayoutX(0);
        layers.setLayoutY(0);
        ScoutingTreesManager.getInstance().addTree(this);
    }

    public void initButton(){
        layers.getChildren().add(addLayer);
    }

    public void addLayer(Event event){
        ScoutingEventLayer layer = new ScoutingEventLayer();
        layer.setTreeNumber(treeNumber);
        layers.getChildren().add(layers.getChildren().indexOf(addLayer), layer);
    }

    public ObservableList<Node> getLayers(){
        return layers.getChildren();
    }

    public List<Node> getArrows() {
        return getChildren().filtered(node -> node instanceof ScoutingEventDirection);
    }

    public byte getTreeNumber() {
        return treeNumber;
    }

    public void setTreeNumber(byte treeNumber) {
        this.treeNumber = treeNumber;
    }

    public String toFXML(String parentTag, String arrows, String layers) {
        return String.format("<ScoutingEventTree %s treeNumber=\"%d\">%n<layers>%n%s</layers>%n<children>%n%s</children>%n</ScoutingEventTree>", parentTag, treeNumber, layers, arrows);
    }
}
