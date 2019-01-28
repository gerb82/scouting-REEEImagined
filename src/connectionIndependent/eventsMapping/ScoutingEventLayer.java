package connectionIndependent.eventsMapping;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class ScoutingEventLayer extends Pane{

    private ScoutingEventTree tree;
    private HBox units;
    private static int count = 0;
    private int myCount;

    public void setTree(ScoutingEventTree tree){
        System.out.println(myCount + " " + tree);
        this.tree = tree;
    }

    public ScoutingEventLayer(){
        super();
        myCount = count++;
        setManaged(true);
        units = new HBox();
        units.setAlignment(Pos.CENTER);
        units.setManaged(true);
        getChildren().add(units);
        units.prefHeightProperty().bind(heightProperty());
        units.prefWidthProperty().bind(widthProperty());
        this.units.setSpacing(100);
        setWidth(600);
        setHeight(200);
    }

    public ObservableList<Node> getUnits() {
        return units.getChildren();
    }

    public boolean isEditing(){
        return tree.isEditing();
    }

    public int layerNumber(){
        return tree.getChildren().indexOf(this);
    }

    public ScoutingEventTree getTree() {
        return tree;
    }

    public Paint getDragColor() {
        return tree.getDragColor();
    }

    public Paint getSelectColor() {
        System.out.println(tree + " shit?" + myCount);
        return tree.getSelectColor();
    }

    public Paint getLineAdded() {
        return tree.getLineAdded();
    }

    public Paint getLineRemoved() {
        return tree.getLineRemoved();
    }

    public Paint getDefaultColor() {
        return tree.getDefaultColor();
    }

    public ScoutingEventUnit getLinkStarter() {
        return tree.getLinkStarter();
    }

    public void setLinkStarter(ScoutingEventUnit linkStarter) {
        tree.setLinkStarter(linkStarter);
    }

    public boolean isLinkExit() {
        return tree.isLinkExit();
    }

    public void setLinkExit(boolean linkExit) {
        tree.setLinkExit(linkExit);
    }
}
