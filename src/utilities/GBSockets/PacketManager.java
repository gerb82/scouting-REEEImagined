package utilities.GBSockets;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import utilities.GBUILibGlobals;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;

public class PacketManager {
    // constructor

    PacketLogger logger;

    protected PacketManager(List<String> sendTypes, int connectionID, GBSocket socket, ActionHandler actionHandler, PacketLogger logger) {
        this.logger = logger;
        // output
        this.socket = socket;
        this.connectionID = connectionID;
        this.sendTypes = sendTypes;
        // input
        this.actionHandler = actionHandler;
        this.timeToDiscard = GBUILibGlobals.getTimeToPacketTimeout();
        this.attemptsPerPacket = GBUILibGlobals.getPacketSendAttempts();
    }

    // outgoing packets
    private List<String> sendTypes;
    private Map<String, Integer> packetNumbers = new HashMap<>();
    private int packetTotal = 0;
    private final int connectionID;
    private GBSocket socket;
    private ChangeListener<PacketLogger.PacketStatus> changeManager = new ChangeListener<PacketLogger.PacketStatus>() {
        @Override
        public void changed(ObservableValue<? extends PacketLogger.PacketStatus> observable, PacketLogger.PacketStatus oldValue, PacketLogger.PacketStatus newValue) {
            PacketLogger.LogLine logLine = ((PacketLogger.ObservablePacketStatus)observable).getParent();
            switch(newValue){
                case TIMED_OUT:
                case SEND_ERRORED:
                case RECEIVED_ERRORED:
                case ACKED:
                case RECEIVED_DONE:
                    logLine.setAttemptsLeftToSend(attemptsPerPacket);
                    scheduleDiscardCheckup(logLine, 0);
                    break;
            }
        }
    };

    private final int timeToDiscard;
    private final int attemptsPerPacket;
    private Timer discarder = new Timer();
    private HashMap<PacketLogger.LogLine, DiscardCheckup> discardTimers = new HashMap<>();

    private class DiscardCheckup extends TimerTask{

        private PacketLogger.LogLine toCheck;
        private Instant scheduledAt;

        private DiscardCheckup(PacketLogger.LogLine toCheck, Instant scheduledAt){
            this.toCheck = toCheck;
            this.scheduledAt = scheduledAt;
        }

