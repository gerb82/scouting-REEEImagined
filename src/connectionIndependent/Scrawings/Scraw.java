package connectionIndependent.Scrawings;

import javafx.scene.Group;

import java.util.ArrayList;
import java.util.HashMap;

/* TODO:
    finish this class, and the Scraws system in general
    dynamic text and colors on Scraws
    flags {start, optional, abstract value}
 */
public class Scraw extends Group {

    private byte scrawNumber = -1;

    private HashMap<Byte, Byte[]> shapesToEvents;
    private HashMap<Byte, Boolean> shapeFlags;

    public void setEventData(ArrayList<Byte[]> events){
        shapesToEvents = new HashMap<>();
        for(byte i = 0; i<getNumberedShapes(); i++){
            shapesToEvents.put(i, events.get((int) i));
        }
    }

    public void setScrawFlags(ArrayList<Boolean> flags){
        shapeFlags = new HashMap<>();
        for(byte i = 0; i<getNumberedShapes(); i++){
            shapeFlags.put(i, flags.get((int) i));
        }
    }

    public byte getScrawNumber() {
        return scrawNumber;
    }

    public void setScrawNumber(byte scrawNumber) {
        this.scrawNumber = scrawNumber;
    }

    public String toFXML(String content) {
        return "";
    }

    public int getNumberedShapes(){
        return 0;
    }
}
