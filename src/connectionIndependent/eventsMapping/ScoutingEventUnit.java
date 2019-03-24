package connectionIndependent.eventsMapping;

import javafx.animation.PauseTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.HashMap;

public class ScoutingEventUnit extends Pane implements ScoutingEventTreePart {

    private ScoutingEventLayer layer;
    protected HashMap<ScoutingEventUnit, ScoutingEventDirection> exiting;
    protected HashMap<ScoutingEventUnit, ScoutingEventDirection> arriving;
    private Pivot<Boolean> out;
    private Pivot<Boolean> in;
    private Pivot<ScoutingEventUnit> anchor;
    private Pivot<ScoutingEventUnit> remover;
    private Text name;
    private TextField editName;
    private CheckBox stamp;
    private Label idLabel;
    protected static ScoutingTreesManager manager = null;

    public ScoutingEventUnit() {
        super();
        parentProperty().addListener((event -> {
            layer = (ScoutingEventLayer) ScoutingEventTreePart.findEventParent(this);
        }));
        setManaged(false);
        setBackground(new Background(new BackgroundFill(Color.RED, null, null)));
        exiting = new HashMap<>();
        arriving = new HashMap<>();
        idLabel = new Label();

        in = new Pivot<>(false);
        out = new Pivot<>(true);
        stamp = new CheckBox("Timestamps?");

        if (manager.isEditing()) {
            in.setOnMouseClicked(unitLinker);
            out.setOnMouseClicked(unitLinker);
            remover = new Pivot<>(this);
            anchor = new Pivot<>(this);
            editName = new TextField();
            enableDrag();
            remover.setOnMouseClicked(this::remove);
            getChildren().addAll(in, out, remover, editName, stamp, anchor, idLabel);
            anchor.setManaged(true);
            editName.setLayoutX((getWidth() - editName.getWidth()) / 2);
            editName.setLayoutY(60);
            stamp.setLayoutX((getWidth() - stamp.getWidth()) / 2);
            stamp.setLayoutY(90);
            stamp.setDisable(false);
            remover.setManaged(true);
        } else {
            name = new Text();
            name.setLayoutX((getWidth() - name.getWrappingWidth()) / 2);
            name.setLayoutY(60);
            getChildren().addAll(in, out, stamp, name, idLabel);
            stamp.setDisable(true);
        }

        in.setManaged(true);
        out.setManaged(true);
        boundsInParentProperty().addListener((observableValue, oldBounds, bounds) -> refreshBounds(bounds));
        setSize(200, 200);
    }

    public void setSize(double width, double height){
        setWidth(width);
        setHeight(height);
    }

    public void removeFromLayer() {
        for (ScoutingEventDirection arrow : arriving.values()) {
            layer.getTree().getChildren().remove(arrow);
            arriving.remove(arrow);
            arrow.getStart().exiting.remove(arrow);
        }
        for (ScoutingEventDirection arrow : exiting.values()) {
            layer.getTree().getChildren().remove(arrow);
            exiting.remove(arrow);
            arrow.getEnd().arriving.remove(arrow);
        }
    }

    public void remove(Event event) {
        for (ScoutingEventDirection arrow : arriving.values()) {
            layer.getTree().getChildren().remove(arrow);
            arriving.remove(arrow);
            arrow.getStart().exiting.remove(arrow);
        }
        for (ScoutingEventDirection arrow : exiting.values()) {
            layer.getTree().getChildren().remove(arrow);
            exiting.remove(arrow);
            arrow.getEnd().arriving.remove(arrow);
        }
        layer.getUnits().remove(this);
    }

    public ScoutingEventLayer getLayer() {
        return layer;
    }

    public String getName() {
        if (manager.isEditing()) {
            return editName.getText();
        } else {
            return name.getText();
        }
    }

    public void setName(String name) {

        if (manager.isEditing()) {
            editName.setText(name);
        } else {
            this.name.setText(name);
        }
    }

    public boolean getStamp() {
        return stamp.isSelected();
    }

    public void setStamp(boolean selected) {
        stamp.setSelected(selected);
    }

    private byte id;

    public void setUnitID(byte id) {
        this.id = id;
        idLabel.setText(String.valueOf(id));
        idLabel.setLayoutX(100-(idLabel.getWidth()/2));
        idLabel.setLayoutY(30);
    }

