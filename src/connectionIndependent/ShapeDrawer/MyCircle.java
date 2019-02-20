package connectionIndependent.ShapeDrawer;

import javafx.scene.control.ColorPicker;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class MyCircle extends Circle {
    private double radius = 4;
    double orgSceneX, orgSceneY, orgLayoutX, orgLayoutY;
    ColorPicker colorPicker = new ColorPicker(Color.BLACK);

    public MyCircle(double x, double y, double radius, boolean isEditing){
        super(x,y,radius);
        setFill(colorPicker.getValue());
        boolean editing = isEditing;
        if (editing){
            MyPoint myPoint = new MyPoint(x,y-this.radius, this.radius,this);

            parentProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    ((Pane) newValue).getChildren().add(myPoint);
                }
            });

            setOnMousePressed(event -> {
                orgSceneX = event.getX();
                orgSceneY = event.getY();
                orgLayoutX = ((Circle)(event.getSource())).getCenterX();
                orgLayoutY = ((Circle)(event.getSource())).getCenterY();
                setStroke(Color.MEDIUMPURPLE);
                setStrokeWidth(2.5);
                Editing.circPressed = this;
                Editing.polyPressed = null;
                Editing.pointPressed = null;
            });


            setOnMouseDragged(event -> {
                double offsetX = event.getSceneX() - orgSceneX;
                double offsetY = event.getSceneY() - orgSceneY;
                double newLayoutX = orgLayoutX + offsetX;
                double newLayoutY = orgLayoutY + offsetY;
                    ((Circle)(event.getSource())).setCenterX(newLayoutX);
                    ((Circle)(event.getSource())).setCenterY(newLayoutY);

                    setCenterX(getCenterX());
                    setCenterY(Math.abs(getCenterY()-getRadius()));

                System.out.println(getCenterX()+ " "+ getCenterY());

            });

            setOnMouseReleased(event -> {
                setStrokeWidth(0);
            });

            setOnKeyPressed(event -> {
                if (Editing.circPressed == this) {
                    if (event.getSource() == KeyCode.LEFT) {
                        double deltaX = 5;
                        double newLayoutX = getCenterX() - deltaX;
                        ((Circle)(event.getSource())).setCenterX(newLayoutX);
                        myPoint.setCenterX(getCenterX());
                    } else if (event.getSource() == KeyCode.RIGHT) {
                        double deltaX = 5;
                        double newLayoutX = getCenterX()+deltaX;
                        ((Circle)(event.getSource())).setCenterX(newLayoutX);
                        myPoint.setCenterX(getCenterX());
                    } else if (event.getSource() == KeyCode.DOWN) {
                        double deltaY = 5;
                        double newLayoutY = getCenterY() + deltaY;
                        ((Circle)(event.getSource())).setCenterX(newLayoutY);
                        myPoint.setCenterX(getCenterX());

                    } else if (event.getSource() == KeyCode.UP) {
                        double deltaY = 5;
                        double newLayoutY = getCenterY() - deltaY;
                        ((Circle)(event.getSource())).setCenterX(newLayoutY);
                        myPoint.setCenterX(getCenterX());
                    }
                }
            });

        }
    }
}
