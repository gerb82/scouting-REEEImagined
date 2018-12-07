package utilities.GBSockets;

import javafx.beans.property.SimpleObjectProperty;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;

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
    protected LogLine packetAlreadyReceived(Packet packet){
        LogLine origin = packets.getLine(false, packet.getIds());
        origin.setStatus(origin.getStatus());
        return origin.getResponse() != null ? origin : null;
    }

    // done
    protected LogLine setResponse(int[] ids, Packet packet){
        packets.getLine(false, ids).setResponse(packet);
        return packets.getLine(false, ids);
    }

    // done
    protected LogLine beat(Packet packet){
        LogLine line = new LogLine(packet, true);
        line.setStatus(PacketStatus.SEND_READY);
        return line;
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
    private ObjectOutputStream logFile;
    protected static File logsRepository;

    @Override
    public void close() throws Exception {
        logFile.close();
    }

    public enum PacketStatus{
        SEND_READY, ACKED, SEND_ERRORED, WAITING, TIMED_OUT,
        RECEIVED, RECEIVED_ERRORED, RECEIVED_DONE
    }

    protected class LogLine{

        private ObservablePacketStatus status;
        private Packet packet;
        private Packet response;
        private boolean wasSent;
        private int attemptsLeftToSend;

        private LogLine(Packet packet, boolean wasSent){
            this.packet = packet;
            this.wasSent = wasSent;
            this.status = new ObservablePacketStatus(this);
        }

        public void discardToLog() {
            packets.remove((wasSent ? "out" : "in") + packet.getIds());
            try {
                logFile.writeUTF("Packet " + PacketManager.formatPacketIDs(packet.getIds(), packet.getPacketType()) + ", was " + (wasSent ? "sent" : "received") + ", on " + packet.getTimeStamp() + ". The final packet status was: " + status + ". The serialized packet was: " + packet + ", and " + (response != null ? (response.getContent().getClass().isAssignableFrom(Packet.class) ? "the response was the packet with the ids: " + response.getIds() : "the response was the packet: " + response) : "there was no response") + ".");
            } catch (IOException e) {
                e.printStackTrace();
            }
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

        protected boolean getWasSent(){
            return wasSent;
        }

        protected int getAttemptsLeftToSend(){
            return attemptsLeftToSend;
        }

        protected void setAttemptsLeftToSend(int i){
            attemptsLeftToSend = i;
        }

        protected ObservablePacketStatus getStatusProperty(){
            return status;
        }
    }

    public class ObservablePacketStatus extends SimpleObjectProperty<PacketStatus> {
        private LogLine parent;

        protected ObservablePacketStatus(LogLine parent){
            this.parent = parent;
        }

        protected LogLine getParent(){
            return parent;
        }

    }

    protected PacketLogger(GBSocket socket){
        this.socket = socket;
    }

    // done
    protected LogLine followPacket(Packet packet, boolean sent){
        LogLine logLine = new LogLine(packet, sent);
        packets.putLine(sent, packet.getIds(), logLine);
        logLine.setStatus(sent ? PacketStatus.SEND_READY : PacketStatus.RECEIVED);
        return logLine;
    }
}