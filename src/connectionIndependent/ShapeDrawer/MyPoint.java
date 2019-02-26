package connectionIndependent.ShapeDrawer;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

class MyPoint extends Circle implements PossibleHitBox{
    private double orgSceneY;
    private double orgLayoutY;
    private Color fill = Color.RED;
    private int offset;

    MyPoint(double x, double y, double radius, int of, MyPolyGroup myPolyGroup){
        super(x, y, radius);
        this.offset = of;
        setFill(fill);
        setOnMousePressed(event -> {
            orgSceneY = event.getY();
            orgLayoutY = ((Circle)(event.getSource())).getCenterY();
        });
            setOnMouseDragged(event -> {
                Point2D point = myPolyGroup.sceneToLocal(event.getSceneX(), event.getSceneY());
                Point2D scene = myPolyGroup.localToScene(point);
                double newX = scene.getX();
                if(newX < radius) newX = myPolyGroup.sceneToLocal(radius, scene.getY()).getX();
                else if(newX > ((Region) myPolyGroup.getParent()).getWidth() - radius) newX = myPolyGroup.sceneToLocal(((Region) myPolyGroup.getParent()).getWidth() - radius, scene.getY()).getX();
                else newX = point.getX();
                ((Circle) (event.getSource())).setCenterX(newX);
                myPolyGroup.getPoly().getPoints().set(offset, getCenterX());


                double newY = scene.getY();
                if(newY < radius) newY = myPolyGroup.sceneToLocal(scene.getX(), radius).getY();
                else if(newY > ((Region) myPolyGroup.getParent()).getHeight() - radius) newY = myPolyGroup.sceneToLocal(scene.getX(), ((Region) myPolyGroup.getParent()).getHeight() - radius).getY();
                else newY = point.getY();
                ((Circle) (event.getSource())).setCenterY(newY);
                myPolyGroup.getPoly().getPoints().set(offset + 1, getCenterY());
            });

            setOnMouseClicked(event -> {
                if(event.getButton() == MouseButton.SECONDARY){
                    myPolyGroup.getPoly().getPoints().remove(offset, offset+2);
                    for(Node node : myPolyGroup.getChildren()){
                        if(node instanceof MyPoint){
                            if(((MyPoint) node).offset > offset) ((MyPoint) node).offset -= 2;
                        }
                    }
                    myPolyGroup.getChildren().remove(this);
                    myPolyGroup.getMyPoints().remove(this);
                }
            });

        setOnKeyPressed(event -> {
            if (Editor.currentlyPressed == this) {
                if (event.getSource() == KeyCode.LEFT) {
                    double deltaX = 5;
                    double newLayoutX = getCenterX() - deltaX;
                    ((Circle)(event.getSource())).setCenterX(newLayoutX);
                    myPolyGroup.getPoly().getPoints().set(offset, getCenterX());
                } else if (event.getSource() == KeyCode.RIGHT) {
                    double deltaX = 5;
                    double newLayoutX = getCenterX()+deltaX;
                    ((Circle)(event.getSource())).setCenterX(newLayoutX);
                    myPolyGroup.getPoly().getPoints().set(offset, getCenterX());
                } else if (event.getSource() == KeyCode.DOWN) {
                    double deltaY = 5;
                    double newLayoutY = getCenterY() + deltaY;
                    ((Circle)(event.getSource())).setCenterX(newLayoutY);
                    myPolyGroup.getPoly().getPoints().set(offset+1, getCenterY());

                } else if (event.getSource() == KeyCode.UP) {
                    double deltaY = 5;
                    double newLayoutY = getCenterY() - deltaY;
                    ((Circle)(event.getSource())).setCenterX(newLayoutY);
                    myPolyGroup.getPoly().getPoints().set(offset+1, getCenterY());
                }
            }
        });

    }



    MyPoint(double x, double y, double radius, MyCircGroup myCircGroup){
        super(x,y,radius);
        setFill(fill);

        setOnMousePressed(event -> {
            orgSceneY = event.getY();
            orgLayoutY = ((Circle)(event.getSource())).getCenterY();
        });

        setOnMouseDragged(event -> {
            if (getCenterY()-getRadius() >= 0 && getCenterY()+getRadius() <= Main.getPane().getHeight()) {
                double offsetY = event.getSceneY() - orgSceneY;
                double newLayoutY = orgLayoutY + offsetY;
                ((Circle) event.getSource()).setCenterY(newLayoutY);
                myCircGroup.getCircle().setRadius(Math.abs(myCircGroup.getCircle().getCenterY() - newLayoutY));
            }

            else if (getCenterY()-getRadius() < 0) setCenterY(0);

            else if (getCenterY()+getRadius() > Main.getPane().getHeight()) setCenterY(Main.getPane().getHeight());
        });


        setOnKeyPressed(event -> {
            if (Editor.currentlyPressed == this) {
                double lastCircleY = myCircGroup.getCircle().getCenterY();
                double lastY = getCenterY();
                if (event.getSource() == KeyCode.LEFT) {
                    double deltaX = 5;
                    double newLayoutX = getCenterX() - deltaX;
                    setCenterX(newLayoutX);
                    myCircGroup.getCircle().setCenterX(getCenterX());
                } else if (event.getSource() == KeyCode.RIGHT) {
                    double deltaX = 5;
                    double newLayoutX = getCenterX()+deltaX;
                    setCenterX(newLayoutX);
                    myCircGroup.getCircle().setCenterX(getCenterX());
                } else if (event.getSource() == KeyCode.DOWN) {
                    double deltaY = 5;
                    double newLayoutY = getCenterY() + deltaY;
                    setCenterY(newLayoutY);
                    if (lastCircleY < lastY) myCircGroup.getCircle().setCenterY(getCenterY()-myCircGroup.getCircle().getRadius());
                    else myCircGroup.getCircle().setCenterY(getCenterY()+ myCircGroup.getCircle().getRadius());
                } else if (event.getSource() == KeyCode.UP) {
                    double deltaY = 5;
                    double newLayoutY = getCenterY() - deltaY;
                    setCenterY(newLayoutY);
                    if (lastCircleY < lastY) myCircGroup.getCircle().setCenterY(getCenterY()-myCircGroup.getCircle().getRadius());
                    else myCircGroup.getCircle().setCenterY(getCenterY()+ myCircGroup.getCircle().getRadius());
                }
            }
        });
    }
}
