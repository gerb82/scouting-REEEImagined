package gbuiLib.GBSockets;

import gbuiLib.GBUILibGlobals;

import java.io.Serializable;
import java.util.*;

public class PacketManager {
    // constructor

    PacketLogger logger;

    protected PacketManager(HashSet<String> sendTypes, int connectionID, GBSocket socket, ActionHandler actionHandler, PacketLogger logger, Timer discarder) {
        this.logger = logger;
        // output
        this.packetNumbers = new HashMap<>();
        for(String string : sendTypes){
            packetNumbers.put(string, -1);
        }
        this.socket = socket;
        this.connectionID = connectionID;
        this.sendTypes = sendTypes;
        // input
        this.actionHandler = actionHandler;
        this.attemptsPerPacket = GBUILibGlobals.getPacketSendAttempts() - 1;
        this.timeToSendPacketOver = GBUILibGlobals.getTimeToSendPacket();
        this.timeToDiscardPacket = GBUILibGlobals.getPacketLingerTime();
        this.discarder = discarder;
    }

    // outgoing packets
    private HashSet<String> sendTypes;
    private Map<String, Integer> packetNumbers;
    private int packetTotal = 0;
    private final int connectionID;
    private GBSocket socket;

    private final int attemptsPerPacket;
    private final int timeToSendPacketOver;
    private final int timeToDiscardPacket;
    private Timer discarder;

    private class DiscarderTask extends TimerTask{

        private PacketLogger.LogLine line;

        protected DiscarderTask(PacketLogger.LogLine line){
            this.line = line;
        }

        @Override
        public void run() {
            try {
                int nextTask = line.discarderTick(timeToDiscardPacket, timeToSendPacketOver / attemptsPerPacket, socket::sendPacket);
                if(nextTask != -1){
                    discarder.schedule(new DiscarderTask(line), nextTask);
                } else {
                    if(line.getPacket().isImportant()){
                        if(socket.isServer()) {
                            socket.stopServerSideConnection();
                        } else {
                            socket.disconnect();
                        }
                        logger.connectionTimedOutImportant(line.getPacket());
                    }
                }
            } catch (BadPacketException e){
                throw new Error("Somehow the packet could be sent on the first time but not be resent again. It appears something has accessed the packet from outside the GBSocket library.", e);
            }
        }
    }

    // done
    protected boolean heartBeat() {
        try {
            Packet packet = new Packet(this);
            PacketLogger.LogLine line = logger.beat(packet);
            socket.sendPacket(line);
            line.initDiscardFollow(attemptsPerPacket);
            discarder.schedule(new DiscarderTask(line), 0);
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
        PacketLogger.LogLine line = logger.setResponse(IDs, packet);
        logger.followPacket((Packet)packet.getContent(), true);
        socket.sendPacket(line);
        line.initDiscardFollow(attemptsPerPacket);
        discarder.schedule(new DiscarderTask(line), 0);
    }

    // done
    protected PacketLogger.ObservablePacketStatus sendAsPacket(Object content, String contentType, String packetType, boolean important) throws BadPacketException {
        Packet packet = new Packet(content, contentType, packetType, this, important);
        PacketLogger.LogLine line = logger.followPacket(packet, true);
        socket.sendPacket(line);
        line.initDiscardFollow(attemptsPerPacket);
        discarder.schedule(new DiscarderTask(line), 0);
        return line.getStatusProperty();
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
            packetNumbers.put(packetType, ++output[1]);
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
            throw new BadPacketException("The packet type for the packet" + formatPacketIDs(numerize(null), packetType, socket.programWideSocketID) + " was of a type that cannot be sent.");
        }
        int[] packetNumbers = numerize(packetType);
        try {
            if (content != null) {
                assert (content instanceof Serializable);
            }
        } catch (AssertionError e) {
            throw new BadPacketException("The packet content for packet " + formatPacketIDs(packetNumbers, packetType, socket.programWideSocketID) + " is invalid, because it can't be serialized.");
        }
        return packetNumbers;
    }

    // done
    protected static String formatPacketIDs(int[] ids, String packetType, int globalSocketID) throws IllegalArgumentException {
        if (ids.length == 2) {
            return "number " + ids[0] + " (total), in connection number " + Integer.toString(ids[1]) + " server side, and " + globalSocketID + " client side";
        } else if (ids.length == 3 && packetType != null) {
            return "number " + ids[0] + " (total), " + Integer.toString(ids[1]) + " (of " + packetType + "), in connection number " + Integer.toString(ids[2]) + " server side, and " + globalSocketID + " client side";
        } else if (ids[0] == -1) {
            return "handshake packet ";
        } else {
            throw new IllegalArgumentException("The method formatPacketIDs has been passed an Illegal argument. Either the \"ids\" array didn't have exactly 2 or 3 cells, or the packet type was null when there are 3 ids in the \"ids\" array." + System.lineSeparator() + "the arguments ids: " + ids.toString() + " packetType: " + packetType);
        }
    }

    // incoming packets

    private ActionHandler actionHandler;

    protected void receivePacket(Packet packet){
        try {
            if(logger.isPacketFollowedOut(packet) && packet.isAck()){
                logger.packetReturned(packet);
                PacketLogger.LogLine origin = logger.packets.getLine(true, packet.getIds());
                socket.pingInMillis.set((int)(origin.getResponse().getTimeStamp().toEpochMilli() - origin.getPacket().getTimeStamp().toEpochMilli()));
                return;
            } else if(logger.isPacketFollowedIn(packet)){
                PacketLogger.LogLine origin = logger.packetAlreadyReceived(packet);
                if(origin != null){
                    socket.sendPacket(origin);
                }
                return;
            }
            PacketLogger.LogLine line = logger.followPacket(packet, false);
            line.initDiscardFollow(0);
            discarder.schedule(new DiscarderTask(line), 0);
            actionHandler.handlePacket(packet, socket);
        } catch (BadPacketException e) {
            if(!packet.isAck()){
                try {
                    error(e.getMessage(), packet.getIds(), packet.getPacketType());
                } catch (BadPacketException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}