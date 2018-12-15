package utilities.GBSockets;

import utilities.GBUILibGlobals;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

public class GBServerSocket implements AutoCloseable{

    // done
    @Override
    public void close() {
        listenToConnections = false;
        for(ProcessingThread fred : processingThreads){
            fred.interrupt();
        }
        for(int id : activeConnectionsMap.keySet()){
            activeConnectionsMap.get(id).close();
        }
        selector.close();
    }

    // done
    @Override
    protected void finalize() {
        close();
    }

    // done
    /**
     * A subclass of {@link Timer} used to check for when sockets time out and notify the containing {@link GBServerSocket} about such events.
     */
    private class SelectorTimeOutThread extends Timer{

        /**
         * Class used to schedule ackChecks on sockets.
         * Contains the time for the next check, as well as the socket to check at that time.
         * Time is measured in milliseconds since the opening of this {@link SelectorTimeOutThread}.
         */
        private class SocketWaitTime{
            /**
             * The time for the next check.
             */
            private long time;
            /**
             * The socket to check.
             */
            private final GBSocket socket;

            /**
             * Constructor used to create the first test on a socket.
             * @param socket The socket being checked.
             * @param time The time to check it.
             */
            private SocketWaitTime(GBSocket socket, long time){
                this.time = time;
                this.socket = socket;
            }

            /**
             * The method used to set the next test time on the socket (to avoid constantly creating new instances).
             * @param time Time for the next check.
             */
            private void setTime(long time) {
                this.time = time;
            }

            /**
             * The method used to get the time for the next test.
             * @return The time for the next test.
             */
            private long getTime() {
                return time;
            }

            /**
             * The method used to get the socket being checked.
             * @return The socket being checked.
             */
            private GBSocket getSocket() {
                return socket;
            }
        }

        /**
         * List containing all the sockets being followed.
         */
        private ArrayList<GBSocket> sockets;
        /**
         * The start time of this {@link SelectorTimeOutThread}.
         */
        private final Instant start;
        /**
         * A map containing the last recorded ack for all the sockets being followed.
         * The key is the socket, and the value is the last recorded ack in milliseconds since the start of this {@link SelectorTimeOutThread}.
         */
        private HashMap<GBSocket, Long> ack;
        /**
         * The amount of time a socket must not respond for in order to be recognised as timed out (in milliseconds).
         */
        private int timeOut;
        /**
         * A boolean representing whether this {@link SelectorTimeOutThread} is currently active.
         * This {@link SelectorTimeOutThread} will not count as active if it was not yet started/is already canceled.
         */
        private boolean active;

        /**
         * The constructor used to default all of this {@link SelectorTimeOutThread}'s saved values.
         */
        private SelectorTimeOutThread(){
            start = Instant.now();
            timeOut = GBUILibGlobals.getSocketTimeout()*1000;
            ack = new HashMap<>();
            sockets = new ArrayList<>();
            active = false;
        }

        /**
         * The method used to remove a socket from being followed.
         * @param socket The socket to stop following.
         */
        private void removeSocket(GBSocket socket){
            sockets.remove(socket);
            ack.remove(socket);
        }

        /**
         * The method used to add a socket to be followed.
         * @param socket The socket to start following.
         * @return If the socket was successfully added.
         */
        private boolean addSocket(GBSocket socket){
            if(active) {
                sockets.add(socket);
                ack.put(socket, calculateTime());
                scheduleAckCheck(new SocketWaitTime(socket, calculateTime() + timeOut));
                return true;
            }
            return false;
        }

        /**
         * The method used to notify this {@link SelectorTimeOutThread} that a socket just received an ack.
         * @param socket The socket that just received the ack.
         */
        private void ack(GBSocket socket){
            if (ack.containsKey(socket)) {
                ack.put(socket, calculateTime());
            }
        }

