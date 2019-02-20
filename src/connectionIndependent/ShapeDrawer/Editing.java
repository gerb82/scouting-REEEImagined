package connectionIndependent.ShapeDrawer;

import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.Pane;

import java.util.ArrayList;

public class Editing {
    private static Editing ourInstance;
    ArrayList<MyGroup> MyGroups = new ArrayList<>();

    public ArrayList<MyGroup> getMyGroups() {
        return MyGroups;
    }

    ArrayList<Button> myButtons = new ArrayList<>();

    static MyGroup polyPressed = null;
    static MyCircle circPressed = null;
    static MyPoint pointPressed = null;
    public static void initialize(boolean isEditing) {
        if (ourInstance == null) {
            ourInstance = new Editing(isEditing);
        }
    }


    double[] square = new double[]{
            365, 0,
            465, 0,
            465, 100,
            365, 100,
    };

    double[] rectangle = new double[]{
            265, 50,
            465, 50,
            465, 150,
            265, 150,
    };


    public static Editing getInstance() {
        return ourInstance;
    }

    boolean editing;
    Pane pane = Main.getPane();

    private Editing(boolean isEditing) {
        editing = isEditing;
        if (editing) {

            Button squareButton = new Button("100*100 square");
            squareButton.setPrefSize(100, 50);
            squareButton.setLayoutX(Main.getPane().getWidth()-squareButton.getPrefWidth());
            squareButton.setLayoutY(0);

            squareButton.setOnAction(event -> {
                MyGroup newSquare = new MyGroup(square, editing);
                MyGroups.add(newSquare);
                pane.getChildren().add(newSquare);
                polyPressed = newSquare;
                circPressed = null;
                pointPressed = null;
            });

            Button rectangleButton = new Button("200*150 square");
            rectangleButton.setPrefSize(100, 50);
            rectangleButton.setLayoutX(Main.getPane().getWidth()-rectangleButton.getPrefWidth());
            rectangleButton.setLayoutY(50);

            rectangleButton.setOnAction(event -> {
                MyGroup newRectangle = new MyGroup(rectangle, editing);
                MyGroups.add(newRectangle);
                pane.getChildren().add(newRectangle);
                polyPressed = newRectangle;
                circPressed = null;
                pointPressed = null;
            });


            Button circle = new Button("circle");
            circle.setPrefSize(85, 50);
            circle.setLayoutX(Main.getPane().getWidth()-circle.getPrefWidth());
            circle.setLayoutY(100);

            circle.setOnAction(event -> {
                MyCircle newCircle = new MyCircle(505, 125, 10, editing);
                pane.getChildren().add(newCircle);
                circPressed = newCircle;
                polyPressed = null;
                pointPressed = null;
            });


            Button point = new Button("new point");
            myButtons.add(point);
            point.setPrefSize(85, 50);
            point.setLayoutX(Main.getPane().getWidth()-point.getPrefWidth());
            point.setLayoutY(150);

            point.setOnAction(event -> {
                if (polyPressed != null) {
                    MyPoint myPoint = new MyPoint(
                            (Math.abs(polyPressed.getPoly().getPoints().get(polyPressed.getPoly().getPoints().size() - 2) +
                                    polyPressed.getPoly().getPoints().get(0))) / 2,
                            (Math.abs(polyPressed.getPoly().getPoints().get(polyPressed.getPoly().getPoints().size() - 1) +
                                    polyPressed.getPoly().getPoints().get(1))) / 2,
                            polyPressed.getRadius(),
                            polyPressed.getPoly().getPoints().size(),
                            polyPressed);
                    polyPressed.getPoly().getPoints().addAll(myPoint.getCenterX(), myPoint.getCenterY());
                    System.out.println(polyPressed.getPoly().getPoints().size());
                    polyPressed.getChildren().add(myPoint);
                }
            });

            myButtons.add(circle);
            myButtons.add(squareButton);
            myButtons.add(rectangleButton);


            ColorPicker colorPicker = new ColorPicker();
            colorPicker.setPrefSize(85, 50);
            colorPicker.setLayoutX(Main.getPane().getWidth()-colorPicker.getPrefWidth());
            colorPicker.setLayoutY(200);
            colorPicker.setOnAction(event -> {
                if (polyPressed != null) polyPressed.getPoly().setFill(colorPicker.getValue());
                if (circPressed != null) circPressed.setFill(colorPicker.getValue());
            });

            double[] doubles = new double[]{
                    Math.random() * Main.getPane().getWidth()-getButtonBigX(myButtons).getWidth(), Math.random() * 500,
                    Math.random() * Main.getPane().getWidth()-getButtonBigX(myButtons).getWidth(), Math.random() * 500,
                    Math.random() * Main.getPane().getWidth()-getButtonBigX(myButtons).getWidth(), Math.random() * 500,
                    Math.random() * Main.getPane().getWidth()-getButtonBigX(myButtons).getWidth(), Math.random() * 500,
            };
            MyGroup MyGroup = new MyGroup(doubles, editing);
            MyGroups.add(MyGroup);
            polyPressed = MyGroup;


            pane.getChildren().addAll(MyGroup, point, squareButton, circle, rectangleButton, colorPicker);


        } else {
            double[] doubles = new double[]{
                    Math.random() * 500, Math.random() * 500,
                    Math.random() * 500, Math.random() * 500,
                    Math.random() * 500, Math.random() * 500,
                    Math.random() * 500, Math.random() * 500,
            };
            MyGroup myGroup = new MyGroup(doubles, editing);

            pane.getChildren().add(myGroup);
        }

    }
    public Button getButtonBigX(ArrayList<Button> buttons){
        Button button = buttons.get(0);
        for (int i = 1; i < buttons.size(); i++) {
            if (buttons.get(i).getWidth()>button.getWidth()){
                button = buttons.get(i);
            }
        }

        return button;
    }

    public ArrayList<Button> getMyButtons() {
        return myButtons;
    }
}