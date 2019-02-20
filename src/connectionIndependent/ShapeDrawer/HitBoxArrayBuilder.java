package connectionIndependent.ShapeDrawer;

import java.util.ArrayList;

public class HitBoxArrayBuilder {

    ArrayList<HitBox> hitBoxes = new ArrayList<>();

    public ArrayList<HitBox> getHitBoxes() {
        return hitBoxes;
    }

    public void setHitBoxes(HitBox hitBox) {
        hitBoxes.add(hitBox);
    }

    public HitBox[] build() {
        HitBox[] hitBoxes1 = new HitBox[hitBoxes.size()];

        for (int i = 0; i < hitBoxes1.length; i++) {
            hitBoxes1[i] = hitBoxes.get(i);
        }

        return hitBoxes1;
    }
}