        /**
         * The method used to schedule a test for if the socket is not timed out.
         * @param ackCheck The {@link SocketWaitTime} that defines which socket will be checked and when.
         */
        private void scheduleAckCheck(SocketWaitTime ackCheck){
            if(active){
                if(sockets.contains(ackCheck.getSocket())){
                    if(ack.get(ackCheck.socket) - ackCheck.getTime() > timeOut){
                        removeSelectorChannel(ackCheck.getSocket());
                    }
                    else{
                        ackCheck.setTime(ack.get(ackCheck.getSocket()) + timeOut);
                        this.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                scheduleAckCheck(ackCheck);
                            }
                        }, ackCheck.getTime());
                    }
                }
            }
        }

        /**
         * An override of the {@link Timer#cancel()} method so that it also notifies this {@link SelectorTimeOutThread} that it has been canceled.
         */
        @Override
        public void cancel(){
            active = false;
            super.cancel();
        }

        /**
         * The method used to activate this {@link SelectorTimeOutThread}.
         */
        private void activate(){
            active = true;
        }

        /**
         * The method used to calculate how much time has passed since {@link SelectorTimeOutThread#start} in milliseconds.
         * @return The amount of time since {@link SelectorTimeOutThread#start} in milliseconds.
         */
        private long calculateTime(){
            return Duration.between(start, Instant.now()).toMillis();
        }
    }

    private SelectorTimeOutThread timeOutThread = new SelectorTimeOutThread();
    private SelectionKey readerKey;
    private SelectorManager selector;

    // done
    protected boolean initSelector(){
        if(selector != null) {
            timeOutThread.activate();
            initializeReceiveSplit();
            selector = new SelectorManager(true, toBeProcessed);
            return true;
        }
        return false;
    }

    // done
    protected boolean addSelectorChannel(GBSocket socket) {
        if (timeOutThread.addSocket(socket)) {
            activeConnectionsMap.put(socket.socketIDServerSide, socket);
            return true;
        }
        return false;
    }

    // done
    protected boolean removeSelectorChannel(GBSocket socket){
        try{
            activeConnectionsMap.remove(socket.socketIDServerSide, socket);
            timeOutThread.removeSocket(socket);
            potentialConnections--;
            return true;
        } catch (NullPointerException e){
            return false;
        }
    }

    // done
    //PacketSplittingOnInput
    protected static class PacketToProcess{

        private Packet packet;
        private GBServerSocket socket;
        protected PacketToProcess(Packet packet, GBServerSocket socket){
            this.packet = packet;
            this.socket = socket;
        }

        protected Packet getPacket() {
            return packet;
        }

        protected GBSocket getSocket() {
             return socket.activeConnectionsMap.get(packet.getIds().length == 3 ? packet.getIds()[2] : packet.getIds()[1]);
        }
    }

    // done
    protected class ProcessingThread extends Thread{

        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    PacketToProcess packet = toBeProcessed.take();
                    packet.getSocket().receivePacket(packet.getPacket());
                } catch (InterruptedException e) {
                    this.interrupt();
                }
            }
        }
    }

    protected PriorityBlockingQueue<PacketToProcess> toBeProcessed;
    protected ArrayList<ProcessingThread> processingThreads;

    // done
    private void initializeReceiveSplit(){
        toBeProcessed = new PriorityBlockingQueue<>();
        int receiveThreadCount = GBUILibGlobals.getInputThreadCount();
        for(int i = 0; i < receiveThreadCount; i++){
            processingThreads.add(new ProcessingThread());
        }
        for(int i = 0; i < processingThreads.size(); i++){
            processingThreads.get(i).start();
        }
    }


    public synchronized void sendAsPacket(GBSocket socket, Object content, String contentType, String packetType, boolean mustArrive) throws BadPacketException{
        if(activeConnectionsMap.containsKey(socket.socketIDServerSide)) {
            socket.sendAsPacket(content, contentType, packetType, mustArrive);
        } else {
            throw new IllegalArgumentException("The selected socket is not connected through this GBServerSocket.");
        }
    }

    // SocketAddress address, String connectionType, boolean autoReconnectArg, SelectorManager selector, ActionHandler handler, SocketConfig config, GBServerSocket parent

    private int port;
    private int sendersPort;
    private int maxReceiveSize;
    private int maxConnections;
    private int potentialConnections;
    private int connectionCount;
    private boolean allowNoAck;
    private GBServerSocket current = this;
    private GBSocket reader = new GBSocket(port, this, maxReceiveSize);
    protected HashMap<String, ActionHandler> connectionTypes = new HashMap<>();
    protected HashMap<Integer, GBSocket> activeConnectionsMap = new HashMap<>();
    // safe
    public GBServerSocket(){

    }

    protected void createNewConnection(Packet packet){
        try {
            if (potentialConnections != maxConnections && listenToConnections) {
                potentialConnections++;
                DatagramChannel channel = DatagramChannel.open();
                channel.socket().setReuseAddress(true);
                channel.bind(new InetSocketAddress(sendersPort));
                ByteBuffer buf = ByteBuffer.allocate(maxReceiveSize);
                SocketAddress address = packet.receivedFrom;
                channel.configureBlocking(false);
                GBSocket.SocketConfig config = new GBSocket.SocketConfig();
                config.setMaxReceiveSize(maxReceiveSize);
                GBSocket socket = new GBSocket(address, null, false, selector, new ActionHandler(), config, current, allowNoAck);
                socket.socket = channel;
                socket.buffer = buf;
                Thread fred = new Thread(() -> {
                    if(socket.socketConnect(packet)){
                        addSelectorChannel(socket);
                    }
                    else {
                        socket.close();
                        potentialConnections--;
                    }
                });
                fred.start();
            }
        } catch (IOException e){

        }
    }

    private boolean listenToConnections;

}