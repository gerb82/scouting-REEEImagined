package connectionIndependent.Scrawings.hitboxes;

import javafx.scene.Group;
import javafx.scene.control.ContextMenu;

import java.util.ArrayList;

public interface PossibleHitBox {
    ArrayList<Byte> bytes = new ArrayList<>();

    default ArrayList<Byte> getBytes() {
        return bytes;
    }

    byte getHitboxId();
    void setHitboxId(byte newId);

    default void setContextMenu(ContextMenu menu) {
        if(menu == null) ((Group) this).setOnContextMenuRequested(null);
        else ((Group) this).setOnContextMenuRequested(event -> {
            menu.show((Group) this, event.getScreenX(), event.getScreenY());
            event.consume();
        });
    }

    String toFXML();

    <T extends PossibleHitBox> T paste(double locX, double locY);
}