    public byte getUnitID() {
        return id;
    }

    private void refreshBounds(Bounds bounds) {
        middleX.set(0 + (bounds.getWidth() / 2));
        bottomY.set(getHeight());

        in.setLayoutX(middleX.get() - (in.getWidth() / 2));
        in.setLayoutY(0);

        stamp.setLayoutX((getWidth() - stamp.getWidth()) / 2);
        stamp.setLayoutY(90);

        if (manager.isEditing()) {
            anchor.setLayoutX(0);
            anchor.setLayoutY(0);
            remover.setLayoutX(getWidth() - remover.getWidth());
            remover.setLayoutY(0);
            editName.setLayoutX((getWidth() - editName.getWidth()) / 2);
            editName.setLayoutY(60);
        } else {
            name.setLayoutX((getWidth() - name.getWrappingWidth()) / 2);
            name.setLayoutY(60);
        }

        out.setLayoutX(middleX.get() - (out.getWidth() / 2));
        out.setLayoutY(bottomY.get() - out.getHeight());

        try {
            Point2D parentEntrance = getLayer().getTree().sceneToLocal(localToScene(middleX.get(), 0));
            Point2D parentExit = getLayer().getTree().sceneToLocal(localToScene(middleX.get(), bottomY.get()));
            for (ScoutingEventDirection direction : arriving.values()) {
                direction.setEndX(parentEntrance.getX());
                direction.setEndY(parentEntrance.getY() - 1);
            }
            for (ScoutingEventDirection direction : exiting.values()) {
                direction.setStartX(parentExit.getX());
                direction.setStartY(parentExit.getY() + 1);
            }
        } catch (NullPointerException e) {
        }
    }

    public static EventHandler<MouseEvent> unitLinker = event -> {
        ScoutingEventTree tree = ((ScoutingEventUnit) ((Pivot<Boolean>) event.getTarget()).getParent()).getLayer().getTree();
        ScoutingEventUnit linkStarter = tree.getLinkStarter();
        boolean linkExit = tree.isLinkExit();
        if (linkStarter == null) {
            tree.setLinkStarter((ScoutingEventUnit) ((Pivot<Boolean>) event.getTarget()).getParent());
            linkStarter = tree.getLinkStarter();
            tree.setLinkExit(((Pivot<Boolean>) event.getTarget()).getValue());
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
                if (lineCheck(tree, source, destination, true)) {
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

    public static boolean lineCheck(ScoutingEventTree tree, ScoutingEventUnit source, ScoutingEventUnit destination, boolean color) {
        if (source.exiting.containsKey(destination)) {
            source.exiting.remove(destination);
            tree.getChildren().remove(destination.arriving.remove(source));
            if (color) {
                source.out.setFill(manager.getLineRemoved());
                destination.in.setFill(manager.getLineRemoved());
            }
        } else {
            if (source.layer.layerNumber() < destination.layer.layerNumber()) {
                ScoutingEventDirection direction = new ScoutingEventDirection(source, destination);
                tree.getChildren().add(direction);
                Point2D parentEntrance = tree.sceneToLocal(destination.localToScene(destination.middleX.get(), 0));
                Point2D parentExit = tree.sceneToLocal(source.localToScene(source.middleX.get(), source.bottomY.get()));
                direction.setEndX(parentEntrance.getX());
                direction.setEndY(parentEntrance.getY() - 1);
                direction.setStartX(parentExit.getX());
                direction.setStartY(parentExit.getY() + 1);
                if (color) {
                    source.out.setFill(manager.getLineAdded());
                    destination.in.setFill(manager.getLineAdded());
                }
            } else {
                if (color) {
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

    public static String unitID(byte id) {
        return "unit" + (id < 0 ? "n" + Math.abs(id) : id);
    }

    private double layoutXBuffer = 0;
    public double getLayoutXBuffer() {
        return layoutXBuffer;
    }

    public void setLayoutXBuffer(double layoutXBuffer) {
        this.layoutXBuffer = layoutXBuffer;
    }

    public void init(){
        setLayoutX(layoutXBuffer);
    }

    public String toFXML() {
        return String.format("<ScoutingEventUnit fx:id=\"%s\" unitID=\"%d\" name=\"%s\" stamp=\"%b\" layoutXBuffer=\"%f\"/>", unitID(id), id, getName(), getStamp(), getLayoutX());
    }
}
