package connectionIndependent.ShapeDrawer;

import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import java.util.ArrayList;

public class Editor {
    private static Editor ourInstance;
    static PossibleHitBox currentlyPressed = null;
    private ArrayList<MyPolyGroup> myPolyGroups = new ArrayList<>();


    private double[] square = new double[]{
            365, 0,
            465, 0,
            465, 100,
            365, 100,
    };
    private double[] rectangle = new double[]{
            265, 50,
            465, 50,
            465, 150,
            265, 150,
    };
    private Pane pane = Main.getPane();


    private Editor(boolean isEditing) {
        if (isEditing) {

            Button squareButton = new Button("100*100 square");
            squareButton.setPrefSize(100, 50);
            squareButton.setLayoutX(Main.getPane().getWidth()-squareButton.getPrefWidth());
            squareButton.setLayoutY(0);

            squareButton.setOnAction(event -> {
                MyPolyGroup newSquare = new MyPolyGroup(square, isEditing);
                myPolyGroups.add(newSquare);
                pane.getChildren().add(newSquare);
                currentlyPressed = newSquare;
            });

            Button rectangleButton = new Button("200*150 square");
            rectangleButton.setPrefSize(100, 50);
            rectangleButton.setLayoutX(Main.getPane().getWidth()-rectangleButton.getPrefWidth());
            rectangleButton.setLayoutY(50);

            rectangleButton.setOnAction(event -> {
                MyPolyGroup newRectangle = new MyPolyGroup(rectangle, isEditing);
                myPolyGroups.add(newRectangle);
                pane.getChildren().add(newRectangle);
                currentlyPressed = newRectangle;
            });


            Button circle = new Button("circle");
            circle.setPrefSize(85, 50);
            circle.setLayoutX(Main.getPane().getWidth()-circle.getPrefWidth());
            circle.setLayoutY(100);

            circle.setOnAction(event -> {
                MyCircGroup newCircle = new MyCircGroup(505, 125, 10, isEditing);
                pane.getChildren().add(newCircle);
                currentlyPressed = newCircle;
            });


            Button point = new Button("new point");
            ArrayList<Button> myButtons = new ArrayList<>();
            myButtons.add(point);
            point.setPrefSize(85, 50);
            point.setLayoutX(Main.getPane().getWidth()-point.getPrefWidth());
            point.setLayoutY(150);

            point.setOnAction(event -> {
                if (currentlyPressed instanceof MyPolyGroup) {
                    MyPolyGroup tempPressed = (MyPolyGroup)currentlyPressed;
                    MyPoint myPoint = new MyPoint(
                            (Math.abs(tempPressed.getPoly().getPoints().get(tempPressed.getPoly().getPoints().size() - 2) +
                                    tempPressed.getPoly().getPoints().get(0))) / 2,
                            (Math.abs(tempPressed.getPoly().getPoints().get(tempPressed.getPoly().getPoints().size() - 1) +
                                    tempPressed.getPoly().getPoints().get(1))) / 2,
                            tempPressed.getRadius(),
                            tempPressed.getPoly().getPoints().size(),
                            tempPressed);
                    tempPressed.getPoly().getPoints().addAll(myPoint.getCenterX(), myPoint.getCenterY());
                    System.out.println(tempPressed.getPoly().getPoints().size());
                    tempPressed.getChildren().add(myPoint);
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
                if (currentlyPressed != null) {
                    if (currentlyPressed instanceof MyPolyGroup) {
                        ((MyPolyGroup) currentlyPressed).getPoly().setFill(colorPicker.getValue());
                    } else if (currentlyPressed instanceof MyCircGroup)
                        ((MyCircGroup) currentlyPressed).getCircle().setFill(colorPicker.getValue());

                }
            });

            TextField textField = new TextField();



            double[] doubles = new double[]{
                    Math.random() * Main.getPane().getWidth()-getButtonBigX(myButtons).getWidth(), Math.random() * 500,
                    Math.random() * Main.getPane().getWidth()-getButtonBigX(myButtons).getWidth(), Math.random() * 500,
                    Math.random() * Main.getPane().getWidth()-getButtonBigX(myButtons).getWidth(), Math.random() * 500,
                    Math.random() * Main.getPane().getWidth()-getButtonBigX(myButtons).getWidth(), Math.random() * 500,
            };
            MyPolyGroup myPolyGroup = new MyPolyGroup(doubles, isEditing);
            myPolyGroups.add(myPolyGroup);
            currentlyPressed = myPolyGroup;


            pane.getChildren().addAll(myPolyGroup, point, squareButton, circle, rectangleButton, colorPicker);


        } else {
            double[] doubles = new double[]{
                    Math.random() * 500, Math.random() * 500,
                    Math.random() * 500, Math.random() * 500,
                    Math.random() * 500, Math.random() * 500,
                    Math.random() * 500, Math.random() * 500,
            };
            MyPolyGroup myPolyGroup = new MyPolyGroup(doubles, isEditing);

            pane.getChildren().add(myPolyGroup);
        }

    }

    public static void initialize(boolean isEditing) {
        if (ourInstance == null) {
            ourInstance = new Editor(isEditing);
        }
    }

    public static Editor getInstance() {
        return ourInstance;
    }

    private Button getButtonBigX(ArrayList<Button> buttons){
        Button button = buttons.get(0);
        for (int i = 1; i < buttons.size(); i++) {
            if (buttons.get(i).getWidth()>button.getWidth()){
                button = buttons.get(i);
            }
        }

        return button;
    }
}