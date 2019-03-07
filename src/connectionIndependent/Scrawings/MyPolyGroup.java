package connectionIndependent.Scrawings;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

import java.util.ArrayList;

class MyPolyGroup extends Group implements PossibleHitBox {

    private ArrayList<MyPoint> myPoints = new ArrayList<>();
    private Polygon poly;

    MyPolyGroup(double[] doubles) {
        super();
        poly = new Polygon(doubles);
        getChildren().add(poly);
        poly.setStrokeWidth(2.5);
        Color fill = Color.BLACK;
        poly.setFill(fill);
        if (ScrawingsManager.getInstance().isEditing()) {
            for (int i = 0; i < doubles.length; i += 2) {
                MyPoint myPoint = new MyPoint(doubles[i], doubles[i + 1], i, this);
                myPoints.add(myPoint);
                getChildren().add(myPoint);
            }

            SimpleDoubleProperty mouseX = new SimpleDoubleProperty();
            SimpleDoubleProperty mouseY = new SimpleDoubleProperty();

            poly.setOnMousePressed(event -> {
                mouseX.set(event.getSceneX());
                mouseY.set(event.getSceneY());
                ScrawingsManager.getInstance().lastPressed = this;
                poly.setStroke(Color.MEDIUMPURPLE);
            });

            poly.setOnMouseDragged(event -> {
                double xMove = mouseX.get() - event.getSceneX();
                double yMove = mouseY.get() - event.getSceneY();
                for (MyPoint myPoint : myPoints) {
                    Point2D original = ScrawingsManager.getInstance().editingGround.sceneToLocal(localToScene(myPoint.getSupposedCenter()));
                    Point2D destination = original.subtract((mouseX.get() - event.getSceneX()), (mouseY.get() - event.getSceneY()));
                    if (destination.getX() < MyPoint.radius) {
                        xMove = Math.min(xMove, MyPoint.radius - original.getX());
                    } else if (destination.getX() > ScrawingsManager.getInstance().editingGround.getWidth() - MyPoint.radius) {
                        xMove = Math.max(xMove, ScrawingsManager.getInstance().editingGround.getWidth() - MyPoint.radius - original.getX());
                    }
                    if (destination.getY() < MyPoint.radius) {
                        yMove = Math.min(yMove, MyPoint.radius - original.getY());
                    } else if (destination.getY() > ScrawingsManager.getInstance().editingGround.getHeight() - MyPoint.radius) {
                        yMove = Math.max(yMove, ScrawingsManager.getInstance().editingGround.getHeight() - MyPoint.radius - original.getY());
                    }
                }
                setLayoutX(getLayoutX() - xMove);
                setLayoutY(getLayoutY() - yMove);
                if(xMove == mouseX.get() - event.getSceneX()) mouseX.set(event.getSceneX());
                if(yMove == mouseY.get() - event.getSceneY()) mouseY.set(event.getSceneY());
            });

            poly.setOnMouseReleased(event -> poly.setStroke(null));

        } else {
            poly.setOnMouseClicked(event -> {
                //actionHere(bytes);
            });
        }
    }

    public Polygon getPoly() {
        return poly;
    }

    public ArrayList<MyPoint> getMyPoints() {
        return myPoints;
    }
}
