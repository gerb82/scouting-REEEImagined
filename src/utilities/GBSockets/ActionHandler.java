package utilities.GBSockets;

import java.time.Instant;
import java.util.HashMap;

public class ActionHandler {

    protected enum DefaultPacketTypes{
        Ack, SmartAck, HeartBeat, HandShake, Error
    }

    private GBSocket parent;

    public class PacketOut extends Packet{
        private boolean acked;
        private GBSocket socket;

        private PacketOut(Packet packet){
            super(packet);
            acked = false;
            this.socket = parent;
        }

        public GBSocket getSocket() {
            return socket;
        }

        @Override
        public Object getContent() {
            return super.getContent();
        }

        @Override
        public String getPacketType() {
            return super.getPacketType();
        }

        @Override
        public String getContentType() {
            return super.getContentType();
        }

        @Override
        public int[] getIds() {
            return super.getIds();
        }

        @Override
        public boolean isErrorChecked() {
            return super.isErrorChecked();
        }

        @Override
        public Instant getTimeStamp() {
            return super.getTimeStamp();
        }

        public void ack(){
            socket.ack(super.getIds(), super.getPacketType());
            acked = true;
        }

        public void ack(Object content, String packetType, String contentType, String ackContentType){
            socket.smartAck(super.getIds(), super.getPacketType(), content, packetType, contentType, ackContentType);
            acked = true;
        }

        public void selfAcked(){
            acked = true;
        }
    }

    public interface PacketHandler{
        void handle(PacketOut packet) throws BadPacketException;
    }

    private HashMap<String, PacketHandler> handlers;

    public void addHandler(String packetType, PacketHandler handler){
        handlers.put(packetType, handler);
    }

    protected void handlePacket(Packet packet) throws BadPacketException {
        try{
            PacketOut packetOut = new PacketOut(packet);
            handlers.get(packet.getPacketType()).handle(packetOut);
            if(!packetOut.acked && parent.allowNoAck()){
                throw new ActionHandlerException("Packet was not acked by the action handler.", packet.getPacketType(), handlers.get(packet.getPacketType()));
            }
        } catch (NullPointerException e){
            throw new BadPacketException("The packet type supplied with a packet was a type the socket cannot handle.");
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
        handlers.putIfAbsent(DefaultPacketTypes.HeartBeat.toString(), this::heartBeat);
        handlers.putIfAbsent(DefaultPacketTypes.Ack.toString(), this::ack);
        handlers.putIfAbsent(DefaultPacketTypes.SmartAck.toString(), this::smartAck);
        handlers.putIfAbsent(DefaultPacketTypes.HandShake.toString(), parent::handShakeReceive);
    }

    private void heartBeat(PacketOut packet) throws BadPacketException {
        if(parent.isServer()){
            packet.ack();
        }
        else{
            throw new BadPacketException("Received Heartbeat packet from server. Server should only be receiving HeartBeat packets from the clients, not vice-versa. More on why that is in the library documentation.", packet);
        }
    }

    private void ack(PacketOut packet){
        packet.selfAcked();
    }

    private void smartAck(PacketOut packet) throws BadPacketException {
        try {
            parent.receivePacket(((Packet.CustomAck) packet.getContent()).packet);
            packet.selfAcked();
        } catch (ClassCastException e){
            throw new BadPacketException("Could not cast packet contents of a smart Ack packet content into a CustomAck Object.", packet);
        }
    }
}