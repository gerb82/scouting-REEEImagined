package connectionIndependent.eventsMapping;

import javafx.animation.PauseTransition;
import javafx.beans.DefaultProperty;
import javafx.beans.NamedArg;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.HashMap;

public class ScoutingEventUnit extends Pane implements ScoutingEventTreePart {

    private ScoutingEventLayer layer;
    private HashMap<ScoutingEventUnit, ScoutingEventDirection> exiting;
    private HashMap<ScoutingEventUnit, ScoutingEventDirection> arriving;
    private Pivot<Boolean> out;
    private Pivot<Boolean> in;
    private Pivot<ScoutingEventUnit> anchor;
    protected static ScoutingTreesManager manager = null;

    public ScoutingEventUnit(){
        super();
        parentProperty().addListener((event ->  layer = (ScoutingEventLayer) ScoutingEventTreePart.findEventParent(this)));
        setManaged(false);
        setBackground(new Background(new BackgroundFill(Color.RED, null, null)));
        exiting = new HashMap<>();
        arriving = new HashMap<>();

        in = new Pivot<>(false);
        out = new Pivot<>(true);
        in.setOnMouseClicked(unitLinker);
        out.setOnMouseClicked(unitLinker);

        if(manager.isEditing()) {
            anchor = new Pivot<>(this);
            enableDrag();
            getChildren().addAll(in, out, anchor);
        } else {
            getChildren().addAll(in, out);
        }

        in.setManaged(true);
        out.setManaged(true);
        anchor.setManaged(true);
        setWidth(200);
        setHeight(200);
        boundsInParentProperty().addListener((observableValue, oldBounds, bounds) -> refreshBounds(bounds));
    }

    public void setLayer(ScoutingEventLayer layer) {
        this.layer = layer;
    }

    public ScoutingEventLayer getLayer() {
        return layer;
    }

    private void refreshBounds(Bounds bounds){
        middleX.set(0 + (bounds.getWidth()/2));
        bottomY.set(bounds.getMaxY());

        in.setLayoutX(middleX.get() - (in.getWidth()/2));
        in.setLayoutY(0);

        if(manager.isEditing()) {
            anchor.setLayoutX(0);
            anchor.setLayoutY(0);
        }

        out.setLayoutX(middleX.get() - (out.getWidth()/2));
        out.setLayoutY(bottomY.get() - out.getHeight());

        Point2D parentEntrance = getLayer().getTree().sceneToLocal(localToScene(middleX.get(), 0));
        Point2D parentExit = getLayer().getTree().sceneToLocal(localToScene(middleX.get(), bottomY.get()));
        for(ScoutingEventDirection direction : arriving.values()){
            direction.setEndX(parentEntrance.getX());
            direction.setEndY(parentEntrance.getY()-1);
        }
        for(ScoutingEventDirection direction : exiting.values()){
            direction.setStartX(parentExit.getX());
            direction.setStartY(parentExit.getY()+1);
        }
    }

    public static EventHandler<MouseEvent> unitLinker = event -> {
        ScoutingEventTree tree = ((ScoutingEventUnit) ((Pivot<Boolean>) event.getTarget()).getParent()).getLayer().getTree();
        ScoutingEventUnit linkStarter = tree.getLinkStarter();
        boolean linkExit = tree.isLinkExit();
        if(linkStarter == null){
            tree.setLinkStarter((ScoutingEventUnit) ((Pivot<Boolean>) event.getTarget()).getParent());
            linkStarter = tree.getLinkStarter();
            tree.setLinkExit(((Pivot<Boolean>)event.getTarget()).getValue());
            linkExit = tree.isLinkExit();
            (linkExit ? linkStarter.out : linkStarter.in).setFill((manager.getSelectColor()));
        } else {
            if (!(linkExit ^ !((Pivot<Boolean>) event.getTarget()).getValue())) {
                ScoutingEventUnit source;
                ScoutingEventUnit destination;
                if (linkExit) {
                    source = linkStarter;
                    destination = (ScoutingEventUnit) ((Pivot<Boolean>) event.getTarget()).getParent();
                } else {
                    source = (ScoutingEventUnit) ((Pivot<Boolean>) event.getTarget()).getParent();
                    destination = linkStarter;
                }
                if(lineCheck(tree, source, destination, true)) {
                    PauseTransition pauseTransition = new PauseTransition(Duration.seconds(1));
                    pauseTransition.setOnFinished(e -> {
                        if (tree.getLinkStarter() == null) {
                            source.out.setFill(manager.getDefaultColor());
                            destination.in.setFill(manager.getDefaultColor());
                        } else {
                            if (!(source.equals(tree.getLinkStarter()) && tree.isLinkExit())) {
                                source.out.setFill(manager.getDefaultColor());
                            }
                            if (!(destination.equals(tree.getLinkStarter()) && !tree.isLinkExit())) {
                                destination.in.setFill(manager.getDefaultColor());
                            }
                        }
                    });
                    pauseTransition.play();
                }
            } else {
                (linkExit ? linkStarter.out : linkStarter.in).setFill(manager.getDefaultColor());
            }
            tree.setLinkStarter(null);
        }
    };

    private DoubleProperty middleX = new SimpleDoubleProperty();
    private DoubleProperty bottomY = new SimpleDoubleProperty();

    public static boolean lineCheck(ScoutingEventTree tree, ScoutingEventUnit source, ScoutingEventUnit destination, boolean color){
        if (source.exiting.containsKey(destination)) {
            source.exiting.remove(destination);
            tree.getChildren().remove(destination.arriving.remove(source));
            if(color) {
                source.out.setFill(manager.getLineRemoved());
                destination.in.setFill(manager.getLineRemoved());
            }
        } else {
            if (source.layer.layerNumber() < destination.layer.layerNumber()) {
                ScoutingEventDirection direction = new ScoutingEventDirection(source, destination);
                tree.getChildren().add(direction);
                source.exiting.put(destination, direction);
                destination.arriving.put(source, direction);
                Point2D parentEntrance = tree.sceneToLocal(destination.localToScene(destination.middleX.get(), 0));
                Point2D parentExit = tree.sceneToLocal(source.localToScene(source.middleX.get(), source.bottomY.get()));
                direction.setEndX(parentEntrance.getX());
                direction.setEndY(parentEntrance.getY()-1);
                direction.setStartX(parentExit.getX());
                direction.setStartY(parentExit.getY()+1);
                if(color) {
                    source.out.setFill(manager.getLineAdded());
                    destination.in.setFill(manager.getLineAdded());
                }
            } else {
                if(color) {
                    (tree.isLinkExit() ? source.out : destination.in).setFill(manager.getDefaultColor());
                }
                return false;
            }
        }
        return true;
    }


    private double orgSceneX;
    private void enableDrag() {
        anchor.setOnMousePressed(event -> {
            orgSceneX = event.getSceneX() - getLayoutX();
            anchor.setFill(manager.getDragColor());
        });
        anchor.setOnMouseDragged(event -> {
            double offsetX = event.getSceneX() - orgSceneX;
            setLayoutX(offsetX > 0 ? (offsetX < layer.getUnitWidth() - getWidth() ? offsetX : layer.getUnitWidth() - getWidth()) : 0);
        });
        anchor.setOnMouseReleased(event -> {
            anchor.setFill(manager.getDefaultColor());
        });
    }
}
