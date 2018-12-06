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
    protected boolean isPacketFollowedOut(Packet packet){
        return packets.getLine(true, packet.getIds()) == null;
    }

    // done
    protected boolean isPacketFollowedIn(Packet packet) {
        return packets.getLine(false, packet.getIds()) == null;
    }

    // done
    protected Packet packetAlreadyReceived(Packet packet){
        LogLine origin = packets.getLine(false, packet.getIds());
        return origin.getResponse() != null ? origin.getResponse() : null;
    }

    // done
    protected void setResponse(int[] ids, Packet packet){
        packets.getLine(false, ids).setResponse(packet);
    }


    // done
    protected void beat(int[] ids){

    }

    protected void packetReturned(Packet packet){
        LogLine origin = packets.getLine(true, packet.getIds());
        origin.setResponse(packet);
        origin.setStatus(PacketStatus.valueOf(packet.getPacketType()));
    }

    protected class PacketMap extends HashMap<String, LogLine>{

        protected LogLine getLine(boolean wasSent, int[] ids){
            return get((wasSent ? "out" : "in") + ids.toString());
        }

        private void putLine(boolean wasSent, int[] ids, LogLine line){
            put((wasSent ? "out" : "in") + ids.toString(), line);
        }

    }

    protected PacketMap packets = new PacketMap();
    private FileOutputStream logFile;
    protected static File logsRepository;

    @Override
    public void close() throws Exception {
        logFile.close();
    }

    public enum PacketStatus{
        READY, ACKED, ERRORED, WAITING, TIMED_OUT,
        RECEIVED
    }

    protected class LogLine{

        private ObservablePacketStatus status;
        private Packet packet;
        private Packet response;

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

        protected void setResponse(Packet packet){
            this.response = packet;
        }

        protected Packet getResponse(){
            return response;
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
}