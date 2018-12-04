package utilities.GBSockets;

import utilities.GBUILibGlobals;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.time.Duration;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;

public class GBSocket implements AutoCloseable{

    public class SocketConfig{
        private Integer heartBeatDelay;
        private Boolean alwaysBeat;
        private Integer connectionTimeout;

        public SocketConfig(Integer heartBeatDelay, Boolean alwaysBeat, Integer connectionTimeout) {
            this.heartBeatDelay = heartBeatDelay;
            this.alwaysBeat = alwaysBeat;
            this.connectionTimeout = connectionTimeout;
        }

        public int getHeartBeatDelay() {
            if(heartBeatDelay == null){
                return GBUILibGlobals.getHeartBeatRate();
            }
            return heartBeatDelay;
        }

        protected void setHeartBeatDelay(int heartBeatDelay) {
            this.heartBeatDelay = heartBeatDelay;
        }

        public boolean alwaysBeat() {
            if(alwaysBeat == null){
                return GBUILibGlobals.alwaysHeartBeat();
            }
            return alwaysBeat;
        }

        protected void setAlwaysBeat(boolean alwaysBeat) {
            this.alwaysBeat = alwaysBeat;
        }

        public int isConnectionTimeout() {
            if(connectionTimeout == null){
                return GBUILibGlobals.getSocketTimeout();
            }
            return connectionTimeout;
        }

        protected void setConnectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }
    }

    private SelectorManager selector;
    private boolean isUnsafe;
    private PacketManager manager;
    private DatagramChannel socket;
    private int connectionTimeout;
    private SocketAddress adress;
    private boolean server;

    private boolean autoReconnect;

    public boolean isServer(){
        return server;
    }

    protected DatagramChannel getChannel(){
        return socket;
    }

    protected void SocketConnect(){
        try {
            socket = DatagramChannel.open();
            if(connect(adress)){
                selector.registerSocket(this);
            }
        } catch (IOException e) {
            new IOException("Couldn't connect the GBSocket", e).printStackTrace();
        }
    }

    private boolean connect(SocketAddress address){
        try {
            socket.connect(adress);
            handShake();
            return ;
        } catch (ClosedChannelException e) {
            new IllegalAccessError("The method GBSocket.connect() was invoked by a method other than GBSocket.SocketConnect()");
        } catch (IOException e) {
            new IOException("Something went wrong with the socket ;-;", e);
        }
        return false;
    }

    public void startConnection(){
        GBUILibGlobals.addShutdownCommand(this::dropConnection);
    }

    public void stopAutoReConnection(){
        autoReconnect = false;
    }

    public void dropConnection(){
        stopAutoReConnection();
        try {
            socket.finishConnect();
        } catch (IOException e) {
            new IOException("Failed to close the socket.", e).printStackTrace();
        }
    }

    public void finalize(){
        close();
    }

    // unsafe socket
    public GBSocket(){
        if(GBUILibGlobals.unsafeSockets()) {
            heartBeatDelay = -1;
            alwaysBeat = false;
            isUnsafe = true;
        } else {
            throw new UnsafeSocketException("There was an attempt to create an unsafe socket, even though unsafe sockets are disabled");
        }
    }

    // safe socket
    public GBSocket(boolean autoReconnect, SelectorManager selector, ActionHandler handler, SocketConfig config){
        this.heartBeatDelay = config.getHeartBeatDelay();
        this.alwaysBeat = config.alwaysBeat();
        this.connectionTimeout = config.connectionTimeout;
        this.selector = selector;
        isUnsafe = false;
    }

    public synchronized void sendPacket(Packet... packets){
        if(GBUILibGlobals.unsafeSockets() && isUnsafe){
            for(Packet packet : packets){
                sendPacket(packet);
            }
        } else {
            throw new UnsafeSocketException("There was an attempt to send a packet directly and not through a packet manager, even though unsafe sockets are disabled");
        }
    }

    protected synchronized PacketLogger.PacketStatus sendPacket(Packet packet){
        try {
            socket.write(ByteBuffer.wrap(packet.toString().getBytes()));
            return PacketLogger.PacketStatus.WAITING;
        } catch (IOException e) {
            e.printStackTrace();
            return PacketLogger.PacketStatus.ERRORED;
        }
    }

    public void sendAsPacket(Object content, String contentType, String packetType) throws BadPacketException{
        if(!GBUILibGlobals.unsafeSockets() && !isUnsafe) {
            manager.sendAsPacket(content, contentType, packetType);
        }
        else {
            throw new UnsafeSocketException("Cannot send the content as a packet, as this socket is not a proper GBSocket, and as such, does not have a PacketManager. To send a packet, you will have to construct and send it yourself");
        }
    }

    protected void receivePacket(Packet packet){

    }

    protected Packet readPacket() throws BadPacketException, IOException {
        try {
            return (Packet) input.readObject();
        } catch (ClassNotFoundException e) {
            throw new BadPacketException("Received packet of an unknown type. Also means IT IS NOT a GBPacket. In fact, it is of an unidentified class that does not exist on this side.");
        } catch (ClassCastException e) {
            throw new BadPacketException("Received packet of an unexpected type. It is not of type GBPacket.");
        }
    }

    @Override
    public void close(){
        dropConnection();
        GBUILibGlobals.removeShutdownCommand(this::dropConnection);
    }

    private void handShake(){

    }

    protected void handShakeReceive(ActionHandler.PacketOut packet){

    }

    private Timer heart;
    private Instant lastSent;
    private final long heartBeatDelay;
    private final boolean alwaysBeat;

    private class heartBeatTask extends TimerTask{

        @Override
        public void run() {
            if (alwaysBeat) {
                heartBeat();
                heart.schedule(new heartBeatTask(), heartBeatDelay);
            }
            else{
                long sinceLast = Duration.between(lastSent, Instant.now()).toMillis();
                if (sinceLast < heartBeatDelay){
                    heart.schedule(new heartBeatTask(), heartBeatDelay-sinceLast);
                }
                else{
                    heartBeat();
                    heart.schedule(new heartBeatTask(), heartBeatDelay);
                }
            }
        }
    }

    private void heartBeat(){
        sendPacket(manager.heartBeat());
        lastSent = Instant.now();
    }

    public void ack(int[] ids, String packetType){
        manager.ack(ids, packetType);
    }

    public void smartAck(int[] IDs, String originalPacketType, Object content, String packetType, String contentType, String ackContentType){
        manager.smartAck(IDs, originalPacketType, content, packetType, contentType, ackContentType);
    }

    private boolean allowNoAck;

    public boolean allowNoAck(){
        return allowNoAck;
    }
}