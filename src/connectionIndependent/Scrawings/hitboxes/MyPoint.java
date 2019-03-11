package connectionIndependent.Scrawings.hitboxes;

import connectionIndependent.Scrawings.ScrawingsManager;
import connectionIndependent.Scrawings.scrawtypes.ScrawRecipe;
import gbuiLib.MethodTypes.CreatorMethod;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

class MyPoint extends Circle {
    protected static final double radius = 4;
    private Color fill = Color.RED;
    private int offset;

    public int getOffset(){
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public MyPoint() {
        super(radius, radius, radius);
        setFill(fill);
    }

    public MyPoint(double x, double y, int of, MyPolyGroup myPolyGroup) {
        super(x, y, radius);
        this.offset = of;
        setFill(fill);
        SimpleObjectProperty<Point2D> dragOff = new SimpleObjectProperty<>();
        setOnMousePressed(event -> {
            Point2D center = myPolyGroup.localToScene(getCenterX(), getCenterY());
            dragOff.set(new Point2D(event.getSceneX(), event.getSceneY()).subtract(center));
        });
        setOnMouseDragged(event -> {
            Point2D editingGround = ScrawingsManager.getInstance().editingGround.sceneToLocal(event.getSceneX(), event.getSceneY()).subtract(dragOff.get());
            double newX = Math.max(radius, Math.min(editingGround.getX(), ScrawingsManager.getInstance().editingGround.getWidth() - radius));
            double newY = Math.max(radius, Math.min(editingGround.getY(), ScrawingsManager.getInstance().editingGround.getHeight() - radius));
            Point2D newCenter = myPolyGroup.sceneToLocal(ScrawingsManager.getInstance().editingGround.localToScene(newX, newY));
            setCenterX(newCenter.getX());
            setCenterY(newCenter.getY());
            myPolyGroup.getPoly().getPoints().set(offset, newCenter.getX());
            myPolyGroup.getPoly().getPoints().set(offset + 1, newCenter.getY());
        });

        setOnContextMenuRequested(event -> {
            myPolyGroup.getPoly().getPoints().remove(offset, offset + 2);
            if(myPolyGroup.getPoly().getPoints().size() == 2) {
                ((ScrawRecipe) myPolyGroup.getParent()).getChildren().remove(myPolyGroup);
            }
            for (MyPoint point : myPolyGroup.getMyPoints()) {
                    if (point.offset > offset) point.offset -= 2;
            }
            myPolyGroup.getChildren().remove(this);
            myPolyGroup.getMyPoints().remove(this);
            event.consume();
        });

        supposedCenter = () -> new Point2D(myPolyGroup.getPoly().getPoints().get(offset), myPolyGroup.getPoly().getPoints().get(offset + 1));
    }

    private CreatorMethod<Point2D> supposedCenter;

    public Point2D getSupposedCenter() {
        return supposedCenter.execute();
    }

    public MyPoint(double x, double y, MyCircGroup myCircGroup) {
        super(x, y, radius);
        setFill(fill);

        SimpleDoubleProperty orgSceneY = new SimpleDoubleProperty();
        SimpleDoubleProperty orgLayoutY = new SimpleDoubleProperty();

        setOnMousePressed(event -> {
            event.consume();
            orgSceneY.set(event.getSceneY());
            orgLayoutY.set(getCenterY());
        });

        setOnMouseDragged(event -> {
            event.consume();
            if (getCenterY() - getRadius() >= 0 ) {
                double offsetY = event.getSceneY() - orgSceneY.get();
                double newLayoutY = orgLayoutY.get() + offsetY;
                setCenterY(newLayoutY);
                myCircGroup.getCircle().setRadius(Math.abs(myCircGroup.getCircle().getCenterY() - newLayoutY));
            } else if (getCenterY() - getRadius() < 0) setCenterY(0);
        });
    }
}
