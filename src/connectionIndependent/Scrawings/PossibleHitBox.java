package connectionIndependent.Scrawings;

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.ContextMenuEvent;

import java.util.ArrayList;

public interface PossibleHitBox {
    ArrayList<Byte> bytes = new ArrayList<>();

    default ArrayList<Byte> getBytes() {
        return bytes;
    }

    default void setContextMenu(ContextMenu menu) {
        ((Group) this).setOnContextMenuRequested(event -> {
            menu.show((Group) this, event.getScreenX(), event.getScreenY());
            event.consume();
        });
    }
}
