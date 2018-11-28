package utilities.GBSockets;

import javafx.beans.property.SimpleObjectProperty;

import java.io.FileOutputStream;

public class PacketLogger implements AutoCloseable{

    private FileOutputStream logFile;

    @Override
    public void close() throws Exception {
        logFile.close();
    }

    public enum PacketStatus{
        ACKED, ERRORED, WAITING, TIMED_OUT, TO_BE_SYNCED
    }

    protected class LogLine{

        protected void discardToLog(){

        }
    }

    public static class ObservablePacketStatus extends SimpleObjectProperty<PacketStatus> {

        private LogLine parent;

        public ObservablePacketStatus(LogLine parent){
            this.parent = parent;
        }

        public void finalize(){
            parent.discardToLog();
        }

        public void discardToLog(){
            parent.discardToLog();
        }
    }

    protected PacketLogger(){

    }

    protected ObservablePacketStatus getLivePacketStatus(int[] packetIDs){
        return null;
    }

    protected Packet[] getToBeSyncedPackets(){
        return null;
    }

    protected Packet[] getTimedOutPackets(){
        return null;
    }

    protected Packet[] getErroredPackets(){
        return null;
    }

    protected Packet[] getAckedPackets(){
        return null;
    }

    protected Packet[] getWaitingPackets(){
        return null;
    }
}