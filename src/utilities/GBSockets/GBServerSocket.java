package utilities.GBSockets;

import utilities.GBUILibGlobals;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class GBServerSocket{

    // selector area (Oh god why)
    private class SelectorTimoutThread extends Thread{
        private class SocketWaitTime{
            private int time;
            private final GBSocket channel;

            public SocketWaitTime(int time, GBSocket channel){
                this.time = time;
                this.channel = channel;
            }

            public void setTime(int time) {
                this.time = time;
            }

            public int getTime() {
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
        private HashMap<GBSocket, Integer> ack;
        private int timeOut;

        public SelectorTimoutThread(){
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
                        wait((long)(temp.getTime() - calculateTime() * 1000));
                    } catch (InterruptedException e) {
                        continue;
                    }
                    if(sockets.contains(temp.channel)) {
                        int lastAck = ack.get(temp.channel);
                        if (lastAck > temp.getTime()) {
                            temp.setTime(lastAck + timeOut);
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
        }

        public void ack(GBSocket socket){
            if (ack.containsKey(socket)) {
                ack.put(socket, calculateTime());
            }
        }

        private int calculateTime(){
            return (int)(Duration.between(start, Instant.now()).toMillis()/1000);
        }
    }

    private SelectorTimoutThread timoutThread = new SelectorTimoutThread();
    private HashMap<GBSocket, SelectionKey> selectionKeys = new HashMap<>();
    private Selector selector;

    private boolean initSelector(){
        try {
            selector = Selector.open();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean addSelectorChannel(GBSocket channel){
        try {
            selectionKeys.put(channel,channel.getChannel().register(selector, SelectionKey.OP_READ));
            return true;
        } catch (ClosedChannelException e) {
            return false;
        }
    }

    private boolean removeSelectorChannel(GBSocket channel){
        try{

            selectionKeys.remove(channel).cancel();
            return true;
        } catch (NullPointerException e){
            return false;
        }
    }
}
