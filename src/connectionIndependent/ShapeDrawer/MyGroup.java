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

public class MyGroup extends Group {

    private Color fill;
    private double radius = 4;
    private ArrayList<MyPoint> myPoints = new ArrayList<>();
    private Polygon poly;
    public MyGroup(double[] doubles, boolean isEditing){
        super();
        poly = new Polygon(doubles);
        getChildren().add(poly);
        poly.setStrokeWidth(2.5);
        fill = Color.BLACK;
        poly.setFill(fill);
        boolean editing = isEditing;
        if (editing) {
            for (int i = 0; i < doubles.length; i += 2) {
                MyPoint myPoint = new MyPoint(doubles[i], doubles[i + 1], radius, i, this);
                myPoints.add(myPoint);
                System.out.println(myPoints.indexOf(myPoint) + ": " + doubles[i] + " " + doubles[i + 1]);
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
                Editing.polyPressed = this;
                Editing.circPressed = null;
                Editing.pointPressed = null;
                poly.setStroke(Color.MEDIUMPURPLE);
            });

            poly.setOnMouseDragged(event -> {
                boolean goodToDrag = true;
                for (int i = 0; i < myPoints.size(); i++) {
                    if (myPoints.get(i).getCenterX()-radius<0){
                        goodToDrag = false;
                        myPoints.get(i).setCenterX(radius);
                    }
                    else if (myPoints.get(i).getCenterX()+radius> Main.getPane().getWidth()){
                        goodToDrag = false;
                        myPoints.get(i).setCenterX(Main.getPane().getWidth()-radius);
                    }
                    else if (myPoints.get(i).getCenterY()-radius<0){
                        goodToDrag = false;
                        myPoints.get(i).setCenterY(radius);
                    }
                    else if (myPoints.get(i).getCenterY()+radius> Main.getPane().getHeight()){
                        goodToDrag = false;
                        myPoints.get(i).setCenterY(Main.getPane().getHeight()-radius);
                    }
                }
                if (goodToDrag) {
                    setLayoutX(getLayoutX() - (mousePosition.get().getX() - event.getSceneX()));
                    setLayoutY(getLayoutY() - (mousePosition.get().getY() - event.getSceneY()));
                    mousePosition.set(new Point2D(event.getSceneX(), event.getSceneY()));
                    System.out.println(poly.getPoints());
                }
            });

            poly.setOnMouseReleased(event -> {
                poly.setStroke(null);
            });

            poly.setOnKeyPressed(event -> {
                if (Editing.polyPressed == this) {
                    if (event.getSource() == KeyCode.LEFT) {
                        double deltaX = 5;
                        for (int i = 0; i < poly.getPoints().size(); i += 2) {
                            poly.getPoints().set(i, poly.getPoints().get(i) - deltaX);
                        }
                        for (int i = 0; i < poly.getPoints().size(); i += 2) {
                            myPoints.get(i / 2).setCenterX(poly.getPoints().get(i));
                        }
                    } else if (event.getSource() == KeyCode.RIGHT) {
                        double deltaX = 5;
                        for (int i = 0; i < poly.getPoints().size(); i += 2) {
                            poly.getPoints().set(i, poly.getPoints().get(i) + deltaX);
                        }
                        for (int i = 0; i < poly.getPoints().size(); i += 2) {
                            myPoints.get(i / 2).setCenterX(poly.getPoints().get(i));
                        }
                    } else if (event.getSource() == KeyCode.DOWN) {
                        double deltaY = 5;
                        for (int i = 1; i < poly.getPoints().size(); i += 2) {
                            poly.getPoints().set(i, poly.getPoints().get(i) + deltaY);
                        }
                        for (int i = 1; i < poly.getPoints().size(); i += 2) {
                            myPoints.get(i / 2).setCenterY(poly.getPoints().get(i));
                        }

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
                System.out.println("לבחירתך");
            });
        }
    }

    public double getRadius(){
        return radius;
    }

    public Polygon getPoly() {
        return poly;
    }
}
