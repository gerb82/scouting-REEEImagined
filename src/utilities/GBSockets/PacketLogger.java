package utilities.GBSockets;

import javafx.beans.property.SimpleObjectProperty;

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;

public class PacketLogger implements AutoCloseable{

    private class PacketMap extends HashMap<String, LogLine>{

        private LogLine getLine(boolean wasSent, Integer[] ids){
            return get((wasSent ? "out" : "in") + ids.toString());
        }

        private void putLine(boolean wasSent, Integer[] ids, LogLine line){
            put((wasSent ? "out" : "in") + ids.toString(), line);
        }

    }

    private HashSet<Integer[]> receivedPackets;
    private HashSet<Integer[]> sentPackets;
    private PacketMap packets = new PacketMap();
    private FileOutputStream logFile;

    @Override
    public void close() throws Exception {
        logFile.close();
    }

    public enum PacketStatus{
        ACKED, ERRORED, WAITING, TIMED_OUT, TO_BE_SYNCED
    }

    protected class LogLine{

        private PacketStatus status;
        private Packet packet;

        protected void discardToLog(){

        }

        protected PacketStatus getStatus(){
            return status;
        }

        protected Packet getPacket(){
            return packet;
        }
    }

    public static class ObservablePacketStatus extends SimpleObjectProperty<PacketStatus> {

        private LogLine parent;

        public ObservablePacketStatus(LogLine parent){
            this.parent = parent;
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

    protected HashSet<Packet> getToBeSyncedPackets(){
        HashSet<Packet> output = new HashSet<>();
        for(Integer[] ids : sentPackets){
            LogLine inCheck = packets.getLine(true, ids);
            if(inCheck.getStatus() == PacketStatus.TO_BE_SYNCED){
                output.add(inCheck.getPacket());
            }
        }
        return output;
    }

    protected HashSet<Packet> getTimedOutPackets(){
        HashSet<Packet> output = new HashSet<>();
        for(Integer[] ids : sentPackets){
            LogLine inCheck = packets.getLine(true, ids);
            if(inCheck.getStatus() == PacketStatus.TIMED_OUT){
                output.add(inCheck.getPacket());
            }
        }
        return output;
    }

    protected HashSet<Packet> getErroredPackets(){
        HashSet<Packet> output = new HashSet<>();
        for(Integer[] ids : sentPackets){
            LogLine inCheck = packets.getLine(true, ids);
            if(inCheck.getStatus() == PacketStatus.ERRORED){
                output.add(inCheck.getPacket());
            }
        }
        return output;
    }

    protected HashSet<Packet> getAckedPackets(){
        HashSet<Packet> output = new HashSet<>();
        for(Integer[] ids : sentPackets){
            LogLine inCheck = packets.getLine(true, ids);
            if(inCheck.getStatus() == PacketStatus.ACKED){
                output.add(inCheck.getPacket());
            }
        }
        return output;
    }

    protected HashSet<Packet> getWaitingPackets(){
        HashSet<Packet> output = new HashSet<>();
        for(Integer[] ids : sentPackets){
            LogLine inCheck = packets.getLine(true, ids);
            if(inCheck.getStatus() == PacketStatus.WAITING){
                output.add(inCheck.getPacket());
            }
        }
        return output;
    }
}