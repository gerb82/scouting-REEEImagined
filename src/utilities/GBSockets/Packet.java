package utilities.GBSockets;

import utilities.GBUILibGlobals;

public class Packet {

    protected Object content;
    protected String packetType;
    protected String contentType;
    protected int[] ids;
    protected boolean errorChecked;

    public Object getContent() {
        if(GBUILibGlobals.unsafeSockcets()) {
            return content;
        } else {
            throw new UnsafeSocketException("There was an attempt to get a packet's content directly, even though unsafe sockets are disabled");
        }
    }

    public String getContentType() {
        if(GBUILibGlobals.unsafeSockcets()) {
            return contentType;
        } else {
            throw new UnsafeSocketException("There was an attempt to get a packet's content type identifier directly, even though unsafe sockets are disabled");
        }
    }

    public String getPacketType() {
        if(GBUILibGlobals.unsafeSockcets()) {
            return packetType;
        } else {
            throw new UnsafeSocketException("There was an attempt to get a packet's type identifier directly, even though unsafe sockets are disabled");
        }
    }

    public Packet(Object content, String contentType, String packetType){
        if(GBUILibGlobals.unsafeSockcets()) {
        this.content = content;
        this.contentType = contentType;
        this.packetType = packetType;
        this.errorChecked = false;
        } else {
            throw new UnsafeSocketException("There was an attempt to create a packet directly, even though unsafe sockets are disabled");
        }
    }

    protected Packet(Object content, String contentType, String packetType, PacketManager parent) throws BadPacketException{
        this.ids = parent.checkPacketValidity(content, packetType);
        this.content = content;
        this.contentType = contentType;
        this.packetType = packetType;
        this.errorChecked = true;
    }

    protected static Packet sendAck(int[] IDs, String originalType){
        return new Packet(IDs, originalType);
    }

    private Packet(int[] IDs, String originalType){
        this.ids = IDs;
        this.packetType = originalType;
    }

    protected static Packet heartBeat(int... ID){
        return new Packet(ID);
    }

    private Packet(int... ID){
        this.packetType = "HeartBeat";
        this.ids = ID;
    }
}
