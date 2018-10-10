package utilities.GBSockets;

public class Packet {

    protected Object content;
    protected String packetType;
    protected String contentType;
    protected int[] ids;
    protected boolean errorChecked;

    @Deprecated
    public Object getContent() {
        return content;
    }

    @Deprecated
    public String getContentType() {
        return contentType;
    }

    @Deprecated
    public String getPacketType() {
        return packetType;
    }

    @Deprecated
    public Packet(Object content, String contentType, String packetType){
        this.content = content;
        this.contentType = contentType;
        this.packetType = packetType;
        this.errorChecked = false;
    }

    protected Packet(Object content, String contentType, String packetType, PacketManager parent) throws BadPacketException{
        this.ids = parent.checkPacketValidity(content, packetType);
        this.content = content;
        this.contentType = contentType;
        this.packetType = packetType;
        this.errorChecked = true;
    }
}
