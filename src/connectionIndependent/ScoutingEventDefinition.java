package connectionIndependent;

import java.io.Serializable;

public class ScoutingEventDefinition implements Serializable{

    private byte[] nextStamps;
    private byte name;
    private boolean starts;
    private String textName;

    public ScoutingEventDefinition(byte[] nextStamps, byte name, boolean starts, String textName) {
        this.nextStamps = nextStamps;
        this.name = name;
        this.starts = starts;
        this.textName = textName;
    }

    public byte[] getNextStamps() {
        return nextStamps;
    }

    public byte getName() {
        return name;
    }

    public boolean doesStart() {
        return starts;
    }

    public String getTextName() {
        return textName;
    }
}
