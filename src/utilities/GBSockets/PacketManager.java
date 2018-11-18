package utilities.GBSockets;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PacketManager {
    // constructor

    protected PacketManager(int ManagerID, GBSocket socket,
                            ActionHandler actionHandler){
        // output
        this.socket = socket;
        this.managerID = ManagerID;
        // input
        this.actionHandler = actionHandler;
    }

    // outgoing packets
    private List<String> sendTypes = new ArrayList<>();
    private Map<String, Integer> packetNumbers = new HashMap<String, Integer>(){{this.put("HeartBeat", 0);}};
    private int packetTotal = 0;
    private final int managerID;
    private GBSocket socket;

    protected void sendAsPacket(Object content, String contentType, String packetType) throws BadPacketException{
        socket.sendPacket(new Packet(content, contentType, packetType, this));
    }

    private synchronized int[] numerize(String packetType){
        int[] output;
        if(packetType != null){
            output = new int[]{packetTotal, packetNumbers.get(packetType), managerID};
            packetNumbers.put(packetType, output[1]++);
        }
        else{
            output = new int[]{packetTotal, managerID};
        }
        packetTotal++;
        return output;
    }

    protected int[] checkPacketValidity(Object content, String packetType) throws BadPacketException{
        try {
            assert (sendTypes.contains(packetType));
        }
        catch (AssertionError e){
            throw new BadPacketException("The packet type for the packet" + formatPacketIDs(numerize(null), packetType) + " was of a type that cannot be sent.");
        }
        int[] packetNumbers = numerize(packetType);
        try {
            if(content != null) {
                assert (content instanceof Serializable);
            }
        }
        catch (AssertionError e){
            throw new BadPacketException("The packet content for packet " + formatPacketIDs(packetNumbers, packetType) + " is invalid, because it can't be serialized.");
        }
        return packetNumbers;
    }

    protected static String formatPacketIDs(int[] ids, String packetType) throws IllegalArgumentException{
        if(ids.length == 2){
            return "number" + ids[0] + " (total), in PacketManager number " + ids[1];
        }
        else if(ids.length == 3 && packetType != null){
            return "number" + ids[0] + " (total), " + ids[1] + " (of " + packetType + "), in PacketManager number " + ids[2];
        }
        else {
            throw new IllegalArgumentException("The method formatPacketIDs has been passed an Illegal argument. Either the \"ids\" array didn't have exactly 2 or 3 cells, or the packet type was null when there are 3 ids in the \"ids\" array.\nthe arguments - ids: " + ids.toString() + " packetType: " + packetType);
        }
    }

    // incoming packets

    private ActionHandler actionHandler;

    protected Packet heartBeat(){
        try {
            return new Packet(null, null, "HeartBeat", this);
        } catch (BadPacketException e) {return null;}
    }
}
