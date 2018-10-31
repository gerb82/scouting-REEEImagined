package utilities.GBSockets;

import javafx.scene.shape.Rectangle;
import utilities.GBUILibGlobals;
import utilities.ProgramWideVariable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class GBSocket{

    private static Selector selector;
    private boolean isInvalid;
    private PacketManager manager;
    private SocketChannel socket;
    private boolean autoReconnect;
    private boolean connectionTimeout;
    private boolean linger;
    private SocketAddress adress;
    private ObjectInputStream input;
    private ObjectOutputStream output;

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
                socket.configureBlocking(false);
                socket.register(selector, SelectionKey.OP_READ);
            }
        } catch (IOException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    public void finalize(){
        GBUILibGlobals.removeShutdownCommand(this::dropConnection);
    }

    public GBSocket(){
        if(GBUILibGlobals.unsafeSockcets()) {
            isInvalid = true;
        } else {
            throw new UnsafeSocketException("There was an attempt to create an unsafe socket, even though unsafe sockets are disabled");
        }
    }

    public GBSocket(boolean autoReconnect, boolean revive, ActionHandler handler){
        isInvalid = false;
    }

    public synchronized void sendPacket(Packet... packets){
        if(GBUILibGlobals.unsafeSockcets()){

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
        if(!GBUILibGlobals.unsafeSockcets()) {
            manager.sendAsPacket(content, contentType, packetType);
        }
        else {
            throw new UnsafeSocketException("Cannot send the content as a packet, as this socket is not a proper GBSocket, and as such, does not have a PacketManager. To send a packet, you will have to construct and send it yourself");
        }
    }
}