package connectionIndependent;

import java.io.*;
import java.util.ArrayList;
import java.util.Stack;

public class ScoutingEvent implements Serializable{

    private static class EventTimeStamp{

        private EventTimeStamp(byte type, Short timeStamp) {
            this.type = type;
            this.timeStamp = timeStamp;
        }

        private byte type;
        private Short timeStamp;
    }

    private ArrayList<EventTimeStamp> timeStamps = new ArrayList<>();

    public ScoutingEvent(byte type, Short timeStamp) {
        timeStamps.add(new EventTimeStamp(type, timeStamp));
    }

    public int getType() {
        return timeStamps.get(0).type;
    }

    public Short getTimeStamp() {
        return timeStamps.get(0).timeStamp;
    }

    public void addProgress(byte type, Short timeStamp){
        timeStamps.add(new EventTimeStamp(type, timeStamp));
    }

    public Short getCertainProgressTime(short step){
        return timeStamps.get(step).timeStamp;
    }

    public short getCertainProgressType(short step){
        return timeStamps.get(step).type;
    }

    public void resetFromCertainStep(short step){
        while(timeStamps.size() >= step){
            timeStamps.remove(step);
        }
    }

    private void writeObject(ObjectOutputStream stream) throws IOException{
        stream.writeByte(timeStamps.size());
        for(short i = 0; timeStamps.size() > i; i += 8){
            short amount = (short)(timeStamps.size()%8);
            byte signifier = 0;
            for(short j = 0; amount > j; j++){
                if(timeStamps.get(i+j).timeStamp != null){
                    signifier++;
                }
                signifier = (byte)(signifier << 1);
            }
            stream.writeByte(signifier);
            for(short j = 0; amount > j; j++){
                stream.writeByte(timeStamps.get(i+j).type);
                if(timeStamps.get(i+j).timeStamp != null){
                    stream.writeShort(timeStamps.get(i+j).timeStamp);
                }
            }
        }
    }

    private void readObject(ObjectInputStream stream) throws IOException{
        byte timeStampsCount = stream.readByte();
        timeStamps = new ArrayList<>();
        for(short i = 0; timeStampsCount > i; i += 8){
            short amount = (short)(timeStampsCount%8);
            byte signifier = (byte)(stream.readByte()<<7-amount);
            for(short j = 0; amount > j; j++){
                if(signifier>>7 == 1){
                    timeStamps.add(new EventTimeStamp(stream.readByte(), stream.readShort()));
                } else {
                    timeStamps.add(new EventTimeStamp(stream.readByte(), null));
                }
            }
        }
    }
}