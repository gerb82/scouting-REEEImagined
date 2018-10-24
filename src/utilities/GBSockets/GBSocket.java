package utilities.GBSockets;

import utilities.ProgramWideVariable;

public class GBSocket{

    private boolean isInvalid;
    private PacketManager manager;

    public GBSocket() throws IllegalAccessException{
        if(!((Boolean)ProgramWideVariable.getVariableWithDefault(ProgramWideVariable.GBUILibVariables.GBUILIB_GBSOCKET_ALLOWUNSAFE.toString(), false, Boolean.class)));
        isInvalid = true;
    }

    public GBSocket(boolean autoReconnect, boolean revive, ActionHandler handler, ){
        isInvalid = false;
    }

    @Deprecated
    public synchronized void sendPacket(Packet... packets){

    }

    protected synchronized void sendPacket(Packet packet){

    }

    public void sendAsPacket(Object content, String contentType, String packetType) throws BadPacketException{
        if(!) {
            manager.sendAsPacket(content, contentType, packetType);
        }
        else {
            throw new DeprecatedSocketException("Cannot send the content as a packet, as this socket is not a proper GBSocket, and as such, does not have a PacketManager. To send a packet, you will have to construct and send it yourself");
        }
    }
}