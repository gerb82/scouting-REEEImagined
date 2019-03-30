package connectionIndependent.scrawings;

import connectionIndependent.scrawings.hitboxes.MyCircGroup;
import connectionIndependent.scrawings.hitboxes.MyPolyGroup;
import connectionIndependent.scrawings.hitboxes.PossibleHitBox;
import connectionIndependent.scrawings.scrawtypes.DataScraw;
import connectionIndependent.scrawings.scrawtypes.ScrawRecipe;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Pair;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

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
        for (File file : directoryPath.listFiles(pathname -> pathname.getAbsolutePath().endsWith(".scraw") || pathname.getAbsolutePath().endsWith(".dscr"))) {
            file.delete();
        }
    }

    public boolean mustOverride(File directoryPath) {
        return directoryPath.listFiles(pathname -> pathname.getAbsolutePath().endsWith(".scraw") || pathname.getAbsolutePath().endsWith(".dscr")).length == 0;
    }

    public Pair<ArrayList<Pair<String, ScrawRecipe>>, ArrayList<Pair<Byte, DataScraw>>> loadDirectory(File directoryPath, Boolean alliance) throws IOException {
        try {
            recipesList.clear();
            scrawsMap.clear();
            ArrayList<Pair<String, ScrawRecipe>> scrawOut = new ArrayList<>();
            for (File file : directoryPath.listFiles(pathname -> pathname.getAbsolutePath().endsWith(".scraw"))) {
                ScrawRecipe scraw = FXMLLoader.load(file.toURI().toURL());
                recipesList.add(scraw);
                scrawOut.add(new Pair<>(file.getName().split("\\.")[0], scraw));
            }
            ArrayList<Pair<Byte, DataScraw>> dataScrawOut = new ArrayList<>();
            File[] files;
            if(alliance == null) files = directoryPath.listFiles(pathname -> pathname.getAbsolutePath().endsWith("alliances.dscr") || pathname.getAbsolutePath().endsWith("teams.dscr"));
            else files = directoryPath.listFiles(pathname -> pathname.getAbsolutePath().endsWith((alliance ? "alliances" : "teams") + ".dscr"));
            for (File file : files) {
                Pane pane = FXMLLoader.load(file.toURI().toURL());
                for (Object scrawO : ((ListView<Object>) pane.getChildren().get(0)).getItems()) {
                    DataScraw scraw = (DataScraw) scrawO;
                    scraw.setAlliance(file.getAbsolutePath().endsWith("alliances.dscr"));
                    scrawsMap.put(scraw.getScrawNumber(), scraw);
                    dataScrawOut.add(new Pair<>(scraw.getScrawNumber(), scraw));
                }
            }
            return new Pair<>(scrawOut, dataScrawOut);
        } catch (IOException e) {
            throw new IOException("Failed to load the trees directory!", e);
        }
    }

    private HashMap<Byte, DataScraw> scrawsMap = new HashMap<>();
    private ArrayList<ScrawRecipe> recipesList = new ArrayList<>();

    public ArrayList<ScrawRecipe> getRecipesList() {
        return recipesList;
    }

    private void registerScraw(DataScraw scraw) {
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
    public ScrawRecipe currentlyEditing;
    public SimpleObjectProperty<PossibleHitBox> lastPressed;
    public Pane editingGround;
    private Point2D menuOpenedAt;
    private boolean isEditing;
    private PossibleHitBox copying;

    private ScrawingsManager(boolean isEditing) {
        this.isEditing = isEditing;
        if (isEditing) {
            lastPressed = new SimpleObjectProperty<>();
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

            ColorPicker colorPick = new ColorPicker();
            MenuItem colorPicker = new MenuItem("Change Color", new Rectangle(20, 20, colorPick.getValue()));
            colorPicker.setOnAction(event -> {
                Dialog<Color> colorer = new Dialog<>();
                colorer.getDialogPane().setContent(colorPick);
                colorPick.setOnAction(event1 -> {
                    colorer.setResult(colorPick.getValue());
                    colorer.close();
                });
                colorer.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);
                Optional<Color> color = colorer.showAndWait();
                if (!color.isPresent()) return;
                if (lastPressed.get() instanceof MyPolyGroup) {
                    ((MyPolyGroup) lastPressed.get()).getPoly().setFill(colorPick.getValue());
                } else if (lastPressed.get() instanceof MyCircGroup)
                    ((MyCircGroup) lastPressed.get()).getCircle().setFill(colorPick.getValue());
            });

            MenuItem delete = new MenuItem("Delete");
            delete.setOnAction(event -> {
                currentlyEditing.getChildren().remove(lastPressed.get());
                lastPressed.set(null);
            });

            MenuItem addPoint = new MenuItem("Add New Point");
            addPoint.setOnAction(event -> ((MyPolyGroup) lastPressed.get()).startPointAdd(((MyPolyGroup) lastPressed.get()).sceneToLocal(menuOpenedAt.getX(), menuOpenedAt.getY())));
            Menu location = new Menu("Priority");
            MenuItem bringToBack = new MenuItem("Bring To Back");
            bringToBack.setOnAction(event -> {
                currentlyEditing.getChildren().remove(lastPressed.get());
                currentlyEditing.getChildren().add(0, (Node) lastPressed.get());
            });
            MenuItem bringToFront = new MenuItem("Bring To Front");
            bringToFront.setOnAction(event -> {
                currentlyEditing.getChildren().remove(lastPressed.get());
                currentlyEditing.getChildren().add((Node) lastPressed.get());
            });
            MenuItem bringBackwards = new MenuItem("Bring Backwards");
            bringBackwards.setOnAction(event -> {
                int index = currentlyEditing.getChildren().indexOf(lastPressed.get());
                currentlyEditing.getChildren().remove(lastPressed.get());
                currentlyEditing.getChildren().add(Math.max(0, index - 1), (Node) lastPressed.get());
            });
            MenuItem bringForward = new MenuItem("Bring Forward");
            bringForward.setOnAction(event -> {
                int index = currentlyEditing.getChildren().indexOf(lastPressed.get());
                currentlyEditing.getChildren().remove(lastPressed.get());
                currentlyEditing.getChildren().add(Math.min(currentlyEditing.getChildren().size(), index + 1), (Node) lastPressed.get());
            });
            location.getItems().addAll(bringToFront, bringToBack, new SeparatorMenuItem(), bringForward, bringBackwards);

            MenuItem copy = new MenuItem("Copy");
            copy.setOnAction(event -> this.copying = lastPressed.get());

            MenuItem paste = new MenuItem("Paste");
            paste.setDisable(true);
            paste.setOnAction(event -> {
                if (copying != null) {
                    PossibleHitBox newShape = copying.paste(menuOpenedAt.getX(), menuOpenedAt.getY());
                    currentlyEditing.getChildren().add((Node) newShape);
                    newShape.setContextMenu(editShape);
                }
            });

            MenuItem changeID = new MenuItem("Change ID", new Label());
            changeID.setOnAction(event -> {
                Dialog<Byte> dialog = new Dialog<>();
                TextField field = new TextField();
                dialog.getDialogPane().setContent(field);
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
                dialog.setResultConverter(param -> {
                    if (param == ButtonType.OK) {
                        try {
                            return Byte.valueOf(field.getText().replaceAll("[^0-9]", ""));
                        } catch (Exception e) {
                        }
                    }
                    return lastPressed.get().getHitboxId();
                });
                Optional<Byte> result = dialog.showAndWait();
                result.ifPresent(aByte -> lastPressed.get().setHitboxId(aByte));
            });

            editShape.showingProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    if (lastPressed.get() instanceof MyPolyGroup) {
                        ((Rectangle) colorPicker.getGraphic()).setFill(((MyPolyGroup) lastPressed.get()).getPoly().getFill());
                        colorPick.setValue((Color) ((MyPolyGroup) lastPressed.get()).getPoly().getFill());
                    } else if (lastPressed.get() instanceof MyCircGroup) {
                        ((Rectangle) colorPicker.getGraphic()).setFill(((MyCircGroup) lastPressed.get()).getCircle().getFill());
                        colorPick.setValue((Color) ((MyCircGroup) lastPressed.get()).getCircle().getFill());
                    }
                    ((Label) changeID.getGraphic()).setText(String.valueOf(lastPressed.get().getHitboxId()));
                }
            });

            lastPressed.addListener((observable, oldValue, newValue) -> {
                if (newValue instanceof MyPolyGroup) {
                    addPoint.setDisable(false);
                    ((Rectangle) colorPicker.getGraphic()).setFill(((MyPolyGroup) lastPressed.get()).getPoly().getFill());
                } else if (newValue instanceof MyCircGroup) {
                    addPoint.setDisable(true);
                    ((Rectangle) colorPicker.getGraphic()).setFill(((MyCircGroup) lastPressed.get()).getCircle().getFill());
                }
                paste.setDisable(false);
            });

            editShape.getItems().addAll(delete, addPoint, location, copy, changeID, colorPicker);
            miscActions.getItems().addAll(square, highRec, sideRect, circle, paste);
        }
    }

    public ScrawRecipe createScraw() {
        return new ScrawRecipe();
    }

    public Optional<ScrawRecipe> startScrawEdit(ScrawRecipe scraw) {
        currentlyEditing = scraw == null ? createScraw() : scraw;
        for (Node node : currentlyEditing.getChildren().filtered(node -> node instanceof PossibleHitBox)) {
            PossibleHitBox hitBox = ((PossibleHitBox) node);
            hitBox.setContextMenu(editShape);
        }
        Dialog<ScrawRecipe> edit = new Dialog<>();
        editingGround = new Pane(scraw);
        edit.getDialogPane().setContent(editingGround);
        edit.getDialogPane().setMinSize(700, 243);
        edit.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        edit.setResultConverter(param -> {
            for (Node node : currentlyEditing.getChildren().filtered(node -> node instanceof PossibleHitBox)) {
                PossibleHitBox hitBox = ((PossibleHitBox) node);
                hitBox.setContextMenu(null);
                editingGround.setOnContextMenuRequested(null);
            }
            if (param == ButtonType.OK) {
                if (!recipesList.contains(currentlyEditing)) recipesList.add(currentlyEditing);
                return currentlyEditing;
            }
            return null;
        });
        editingGround.setOnContextMenuRequested(event -> {
            if (!(event.getTarget() instanceof PossibleHitBox)) {
                menuOpenedAt = currentlyEditing.sceneToLocal(event.getSceneX(), event.getSceneY());
                miscActions.show(currentlyEditing, event.getScreenX(), event.getScreenY());
            }
        });
        return edit.showAndWait();
    }

    private String scrawRecipeAsFXML(int scrawNumber) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator() +
                "<?import connectionIndependent.Scrawings.scrawtypes.ScrawRecipe?>" + System.lineSeparator() +
                "<?import connectionIndependent.Scrawings.hitboxes.MyPolyGroup?>" + System.lineSeparator() +
                "<?import connectionIndependent.Scrawings.hitboxes.MyCircGroup?>" + System.lineSeparator() +
                "<?import javafx.scene.shape.Circle?>" + System.lineSeparator() +
                "<?import javafx.scene.shape.Polygon?>" + System.lineSeparator() +
                recipesList.get(scrawNumber).toFXML("xmlns=\"http://javafx.com/javafx\" xmlns:fx=\"http://javafx.com/fxml\"");
    }

    public void saveScrawsToDirectory(File directory) throws IOException {
        for (ScrawRecipe recipe : recipesList) {
            File scraw = new File(directory, recipe.getName() + ".scraw");
            scraw.createNewFile();
            new ObjectOutputStream(new FileOutputStream(scraw)).writeUTF(scrawRecipeAsFXML(recipesList.indexOf(recipe)));
        }
        File teamScraws = new File(directory, "teams.dscr");
        String teamScrawsContents = "";
        String teamScrawsData = "";
        String teamScrawDecs = "";
        File allianceScraws = new File(directory, "alliances.dscr");
        String allianceScrawsContents = "";
        String allianceScrawsData = "";
        String allianceScrawDecs = "";
        for (DataScraw scraw : scrawsMap.values()) {
            if (scraw.isAlliance()) {
                allianceScrawsContents += String.format("<String value=\"%s.scraw\"/>%n", scraw.getRecipeName());
                allianceScrawsData += String.format("<DataScraw rootEvent=\"%d\" fx:id=\"s%s\" fx:factory=\"bytesArray\">%n", scraw.getRootEvent(), scraw.getScrawNumber());
                for (ArrayList<Byte> array : scraw.getData()) {
                    allianceScrawsData += String.format("<DataScraw fx:factory=\"byteArray\">%n");
                    for (Byte bite : array) {
                        allianceScrawsData += String.format("<Byte value=\"%d\"/>%n", bite);
                    }
                    allianceScrawsData += String.format("</DataScraw>%n");
                }
                allianceScrawsData += String.format("</DataScraw>%n");
                allianceScrawDecs += String.format("<DataScraw scraw\"%s\" data=\"$s%s\"/>%n", scraw.getRecipeName(), scraw.getScrawNumber());
            } else {
                teamScrawsContents += String.format("<String value=\"%s.scraw\"/>%n", scraw.getRecipeName());
                teamScrawsData += String.format("<DataScraw rootEvent=\"%d\" fx:id=\"s%d\" fx:factory=\"bytesArray\">%n", scraw.getRootEvent(), scraw.getScrawNumber());
                for (ArrayList<Byte> array : scraw.getData()) {
                    teamScrawsData += String.format("<DataScraw fx:factory=\"byteArray\">%n");
                    for (Byte bite : array) {
                        teamScrawsData += String.format("<Byte value=\"%d\"/>%n", bite);
                    }
                    teamScrawsData += String.format("</DataScraw>%n");
                }
                teamScrawsData += String.format("</DataScraw>%n");
                teamScrawDecs += String.format("<DataScraw scraw\"%s\" data=\"$s%s\"/>%n", scraw.getRecipeName(), scraw.getScrawNumber());
            }
        }
        if (!allianceScrawsContents.isEmpty()) {
            allianceScraws.createNewFile();
            new ObjectOutputStream(new FileOutputStream(allianceScraws)).writeUTF(String.format(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>%n" +
                            "<?import connectionIndependent.Scrawings.RemoteLoader?>%n" +
                            "<?import java.lang.String?>%n" +
                            "<?import connectionIndependent.Scrawings.scrawtypes.DataScraw?>%n" +
                            "<?import java.lang.Byte?>%n" +
                            "<Pane xmlns=\"http://javafx.com/javafx\" xmlns:fx=\"http://javafx.com/fxml\">%n" +
                            "<fx:define>%n" +
                            "<RemoteLoader>%n" +
                            "<fileLocs>%n" +
                            "%s" +
                            "</fileLocs>%n" +
                            "</RemoteLoader>%n" +
                            "%s" +
                            "</fx:define>%n" +
                            "<ListView>%n" +
                            "%s" +
                            "</ListView>%n" +
                            "</Pane>"
                    , allianceScrawsContents, allianceScrawsData, allianceScrawDecs));
        }
        teamScraws.createNewFile();
        new ObjectOutputStream(new FileOutputStream(teamScraws)).writeUTF(String.format(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>%n" +
                        "<?import connectionIndependent.Scrawings.RemoteLoader?>%n" +
                        "<?import java.lang.String?>%n" +
                        "<?import connectionIndependent.Scrawings.scrawtypes.DataScraw?>%n" +
                        "<?import java.lang.Byte?>%n" +
                        "<Pane xmlns=\"http://javafx.com/javafx\" xmlns:fx=\"http://javafx.com/fxml\">%n" +
                        "<fx:define>%n" +
                        "<RemoteLoader>%n" +
                        "<fileLocs>%n" +
                        "%s" +
                        "</fileLocs>%n" +
                        "</RemoteLoader>%n" +
                        "%s" +
                        "</fx:define>%n" +
                        "<ListView>%n" +
                        "%s" +
                        "</ListView>%n" +
                        "</Pane>"
                , teamScrawsContents, teamScrawsData, teamScrawDecs));

    }

    public boolean isEditing() {
        return isEditing;
    }
}