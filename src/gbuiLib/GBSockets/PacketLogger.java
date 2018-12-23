package gbuiLib.GBSockets;

import javafx.beans.property.SimpleObjectProperty;
import gbuiLib.GBUILibGlobals;
import gbuiLib.Utils;

import java.io.*;
import java.net.SocketAddress;
import java.time.Instant;
import java.util.HashMap;

public class PacketLogger implements Closeable{

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
        origin.mention();
        return origin.getResponse() != null ? origin : null;
    }

    // done
    protected LogLine setResponse(int[] ids, Packet packet){
        LogLine line = packets.getLine(false, ids);
        line.setResponse(packet);
        line.mention();
        return line;
    }

    // done
    protected LogLine beat(Packet packet){
        LogLine line = followPacket(packet, true);
        line.setStatus(PacketStatus.SEND_READY);
        return line;
    }

    protected void packetReturned(Packet packet){
        LogLine origin = packets.getLine(true, packet.getIds());
        origin.setResponse(packet);
        origin.mention();
        if(packet.getPacketType().equals(ActionHandler.DefaultPacketTypes.Error)){
            origin.setStatus(PacketStatus.SEND_ERRORED);
        } else {
            origin.setStatus(PacketStatus.ACKED);
        }
    }

    protected class PacketMap extends HashMap<String, LogLine>{

        protected LogLine getLine(boolean wasSent, int[] ids){
            return get((wasSent ? "out" : "in") + intArrayToString(ids));
        }

        private void putLine(boolean wasSent, int[] ids, LogLine line){
            put((wasSent ? "out" : "in") + intArrayToString(ids), line);
        }

    }

    private static String intArrayToString(int[] ids){
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
            logFileStream.writeUTF("Socket closed");
            logFileStream.close();
        }
    }

    protected interface LogLineMethod {
        void handle(LogLine line) throws BadPacketException;
    }

    public enum PacketStatus{
        SEND_READY, ACKED, SEND_ERRORED, WAITING, TIMED_OUT, SENT,
        RECEIVED, RECEIVED_ERRORED, RECEIVED_DONE, DISCARDED
    }

    protected class LogLine{

        private transient ObservablePacketStatus status;
        private Packet packet;
        private Packet response;
        private boolean wasSent;
        private int attemptsLeftToSend;
        private int initialAttemptsAmount;
        private transient Instant lastMentioned;
        private transient Instant lastSent;

        private LogLine(Packet packet, boolean wasSent){
            this.packet = packet;
            this.wasSent = wasSent;
            this.status = new ObservablePacketStatus(this);
        }

        protected void discardToLog() {
            packets.remove((wasSent ? "out" : "in") + packet.getIds());
            PacketStatus tempStatus = status.get();
            if(tempStatus.equals(PacketStatus.WAITING)){
                tempStatus = PacketStatus.TIMED_OUT;
            }
            status.set(PacketStatus.DISCARDED);
            if(writeToLog && logFileStream != null && !packet.getPacketType().equals(ActionHandler.DefaultPacketTypes.HeartBeat) && !packet.getIsAck()) {
                try {
                    if(!GBUILibGlobals.writePacketsSerialized()) {
                        logFileStream.writeUTF("The serialized packet was: ");
                        logFileStream.writeObject(packet);
                        logFileStream.writeUTF(", and ");
                        if (response != null) {
                            if (response.getContent() != null && response.getContent().getClass().isAssignableFrom(Packet.class)) {
                                logFileStream.writeUTF("the response was the packet with the ids: " + response.getIds());
                            } else {
                                logFileStream.writeUTF("the response was the packet: ");
                                logFileStream.writeObject(response);
                            }
                        } else {
                            logFileStream.writeUTF("there was no response");
                        }
                        logFileStream.writeUTF(". The packet " + PacketManager.formatPacketIDs(packet.getIds(), packet.getPacketType(), socket.programWideSocketID) + ", was " + (wasSent ? "sent" : "received") + ", on " + Utils.instantToTimestamp(packet.getTimeStamp(), true) + ". The final packet status was: " + tempStatus + "." + (wasSent ? " The packet was sent " + (initialAttemptsAmount - attemptsLeftToSend + 1) + " times out of the " + (initialAttemptsAmount+1) + " maximum amount of attempts it had to be sent." : "") + System.lineSeparator());
                    } else {
                        logFileStream.writeObject(this);
                        logFileStream.writeUTF(tempStatus.toString());
                    }
                    logFileStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        protected Packet getPacket(){
            mention();
            return packet;
        }

        protected void setStatus(PacketStatus status){
            mention();
            this.status.set(status);
        }

        protected void setResponse(Packet packet){
            mention();
            this.response = packet;
        }

        protected Packet getResponse(){
            mention();
            return response;
        }

        protected boolean getWasSent(){
            mention();
            return wasSent;
        }

        protected void mention(){
            this.lastMentioned = Instant.now();
        }

        protected int discarderTick(int discardTimer, int sendIntervals, LogLineMethod sender) throws BadPacketException {
            long now = Instant.now().toEpochMilli();
            int nextCheck;
            if(attemptsLeftToSend != 0 && status.get().equals(PacketStatus.WAITING)) {
                if (now - lastSent.toEpochMilli() < sendIntervals) {
                    nextCheck = sendIntervals - (int) (now - lastSent.toEpochMilli());
                } else {
                    sender.handle(this);
                    lastSent = Instant.ofEpochMilli(now);
                    attemptsLeftToSend--;
                    nextCheck = attemptsLeftToSend != 0 ? sendIntervals : discardTimer;
                }
            } else {
                nextCheck = discardTimer - (int) (now - lastMentioned.toEpochMilli());
                if(nextCheck < 1){
                    discardToLog();
                    return -1;
                }
            }
            mention();
            return nextCheck;
        }

        protected void initDiscardFollow(int i){
            mention();
            lastSent = Instant.now();
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

    public static void setDirectory(){
        setDirectory(null);
    }

    public static void setDirectory(File file){
        if(logsRepository == null) {
            logsRepository = file == null ? GBUILibGlobals.getLogsDirectory() : file;
            logsRepository = new File(logsRepository.getPath() + File.separator + "socketLogs");
            logsRepository.mkdirs();
            return;
        }
        throw new IllegalStateException("The logs repository is already set!");
    }

    boolean writeToLog;
    protected PacketLogger(GBSocket socket) throws IOException {
        this.socket = socket;
        writeToLog = GBUILibGlobals.writePacketsToFile();
        if(writeToLog) {
            logFile = logsRepository;
            if(socket.isServer()){
                logFile = new File(logsRepository, socket.parent.name);
                logFile.mkdirs();
            }
        }
    }

    protected void setSocketID(int socketID) throws IOException {
        if (socketID != -1){
            logFile = new File(logFile, socketID + ".txt");
            logFile.createNewFile();
            logFileStream = new ObjectOutputStream(new FileOutputStream(logFile));
        }
    }

    protected static void suspiciousPacket(SocketAddress address, GBSocket socket) {
        if(GBUILibGlobals.writePacketsToFile()) {
            File file = new File(logsRepository + (socket.isServer() ? File.separator + socket.parent.name : ""), "SuspiciousPackets.txt");
            try {
                file.createNewFile();
                ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(file, true));
                writer.writeUTF("A suspicious packet was received from the address: " + address + (socket.isServer() ? " by the sever socket " : " by the socket numbered: " + socket.programWideSocketID + " program-wide, and " + socket.socketIDServerSide + " by it's connected server ") + "at " + Instant.now() + System.lineSeparator());
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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