package connectionIndependent.scrawings.hitboxes;

import connectionIndependent.scrawings.ScrawingsManager;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.function.Consumer;

public class MyCircGroup extends Group implements PossibleHitBox {

    private double orgSceneX, orgSceneY, orgLayoutX, orgLayoutY;
    private Circle circle;

    public MyCircGroup(double x, double y, double radius) {
        super();
        circle = new Circle(x, y, radius);
        prepareForEdit();
    }

    public MyCircGroup(){}

    public MyCircGroup paste(double centerX, double centerY){
        MyCircGroup result = new MyCircGroup();
        result.setCircle(new Circle(centerX, centerY, this.getCircle().getRadius(), this.getCircle().getFill()));
        return result;
    }

    private void prepareForEdit() {
        getChildren().add(circle);
        MyPoint myPoint = new MyPoint(circle.getCenterX(), circle.getCenterY() - circle.getRadius(), this);
        getChildren().add(myPoint);

        setOnMousePressed(event -> {
            orgSceneX = event.getX();
            orgSceneY = event.getY();
            orgLayoutX = ((MyCircGroup) event.getSource()).circle.getCenterX();
            orgLayoutY = ((MyCircGroup) event.getSource()).circle.getCenterY();
            circle.setStroke(Color.MEDIUMPURPLE);
            circle.setStrokeWidth(2.5);
            ScrawingsManager.getInstance().lastPressed.set(this);
        });


        setOnMouseDragged(event -> {
            double offsetX = event.getSceneX() - orgSceneX;
            double offsetY = event.getSceneY() - orgSceneY;
            double newLayoutX = orgLayoutX + offsetX;
            double newLayoutY = orgLayoutY + offsetY;
            getCircle().setCenterX(newLayoutX);
            getCircle().setCenterY(newLayoutY);

            myPoint.setCenterX(circle.getCenterX());
            myPoint.setCenterY(circle.getCenterY() - circle.getRadius());
        });

        setOnMouseReleased(event -> circle.setStrokeWidth(0));
    }

    private Consumer<ArrayList<Byte>> eventsUser;

    public void setCircle(Circle circle) {
        this.circle = circle;
        if (ScrawingsManager.getInstance().isEditing())
            prepareForEdit();
        else {
            getChildren().add(circle);
            circle.setOnMouseClicked(event -> {
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

    public Circle getCircle() {
        return circle;
    }

    public String toFXML(){
        return String.format("<MyCircGroup hitboxId=\"%d\">%n<circle>%n<Circle centerX=\"%d\" centerY=\"%d\" radius=\'%d\' fill=\"%s\"/>%n</circle>%n</MyCircGroup>", hitboxId, (int) circle.getCenterX(), (int) circle.getCenterY(), (int) circle.getRadius(), circle.getFill().toString());
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
