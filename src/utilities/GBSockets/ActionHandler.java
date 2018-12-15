package utilities.GBSockets;

import com.sun.javafx.collections.ObservableSetWrapper;
import javafx.collections.ObservableSet;

import java.io.IOException;
import java.net.SocketAddress;
import java.time.Instant;
import java.util.*;

public class ActionHandler {

    protected enum DefaultPacketTypes{
        Ack, SmartAck, HeartBeat, HandShake, Error
    }

    public class PacketOut extends Packet{
        private boolean acked;
        private GBSocket socket;

        private PacketOut(Packet packet, GBSocket socket){
            super(packet.getContent(), packet.getIds(), packet.getContentType(), packet.getPacketType(), true);
            acked = packet.getResend();
            this.socket = socket;
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

        public void ack() throws BadPacketException {
            if(!acked) {
                socket.ack(super.getIds(), super.getPacketType());
                acked = true;
            }
        }

        // done
        public boolean shouldAck(){
            return !acked;
        }

        // done
        public void ack(Object content, String packetType, String contentType, String ackContentType) throws BadPacketException {
            if(!acked) {
                socket.smartAck(super.getIds(), super.getPacketType(), content, packetType, contentType);
                acked = true;
            }
        }

        // done
        public void selfAcked(){
            acked = true;
        }
    }

    public interface PacketHandler{
        void handle(PacketOut packet) throws BadPacketException;
    }

    private HashMap<String, PacketHandler> handlers;
    private List<GBSocket> sockets = new ArrayList<>();

    // done
    public void setHandler(String packetType, PacketHandler handler){
        if(sockets.isEmpty()) {
            if(packetType == DefaultPacketTypes.HandShake.toString()){
                throw new IllegalArgumentException("Cannot use type HandShake for a packet handler, as it is reserved for GBSocket functions");
            }
            handlers.put(packetType, handler);
        } else {
            throw new IllegalStateException("Can't add a handler to an ActionHandler that is being used by a running socket.");
        }
    }

    // done
    protected void handlePacket(Packet packet, GBSocket socket) throws BadPacketException {
        try{
            assert(sockets.contains(socket));
            PacketOut packetOut = new PacketOut(packet, socket);
            PacketHandler handler = handlers.get(packet.getPacketType());
            assert(handler != null);
            handler.handle(packetOut);
            if(!packetOut.acked && socket.allowNoAck()){
                throw new ActionHandlerException("Packet was not acked by the action handler.", packet.getPacketType(), handlers.get(packet.getPacketType()));
            }
        } catch (AssertionError e){
            throw new BadPacketException("The packet type supplied with the packet was a type that the socket cannot handle, or this actionHandler is not associated with this socket.", packet);
        }
    }

    public void setAuthProtocol(CheckConnectionAuth checker, CreateConnectionAuth creator){
        if(checker == null || creator == null){
            throw new IllegalArgumentException("Cannot set the authentication checker or creator to null");
        }
        authChecker = checker;
        authMaker = creator;
    }

    public interface CheckConnectionAuth {
        boolean check(Stack<Object> params, boolean server);
    }

    private CheckConnectionAuth authChecker;
    protected boolean checkConnectionData(Stack<Object> params, boolean server) {
        return authChecker == null ? true : authChecker.check(params, server);
    }

    public interface CreateConnectionAuth {
        void make(Stack<Object> stack, boolean server);
    }

    private CreateConnectionAuth authMaker;
    protected void getConnectionData(Stack<Object> stack, boolean server){
        if(authMaker != null){
            authMaker.make(stack, server);
        }
    }

    // done
    protected ActionHandler(ActionHandlerRecipe... recipes){
        handlers = new HashMap<>();
        for(ActionHandlerRecipe recipe : recipes){
            for(String key : recipe.getHandlers().keySet()){
                setHandler(key, recipe.getHandlers().get(key));
            }
        }
        handlers.putIfAbsent(DefaultPacketTypes.HeartBeat.toString(), this::heartBeat);
        handlers.putIfAbsent(DefaultPacketTypes.Ack.toString(), this::ack);
        handlers.putIfAbsent(DefaultPacketTypes.SmartAck.toString(), this::smartAck);
        handlers.putIfAbsent(DefaultPacketTypes.Error.toString(), this::error);
    }

    protected HashSet<String> getHandledTypes(GBSocket socket){
        sockets.add(socket);
        return new HashSet<>(handlers.keySet());
    }

    protected void connectionClosed(GBSocket socket){
        sockets.remove(socket);
    }

    private void error(PacketOut packet){

    }

    private void heartBeat(PacketOut packet) throws BadPacketException {
        if(packet.socket.isServer()){
            packet.ack();
        }
        else{
            throw new BadPacketException("Received Heartbeat packet from server. Server should only be receiving HeartBeat packets from the clients, not vice-versa. More on why that is in the library documentation.", packet);
        }
    }

    private void ack(PacketOut packet){
    }

    private void smartAck(PacketOut packet) throws BadPacketException {
        try {
            packet.socket.receivePacket((Packet)packet.getContent());
        } catch (ClassCastException e){
            throw new BadPacketException("Could not cast packet contents of a smart Ack packet content into a CustomAck Object.", packet);
        }
    }
}