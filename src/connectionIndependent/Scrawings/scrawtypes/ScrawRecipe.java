package connectionIndependent.Scrawings.scrawtypes;

import connectionIndependent.Scrawings.RemoteRecipe;
import connectionIndependent.Scrawings.hitboxes.MyCircGroup;
import connectionIndependent.Scrawings.hitboxes.MyPolyGroup;
import connectionIndependent.Scrawings.hitboxes.PossibleHitBox;
import javafx.scene.Group;
import javafx.scene.Node;

/* TODO:
    finish this class, and the Scraws system in general
    dynamic text and colors on Scraws
    flags {start, optional, abstract value}
 */
public class ScrawRecipe extends Group implements RemoteRecipe {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ScrawRecipe replicate() {
        ScrawRecipe output = new ScrawRecipe();
        output.setName(name);
        for (Node node : getChildren()) {
            if (node instanceof PossibleHitBox) {
                PossibleHitBox newShape = null;
                if (node instanceof MyCircGroup) newShape = ((MyCircGroup) node).paste(((MyCircGroup) node).getCircle().getCenterX(), ((MyCircGroup) node).getCircle().getCenterY());
                else if (node instanceof MyPolyGroup) newShape = ((MyPolyGroup) node).paste(node.getLayoutX(), node.getLayoutY());
                newShape.setHitboxId(((PossibleHitBox) node).getHitboxId());
                output.getChildren().add((Node) newShape);
            }
        }
        return output;
    }

    public String toFXML(String params) {
        String children = "";
        for (Node node : getChildren()) {
            if (node instanceof PossibleHitBox) {
                children += ((PossibleHitBox) node).toFXML();
            }
        }
        return String.format("ScrawRecipe name=\"%s\" %s>%n%s</ScrawRecipe>", name, params, children);
    }

}
