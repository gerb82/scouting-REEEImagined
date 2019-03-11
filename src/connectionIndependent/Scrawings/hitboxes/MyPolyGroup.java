package connectionIndependent.Scrawings.hitboxes;

import connectionIndependent.Scrawings.ScrawingsManager;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;

import java.util.ArrayList;
import java.util.function.Consumer;

public class MyPolyGroup extends Group implements PossibleHitBox {

    private ArrayList<MyPoint> myPoints = new ArrayList<>();
    private Polygon poly;

    public MyPolyGroup(double[] doubles) {
        super();
        poly = new Polygon(doubles);
        prepareForEdit();
    }

    public MyPolyGroup(){}

    public MyPolyGroup paste(double layoutX, double layoutY){
        MyPolyGroup result = new MyPolyGroup();
        Polygon poly = new Polygon();
        poly.getPoints().addAll(this.getPoly().getPoints());
        result.setPoly(poly);
        result.setLayoutX(layoutX);
        result.setLayoutY(layoutY);
        result.getPoly().setFill(this.getPoly().getFill());
        return result;
    }

    private void prepareForEdit() {
        getChildren().add(poly);
        poly.setStrokeWidth(2.5);
        Color fill = Color.BLACK;
        poly.setFill(fill);
        for (int i = 0; i < poly.getPoints().size(); i += 2) {
            MyPoint myPoint = new MyPoint(poly.getPoints().get(i), poly.getPoints().get(i + 1), i, this);
            myPoints.add(myPoint);
            getChildren().add(myPoint);
        }

        SimpleDoubleProperty mouseX = new SimpleDoubleProperty();
        SimpleDoubleProperty mouseY = new SimpleDoubleProperty();

        poly.setOnMousePressed(event -> {
            mouseX.set(event.getSceneX());
            mouseY.set(event.getSceneY());
            ScrawingsManager.getInstance().lastPressed.set(this);
            poly.setStroke(Color.MEDIUMPURPLE);
        });

        poly.setOnMouseDragged(event -> {
            double xMove = mouseX.get() - event.getSceneX();
            double yMove = mouseY.get() - event.getSceneY();
            setLayoutX(getLayoutX() - xMove);
            setLayoutY(getLayoutY() - yMove);
            mouseX.set(event.getSceneX());
            mouseY.set(event.getSceneY());
        });

        poly.setOnMouseReleased(event -> poly.setStroke(null));
    }

    private Consumer<ArrayList<Byte>> eventsUser;

    public void setPoly(Polygon polygon) {
        this.poly = polygon;
        if (ScrawingsManager.getInstance().isEditing())
            prepareForEdit();
        else {
            getChildren().add(poly);
            poly.setOnMouseClicked(event -> {
                if (!getBytes().isEmpty()) {
                    eventsUser.accept(bytes);
                    event.consume();
                }
            });
        }
    }

    public void setEventsUser(Consumer<ArrayList<Byte>> consumer){
        this.eventsUser = consumer;
    }

    public void startPointAdd(Point2D mouseLocation) {
        closestLine = new Line();
        completerLine = new Line();
        getChildren().addAll(closestLine, completerLine);
        newPointMover(sceneToLocal(mouseLocation));
        ScrawingsManager.getInstance().editingGround.setOnMouseMoved(event -> newPointMover(sceneToLocal(event.getSceneX(), event.getSceneY())));
        ScrawingsManager.getInstance().editingGround.setOnMouseClicked(event -> finalizeAddPoint());
    }

    private Point2D newPoint;
    private MyPoint closestPoint;
    private MyPoint completerPoint;
    private Line closestLine;
    private Line completerLine;

    private void newPointMover(Point2D mouseLoc) {
        newPoint = mouseLoc;
        closestPoint = null;
        for (MyPoint point : myPoints) {
            if (closestPoint == null || closestPoint.getSupposedCenter().distance(newPoint) > point.getSupposedCenter().distance(newPoint)) {
                closestPoint = point;
            }
        }
        completerPoint = (myPoints.indexOf(closestPoint) == 0 ? myPoints.get(myPoints.size() - 1) : myPoints.get(myPoints.indexOf(closestPoint) - 1)).getSupposedCenter().distance(newPoint) > (myPoints.indexOf(closestPoint) == myPoints.size() - 1 ? myPoints.get(0) : myPoints.get(myPoints.indexOf(closestPoint) + 1)).getSupposedCenter().distance(newPoint) ? myPoints.indexOf(closestPoint) == myPoints.size() - 1 ? myPoints.get(0) : myPoints.get(myPoints.indexOf(closestPoint) + 1) : myPoints.indexOf(closestPoint) == 0 ? myPoints.get(myPoints.size() - 1) : myPoints.get(myPoints.indexOf(closestPoint) - 1);
        closestLine.setEndX(newPoint.getX());
        closestLine.setEndY(newPoint.getY());
        completerLine.setEndX(newPoint.getX());
        completerLine.setEndY(newPoint.getY());
        closestLine.setStartX(closestPoint.getCenterX());
        closestLine.setStartY(closestPoint.getCenterY());
        completerLine.setStartX(completerPoint.getCenterX());
        completerLine.setStartY(completerPoint.getCenterY());
    }

    private void finalizeAddPoint() {
        getChildren().removeAll(completerLine, closestLine);
        ScrawingsManager.getInstance().editingGround.setOnMouseMoved(null);
        ScrawingsManager.getInstance().editingGround.setOnMouseClicked(null);
        int indexToAdd = closestPoint.getOffset();
        if ((indexToAdd == 0 && completerPoint.getOffset() != 2) || (indexToAdd == myPoints.size() - 1 && completerPoint.getOffset() == 0)) {
            MyPoint newP = new MyPoint(newPoint.getX(), newPoint.getY(), (myPoints.size()) * 2, this);
            getPoly().getPoints().addAll(newPoint.getX(), newPoint.getY());
            getChildren().add(newP);
            getMyPoints().add(newP);
            return;
        }
        indexToAdd += (closestPoint.getOffset() < completerPoint.getOffset() ? 2 : 0);
        poly.getPoints().addAll(indexToAdd, new ArrayList<Double>() {{
            add(newPoint.getX());
            add(newPoint.getY());
        }});
        for (MyPoint point : myPoints) {
            if (point.getOffset() >= indexToAdd) point.setOffset(point.getOffset() + 2);
        }
        MyPoint newP = new MyPoint(newPoint.getX(), newPoint.getY(), indexToAdd, this);
        getChildren().add(newP);
        getMyPoints().add(indexToAdd / 2, newP);
    }

    public Polygon getPoly() {
        return poly;
    }

    public ArrayList<MyPoint> getMyPoints() {
        return myPoints;
    }

    public String toFXML(){
        String points = "";
        for(double kill : this.poly.getPoints()){
            points += String.format("<Double value=\"%d\"/>%n", (int) kill);
        }
        return String.format("<MyPolyGroup hitboxId=\"%d\" layoutX=\"%d\" layoutY=\"%d\">%n<poly>%n<Polygon fill=\"%s\">%n%s</Polygon>%n</poly>%n</MyPolyGroup>", hitboxId, (int) getLayoutX(), (int) getLayoutY(), poly.getFill().toString(), points);
    }

    private byte hitboxId = -1;

    @Override
    public byte getHitboxId() {
        return hitboxId;
    }

    @Override
    public void setHitboxId(byte newId) {
        this.hitboxId = newId;
    }
}
