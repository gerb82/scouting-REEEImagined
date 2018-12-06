package utilities.GBSockets;

import javafx.beans.property.SimpleObjectProperty;
import utilities.GBUILibGlobals;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;

public class PacketLogger implements AutoCloseable{

    private final GBSocket socket;
    
    // done
    protected boolean isPacketFollowed(Packet packet){
        return packets.getLine(false, packet.getIds()) == null;
    }
    
    // done
    protected void packetDidntSend(Packet packet){
        packets.getLine(true, packet.getIds()).setStatus(PacketStatus.READY);
    }

    protected class PacketMap extends HashMap<String, LogLine>{

        protected LogLine getLine(boolean wasSent, int[] ids){
            return get((wasSent ? "out" : "in") + ids.toString());
        }

        private void putLine(boolean wasSent, int[] ids, LogLine line){
            put((wasSent ? "out" : "in") + ids.toString(), line);
        }

    }

    private HashSet<int[]> receivedPackets;
    private HashSet<int[]> sentPackets;
    protected PacketMap packets = new PacketMap();
    private FileOutputStream logFile;
    protected static File logsRepository;

    @Override
    public void close() throws Exception {
        logFile.close();
    }

    public enum PacketStatus{
        READY, ACKED, ERRORED, WAITING, TIMED_OUT, TO_BE_SYNCED, RECEIVED
    }

    protected class LogLine{

        private ObservablePacketStatus status;
        private Packet packet;

        private LogLine(Packet packet){
            this.packet = packet;
            this.status = new ObservablePacketStatus(this);
        }

        public void discardToLog(){

        }

        public PacketStatus getStatus(){
            return status.getValue();
        }

        protected Packet getPacket(){
            return packet;
        }
        
        protected void setStatus(PacketStatus status){
            this.status.set(status);
        }
    }

    public class ObservablePacketStatus extends SimpleObjectProperty<PacketStatus> {

        private LogLine parent;

        @Override
        public void set(PacketStatus newValue){
            switch (newValue){
                case TIMED_OUT:
                    if(parent.getPacket().getResend()){
                        newValue = PacketStatus.READY;
                        break;
                    }
                case ACKED:
                case ERRORED:
                    super.set(newValue);
                    if(GBUILibGlobals.getAutoDiscardFinishedPackets()){
                        parent.discardToLog();
                    }
                    break;
                case WAITING:
                case TO_BE_SYNCED:
                    super.set(newValue);
                    break;
                case READY:
                    super.set(socket.sendPacket(parent.getPacket()));
            }
        }

        protected ObservablePacketStatus(LogLine parent){
            this.parent = parent;
        }

        protected LogLine getParent() {
            return parent;
        }
    }

    protected PacketLogger(GBSocket socket){
        this.socket = socket;
    }

    // done
    protected void followPacket(PacketStatus status, Packet packet, boolean sent){
        LogLine logLine = new LogLine(packet);
        packets.putLine(sent, packet.getIds(), logLine);
        logLine.setStatus(status);
    }

    // done
    protected HashSet<Packet> getToBeSyncedPackets(){
        HashSet<Packet> output = new HashSet<>();
        for(int[] ids : sentPackets){
            LogLine inCheck = packets.getLine(true, ids);
            if(inCheck.getStatus() == PacketStatus.TO_BE_SYNCED){
                output.add(inCheck.getPacket());
            }
        }
        return output;
    }

    // done
    protected HashSet<Packet> getTimedOutPackets(){
        HashSet<Packet> output = new HashSet<>();
        for(int[] ids : sentPackets){
            LogLine inCheck = packets.getLine(true, ids);
            if(inCheck.getStatus() == PacketStatus.TIMED_OUT){
                output.add(inCheck.getPacket());
            }
        }
        return output;
    }

    // done
    protected HashSet<Packet> getErroredPackets(){
        HashSet<Packet> output = new HashSet<>();
        for(int[] ids : sentPackets){
            LogLine inCheck = packets.getLine(true, ids);
            if(inCheck.getStatus() == PacketStatus.ERRORED){
                output.add(inCheck.getPacket());
            }
        }
        return output;
    }

    // done
    protected HashSet<Packet> getAckedPackets(){
        HashSet<Packet> output = new HashSet<>();
        for(int[] ids : sentPackets){
            LogLine inCheck = packets.getLine(true, ids);
            if(inCheck.getStatus() == PacketStatus.ACKED){
                output.add(inCheck.getPacket());
            }
        }
        return output;
    }

    // done
    protected HashSet<Packet> getWaitingPackets(){
        HashSet<Packet> output = new HashSet<>();
        for(int[] ids : sentPackets){
            LogLine inCheck = packets.getLine(true, ids);
            if(inCheck.getStatus() == PacketStatus.WAITING){
                output.add(inCheck.getPacket());
            }
        }
        return output;
    }
}