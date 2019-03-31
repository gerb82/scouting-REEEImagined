package connectionIndependent.scrawings.hitboxes;

import javafx.scene.Group;
import javafx.scene.control.ContextMenu;
import javafx.scene.paint.Color;

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

    default String fillToRGB(Color color){
        return "#" + hexFixer(color.getRed()) + hexFixer(color.getGreen()) + hexFixer(color.getBlue());
    }

    default String hexFixer(double color){
        String s = Integer.toHexString((int) (color*255));
        if(s.length() == 1) return "0" + s;
        return s;
    }
}
