package gbuiLib.GBSockets;

public class ActionHandlerException extends RuntimeException {

    public ActionHandlerException(String reason, String packetType, ActionHandler.PacketHandler handler){
        super(reason + System.lineSeparator() + "The handler that caused the issue is the handler: " + handler + ", for the packet Type " + packetType);
    }
}
