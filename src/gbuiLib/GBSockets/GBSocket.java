package gbuiLib.GBSockets;

import gbuiLib.AssertionYouDimwitException;
import gbuiLib.GBUILibGlobals;
import gbuiLib.SmartAssert;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
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

public class GBSocket implements Closeable{


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
    protected PacketLogger logger;
    private ActionHandler handler;
    private boolean isUnsafe;
    private PacketManager manager;
    protected DatagramChannel socket;
    protected int socketIDServerSide = -1;
    protected int programWideSocketID = -1;
    private int connectionTimeout;
    private SocketAddress address;
    private boolean server;
    protected GBServerSocket parent;
    private int maxReceiveSize;
    private int maxSendSize;
    private HashSet<String> sendTypes;
    private boolean autoReconnect;
    private String connectionType;
    private Instant lastReceived;
    private Timer reconnecter;
    protected SimpleIntegerProperty pingInMillis = new SimpleIntegerProperty(0);
    public ObservableValue<Integer> ping = pingInMillis.asObject();
    private Timer timer;
    private int lastTimeout;
    private ChangeListener<Boolean> connectedListener;


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

    protected boolean socketConnect(Packet packet) throws IllegalArgumentException{
        if(!connected.get()) {
            try {
                if (PacketLogger.logsRepository == null) {
                    PacketLogger.logsRepository = GBUILibGlobals.getLogsDirectory();
                    PacketLogger.logsRepository.createNewFile();
                }
                logger = new PacketLogger(this);
                if(!server) {
                    socket = DatagramChannel.open();
                    socket.configureBlocking(false);
                }
                if (connect(address)) {
                    try {
                        if (server ? !serverHandShake(packet) : !handShake()) {
                            socketIDServerSide = -1;
                            logger.close();
                            return false;
                        }
                    } catch (RuntimePortUnreachable e){
                        socketIDServerSide = -1;
                        logger.close();
                        return false;
                    }
                    logger.setSocketID(socketIDServerSide);
                    if(!server) {
                        timer = new Timer();
                        manager = new PacketManager(sendTypes, socketIDServerSide, this, handler, logger, timer);
                        selector.registerSocket(this);
                        heart = new Timer();
                        heart.scheduleAtFixedRate(new HeartBeatTask(), heartBeatDelay, heartBeatDelay);
                        logger.packets.getLine(true, new int[]{-1}).discardToLog();
                        PacketLogger.LogLine line = logger.packets.getLine(false, new int[]{-1, socketIDServerSide});
                        line.setStatus(PacketLogger.PacketStatus.RECEIVED);
                    } else {
                        timer = parent.askForDiscarder();
                        manager = new PacketManager(sendTypes, socketIDServerSide, this, handler, logger, timer);
                        logger.packets.getLine(false, new int[]{-1}).discardToLog();
                        PacketLogger.LogLine line = logger.packets.getLine(true, new int[]{-1, socketIDServerSide});
                        line.setStatus(PacketLogger.PacketStatus.RECEIVED_DONE);
                        line.discardToLog();
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

    private boolean connect(SocketAddress address){
        try {
            if(!server) {
                socket.connect(address);
            }
            return true;
        } catch (ClosedChannelException e) {
            new IllegalAccessError("The method GBSocket.connect() was invoked by a method other than GBSocket.socketConnect()");
        } catch (IOException e) {
            new IOException("Something went wrong with the socket ;-;", e);
        }
        return false;
    }

    public boolean startConnection(){
        return socketConnect(null);
    }

    // done
    public void finalize(){
        close();
    }

    public void disconnect(){
        try {
            if(socket.isConnected() && server){
                socket.send(ByteBuffer.wrap(("die " + socketIDServerSide).getBytes()), address);
            }
            handler.connectionClosed(this);
            socket.close();
            logger.close();
            logger = null;
            socketIDServerSide = -1;
            connected.set(false);
            if(!server) {
                key.cancel();
                heart.cancel();
                timer.cancel();
                try {
                    reconnecter.cancel();
                    connected.removeListener(connectedListener);
                } catch (NullPointerException e) {}
            }
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
        disconnect();
    }

    public void stopServerSideConnection(){
        if(server){
            parent.removeSelectorChannel(this);
            close();
        } else {
            throw new IllegalStateException("This socket is not a server socket, and yet it was attempted to be closed through stopServerSideConnection");
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
        if(!connected.get()){
            this.address = address;
            return;
        }
        throw new IllegalAccessException("Cannot set the address on an already connected socket.");
    }

    // safe socket

    public GBSocket(SocketAddress address, String connectionType, boolean autoReconnectArg, SelectorManager selector, ActionHandler handler, SocketConfig config, boolean allowNoAck){
        this(address, connectionType, autoReconnectArg, selector, handler, config, null, allowNoAck);
    }

    // actual socket constructor
    protected GBSocket(SocketAddress address, String connectionType, boolean autoReconnectArg, SelectorManager selector, ActionHandler handler, SocketConfig config, GBServerSocket parent, boolean allowNoAck){
        if(parent != null){
            server = true;
            this.parent = parent;
            heartBeatDelay = -1;
        } else {
            this.heartBeatDelay = config.getHeartBeatDelay();
            if(this.heartBeatDelay < 1){
                throw new IllegalArgumentException("Heartbeat delay cannot be negavite");
            }
            this.autoReconnect = autoReconnectArg;
            if(autoReconnect) {
                this.reconnecter = new Timer();
                this.lastTimeout = 0;
                this.connectedListener = (observable, oldValue, newValue) -> {
                    if (!newValue && autoReconnect) {
                        reconnecter.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if(socketConnect(null)){
                                    lastTimeout = 0;
                                } else {
                                    lastTimeout += GBUILibGlobals.getConnectionTimeoutIncrement();
                                }
                            }
                        }, lastTimeout);
                    }
                };
                this.connected.addListener(connectedListener);
            }
            this.connectionTimeout = config.shouldConnectionTimeout();
            this.allowNoAck = allowNoAck;
            server = false;
        }
        this.connectionType = connectionType;
        this.selector = selector;
        this.address = address;
        this.handler = handler;
        isUnsafe = false;
        programWideSocketID = GBUILibGlobals.newSocketFormed();
        maxReceiveSize = config.getMaxPacketReceiveSize();
    }

    // serverSocket reader
    protected GBSocket(int port, GBServerSocket parent, int maxReceiveSize) {
        this.server = true;
        this.parent = parent;
        this.address = new InetSocketAddress(port);
        heartBeatDelay = -1;
        isUnsafe = false;
        this.maxReceiveSize = maxReceiveSize;
        try {
            this.socket = DatagramChannel.open();
            this.socket.socket().setReuseAddress(true);
            this.socket.bind(address);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void setKey(SelectionKey key) {
        this.key = key;
    }

    protected SelectionKey getKey() {
        return key;
    }

    // unsafe, done
    public synchronized void sendPackets(byte[]... packets) throws IOException {
        if(isUnsafe){
            for(byte[] packet : packets){
                socket.send(ByteBuffer.wrap(packet), address);
            }
        } else {
            throw new UnsafeSocketException("There was an attempt to send a packet directly and not through a packet manager, even though unsafe sockets are disabled");
        }
    }

    // done
    protected synchronized void sendPacket(PacketLogger.LogLine logLine) throws BadPacketException {
        boolean wasSent = logLine.getWasSent();
        try {
            Packet toSend = wasSent ? logLine.getPacket() : logLine.getResponse();
            ByteArrayOutputStream sendInput = new ByteArrayOutputStream();
            ObjectOutputStream sendObjInput = new ObjectOutputStream(sendInput);
            sendObjInput.writeObject(toSend);
            sendObjInput.flush();
            byte[] bytes = sendInput.toByteArray();
            sendObjInput.close();
            if(bytes.length < maxSendSize) {
                if(server) {
                    socket.send(ByteBuffer.wrap(bytes), address);
                } else {
                    socket.write(ByteBuffer.wrap(bytes));
                }
            } else{
                logLine.setStatus(PacketLogger.PacketStatus.SEND_ERRORED);
                throw new BadPacketException("Packet is too big to be sent");
            }
            if(!logLine.getPacket().isImportant()){
                logLine.setStatus(PacketLogger.PacketStatus.SENT);
            } else {
                logLine.setStatus(wasSent ? PacketLogger.PacketStatus.WAITING : PacketLogger.PacketStatus.RECEIVED_DONE);
            }
        } catch (IOException e) {
            e.printStackTrace();
            logLine.setStatus(wasSent ? PacketLogger.PacketStatus.SEND_ERRORED : PacketLogger.PacketStatus.RECEIVED_ERRORED);
        }
    }

    public PacketLogger.ObservablePacketStatus sendAsPacket(Object content, String contentType, String packetType, boolean mustArrive) throws BadPacketException{
        if(!isUnsafe) {
            if(connected.get()) {
                if(packetType == ActionHandler.DefaultPacketTypes.HandShake.toString()){
                    throw new IllegalArgumentException("Cannot use packet type HandShake, as it is reserved for GBSocket actions.");
                }
                return manager.sendAsPacket(content, contentType, packetType, mustArrive);
            } else {
                throw new IllegalStateException("Socket is not connected, cannot send packet.");
            }
        }
        else {
            throw new UnsafeSocketException("Cannot send the content as a packet, as this socket is not a proper GBSocket, and as such, does not have a PacketManager. To send a packet, you will have to construct and send it yourself");
        }
    }

    protected void receivePacket(Packet packet){
        if(server){
            if(packet.receivedFrom.equals(address) && socketIDServerSide == (packet.getIds().length == 3 ? packet.getIds()[2] : packet.getIds()[1])){
                this.address = packet.receivedFrom;
            }
            parent.ackSocket(this);
        }
        manager.receivePacket(packet);
    }

    protected synchronized Packet readPacket() {
        SocketAddress senderAddress;
        Packet output;
        while(true) {
            try {
                ByteBuffer buffer = ByteBuffer.wrap(new byte[maxReceiveSize]);
                if(server) {
                    senderAddress = socket.receive(buffer);
                } else {
                    socket.read(buffer);
                    senderAddress = address;
                }
                int position = buffer.position();
                buffer.rewind();
                byte[] bytes = new byte[position];
                buffer.get(bytes, 0, position);
                lastReceived = Instant.now();
                try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes))){
                    Object attempt = input.readObject();
                    if(attempt instanceof Packet){
                        output = (Packet) attempt;
                    } else {
                        if(attempt instanceof String){
                            if(attempt.equals("you are already dead " + socketIDServerSide) || attempt.equals("die " + socketIDServerSide)){
                                logger.connectionRemotelyTerminated();
                                disconnect();
                                return null;
                            }
                        }
                        PacketLogger.suspiciousPacket(senderAddress, this);
                        continue;
                    }
                    output.receivedFrom = senderAddress;
                    break;
                } catch (Exception e) {
                    PacketLogger.suspiciousPacket(senderAddress, this);
                }
            } catch (PortUnreachableException e){
                throw new RuntimePortUnreachable(e);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return output;
    }

    private void getAuthData(Stack<Object> stack){
        handler.getConnectionData(stack, server);
    }

    private boolean validateAuthData(Stack<Object> params){
        return handler.checkConnectionData(params, server);
    }

    private boolean handShake(){
        try {
            Stack<Object> stack = new Stack<>();
            getAuthData(stack);
            stack.add(maxReceiveSize);
            stack.add(handler.getHandledTypes(this));
            Packet packet = new Packet(stack, new int[]{-1}, connectionType, ActionHandler.DefaultPacketTypes.HandShake.toString(), !isUnsafe);
            PacketLogger.LogLine toSend = logger.followPacket(packet, true);
            int attempts = GBUILibGlobals.getPacketSendAttempts();
            int intervals = (GBUILibGlobals.getTimeToSendPacket()/attempts);
            Packet receivedShake = null;
            maxSendSize = maxReceiveSize;
            for(int i = 0; i < attempts; i++) {
                sendPacket(toSend);
                Instant start = Instant.now();
                while(Instant.now().toEpochMilli() - start.toEpochMilli() < intervals){
                    try {
                        Thread.sleep(intervals - (Instant.now().toEpochMilli() - start.toEpochMilli()));
                    } catch (InterruptedException e) {}
                }
                receivedShake = readPacket();
                if(receivedShake != null && receivedShake.getPacketType().equals(ActionHandler.DefaultPacketTypes.HandShake.toString())){
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
            sendTypes = (HashSet<String>)responseContent.pop();
            maxSendSize = (Integer) responseContent.pop();
            socketIDServerSide = receivedShake.getIds()[1];
            SmartAssert.makeSure(validateAuthData(responseContent), "The authentication data sent from the server did not match the expected authentication data for this connection type");
            PacketLogger.LogLine response = logger.followPacket(receivedShake, false);
            response.setResponse(new Packet(null, new int[]{-1, socketIDServerSide}, ActionHandler.DefaultPacketTypes.Ack.toString(), ActionHandler.DefaultPacketTypes.HandShake.toString()));
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
            sendTypes = (HashSet<String>) receivedShakeContent.pop();
            maxSendSize = (Integer) receivedShakeContent.pop();
            SmartAssert.makeSure(validateAuthData(receivedShakeContent), "The authentication data sent from the client did not match the expected authentication data for this connection type");
            PacketLogger.LogLine response = logger.followPacket(receivedShake, false);
            Stack<Object> stack = new Stack<>();
            getAuthData(stack);
            stack.add(maxReceiveSize);
            stack.add(handler.getHandledTypes(this));
            response.setResponse(new Packet(stack, new int[]{-1, socketIDServerSide}, connectionType, ActionHandler.DefaultPacketTypes.HandShake.toString(), !isUnsafe));
            PacketLogger.LogLine toSend = logger.followPacket(response.getResponse(), true);
            int attempts = GBUILibGlobals.getPacketSendAttempts();
            int intervals = (GBUILibGlobals.getTimeToSendPacket() / attempts);
            for (int i = 0; i < attempts; i++) {
                sendPacket(toSend);
                Instant start = Instant.now();
                while (Instant.now().toEpochMilli() - start.toEpochMilli() < intervals) {
                    try {
                        Thread.sleep(intervals - (Instant.now().toEpochMilli() - start.toEpochMilli()));
                    } catch (InterruptedException e) {
                    }
                }
                if(toSend.getResponse() != null){
                    return true;
                }
            }
            handler.connectionClosed(this);
            return false;
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
                disconnect();
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