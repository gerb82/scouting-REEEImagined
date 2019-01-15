package connectionIndependent;

public class EventGroup {

    private byte[] containedEvents;
    private byte groupID;
    private String groupName;

    public EventGroup(byte[] containedEvents, byte groupID, String groupName) {
        this.containedEvents = containedEvents;
        this.groupID = groupID;
        this.groupName = groupName;
    }

    public byte[] getContainedEvents() {
        return containedEvents;
    }

    public void setContainedEvents(byte[] containedEvents) {
        this.containedEvents = containedEvents;
    }

    public byte getGroupID() {
        return groupID;
    }

    public void setGroupID(byte groupID) {
        this.groupID = groupID;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
