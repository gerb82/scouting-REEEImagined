package utilities.GBSockets;

public class GBSocket{

    private boolean isDeprecated;
    private PacketManager manager;

    @Deprecated
    public GBSocket(){
        isDeprecated = true;
    }

    public GBSocket(boolean autoReconnect, boolean revive, ActionHandler handler, ){
        isDeprecated = true;
    }

    @Deprecated
    public synchronized void sendPacket(Packet... packets){

    }

    protected synchronized void sendPacket(Packet packet){

    }

    public void sendAsPacket(Object content, String contentType, String packetType) throws BadPacketException{
        if(!isDeprecated) {
            manager.sendAsPacket(content, contentType, packetType);
        }
        else {
            throw new DeprecatedSocketException("Cannot send the content as a packet, as this socket is not a proper GBSocket, and as such, does not have a PacketManager. To send a packet, you will have to construct and send it yourself");
        }
    }
}