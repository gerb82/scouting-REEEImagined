package utilities.GBSockets;

import utilities.GBUILibGlobals;

public class Packet {

    private Object content;
    private String packetType;
    private String contentType;
    private int[] ids;
    private boolean errorChecked;

    public Object getContentUnsafe() {
        if(GBUILibGlobals.unsafeSockcets()) {
            return content;
        } else {
            throw new UnsafeSocketException("There was an attempt to get a packet's content directly, even though unsafe sockets are disabled");
        }
    }

    public String getContentTypeUnsafe() {
        if(GBUILibGlobals.unsafeSockcets()) {
            return contentType;
        } else {
            throw new UnsafeSocketException("There was an attempt to get a packet's content type identifier directly, even though unsafe sockets are disabled");
        }
    }

    public String getPacketTypeUnsafe() {
        if(GBUILibGlobals.unsafeSockcets()) {
            return packetType;
        } else {
            throw new UnsafeSocketException("There was an attempt to get a packet's type identifier directly, even though unsafe sockets are disabled");
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
}
