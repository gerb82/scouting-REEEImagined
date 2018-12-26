package connectionIndependent;

import java.io.Serializable;

public class ScoutingEventDefinition implements Serializable{

    private ScoutingEventDefinition[] contained;
    private int name;
    private boolean starts;
    private String textName;

    public ScoutingEventDefinition(ScoutingEventDefinition[] contained, int name, boolean starts, String textName) {
        this.contained = contained;
        this.name = name;
        this.starts = starts;
        this.textName = textName;
    }

    public ScoutingEventDefinition[] getContained() {
        return contained;
    }

    public int getName() {
        return name;
    }

    public boolean doesStart() {
        return starts;
    }

    public String getTextName() {
        return textName;
    }
}
