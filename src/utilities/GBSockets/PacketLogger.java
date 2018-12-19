package utilities.GBSockets;

import javafx.beans.property.SimpleObjectProperty;
import utilities.GBUILibGlobals;

import java.io.*;
import java.net.SocketAddress;
import java.time.Instant;
import java.util.HashMap;

public class PacketLogger implements AutoCloseable{

    private final GBSocket socket;

    // done
    protected boolean isPacketFollowedOut(Packet packet){
        return packets.getLine(true, packet.getIds()) != null;
    }

    // done
    protected boolean isPacketFollowedIn(Packet packet) {
        return packets.getLine(false, packet.getIds()) != null;
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
            return get((wasSent ? "out" : "in") + arrayToString(ids));
        }

        private void putLine(boolean wasSent, int[] ids, LogLine line){
            put((wasSent ? "out" : "in") + arrayToString(ids), line);
        }

    }

    private static String arrayToString(int[] ids){
        String output = Integer.toString(ids[0]);
        for (int i = 1; i<ids.length; i++){
            output += "," + Integer.toString(ids[i]);
        }
        return output;
    }

    protected PacketMap packets = new PacketMap();
    private ObjectOutputStream logFileStream = null;
    protected static File logsRepository;
    protected File logFile;

    @Override
    public void close() throws IOException {
        for (String key : packets.keySet()){
            packets.get(key).discardToLog();
        }
        if(logFileStream != null){
            logFileStream.close();
        }
    }

    public enum PacketStatus{
        SEND_READY, ACKED, SEND_ERRORED, WAITING, TIMED_OUT, SENT,
        RECEIVED, RECEIVED_ERRORED, RECEIVED_DONE
    }

    protected class LogLine{

        private ObservablePacketStatus status;
        private Packet packet;
        private Packet response;
        private boolean wasSent;
        private int attemptsLeftToSend;
        private int initialAttemptsAmount;

        private LogLine(Packet packet, boolean wasSent){
            this.packet = packet;
            this.wasSent = wasSent;
            this.status = new ObservablePacketStatus(this);
        }

        public void discardToLog() {
            packets.remove((wasSent ? "out" : "in") + packet.getIds());
            if(writeToLog && logFileStream != null) {
                try {
                    logFileStream.writeUTF("The serialized packet was: ");
                    logFileStream.writeObject(packet);
                    logFileStream.writeUTF(", and ");
                    if(response != null) {
                        if(response.getContent() != null) {
                            if (response.getContent().getClass().isAssignableFrom(Packet.class)) {
                                logFileStream.writeUTF("the response was the packet with the ids: " + response.getIds());
                            } else {
                                logFileStream.writeUTF("the response was the packet: ");
                                logFileStream.writeObject(response);
                            }
                        }
                    } else {
                        logFileStream.writeUTF("there was no response");
                    }
                    logFileStream.writeUTF(". The packet " + PacketManager.formatPacketIDs(packet.getIds(), packet.getPacketType(), socket.programWideSocketID) + ", was " + (wasSent ? "sent" : "received") + ", on " + packet.getTimeStamp() + ". The final packet status was: " + status + ". The packet was sent " + (initialAttemptsAmount-attemptsLeftToSend) + " times out of the " + initialAttemptsAmount + " maximum amount of attempts it had to be sent." + System.lineSeparator());
                    logFileStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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

        protected void lowerAttemptsLeftToSend(int i){
            attemptsLeftToSend = --i;
        }

        protected void setAttemptsLeftToSend(int i){
            attemptsLeftToSend = i;
            initialAttemptsAmount = i;
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

    public static void setDirectory(File file){
        if(logsRepository == null) {
            logsRepository = file == null ? GBUILibGlobals.getSocketLogsDirectory() : file;
            logsRepository.mkdirs();
            System.out.println(logsRepository);
            return;
        }
        throw new IllegalStateException("The logs repository is already set!");
    }

    boolean writeToLog;
    protected PacketLogger(GBSocket socket) throws IOException {
        this.socket = socket;
        writeToLog = GBUILibGlobals.writePacketsToFile();
        if(writeToLog) {
            logFile = new File(logsRepository.getAbsolutePath());
            if(socket.isServer()){
                logFile = new File(logsRepository.getAbsolutePath() + File.separator + socket.parent.name);
                logFile.mkdirs();
            }
        }
    }

    protected void setSocketID(int socketID) throws IOException {
        logFile = socket.socketIDServerSide != -1 ? new File(logFile.getPath() + File.separator + socketID + ".txt") : logFile;
        logFile.createNewFile();logFileStream = new ObjectOutputStream(new FileOutputStream(logFile));
    }

    protected static void suspiciousPacket(SocketAddress address, GBSocket socket) {
        File file = new File(logsRepository.getAbsoluteFile() + File.separator + (socket.isServer() ? socket.parent.name + File.separator : "") + "SuspiciousPackets.txt");
        try {
            file.createNewFile();
            ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(file));
            writer.writeUTF("A suspicious packet was received from the address: " + address + (socket.isServer() ? " by the sever socket " : " by the socket numbered: " + socket.programWideSocketID + " program-wide, and " + socket.socketIDServerSide + " by it's connected server ") + "at " + Instant.now());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // done
    protected LogLine followPacket(Packet packet, boolean sent){
        LogLine logLine = new LogLine(packet, sent);
        packets.putLine(sent, packet.getIds(), logLine);
        logLine.setStatus(sent ? PacketStatus.SEND_READY : PacketStatus.RECEIVED);
        return logLine;
    }
}