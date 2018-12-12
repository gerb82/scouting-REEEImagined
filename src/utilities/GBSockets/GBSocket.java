package utilities.GBSockets;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import utilities.AssertionYouDimwitException;
import utilities.GBUILibGlobals;
import utilities.SmartAssert;

import java.io.*;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.time.Instant;
import java.util.HashSet;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

public class GBSocket implements AutoCloseable{


    // done
    public static class SocketConfig{
        private Integer heartBeatDelay;
        private Integer connectionTimeout;
        private Integer maxReceiveSize;

        public int getHeartBeatDelay() {
            if(heartBeatDelay == null){
                return GBUILibGlobals.getHeartBeatRate();
            }
            return heartBeatDelay;
        }

        public void setHeartBeatDelay(int heartBeatDelay) {
            this.heartBeatDelay = heartBeatDelay;
        }

        public int shouldConnectionTimeout() {
            if(connectionTimeout == null){
                return GBUILibGlobals.getSocketTimeout();
            }
            return connectionTimeout;
        }

        public void setConnectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }

        public int getMaxPacketReceiveSize() {
            if(maxReceiveSize == null){
                return GBUILibGlobals.getMaxReceivePacketSize();
            }
            return maxReceiveSize;
        }

        public void setMaxReceiveSize(int maxReceiveSize){
            this.maxReceiveSize = maxReceiveSize;
        }
    }

    private SelectorManager selector;
    private SelectionKey key;
    private PacketLogger logger;
    private ActionHandler handler;
    private boolean isUnsafe;
    private PacketManager manager;
    protected DatagramChannel socket;
    protected int socketID = -1;
    private int connectionTimeout;
    private SocketAddress address;
    private boolean server;
    private GBServerSocket parent;
    private int maxReceiveSize;
    private int maxSendSize;
    private HashSet<String> sendTypes;
    private boolean autoReconnect;
    private String connectionType;
    private Instant lastReceived;
    protected SimpleIntegerProperty pingInMillis = new SimpleIntegerProperty(0);
    public ObservableValue<Integer> ping = pingInMillis.asObject();

    protected DatagramChannel getChannel(){
        return socket;
    }

    public HashSet<String> getSendTypes(){
        return new HashSet<String>(sendTypes);
    }

    // done
    public boolean isServer(){
        return server;
    }

    protected synchronized boolean socketConnect(Packet packet) throws IllegalArgumentException{
        if(connected.get()) {
            try {
                logger = new PacketLogger(this);
                if(!server) {
                    socket = DatagramChannel.open();
                }
                if (connect(address)) {
                    socketID = GBUILibGlobals.newSocketFormed();
                    if (PacketLogger.logsRepository == null) {
                        PacketLogger.logsRepository = GBUILibGlobals.getSocketLogsDirectory();
                    }
                    logger = new PacketLogger(this);
                    if(server ? !serverHandShake(packet) : !handShake()){
                        socketID = -1;
                        logger.close();
                        return false;
                    }
                    manager = new PacketManager(sendTypes, socketID, this, handler, logger);
                    if(!server) {
                        key = selector.registerSocket(this);
                        heart = new Timer();
                        heart.scheduleAtFixedRate(new HeartBeatTask(), heartBeatDelay, heartBeatDelay);
                    }
                    connected.set(true);
                    return true;
                } else {
                    throw new IllegalArgumentException("The given SocketAddress could not complete the handshake");
                }
            } catch (IOException e) {
                new IOException("Couldn't connect the GBSocket", e).printStackTrace();
            }
        }
        throw new IllegalStateException("Cannot connect an already connected socket");
    }

    private synchronized boolean connect(SocketAddress address){
        try {
            socket.connect(address);
            return true;
        } catch (ClosedChannelException e) {
            new IllegalAccessError("The method GBSocket.connect() was invoked by a method other than GBSocket.socketConnect()");
        } catch (IOException e) {
            new IOException("Something went wrong with the socket ;-;", e);
        }
        return false;
    }

    public void startConnection(){
        socketConnect(null);
    }

    // done
    public void finalize(){
        close();
    }

    public void stop(){
        try {
            heart.cancel();
            handler.connectionClosed(this);
            socket.close();
            key.cancel();
            logger.close();
            logger = null;
            socketID = -1;
            connected.set(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private SimpleBooleanProperty connected = new SimpleBooleanProperty(false);
    public ObservableValue<Boolean> isConnected = connected.asObject();

    // done
    @Override
    public void close(){
        autoReconnect = false;
        try {
            input.close();
            output.close();
        } catch (IOException e) {
            new IOException("Failed to close the socket.", e).printStackTrace();
        }
        stop();
    }

    public void stopServerConnection(){
        if(server){
            parent.removeSelectorChannel(this);
        }
    }

    // unsafe socket
    public GBSocket(SocketAddress address){
        if(GBUILibGlobals.unsafeSockets()) {
            heartBeatDelay = -1;
            isUnsafe = true;
            this.address = address;
        } else {
            throw new UnsafeSocketException("There was an attempt to create an unsafe socket, even though unsafe sockets are disabled");
        }
    }

    public DatagramChannel getSocket() {
        if(isUnsafe){
            return socket;
        }
        throw new UnsafeSocketException("There was an attempt to directly access the DatagramChannel of the socket, even though it is not an unsafe socket.");
    }

    public void setAddress(SocketAddress address) throws IllegalAccessException {
        if(connected.get()){
            this.address = address;
            return;
        }
        throw new IllegalAccessException("Cannot set the address on an already connected socket.");
    }

    // safe socket

    public GBSocket(SocketAddress address, String connectionType, boolean autoReconnectArg, SelectorManager selector, ActionHandler handler, SocketConfig config, GBServerSocket parent, boolean allowNoAck){
        if(parent != null){
            server = true;
            this.parent = parent;
            heartBeatDelay = -1;
        } else {
            this.heartBeatDelay = config.getHeartBeatDelay();
            this.autoReconnect = autoReconnectArg;
            this.connected.addListener((observable, oldValue, newValue) -> {
                if(!newValue && autoReconnect){
                    socketConnect(null);
                }
            });
            this.connectionTimeout = config.shouldConnectionTimeout();
            this.allowNoAck = allowNoAck;
            server = false;
            buffer = ByteBuffer.allocate(maxReceiveSize);
        }
        this.connectionType = connectionType;
        this.selector = selector;
        this.address = address;
        this.handler = handler;
        isUnsafe = false;
        maxReceiveSize = config.getMaxPacketReceiveSize();
        try {
            input = new PipedInputStream();
            output = new PipedOutputStream();
            objInput = new ObjectInputStream(input);
            input.connect(output);

            sendOut = new PipedInputStream();
            sendInput = new PipedOutputStream();
            sendObjInput = new ObjectOutputStream(sendInput);
            sendOut.connect(sendInput);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void setKey(SelectionKey key) {
        this.key = key;
    }

    // unsafe, done
    public synchronized void sendPackets(byte[]... packets) throws IOException {
        if(isUnsafe){
            for(byte[] packet : packets){
                socket.write(ByteBuffer.wrap(packet));
            }
        } else {
            throw new UnsafeSocketException("There was an attempt to send a packet directly and not through a packet manager, even though unsafe sockets are disabled");
        }
    }

    private PipedOutputStream sendInput;
    private ObjectOutputStream sendObjInput;
    private PipedInputStream sendOut;

    // done
    protected synchronized void sendPacket(PacketLogger.LogLine logLine) throws BadPacketException {
        boolean wasSent = logLine.getWasSent();
        try {
            Packet toSend = wasSent ? logLine.getPacket() : logLine.getResponse();
            sendObjInput.writeObject(toSend);
            sendObjInput.flush();
            byte[] bytes = new byte[sendOut.available()];
            sendOut.read(bytes);
            if(bytes.length < maxSendSize) {
                socket.write(ByteBuffer.wrap(bytes));
            } else{
                logLine.setStatus(PacketLogger.PacketStatus.SEND_ERRORED);
                throw new BadPacketException("Packet is too big to be sent");
            }
            if(!logLine.getPacket().getResend()){
                logLine.setStatus(PacketLogger.PacketStatus.SENT);
            } else {
                logLine.setStatus(wasSent ? PacketLogger.PacketStatus.WAITING : PacketLogger.PacketStatus.RECEIVED_DONE);
            }
        } catch (IOException e) {
            e.printStackTrace();
            logLine.setStatus(wasSent ? PacketLogger.PacketStatus.SEND_ERRORED : PacketLogger.PacketStatus.RECEIVED_ERRORED);
        }
    }

    public void sendAsPacket(Object content, String contentType, String packetType, boolean mustArrive) throws BadPacketException{
        if(!isUnsafe) {
            if(connected.get()) {
                manager.sendAsPacket(content, contentType, packetType, mustArrive);
            } else {
                throw new IllegalStateException("Socket is not connected, cannot send packet");
            }
        }
        else {
            throw new UnsafeSocketException("Cannot send the content as a packet, as this socket is not a proper GBSocket, and as such, does not have a PacketManager. To send a packet, you will have to construct and send it yourself");
        }
    }

    protected void receivePacket(Packet packet){
        manager.receivePacket(packet);
    }

    private PipedInputStream input;
    private PipedOutputStream output;
    private ObjectInputStream objInput;
    protected ByteBuffer buffer;

    protected synchronized Packet readPacket() throws BadPacketException {
        try {
            int counter = socket.read(buffer);
            if(counter == 0){
                return null;
            }
            buffer.flip();
            output.write(buffer.array());
            output.flush();
            buffer.clear();
            lastReceived = Instant.now();
            return (Packet) objInput.readObject();
        } catch (ClassCastException | ClassNotFoundException e) {
            throw new BadPacketException("Received packet of an unexpected type. It is not of type GBPacket.");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void getAuthData(Stack<Object> stack){
        handler.getConnectionData(stack, server);
    }

    private boolean validateAuthData(Stack<Object> params){
        return handler.checkConnectionData(params, server);
    }

    private synchronized boolean handShake(){
        try {
            GBSocket thisSocket = this;
            Packet packet = new Packet(new Stack<Object>(){{getAuthData(this); add(maxReceiveSize); add(handler.getHandledTypes(thisSocket).toArray());}}, new int[]{-1}, connectionType, ActionHandler.DefaultPacketTypes.HandShake.toString(), !isUnsafe);
            PacketLogger.LogLine toSend = logger.followPacket(packet, true);
            int attempts = GBUILibGlobals.getPacketSendAttempts();
            int intervals = (1000*GBUILibGlobals.getTimeToSendPacket()/attempts);
            Packet receivedShake = null;
            for(int i = 0; i < attempts; i++) {
                sendPacket(toSend);
                Instant start = Instant.now();
                while(Instant.now().toEpochMilli() - start.toEpochMilli() < intervals){
                    try {
                        Thread.sleep(intervals - (Instant.now().toEpochMilli() - start.toEpochMilli()));
                    } catch (InterruptedException e) {}
                }
                receivedShake = readPacket();
                if(receivedShake != null){
                    break;
                }
                if(i == attempts-1){
                    handler.connectionClosed(this);
                    return false;
                }
            }
            toSend.setResponse(receivedShake);
            SmartAssert.makeSure(ActionHandler.DefaultPacketTypes.valueOf(receivedShake.getPacketType()).equals(ActionHandler.DefaultPacketTypes.HandShake), "Received a non-handshake packet before handshake was complete");
            SmartAssert.makeSure(receivedShake.isErrorChecked(), "A safe socket CANNOT handle a non-errorChecked packet. The safe sockets place equal trust on both sides for error-checking their sockets, and as such cannot work if the sending side didn't error check it's own packets.");
            SmartAssert.makeSure(connectionType == receivedShake.getContentType(), "Connection type must match between both sockets in order to avoid unexpected behaviour");
            Stack responseContent = (Stack) receivedShake.getContent();
            maxSendSize = (int) responseContent.pop();
            sendTypes = (HashSet<String>)responseContent.pop();
            SmartAssert.makeSure(validateAuthData(responseContent), "The authentication data sent from the server did not match the expected authentication data for this connection type");
            PacketLogger.LogLine response = logger.followPacket(receivedShake, false);
            response.setResponse(new Packet(null, new int[]{-1}, ActionHandler.DefaultPacketTypes.Ack.toString(), ActionHandler.DefaultPacketTypes.HandShake.toString()));
            sendPacket(response);
            return true;
        } catch (BadPacketException e) {
            throw new IllegalStateException("The handshake created a packet that could not be sent.", e);
        } catch (AssertionYouDimwitException e) {
            e.printStackTrace();
            handler.connectionClosed(this);
            return false;
        }
    }

    private boolean serverHandShake(Packet receivedShake) {
        try {
            SmartAssert.makeSure(ActionHandler.DefaultPacketTypes.valueOf(receivedShake.getPacketType()).equals(ActionHandler.DefaultPacketTypes.HandShake), "Received a non-handshake packet before handshake was complete");
            SmartAssert.makeSure(receivedShake.isErrorChecked(), "A safe socket CANNOT handle a non-errorChecked packet. The safe sockets place equal trust on both sides for error-checking their sockets, and as such cannot work if the sending side didn't error check it's own packets.");
            SmartAssert.makeSure(parent.connectionTypes.containsKey(receivedShake.getContentType()), "Connection type must match between both sockets in order to avoid unexpected behaviour");
            connectionType = receivedShake.getContentType();
            handler = parent.connectionTypes.get(connectionType);
            Stack receivedShakeContent = (Stack) receivedShake.getContent();
            maxSendSize = (int) receivedShakeContent.pop();
            sendTypes = (HashSet<String>)receivedShakeContent.pop();
            SmartAssert.makeSure(validateAuthData(receivedShakeContent), "The authentication data sent from the client did not match the expected authentication data for this connection type");
            PacketLogger.LogLine response = logger.followPacket(receivedShake, false);
            GBSocket thisSocket = this;
            response.setResponse(new Packet(new Stack<Object>(){{getAuthData(this); add(maxReceiveSize); add(handler.getHandledTypes(thisSocket).toArray());}}, new int[]{-1}, connectionType, ActionHandler.DefaultPacketTypes.HandShake.toString(), !isUnsafe));
            PacketLogger.LogLine toSend = logger.followPacket(response.getResponse(), true);
            int attempts = GBUILibGlobals.getPacketSendAttempts();
            int intervals = (1000 * GBUILibGlobals.getTimeToSendPacket() / attempts);
            Packet ack = null;
            for (int i = 0; i < attempts; i++) {
                sendPacket(toSend);
                Instant start = Instant.now();
                while (Instant.now().toEpochMilli() - start.toEpochMilli() < intervals) {
                    try {
                        Thread.sleep(intervals - (Instant.now().toEpochMilli() - start.toEpochMilli()));
                    } catch (InterruptedException e) {
                    }
                }
                ack = readPacket();
                while(ack != null){
                    if(ack.getPacketType().equals(ActionHandler.DefaultPacketTypes.Ack)){
                        break;
                    }
                    else {
                        ack = readPacket();
                    }
                }
                if (i == attempts - 1) {
                    handler.connectionClosed(this);
                    return false;
                }
            }
            toSend.setResponse(ack);
            return true;
        } catch (BadPacketException e) {
            throw new IllegalStateException("The handshake created a packet that could not be sent.", e);
        } catch (AssertionYouDimwitException e) {
            e.printStackTrace();
            handler.connectionClosed(this);
            return false;
        }
    }

    private Timer heart;
    private final long heartBeatDelay;

    // done
    private class HeartBeatTask extends TimerTask{

        @Override
        public void run() {
            manager.heartBeat();
            if(Instant.now().toEpochMilli() - lastReceived.toEpochMilli() >= connectionTimeout*1000) {
                stop();
            }
        }
    }

    // done
    public void ack(int[] ids, String packetType) throws BadPacketException {
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