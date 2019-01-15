package connectionIndependent;

import java.io.Serializable;

public class ScoutingEventDefinition implements Serializable{

    private byte[] nextStamps;
    private byte name;
    private boolean stamp;
    private String textName;

    public ScoutingEventDefinition(byte[] nextStamps, byte name, boolean stamp, String textName) {
        this.nextStamps = nextStamps;
        this.name = name;
        this.stamp = stamp;
        this.textName = textName;
    }

    public byte[] getNextStamps() {
        return nextStamps;
    }

    public byte getName() {
        return name;
    }

    public boolean followStamp() {
        return stamp;
    }

    public String getTextName() {
        return textName;
    }
}
