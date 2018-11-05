package utilities.GBSockets;

import utilities.GBUILibGlobals;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class GBServerSocket{

    // selector area (Oh god why)
    private class SelectorTimeOutThread extends Thread{
        private class SocketWaitTime{
            private long time;
            private final GBSocket channel;

            public SocketWaitTime(long time, GBSocket channel){
                this.time = time;
                this.channel = channel;
            }

            public void setTime(long time) {
                this.time = time;
            }

            public long getTime() {
                return time;
            }

            public GBSocket getChannel() {
                return channel;
            }
        }

        private ArrayList<GBSocket> sockets;
        private Queue<SocketWaitTime> sleepQueue;
        private boolean keepRunning;
        private Instant start;
        private HashMap<GBSocket, Long> ack;
        private int timeOut;

        public SelectorTimeOutThread(){
            start = Instant.now();
            timeOut = GBUILibGlobals.getSocketTimeout();
            ack = new HashMap<>();
            sleepQueue = new PriorityQueue<>();
            sockets = new ArrayList<>();
            keepRunning = true;
        }
        @Override
        public void run() {
            while(keepRunning){
                if(sockets.isEmpty()){
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        calculateTime();
                        continue;
                    }
                }
                else{
                    SocketWaitTime temp = sleepQueue.poll();
                    try {
                        wait(temp.getTime() - calculateTime());
                    } catch (InterruptedException e) {
                        continue;
                    }
                    if(sockets.contains(temp.channel)) {
                        long lastAck = ack.get(temp.channel);
                        if (lastAck > temp.getTime()) {
                            temp.setTime(calculateTime() + timeOut);
                        } else {
                            removeSelectorChannel(temp.channel);
                        }
                    }
                }
            }
        }

        public boolean closeThread(boolean force){
            if(force || sockets.isEmpty()){
                this.keepRunning = false;
                notify();
            }
            return false;
        }

        public void removeSocket(GBSocket socket){
            sockets.remove(socket);
            ack.remove(socket);
        }

        public void addSocket(GBSocket socket){
            sockets.add(socket);
            ack.put(socket, calculateTime());
            sleepQueue.add(new SocketWaitTime(calculateTime(), socket));
            if(sockets.isEmpty()) {
                notify();
            }
        }

        public void ack(GBSocket socket){
            if (ack.containsKey(socket)) {
                ack.put(socket, calculateTime());
            }
        }

        private long calculateTime(){
            return Duration.between(start, Instant.now()).toMillis();
        }
    }

    private SelectorTimeOutThread timeOutThread = new SelectorTimeOutThread();
    private HashMap<GBSocket, SelectionKey> selectionKeys = new HashMap<>();
    private Selector selector;

    private boolean initSelector(){
        try {
            selector = Selector.open();
            timeOutThread.start();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean addSelectorChannel(GBSocket socket){
        try {
            selectionKeys.put(socket,socket.getChannel().register(selector, SelectionKey.OP_READ));
            timeOutThread.addSocket(socket);
            return true;
        } catch (ClosedChannelException e) {
            return false;
        }
    }

    private boolean removeSelectorChannel(GBSocket socket){
        try{
            selectionKeys.remove(socket).cancel();
            timeOutThread.removeSocket(socket);
            return true;
        } catch (NullPointerException e){
            return false;
        }
    }
}
