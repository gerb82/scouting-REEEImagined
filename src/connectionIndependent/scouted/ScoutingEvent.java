package connectionIndependent.scouted;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;

public class ScoutingEvent implements Serializable {

    private static final long serialVersionUID = 1001L;

    // TODO Add comment serialization and loading from database
    public static class EventTimeStamp {

        private EventTimeStamp(byte type, Short timeStamp) {
            this.type = type;
            this.timeStamp = timeStamp;
        }

        private byte type;
        private Short timeStamp;

        public byte getType() {
            return type;
        }

        public Short getTimeStamp() {
            return timeStamp;
        }

        @Override
        public String toString() {
            return type + (timeStamp == null ? "" : " " + timeStamp);
        }
    }

    private transient ArrayList<EventTimeStamp> timeStamps = new ArrayList<>();
    private transient ArrayList<ScouterCommentEvent> comments = new ArrayList<>();
    private transient int chainID = -1;

    public byte getType() {
        return timeStamps.get(0).type;
    }

    public Short getFirstTimeStamp() {
        return timeStamps.get(0).timeStamp;
    }

    public void addProgress(byte type, Short timeStamp) {
        if (timeStamp != null && getLastStamp() != null) {
            if (timeStamp < getLastStamp()) {
                throw new IllegalArgumentException("Cannot set the current timestamp to be before the last timestamp!");
            }
        }
        timeStamps.add(new EventTimeStamp(type, timeStamp));
    }

    private Short getLastStamp(){
        Short result = null;
        for (EventTimeStamp stamp : timeStamps){
            if(stamp.timeStamp != null){
                if(result != null) {
                    if (stamp.getTimeStamp() > result) {
                        result = stamp.timeStamp;
                    }
                } else {
                    result = stamp.timeStamp;
                }
            }
        }
        return result;
    }

    public Short getCertainProgressTime(short step) {
        return timeStamps.get(step).timeStamp;
    }

    public short getCertainProgressType(short step) {
        return timeStamps.get(step).type;
    }

    public void setChainID(int chainID) {
        this.chainID = chainID;
    }

    public int getChainID() {
        return this.chainID;
    }

    public void resetFromCertainStep(short step) {
        while (timeStamps.size() >= step) {
            timeStamps.remove(step);
        }
    }

    public int getSize() {
        return timeStamps.size();
    }

    public byte getLastType() {
        return timeStamps.get(timeStamps.size() - 1).type;
    }

    public void removeLast() {
        timeStamps.remove(timeStamps.size() - 1);
    }

    public ArrayList<EventTimeStamp> getStamps() {
        return timeStamps;
    }

    public void addRelatedComment(ScouterCommentEvent comment){
        comments.add(comment);
    }

    public ArrayList<ScouterCommentEvent> getComments(){
        return comments;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeInt(chainID);
        stream.writeByte(timeStamps.size());
        for (short i = 0; timeStamps.size() > i; i += 8) {
            short amount = (short) (timeStamps.size() % 8);
            BitSet set = BitSet.valueOf(new byte[]{Byte.MIN_VALUE});
            for (short j = 0; amount > j; j++) {
                if (timeStamps.get(i + j).timeStamp != null) {
                    set.set(j);
                }
            }
            stream.writeByte(set.toByteArray()[0]);
            for (short j = 0; amount > j; j++) {
                stream.writeByte(timeStamps.get(i + j).type);
                if (timeStamps.get(i + j).timeStamp != null) {
                    stream.writeShort(timeStamps.get(i + j).timeStamp);
                }
            }
        }
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        chainID = stream.readInt();
        byte timeStampsCount = stream.readByte();
        timeStamps = new ArrayList<>();
        for (short i = 0; timeStampsCount > i; i += 8) {
            short amount = (short) (timeStampsCount % 8);
            BitSet signifier = BitSet.valueOf(new byte[]{stream.readByte()});
            for (short j = 0; amount > j; j++) {
                if (signifier.get(j)) {
                    timeStamps.add(new EventTimeStamp(stream.readByte(), stream.readShort()));
                } else {
                    timeStamps.add(new EventTimeStamp(stream.readByte(), null));
                }
            }
        }
    }
}