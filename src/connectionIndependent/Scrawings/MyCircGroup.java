package connectionIndependent.Scrawings;

import javafx.scene.Group;
import javafx.scene.control.ColorPicker;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

class MyCircGroup extends Group implements PossibleHitBox{
    ColorPicker colorPicker = new ColorPicker(Color.BLACK);
    private double orgSceneX, orgSceneY, orgLayoutX, orgLayoutY;
    private double radius = 4;
    private Circle circle;
    MyCircGroup(double x, double y, double radius, boolean isEditing){
        super();
        circle = new Circle(x,y,radius);
        getChildren().add(circle);
        if (isEditing){
            MyPoint myPoint = new MyPoint(x,y-radius, this.radius,this);
            getChildren().add(myPoint);
            parentProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    ((Pane) newValue).getChildren().add(myPoint);
                }
            });

            setOnMousePressed(event -> {
                orgSceneX = event.getX();
                orgSceneY = event.getY();
                orgLayoutX = ((MyCircGroup) event.getSource()).circle.getCenterX();
                orgLayoutY = ((MyCircGroup) event.getSource()).circle.getCenterY();
                circle.setStroke(Color.MEDIUMPURPLE);
                circle.setStrokeWidth(2.5);
                ScrawingsManager.currentlyPressed = this;
            });


            setOnMouseDragged(event -> {
                double offsetX = event.getSceneX() - orgSceneX;
                double offsetY = event.getSceneY() - orgSceneY;
                double newLayoutX = orgLayoutX + offsetX;
                double newLayoutY = orgLayoutY + offsetY;
                ((MyCircGroup)(event.getSource())).getCircle().setCenterX(newLayoutX);
                ((MyCircGroup)(event.getSource())).getCircle().setCenterY(newLayoutY);

                myPoint.setCenterX(circle.getCenterX());
                myPoint.setCenterY(Math.abs(circle.getCenterY()-circle.getRadius()));

                System.out.println(circle.getCenterX()+ " "+ circle.getCenterY());

            });

            setOnMouseReleased(event -> circle.setStrokeWidth(0));

            setOnKeyPressed(event -> {
                if (ScrawingsManager.currentlyPressed == this) {
                    if (event.getSource() == KeyCode.LEFT) {
                        double deltaX = 5;
                        double newLayoutX = circle.getCenterX() - deltaX;
                        ((Circle)(event.getSource())).setCenterX(newLayoutX);
                        myPoint.setCenterX(circle.getCenterX());
                    } else if (event.getSource() == KeyCode.RIGHT) {
                        double deltaX = 5;
                        double newLayoutX = circle.getCenterX()+deltaX;
                        ((Circle)(event.getSource())).setCenterX(newLayoutX);
                        myPoint.setCenterX(circle.getCenterX());
                    } else if (event.getSource() == KeyCode.DOWN) {
                        double deltaY = 5;
                        double newLayoutY = circle.getCenterY() + deltaY;
                        ((Circle)(event.getSource())).setCenterX(newLayoutY);
                        myPoint.setCenterX(circle.getCenterX());

                    } else if (event.getSource() == KeyCode.UP) {
                        double deltaY = 5;
                        double newLayoutY = circle.getCenterY() - deltaY;
                        ((Circle)(event.getSource())).setCenterX(newLayoutY);
                        myPoint.setCenterX(circle.getCenterX());
                    }
                }
            });
        }
    }
    Circle getCircle(){
        return circle;
    }
}
