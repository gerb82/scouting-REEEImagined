package connectionIndependent.ShapeDrawer;

import java.util.ArrayList;

public interface PossibleHitBox {
    ArrayList<Byte> bytes = new ArrayList<>();

    default ArrayList<Byte> getBytes(){
        return bytes;
    }
}
