package utilities.GBSockets;

import utilities.GBUILibGlobals;
import utilities.SmartAssert;

import java.io.*;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.time.Duration;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;

public class GBSocket implements AutoCloseable{

    // done
    public static class SocketConfig{
        private Integer heartBeatDelay;
        private Boolean alwaysBeat;
        private Integer connectionTimeout;

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

        public int shouldConnectionTimeout() {
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
    private GBServerSocket parent;
    private int maxReceiveSize;


    private boolean autoReconnect;

    protected DatagramChannel getChannel(){
        return socket;
    }

    // done
    public boolean isServer(){
        return server;
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

    // done
    public void finalize(){
        close();
    }

    // done
    @Override
    public void close(){
        autoReconnect = false;
        try {
            socket.close();
        } catch (IOException e) {
            new IOException("Failed to close the socket.", e).printStackTrace();
        }
    }

    public void stopServerConnection(){
        if(server){
            parent.removeSelectorChannel(this);
        }
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
        try {
            input = new PipedInputStream();
            output = new PipedOutputStream();
            objInput = new ObjectInputStream(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // unsafe, done
    public synchronized void sendPackets(Packet... packets) throws IOException {
        if(GBUILibGlobals.unsafeSockets() && isUnsafe){
            for(Packet packet : packets){
                socket.write(ByteBuffer.wrap(packet.toString().getBytes()));
            }
        } else {
            throw new UnsafeSocketException("There was an attempt to send a packet directly and not through a packet manager, even though unsafe sockets are disabled");
        }
    }

    // done
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
    private PipedInputStream input;
    private PipedOutputStream output;
    private ObjectInputStream objInput;

    protected Packet readPacket() throws BadPacketException {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(maxReceiveSize);
            socket.read(buffer);
            output.write(buffer.array());
            output.flush();
            return (Packet) objInput.readObject();
        } catch (ClassCastException | ClassNotFoundException e) {
            throw new BadPacketException("Received packet of an unexpected type. It is not of type GBPacket.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void handShake(){
        SmartAssert.makeSure(packet.isErrorChecked(), "A safe socket CANNOT handle a non-errorChecked packet. The safe sockets place equal trust on both sides for error-checking their sockets, and as such cannot work if the sending side didn't error check it's own packets.");
    }

    protected void handShakeReceive(ActionHandler.PacketOut packet){

    }

    private Timer heart;
    private Instant lastSent;
    private final long heartBeatDelay;
    private final boolean alwaysBeat;

    // done
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

    // done
    private void heartBeat(){
        if(manager.heartBeat()){
            lastSent = Instant.now();
        }
    }

    // done
    public void ack(int[] ids, String packetType){
        manager.ack(ids, packetType);
    }

    // done
    public void smartAck(int[] IDs, String originalPacketType, Object content, String packetType, String contentType) throws BadPacketException {
        manager.smartAck(IDs, originalPacketType, content, contentType, packetType);
    }

    private boolean allowNoAck;

    public boolean allowNoAck(){
        return allowNoAck;
    }
}