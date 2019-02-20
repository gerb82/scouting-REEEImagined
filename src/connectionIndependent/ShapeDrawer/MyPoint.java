package connectionIndependent.ShapeDrawer;

import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class MyPoint extends Circle{
    double orgSceneX, orgSceneY, orgLayoutX, orgLayoutY;
    public Color fill = Color.RED;
    boolean dragged;

    public MyPoint(double x, double y, double radius, int of, MyGroup myGroup){
        super(x, y, radius);
        setFill(fill);

        setOnMousePressed(event -> {

            orgSceneX = event.getX();
            orgSceneY = event.getY();
            orgLayoutX = ((Circle)(event.getSource())).getCenterX();
            orgLayoutY = ((Circle)(event.getSource())).getCenterY();
            dragged = event.getButton() == MouseButton.PRIMARY;


        });

            setOnMouseDragged(event -> {
                Point2D point = myGroup.sceneToLocal(event.getSceneX(), event.getSceneY());
                Point2D scene = myGroup.localToScene(point);
                double newX = scene.getX();
                if(newX < radius) newX = myGroup.sceneToLocal(radius, scene.getY()).getX();
                else if(newX > ((Region) myGroup.getParent()).getWidth() - radius) newX = myGroup.sceneToLocal(((Region) myGroup.getParent()).getWidth() - radius, scene.getY()).getX();
                else newX = point.getX();
                ((Circle) (event.getSource())).setCenterX(newX);
                myGroup.getPoly().getPoints().set(of, getCenterX());


                double newY = scene.getY();
                if(newY < radius) newY = myGroup.sceneToLocal(scene.getX(), radius).getY();
                else if(newY > ((Region) myGroup.getParent()).getHeight() - radius) newY = myGroup.sceneToLocal(scene.getX(), ((Region) myGroup.getParent()).getHeight() - radius).getY();
                else newY = point.getY();
                ((Circle) (event.getSource())).setCenterY(newY);
                myGroup.getPoly().getPoints().set(of + 1, getCenterY());
            });

        setOnKeyPressed(event -> {
            if (Editing.pointPressed == this) {
                if (event.getSource() == KeyCode.LEFT) {
                    double deltaX = 5;
                    double newLayoutX = getCenterX() - deltaX;
                    ((Circle)(event.getSource())).setCenterX(newLayoutX);
                    myGroup.getPoly().getPoints().set(of, getCenterX());
                } else if (event.getSource() == KeyCode.RIGHT) {
                    double deltaX = 5;
                    double newLayoutX = getCenterX()+deltaX;
                    ((Circle)(event.getSource())).setCenterX(newLayoutX);
                    myGroup.getPoly().getPoints().set(of, getCenterX());
                } else if (event.getSource() == KeyCode.DOWN) {
                    double deltaY = 5;
                    double newLayoutY = getCenterY() + deltaY;
                    ((Circle)(event.getSource())).setCenterX(newLayoutY);
                    myGroup.getPoly().getPoints().set(of+1, getCenterY());

                } else if (event.getSource() == KeyCode.UP) {
                    double deltaY = 5;
                    double newLayoutY = getCenterY() - deltaY;
                    ((Circle)(event.getSource())).setCenterX(newLayoutY);
                    myGroup.getPoly().getPoints().set(of+1, getCenterY());
                }
            }
        });

    }



    public MyPoint(double x, double y, double radius, MyCircle myCircle){
        super(x,y,radius);
        setFill(fill);

        setOnMousePressed(event -> {
            orgSceneX = event.getX();
            orgSceneY = event.getY();
            orgLayoutX = ((Circle)(event.getSource())).getCenterX();
            orgLayoutY = ((Circle)(event.getSource())).getCenterY();
        });

        setOnMouseDragged(event -> {
            if (getCenterY()-getRadius() != 0) {
                double offsetY = event.getSceneY() - orgSceneY;
                double newLayoutY = orgLayoutY + offsetY;
                ((Circle) event.getSource()).setCenterY(newLayoutY);
                myCircle.setRadius(Math.abs(myCircle.getCenterY() - newLayoutY));
            }
        });


        setOnKeyPressed(event -> {
            if (Editing.pointPressed == this) {
                double lastCircleY = myCircle.getCenterY();
                double lastY = getCenterY();
                if (event.getSource() == KeyCode.LEFT) {
                    double deltaX = 5;
                    double newLayoutX = getCenterX() - deltaX;
                    setCenterX(newLayoutX);
                    myCircle.setCenterX(getCenterX());
                } else if (event.getSource() == KeyCode.RIGHT) {
                    double deltaX = 5;
                    double newLayoutX = getCenterX()+deltaX;
                    setCenterX(newLayoutX);
                    myCircle.setCenterX(getCenterX());
                } else if (event.getSource() == KeyCode.DOWN) {
                    double deltaY = 5;
                    double newLayoutY = getCenterY() + deltaY;
                    setCenterY(newLayoutY);
                    if (lastCircleY < lastY) myCircle.setCenterY(getCenterY()-myCircle.getRadius());
                    else myCircle.setCenterY(getCenterY()+ myCircle.getRadius());
                } else if (event.getSource() == KeyCode.UP) {
                    double deltaY = 5;
                    double newLayoutY = getCenterY() - deltaY;
                    setCenterY(newLayoutY);
                    if (lastCircleY < lastY) myCircle.setCenterY(getCenterY()-myCircle.getRadius());
                    else myCircle.setCenterY(getCenterY()+ myCircle.getRadius());
                }
            }
        });
    }
}
