package connectionIndependent.Scrawings;

import javafx.scene.Group;

public class Scraw extends Group{

    private byte scrawNumber = -1;


    public byte getScrawNumber() {
        return scrawNumber;
    }

    public void setScrawNumber(byte scrawNumber) {
        this.scrawNumber = scrawNumber;
    }

    public String toFXML(){

    }
}