        @Override
        public void run() {
            if(discardTimers.get(toCheck) == this){
                if (toCheck.getAttemptsLeftToSend() == 0) {
                    toCheck.discardToLog();
                    return;
                } else {
                    toCheck.setAttemptsLeftToSend(toCheck.getAttemptsLeftToSend() - 1);
                }
                scheduleDiscardCheckup(toCheck, 0);
            } else if(discardTimers.get(toCheck) != null){
                scheduleDiscardCheckup(toCheck, (int)Instant.now().toEpochMilli() - (int)discardTimers.get(toCheck).scheduledAt.toEpochMilli());
            }
            if(toCheck.getStatus() == PacketLogger.PacketStatus.WAITING){
                try {
                    socket.sendPacket(toCheck);
                } catch (BadPacketException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void scheduleDiscardCheckup(PacketLogger.LogLine line, int time){
        if(discardTimers.containsKey(line)){
            discardTimers.remove(line).cancel();
        }
        discardTimers.put(line, new DiscardCheckup(line, Instant.now()));
        discarder.schedule(discardTimers.get(line), ((1000*timeToDiscard)/attemptsPerPacket)-time);
    }

    // done
    protected boolean heartBeat() {
        try {
            Packet packet = new Packet(this);
            socket.sendPacket(logger.beat(packet));
            return true;
        } catch (BadPacketException e) {
            return false;
        }
    }

    // done
    protected void ack(int[] ids, String originalPacketType) throws BadPacketException {
        Packet packet = new Packet(null, ids, ActionHandler.DefaultPacketTypes.Ack.toString(), originalPacketType);
        socket.sendPacket(logger.setResponse(ids, packet));
    }

    // done
    /**
     * Creates a custom ack packet that contains a normal packet, and sends it.
     * @param IDs IDs for the SmartAck packet.
     * @param originalPacketType The packetType of the packet being acked, used as the contentType of the ack packet.
     * @param content The content for the new packet.
     * @param contentType The contentType for the new packet.
     * @param newPacketType The packetType for the new packet.
     * @throws BadPacketException If the packet failed to create.
     */
    protected void smartAck(int[] IDs, String originalPacketType, Object content, String contentType, String newPacketType) throws BadPacketException{
        Packet packet = new Packet(new Packet(content, contentType, newPacketType, this, false), IDs, ActionHandler.DefaultPacketTypes.SmartAck.toString(), originalPacketType);
        socket.sendPacket(logger.setResponse(IDs, packet));
    }

    // done
    protected void sendAsPacket(Object content, String contentType, String packetType, boolean important) throws BadPacketException {
        Packet packet = new Packet(content, contentType, packetType, this, important);
        PacketLogger.LogLine line = logger.followPacket(packet, true);
        line.getStatusProperty().addListener(changeManager);
        socket.sendPacket(line);
    }

    // done
    private void error(Object reason, int[] ids, String originalPacketType) throws BadPacketException {
        Packet packet = new Packet(reason, ids, ActionHandler.DefaultPacketTypes.Error.toString(), originalPacketType);
        socket.sendPacket(logger.setResponse(ids, packet));
    }

    // done
    private synchronized int[] numerize(String packetType) {
        int[] output;
        if (packetType != null) {
            output = new int[]{packetTotal, packetNumbers.get(packetType), connectionID};
            packetNumbers.put(packetType, output[1]++);
        } else {
            output = new int[]{packetTotal, connectionID};
        }
        packetTotal++;
        return output;
    }

    // done
    protected int[] checkPacketValidity(Object content, String packetType) throws BadPacketException {
        try {
            assert (sendTypes.contains(packetType));
        } catch (AssertionError e) {
            throw new BadPacketException("The packet type for the packet" + formatPacketIDs(numerize(null), packetType) + " was of a type that cannot be sent.");
        }
        int[] packetNumbers = numerize(packetType);
        try {
            if (content != null) {
                assert (content instanceof Serializable);
            }
        } catch (AssertionError e) {
            throw new BadPacketException("The packet content for packet " + formatPacketIDs(packetNumbers, packetType) + " is invalid, because it can't be serialized.");
        }
        return packetNumbers;
    }

    // done
    protected static String formatPacketIDs(int[] ids, String packetType) throws IllegalArgumentException {
        if (ids.length == 2) {
            return "number" + ids[0] + " (total), in connection number " + ids[1];
        } else if (ids.length == 3 && packetType != null) {
            return "number" + ids[0] + " (total), " + ids[1] + " (of " + packetType + "), in connection number " + ids[2];
        } else {
            throw new IllegalArgumentException("The method formatPacketIDs has been passed an Illegal argument. Either the \"ids\" array didn't have exactly 2 or 3 cells, or the packet type was null when there are 3 ids in the \"ids\" array." + System.lineSeparator() + "the arguments ids: " + ids.toString() + " packetType: " + packetType);
        }
    }

    // incoming packets

    private ActionHandler actionHandler;

    protected void receivePacket(Packet packet){
        try {
            if(logger.isPacketFollowedOut(packet) && packet.getIsAck()){
                logger.packetReturned(packet);
                return;
            } else if(logger.isPacketFollowedIn(packet)){
                PacketLogger.LogLine origin = logger.packetAlreadyReceived(packet);
                if(origin != null){
                    socket.sendPacket(origin);
                }
                return;
            }
            logger.followPacket(packet, false);
            actionHandler.handlePacket(packet);
        } catch (BadPacketException e) {
            if(packet.getIsAck()){
                try {
                    error(e.getMessage(), packet.getIds(), packet.getPacketType());
                } catch (BadPacketException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}