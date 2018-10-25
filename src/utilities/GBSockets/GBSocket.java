package utilities.GBSockets;

import utilities.GBUILibGlobals;
import utilities.ProgramWideVariable;

public class GBSocket{

    private boolean isInvalid;
    private PacketManager manager;

    public GBSocket(){
        if(GBUILibGlobals.unsafeSockcets()) {
            isInvalid = true;
        } else {
            throw new UnsafeSocketException("There was an attempt to create an unsafe socket, even though unsafe sockets are disabled");
        }
    }

    public GBSocket(boolean autoReconnect, boolean revive, ActionHandler handler){
        isInvalid = false;
    }

    public synchronized void sendPacket(Packet... packets){
        if(GBUILibGlobals.unsafeSockcets()){

        } else {
            throw new UnsafeSocketException("There was an attempt to send a packet directly and not through a packet manager, even though unsafe sockets are disabled");
        }
    }

    protected synchronized void sendPacket(Packet packet){

    }

    public void sendAsPacket(Object content, String contentType, String packetType) throws BadPacketException{
        if(!GBUILibGlobals.unsafeSockcets()) {
            manager.sendAsPacket(content, contentType, packetType);
        }
        else {
            throw new UnsafeSocketException("Cannot send the content as a packet, as this socket is not a proper GBSocket, and as such, does not have a PacketManager. To send a packet, you will have to construct and send it yourself");
        }
    }
}