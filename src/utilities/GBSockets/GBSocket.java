package utilities.GBSockets;

import utilities.GBUILibGlobals;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.time.Duration;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;

public class GBSocket implements AutoCloseable{

    private SelectorManager selector;
    private boolean isUnsafe;
    private PacketManager manager;
    private SocketChannel socket;
    private boolean autoReconnect;
    private boolean connectionTimeout;
    private boolean linger;
    private SocketAddress adress;
    private ObjectInputStream input;
    private ObjectOutputStream output;

    protected SocketChannel getChannel(){
        return socket;
    }

    protected void SocketConnect(){
        try {
            socket = SocketChannel.open();
            socket.socket().setReceiveBufferSize(GBUILibGlobals.getSocketReceiveStreamSize());
            if(connectionTimeout) {
                socket.socket().setSoTimeout(GBUILibGlobals.getSocketTimeout());
            }
            if(socket.connect(adress)){
                output = new ObjectOutputStream(socket.socket().getOutputStream());
                input = new ObjectInputStream(socket.socket().getInputStream());
                selector.registerSocket(this);
            }
        } catch (IOException e) {
            new IOException("Couldn't connect the GBSocket", e).printStackTrace();
        }
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

    public GBSocket(){
        if(GBUILibGlobals.unsafeSockcets()) {
            isUnsafe = true;
        } else {
            throw new UnsafeSocketException("There was an attempt to create an unsafe socket, even though unsafe sockets are disabled");
        }
    }

    public GBSocket(boolean autoReconnect, SelectorManager selector, ActionHandler handler){
        this.selector = selector;
        isUnsafe = false;
    }

    public synchronized void sendPacket(Packet... packets){
        if(GBUILibGlobals.unsafeSockcets() && isUnsafe){
            for(Packet packet : packets){
                sendPacket(packet);
            }
        } else {
            throw new UnsafeSocketException("There was an attempt to send a packet directly and not through a packet manager, even though unsafe sockets are disabled");
        }
    }

    protected synchronized void sendPacket(Packet packet){
        try {
            output.writeObject(packet);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendAsPacket(Object content, String contentType, String packetType) throws BadPacketException{
        if(!GBUILibGlobals.unsafeSockcets() && !isUnsafe) {
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

    private Timer heart;
    private Instant lastSent;
    private final long heartBeatDelay;
    private final boolean alwaysBeat;

    private class heartBeatTask extends TimerTask{

        @Override
        public void run() {
            if (alwaysBeat) {
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
    }
}