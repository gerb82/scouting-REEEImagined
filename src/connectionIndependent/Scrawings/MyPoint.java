package connectionIndependent.Scrawings;

import gbuiLib.MethodTypes.CreatorMethod;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

class MyPoint extends Circle implements PossibleHitBox {
    protected static final double radius = 4;
    private Color fill = Color.RED;
    private int offset;

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

        setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                myPolyGroup.getPoly().getPoints().remove(offset, offset + 2);
                for (Node node : myPolyGroup.getChildren()) {
                    if (node instanceof MyPoint) {
                        if (((MyPoint) node).offset > offset) ((MyPoint) node).offset -= 2;
                    }
                }
                myPolyGroup.getChildren().remove(this);
                myPolyGroup.getMyPoints().remove(this);
            }
        });

        supposedCenter = () -> new Point2D(myPolyGroup.getPoly().getPoints().get(offset), myPolyGroup.getPoly().getPoints().get(offset + 1));
    }

    private CreatorMethod<Point2D> supposedCenter;

    public Point2D getSupposedCenter(){
        return supposedCenter.execute();
    }

    public MyPoint(double x, double y, MyCircGroup myCircGroup) {
        super(x, y, radius);
        setFill(fill);

//        setOnMousePressed(event -> {
//            orgSceneY = event.getY();
//            orgLayoutY = ((Circle) (event.getSource())).getCenterY();
//        });
//
//        setOnMouseDragged(event -> {
//            if (getCenterY() - getRadius() >= 0 && getCenterY() + getRadius() <= Main.getPane().getHeight()) {
//                double offsetY = event.getSceneY() - orgSceneY;
//                double newLayoutY = orgLayoutY + offsetY;
//                ((Circle) event.getSource()).setCenterY(newLayoutY);
//                myCircGroup.getCircle().setRadius(Math.abs(myCircGroup.getCircle().getCenterY() - newLayoutY));
//            } else if (getCenterY() - getRadius() < 0) setCenterY(0);
//
//            else if (getCenterY() + getRadius() > Main.getPane().getHeight()) setCenterY(Main.getPane().getHeight());
//        });
//
//
//        setOnKeyPressed(event -> {
//            if (ScrawingsManager.currentlyPressed == this) {
//                double lastCircleY = myCircGroup.getCircle().getCenterY();
//                double lastY = getCenterY();
//                if (event.getSource() == KeyCode.LEFT) {
//                    double deltaX = 5;
//                    double newLayoutX = getCenterX() - deltaX;
//                    setCenterX(newLayoutX);
//                    myCircGroup.getCircle().setCenterX(getCenterX());
//                } else if (event.getSource() == KeyCode.RIGHT) {
//                    double deltaX = 5;
//                    double newLayoutX = getCenterX() + deltaX;
//                    setCenterX(newLayoutX);
//                    myCircGroup.getCircle().setCenterX(getCenterX());
//                } else if (event.getSource() == KeyCode.DOWN) {
//                    double deltaY = 5;
//                    double newLayoutY = getCenterY() + deltaY;
//                    setCenterY(newLayoutY);
//                    if (lastCircleY < lastY)
//                        myCircGroup.getCircle().setCenterY(getCenterY() - myCircGroup.getCircle().getRadius());
//                    else myCircGroup.getCircle().setCenterY(getCenterY() + myCircGroup.getCircle().getRadius());
//                } else if (event.getSource() == KeyCode.UP) {
//                    double deltaY = 5;
//                    double newLayoutY = getCenterY() - deltaY;
//                    setCenterY(newLayoutY);
//                    if (lastCircleY < lastY)
//                        myCircGroup.getCircle().setCenterY(getCenterY() - myCircGroup.getCircle().getRadius());
//                    else myCircGroup.getCircle().setCenterY(getCenterY() + myCircGroup.getCircle().getRadius());
//                }
//            }
//        });
    }
}
