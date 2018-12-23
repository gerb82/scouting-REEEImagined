package gbuiLib.GBSockets;

public class BadPacketException extends Exception {

    public BadPacketException(String message){
        super(message);
    }

    public BadPacketException(String message, Packet packet){
        super(message);
        this.packet = packet;
    }

    public Packet getPacket(){
        return packet;
    }

    private Packet packet;
}
