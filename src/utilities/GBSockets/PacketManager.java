package utilities.GBSockets;

import utilities.AssertionYouDimwitException;
import utilities.SmartAssert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PacketManager {
    // constructor

    PacketLogger logger;

    protected PacketManager(int connectionID, GBSocket socket, ActionHandler actionHandler, PacketLogger logger) {
        this.logger = logger;
        // output
        this.socket = socket;
        this.connectionID = connectionID;
        // input
        this.actionHandler = actionHandler;
    }

    // outgoing packets
    private List<String> sendTypes = new ArrayList<>();
    private Map<String, Integer> packetNumbers = new HashMap<>();
    private int packetTotal = 0;
    private final int connectionID;
    private GBSocket socket;

    // done
    protected boolean heartBeat() {
        try {
            socket.sendPacket(new Packet(this));
            return true;
        } catch (BadPacketException e) {
            return false;
        }
    }

    // done
    protected void ack(int[] ids, String originalPacketType) {
        socket.sendPacket(new Packet(null, ids, ActionHandler.DefaultPacketTypes.Ack.toString(), originalPacketType));
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
        socket.sendPacket(new Packet(new Packet(content, contentType, newPacketType, this, false), IDs, ActionHandler.DefaultPacketTypes.SmartAck.toString(), originalPacketType));
    }

    // done
    protected void sendAsPacket(Object content, String contentType, String packetType, boolean important) throws BadPacketException {
        Packet packet = new Packet(content, contentType, packetType, this, important);
        logger.followPacket(socket.sendPacket(packet), packet, true);
    }

    // done
    private void error(Object reason, int[] ids, String originalPacketType){
        socket.sendPacket(new Packet(reason, ids, ActionHandler.DefaultPacketTypes.Error.toString(), originalPacketType));
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
            if(logger.isPacketFollowed(packet)){
                if(packet.getIsAck()) {
                    Packet originalAck = logger.packets.getLine(true, packet.getIds()).getPacket();
                    if(originalAck != null){
                        logger.packetDidntSend(originalAck);
                    }
                }
                return;
            }
            logger.followPacket(PacketLogger.PacketStatus.RECEIVED, packet, false);
            actionHandler.handlePacket(packet);
        } catch (BadPacketException e) {
            if(packet.getIsAck()){
                error(e.getMessage(), packet.getIds(), packet.getPacketType());
            }
        }
    }
}