package utilities.GBSockets;

import utilities.GBUILibGlobals;

import java.time.Instant;

public class Packet {

    private Object content;
    private String packetType;
    private String contentType;
    private int[] ids;
    private boolean errorChecked;
    private Instant timeStamp;
    private transient boolean resend;
    private boolean isAck;

    public Object getContentUnsafe() {
        if(GBUILibGlobals.unsafeSockets()) {
            return content;
        } else {
            throw new UnsafeSocketException("There was an attempt to get a packet's content directly, even though unsafe sockets are disabled");
        }
    }

    public String getContentTypeUnsafe() {
        if(GBUILibGlobals.unsafeSockets()) {
            return contentType;
        } else {
            throw new UnsafeSocketException("There was an attempt to get a packet's content type identifier directly, even though unsafe sockets are disabled");
        }
    }

    public String getPacketTypeUnsafe() {
        if(GBUILibGlobals.unsafeSockets()) {
            return packetType;
        } else {
            throw new UnsafeSocketException("There was an attempt to get a packet's type identifier directly, even though unsafe sockets are disabled");
        }
    }

    public Instant getTimeStampUnsafe() {
        if(GBUILibGlobals.unsafeSockets()) {
            return timeStamp;
        } else {
            throw new UnsafeSocketException("There was an attempt to get a packet's time stamp directly, even though unsafe sockets are disabled");
        }
    }

    protected Object getContent() {
        return content;
    }

    protected String getPacketType() {
        return packetType;
    }

    protected String getContentType() {
        return contentType;
    }

    protected int[] getIds() {
        return ids;
    }

    protected boolean isErrorChecked() {
        return errorChecked;
    }

    protected Instant getTimeStamp(){
        return timeStamp;
    }

    protected boolean getResend() {
        return resend;
    }

    protected boolean getIsAck(){
        return isAck;
    }

    // Unsafe
    public Packet(Object content, String contentType, String packetType){
        this(content, null, contentType, packetType, false);
        if(!GBUILibGlobals.unsafeSockets()) {
            throw new UnsafeSocketException("There was an attempt to create a packet directly, even though unsafe sockets are disabled");
        }
    }

    // Normal
    protected Packet(Object content, String contentType, String packetType, PacketManager parent, boolean resend) throws BadPacketException{
        this(content, parent.checkPacketValidity(content, packetType), contentType, packetType, true);
        this.resend = resend;
        this.isAck = false;
    }

    // Base Constructor
    private Packet(Object content, int[] ids, String contentType, String packetType, boolean errorChecked){
        this.content = content;
        this.ids = ids;
        this.contentType = contentType;
        this.packetType = packetType;
        this.errorChecked = errorChecked;
        this.timeStamp = Instant.now();
    }

    // ACK
    protected Packet(Object content, int[] ids, String packetType, String originalPacketType){
        this(content, ids, originalPacketType, packetType, true);
        this.resend = true;
        this.isAck = true;
    }

    // HeartBeat
    protected Packet(PacketManager parent) throws BadPacketException {
        this(null, parent.checkPacketValidity(null, ActionHandler.DefaultPacketTypes.HeartBeat.toString()), null, ActionHandler.DefaultPacketTypes.HeartBeat.toString(), true);
        this.resend = false;
        this.isAck = false;
    }
}
