package utilities.GBSockets;

public class BadPacketException extends Exception {

    public BadPacketException(String message){
        super(message);
    }

    public BadPacketException(String message, Packet packet){
        super(message);
        this.packet = packet;
    }

    private Packet packet;
}
