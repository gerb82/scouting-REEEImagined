package connectionIndependent;

import java.io.Serializable;

public class ScoutingEvent implements Serializable{

    private int type;
    private ScoutingEvent contained;
    private Integer timeStamp;

    public ScoutingEvent(int type, Integer timeStamp) {
        this.type = type;
        this.timeStamp = timeStamp;
    }

    public ScoutingEvent(ScoutingEvent event) {
        this.type = event.type;
        this.timeStamp = new Integer(event.timeStamp);
        if(event.contained != null){
            this.contained = new ScoutingEvent(event.contained);
        }
    }

    public int getType() {
        return type;
    }

    public ScoutingEvent getContained() {
        return contained;
    }

    public Integer getTimeStamp() {
        return timeStamp;
    }

    public void setContained(ScoutingEvent contained) {
        setContained(this, contained);
    }

    private void setContained(ScoutingEvent self, ScoutingEvent contained){
        if(self.getContained() == null) {
            self.setContained(contained);
        } else {
            setContained(self.getContained(), contained);
        }
    }
}
