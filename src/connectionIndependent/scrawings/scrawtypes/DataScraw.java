package connectionIndependent.scrawings.scrawtypes;

import connectionIndependent.scrawings.ScrawingsManager;
import connectionIndependent.scrawings.hitboxes.PossibleHitBox;
import javafx.beans.DefaultProperty;
import javafx.beans.NamedArg;
import javafx.scene.Group;
import javafx.scene.Node;

import java.util.ArrayList;

public class DataScraw extends Group {

    private byte scrawNumber = -1;
    private ArrayList<ArrayList<Byte>> data;
    private byte rootEvent;

    public byte getRootEvent() {
        return rootEvent;
    }

    public void setRootEvent(byte rootEvent) {
        this.rootEvent = rootEvent;
    }

    public ArrayList<ArrayList<Byte>> getData() {
        return data;
    }

    public String getRecipeName() {
        try {
            return ((ScrawRecipe) getChildren().get(0)).getName();
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public void setRecipeName(String recipeName) {
        getChildren().clear();
        getChildren().add(ScrawingsManager.getInstance().getRecipesList().filtered(scrawRecipe -> scrawRecipe.getName().equals(recipeName)).get(0).replicate());
    }

    public byte getScrawNumber() {
        return scrawNumber;
    }

    public void setScrawNumber(byte scrawNumber) {
        this.scrawNumber = scrawNumber;
    }

    public DataScraw(@NamedArg("scraw") ScrawRecipe scraw, @NamedArg("data") BytesArray data, @NamedArg("rootEvent") byte root) {
        rootEvent = root;
        getChildren().add(scraw.replicate());
        this.data = data;
        if(!data.isEmpty()) {
            for (Node node : scraw.getChildren()) {
                if (node instanceof PossibleHitBox) {
                    if (((PossibleHitBox) node).getHitboxId() > -1) {
                        ((PossibleHitBox) node).getBytes().addAll(data.get(((PossibleHitBox) node).getHitboxId()));
                    }
                }
            }
        }
    }

    public static ByteArray byteArray() {
        return new ByteArray();
    }

    @DefaultProperty("Values")
    public static class ByteArray extends ArrayList<Byte> {

        public ByteArray getValues() {
            return this;
        }
    }

    public static BytesArray bytesArray() {
        return new BytesArray();
    }

    @DefaultProperty("Values")
    public static class BytesArray extends ArrayList<ArrayList<Byte>> {

        public BytesArray getValues() {
            return this;
        }
    }
}
