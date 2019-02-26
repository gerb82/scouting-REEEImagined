package connectionIndependent.ShapeDrawer;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

import java.util.ArrayList;

class MyPolyGroup extends Group implements PossibleHitBox {

    private double radius = 4;
    private ArrayList<MyPoint> myPoints = new ArrayList<>();
    private Polygon poly;
    MyPolyGroup(double[] doubles, boolean isEditing){
        super();
        poly = new Polygon(doubles);
        getChildren().add(poly);
        poly.setStrokeWidth(2.5);
        Color fill = Color.BLACK;
        poly.setFill(fill);
        if (isEditing) {
            for (int i = 0; i < doubles.length; i += 2) {
                MyPoint myPoint = new MyPoint(doubles[i], doubles[i + 1], radius, i, this);
                myPoints.add(myPoint);
                getChildren().add(myPoint);
            }

            poly.parentProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    ((Pane) newValue).getChildren().addAll(myPoints);
                }
            });

            final ObjectProperty<Point2D> mousePosition = new SimpleObjectProperty<>();

            poly.setOnMousePressed(event -> {
                mousePosition.set(new Point2D(event.getSceneX(), event.getSceneY()));
                Editor.currentlyPressed = this;
                poly.setStroke(Color.MEDIUMPURPLE);
            });

            poly.setOnMouseDragged(event -> {
                boolean goodToDrag = true;
                for (MyPoint myPoint : myPoints) {
                    if (myPoint.getCenterX() - radius < 0) {
                        goodToDrag = false;
                        myPoint.setCenterX(radius);
                    } else if (myPoint.getCenterX() + radius > Main.getPane().getWidth()) {
                        goodToDrag = false;
                        myPoint.setCenterX(Main.getPane().getWidth() - radius);
                    } else if (myPoint.getCenterY() - radius < 0) {
                        goodToDrag = false;
                        myPoint.setCenterY(radius);
                    } else if (myPoint.getCenterY() + radius > Main.getPane().getHeight()) {
                        goodToDrag = false;
                        myPoint.setCenterY(Main.getPane().getHeight() - radius);
                    }
                }
                if (goodToDrag) {
                    setLayoutX(getLayoutX() - (mousePosition.get().getX() - event.getSceneX()));
                    setLayoutY(getLayoutY() - (mousePosition.get().getY() - event.getSceneY()));
                    mousePosition.set(new Point2D(event.getSceneX(), event.getSceneY()));
                }
            });

            poly.setOnMouseReleased(event -> poly.setStroke(null));

            poly.setOnKeyPressed(event -> {
                if (Editor.currentlyPressed == this) {
                    if (event.getSource() == KeyCode.LEFT) {
                        double deltaX = 5;
                        ((MyPolyGroup) Editor.currentlyPressed).setLayoutX(((MyPolyGroup) Editor.currentlyPressed).getLayoutX()-deltaX);
                    } else if (event.getSource() == KeyCode.RIGHT) {
                        double deltaX = 5;
                        ((MyPolyGroup) Editor.currentlyPressed).setLayoutX(((MyPolyGroup) Editor.currentlyPressed).getLayoutX()+deltaX);
                    } else if (event.getSource() == KeyCode.DOWN) {
                        double deltaY = 5;
                        ((MyPolyGroup) Editor.currentlyPressed).setLayoutX(((MyPolyGroup) Editor.currentlyPressed).getLayoutX()-deltaY);

                    } else if (event.getSource() == KeyCode.UP) {
                        double deltaY = 5;

                        for (int i = 0; i < poly.getPoints().size(); i += 2) {
                            poly.getPoints().set(i, poly.getPoints().get(i) - deltaY);
                        }
                        for (int i = 0; i < poly.getPoints().size(); i += 2) {
                            myPoints.get(i / 2).setCenterX(poly.getPoints().get(i));
                        }
                    }
                }
            });
        }
        else {
            Main.getPane().getChildren().add(poly);
            poly.setOnMouseClicked(event -> {
                //actionHere(bytes);
            });
        }
    }

    public double getRadius(){
        return radius;
    }

    public Polygon getPoly() {
        return poly;
    }

    public ArrayList<MyPoint> getMyPoints() {
        return myPoints;
    }
}
