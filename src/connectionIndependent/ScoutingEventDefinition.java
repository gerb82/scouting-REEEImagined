package connectionIndependent;

import java.io.Serializable;

public class ScoutingEventDefinition implements Serializable{

    private ScoutingEventDefinition[] nextStamps;
    private byte name;
    private boolean starts;
    private String textName;

    public ScoutingEventDefinition(ScoutingEventDefinition[] nextStamps, byte name, boolean starts, String textName) {
        this.nextStamps = nextStamps;
        this.name = name;
        this.starts = starts;
        this.textName = textName;
    }

    public ScoutingEventDefinition[] getNextStamps() {
        return nextStamps;
    }

    public short getName() {
        return name;
    }

    public boolean doesStart() {
        return starts;
    }

    public String getTextName() {
        return textName;
    }
}
