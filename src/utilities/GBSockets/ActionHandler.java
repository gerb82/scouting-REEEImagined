package utilities.GBSockets;

import java.time.Instant;
import java.util.HashMap;

public class ActionHandler {

    private GBSocket parent;

    public class PacketOut {
        private Object content;
        private String contentType;
        private String packetType;
        private int[] IDs;
        private boolean errorChecked;
        private Instant timestamp;

        private PacketOut(Packet packet){
            content = packet.getContent();
            contentType = packet.getContentType();
            packetType = packet.getPacketType();
            IDs = packet.getIds();
            errorChecked = packet.isErrorChecked();
            timestamp = packet.getTimeStamp();
        }

        public Object getContent() {
            return content;
        }

        public String getContentType() {
            return contentType;
        }

        public String getPacketType() {
            return packetType;
        }

        public int[] getIDs() {
            return IDs;
        }

        public boolean isErrorChecked() {
            return errorChecked;
        }

        public Instant getTimestamp(){
            return timestamp;
        }
    }

    public interface PacketHandler{
        void handle(GBSocket socket, PacketOut packet) throws BadPacketException;
    }

    private HashMap<String, PacketHandler> handlers;

    public void addHandler(String packetType, PacketHandler handler){
        handlers.put(packetType, handler);
    }

    protected void handlePacket(Packet packet) throws BadPacketException {
        try{
            handlers.get(packet.getPacketType()).handle(parent, new PacketOut(packet));
        } catch (NullPointerException e){
            throw new BadPacketException("The packet type supplied with a packet was a type the socket cannot handle");
        }
    }

    protected ActionHandler(GBSocket parent, ActionHandlerRecipe... recipes){
        this.parent = parent;
        handlers = new HashMap<>();
        for(ActionHandlerRecipe recipe : recipes){
            for(String key : recipe.getHandlers().keySet()){
                handlers.putIfAbsent(key, recipe.getHandlers().get(key));
            }
        }
    }
}