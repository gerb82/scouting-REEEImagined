package utilities.GBSockets;

import utilities.GBUILibGlobals;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

public class GBServerSocket implements AutoCloseable{

    // done
    @Override
    public void close() {
        for(ProcessingThread fred : processingThreads){
            fred.interrupt();
        }
        selector.close();
        for(GBSocket socket : selectionKeys.keySet()){
            socket.close();
        }
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
         * The method used to schedule a test for  if the socket is not timed out.
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
    private HashMap<GBSocket, SelectionKey> selectionKeys = new HashMap<>();
    private SelectorManager selector;
    private boolean isUnsafe;

    // done
    protected boolean initSelector(){
        if(selector != null) {
            if (!isUnsafe) {
                timeOutThread.activate();
                initializeReceiveSplit();
            }
            selector = new SelectorManager(true, toBeProcessed);
            return true;
        }
        return false;
    }

    // done
    protected boolean addSelectorChannel(GBSocket socket) {
        if (timeOutThread.addSocket(socket)) {
            SelectionKey key = selector.registerSocket(socket);
            if (key != null) {
                selectionKeys.put(socket, key);
                return true;
            }
        }
        return false;
    }

    // done
    protected boolean removeSelectorChannel(GBSocket socket){
        try{
            selectionKeys.remove(socket).cancel();
            timeOutThread.removeSocket(socket);
            socket.close();
            return true;
        } catch (NullPointerException e){
            return false;
        }
    }



    // done
    //PacketSplittingOnInput
    protected static class PacketToProcess{

        private Packet packet;
        private GBSocket socket;
        protected PacketToProcess(Packet packet, GBSocket socket){
            this.packet = packet;
            this.socket = socket;
        }

        protected GBSocket getSocket() {
            return socket;
        }

        protected Packet getPacket() {
            return packet;
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
        if(selectionKeys.keySet().contains(socket)) {
            socket.sendAsPacket(content, contentType, packetType, mustArrive);
        }
    }

    // unsafe, done
    public synchronized void sendPacket(GBSocket socket, Packet packet) throws IOException {
        if(selectionKeys.keySet().contains(socket)) {
            if (GBUILibGlobals.unsafeSockets() && isUnsafe) {
                socket.sendPackets(packet);
            } else {
                throw new UnsafeSocketException("There was an attempt to send a packet directly and not through a packet manager, even though unsafe sockets are disabled");
            }
        }
    }

    private void handShake(){

    }

    // safe
    public GBServerSocket(){

    }

    // unsafe
    public GBServerSocket(boolean unsafe){

    }
}