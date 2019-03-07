package connectionIndependent.Scrawings;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public final class ScrawingsManager {

    private static ScrawingsManager ourInstance;

    public static void initialize(boolean isEditing) {
        if (ourInstance == null) {
            ourInstance = new ScrawingsManager(isEditing);
        }
    }

    public static ScrawingsManager getInstance() {
        return ourInstance;
    }

    public void prepareDirectory(File directoryPath) {
        directoryPath.mkdirs();
        for (File file : directoryPath.listFiles(pathname -> pathname.getAbsolutePath().endsWith(".scraw"))) {
            file.delete();
        }
    }

    public boolean mustOverride(File directoryPath) {
        return directoryPath.listFiles(pathname -> pathname.getAbsolutePath().endsWith(".scraw")).length == 0;
    }

    public ArrayList<Pair<String, Scraw>> loadDirectory(File directoryPath) throws IOException {
        try {
            ArrayList<Pair<String, Scraw>> output = new ArrayList<>();
            for (File file : directoryPath.listFiles(pathname -> pathname.getAbsolutePath().endsWith(".scraw"))) {
                FXMLLoader loader = new FXMLLoader(file.toURI().toURL());
                loader.setController(this);
                Scraw scraw = loader.load();
                registerScraw(scraw);
                output.add(new Pair<>(file.getName().split("\\.")[0], scraw));
            }
            return output;
        } catch (IOException e) {
            throw new IOException("Failed to load the trees directory!", e);
        }
    }

    private HashMap<Byte, Scraw> scrawsMap = new HashMap<>();

    private void registerScraw(Scraw scraw) {
        byte scrawNumber = scraw.getScrawNumber();
        while (scrawNumber == -1 || scrawsMap.keySet().contains(scrawNumber)) {
            scrawNumber++;
        }
        scraw.setScrawNumber(scrawNumber);
        scrawsMap.put(scrawNumber, scraw);
        scraw.requestLayout();
    }

    private ContextMenu editShape = new ContextMenu();
    private ContextMenu miscActions = new ContextMenu();
    protected Scraw currentlyEditing;
    protected PossibleHitBox lastPressed;
    protected Pane editingGround;
    private Point2D menuOpenedAt;
    private boolean isEditing;

    private ScrawingsManager(boolean isEditing) {
        this.isEditing = isEditing;
        if (isEditing) {
            editShape.setAutoHide(true);
            miscActions.setAutoHide(true);
            MenuItem square = new MenuItem("New Square", new Rectangle(30, 30, Color.BLACK));
            square.setOnAction(event -> {
                double[] points = new double[]{
                        0, 0, // top left
                        0, 100, // bottom left
                        100, 100, // bottom right
                        100, 0 // top right
                };
                MyPolyGroup newSquare = new MyPolyGroup(points);
                currentlyEditing.getChildren().add(newSquare);
                newSquare.setLayoutX(menuOpenedAt.getX());
                newSquare.setLayoutY(menuOpenedAt.getY());
                newSquare.setContextMenu(editShape);
            });

            MenuItem sideRect = new MenuItem("New Rectangle", new Rectangle(40, 20, Color.BLACK));
            sideRect.setOnAction(event -> {
                double[] points = new double[]{
                        0, 0, // top left
                        0, 50, // bottom left
                        100, 50, // bottom right
                        100, 0 // top right
                };
                MyPolyGroup newRectangle = new MyPolyGroup(points);
                currentlyEditing.getChildren().add(newRectangle);
                newRectangle.setLayoutX(menuOpenedAt.getX());
                newRectangle.setLayoutY(menuOpenedAt.getY());
                newRectangle.setContextMenu(editShape);
            });

            MenuItem highRec = new MenuItem("New Rectangle", new Rectangle(20, 40, Color.BLACK));
            highRec.setOnAction(event -> {
                double[] points = new double[]{
                        0, 0, // top left
                        0, 100, // bottom left
                        50, 100, // bottom right
                        50, 0 // top right
                };
                MyPolyGroup newRectangle = new MyPolyGroup(points);
                currentlyEditing.getChildren().add(newRectangle);
                newRectangle.setLayoutX(menuOpenedAt.getX());
                newRectangle.setLayoutY(menuOpenedAt.getY());
                newRectangle.setContextMenu(editShape);
            });

            MenuItem circle = new MenuItem("New Circle", new Circle(15, 15, 15, Color.BLACK));
            circle.setOnAction(event -> {
                MyCircGroup newCircle = new MyCircGroup(menuOpenedAt.getX(), menuOpenedAt.getY(), 20);
                currentlyEditing.getChildren().add(newCircle);
                newCircle.setContextMenu(editShape);
            });

            miscActions.getItems().addAll(square, highRec, sideRect, circle);


//            MenuItem addPoint = new MenuItem("Add Point", new MyPoint());
//            addPoint.setOnAction(event -> {
//                    MyPolyGroup tempPressed = (MyPolyGroup) currentlyPressed;
//                    tempPressed.initAddPoint();
//            });

            MenuItem colorPicker = new MenuItem("", new ColorPicker());
            ((ColorPicker) colorPicker.getGraphic()).setOnAction(event -> {
                if (lastPressed instanceof MyPolyGroup) {
                    ((MyPolyGroup) lastPressed).getPoly().setFill(((ColorPicker) colorPicker.getGraphic()).getValue());
                } else if (lastPressed instanceof MyCircGroup)
                    ((MyCircGroup) lastPressed).getCircle().setFill(((ColorPicker) colorPicker.getGraphic()).getValue());
            });

            editShape.getItems().add(colorPicker);
        }
    }

    public Scraw createScraw() {
        return new Scraw();
    }

    public void startScrawEdit(Scraw scraw) {
        currentlyEditing = scraw == null ? createScraw() : scraw;
        for (Node node : currentlyEditing.getChildren().filtered(node -> node instanceof PossibleHitBox)) {
            PossibleHitBox hitBox = ((PossibleHitBox) node);
            hitBox.setContextMenu(editShape);
        }
        editingGround = (Pane) scraw.getParent();
        editingGround.setOnContextMenuRequested(event -> {
            if(!(event.getTarget() instanceof PossibleHitBox)) {
                menuOpenedAt = currentlyEditing.sceneToLocal(event.getSceneX(), event.getSceneY());
                miscActions.show(currentlyEditing, event.getScreenX(), event.getScreenY());
            }
        });
    }

    public String scrawAsFXML(byte scrawNumber) {
        Scraw scraw = scrawsMap.get(scrawNumber);
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator() +
                "<?import connectionIndependent.eventsMapping.ScoutingEventTree?>" + System.lineSeparator() +
                "<?import connectionIndependent.eventsMapping.ScoutingEventLayer?>" + System.lineSeparator() +
                "<?import connectionIndependent.eventsMapping.ScoutingEventUnit?>" + System.lineSeparator() +
                "<?import connectionIndependent.eventsMapping.ScoutingEventDirection?>" + System.lineSeparator() +
                scraw.toFXML("xmlns=\"http://javafx.com/javafx\" xmlns:fx=\"http://javafx.com/fxml\"");
    }

    public boolean isEditing() {
        return isEditing;
    }
}