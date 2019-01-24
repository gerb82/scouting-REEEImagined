package connectionIndependent.eventsMapping;

import javafx.animation.PauseTransition;
import javafx.beans.NamedArg;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.HashMap;

public class ScoutingEventUnit extends Pane {

    private ScoutingEventLayer layer;
    private HashMap<ScoutingEventUnit, ScoutingEventDirection> exiting;
    private HashMap<ScoutingEventUnit, ScoutingEventDirection> arriving;
    private Pivot<Boolean> out;
    private Pivot<Boolean> in;
    private Pivot<ScoutingEventUnit> anchor;

    public ScoutingEventUnit(@NamedArg("layer") ScoutingEventLayer layer){
        super();
        setManaged(false);
        layoutBoundsProperty().addListener((observableValue, oldBounds, bounds) -> refreshBounds(bounds));
        exiting = new HashMap<>();
        arriving = new HashMap<>();

        this.layer = layer;
        in = new Pivot<>(false);
        out = new Pivot<>(true);
        in.setOnMouseClicked(unitLinker);
        out.setOnMouseClicked(unitLinker);
        anchor = new Pivot<>(this);
        in.setFill(layer.getDefaultColor());
        out.setFill(layer.getDefaultColor());
        anchor.setFill(layer.getDefaultColor());

        getChildren().addAll(in, out, anchor);
        in.setManaged(true);
        out.setManaged(true);
        anchor.setManaged(true);

        anchor.layoutXProperty().bind(layoutXProperty());
        anchor.layoutYProperty().bind(layoutYProperty());

        in.layoutYProperty().bind(layoutYProperty());
    }

    private void refreshBounds(Bounds bounds){
        middleX.set(bounds.getMinX() + (bounds.getWidth()/2));
        bottomY.set(bounds.getMinX());

        in.setLayoutX(middleX.get() - (in.getWidth()/2));

        out.setLayoutX(middleX.get() - (out.getWidth()/2));
        out.setLayoutY(bottomY.get() - out.getHeight());
    }

    private static ScoutingEventUnit linkStarter = null;
    private static boolean linkExit;

    public static EventHandler<MouseEvent> unitLinker = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            if(linkStarter == null){
                linkStarter = (ScoutingEventUnit) ((Pivot<Boolean>) event.getTarget()).getParent();
                linkExit = ((Pivot<Boolean>)event.getTarget()).getValue();
                (linkExit ? linkStarter.out : linkStarter.in).setFill(linkStarter.layer.getSelectColor());
            } else {
                if (linkExit != ((Pivot<Boolean>) event.getTarget()).getValue()) {
                    ScoutingEventUnit source;
                    ScoutingEventUnit destination;
                    if (linkExit) {
                        source = linkStarter;
                        destination = (ScoutingEventUnit) ((Pivot<Boolean>) event.getTarget()).getParent();
                    } else {
                        source = (ScoutingEventUnit) ((Pivot<Boolean>) event.getTarget()).getParent();
                        destination = linkStarter;
                    }
                    lineCheck(source, destination, true);
                    PauseTransition pauseTransition = new PauseTransition(Duration.seconds(1));
                    pauseTransition.setOnFinished(e -> {
                        source.out.setFill(source.layer.getDefaultColor());
                        destination.in.setFill(destination.layer.getDefaultColor());
                    });
                    pauseTransition.play();
                } else {
                    (linkExit ? linkStarter.out : linkStarter.in).setFill(linkStarter.layer.getDefaultColor());
                }
                linkStarter = null;
            }
        }
    };

    private DoubleProperty middleX = new SimpleDoubleProperty();
    private DoubleProperty bottomY = new SimpleDoubleProperty();

    public void bindToTop(DoubleProperty x, DoubleProperty y){
        x.bind(middleX);
        y.bind(layoutYProperty());
    }

    public void bindToBottom(DoubleProperty x, DoubleProperty y){
        x.bind(middleX);
        y.bind(bottomY);
    }

    public static void lineCheck(ScoutingEventUnit source, ScoutingEventUnit destination, boolean color){
        if (source.exiting.containsKey(destination)) {
            source.exiting.remove(destination);
            destination.arriving.remove(source).discard();
            if(color) {
                source.out.setFill(source.layer.getLineRemoved());
                destination.out.setFill(destination.layer.getLineRemoved());
            }
        } else {
            if (source.layer.layerNumber() > destination.layer.layerNumber()) {
                ScoutingEventDirection direction = new ScoutingEventDirection(source, destination, source.layer.getTree());
                source.exiting.put(destination, direction);
                destination.arriving.put(source, direction);
                if(color) {
                    source.out.setFill(source.layer.getLineAdded());
                    destination.out.setFill(destination.layer.getLineAdded());
                }
            } else {
                if(color) {
                    (linkExit ? linkStarter.out : linkStarter.in).setFill(linkStarter.layer.getDefaultColor());
                }
            }
        }
    }
}
