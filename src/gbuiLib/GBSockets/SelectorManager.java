package gbuiLib.GBSockets;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

public class SelectorManager implements Closeable {

    private Selector selector;
    private boolean alive;
    private boolean server;
    private GBServerSocket.AddPacket serverPackets;

    public SelectorManager(){
        try{
            waitingSockets = new PriorityQueue<>();
            selector = Selector.open();
            alive = true;
            this.server = false;
            new SelectorThread().start();
        } catch (IOException e) {
            new IOException("Selector could not be opened", e).printStackTrace();
        }
    }

    protected SelectorManager(GBServerSocket.AddPacket addPacket){
        try{
            waitingSockets = new PriorityQueue<>();
            selector = Selector.open();
            alive = true;
            this.server = true;
            this.serverPackets = addPacket;
            new SelectorThread().start();
        } catch (IOException e) {
            new IOException("Selector could not be opened", e).printStackTrace();
        }
    }

    private Queue<GBSocket> waitingSockets;
    protected void registerSocket(GBSocket socket){
        try {
            socket.getChannel().configureBlocking(false);
            waitingSockets.add(socket);
            selector.wakeup();
        } catch (IOException e) {}
    }

    @Override
    protected void finalize() {
        close();
    }

    @Override
    public void close() {
        try {
            alive = false;
            selector.close();
        } catch (IOException e) {
            new IOException("Couldn't close the selector", e).printStackTrace();
        }
    }

    private class SelectorThread extends Thread{

        @Override
        public void run() {
            while(alive) {
                try {
                    if (selector.select() > 0) {
                        for (SelectionKey key : selector.selectedKeys()) {
                            GBSocket socket = ((GBSocket) key.attachment());
                            Packet packet = socket.readPacket();
                            selector.selectedKeys().remove(key);
                            if(packet == null){
                                continue;
                            }
                            if (server) {
                                if (packet.getIds()[0] == -1) {
                                    if (packet.getPacketType().equals(ActionHandler.DefaultPacketTypes.HandShake.toString())) {
                                        socket.parent.createNewConnection(packet);
                                    } else {
                                        socket.parent.activeConnectionsMap.get(packet.getIds()[1]).logger.packets.getLine(true, packet.getIds()).setResponse(packet);
                                    }
                                } else {
                                    if (socket.parent.activeConnectionsMap.containsValue(packet.getIds().length == 3 ? packet.getIds()[2] : packet.getIds()[1])) {
                                        serverPackets.addPacket(new GBServerSocket.PacketToProcess(packet, socket.parent));
                                    } else {
                                        socket.getChannel().write(ByteBuffer.wrap("You're already dead".getBytes()));
                                    }
                                }
                            } else {
                                socket.receivePacket(packet);
                            }
                        }
                    }
                    while(!waitingSockets.isEmpty()) {
                        GBSocket socket = waitingSockets.remove();
                        socket.setKey(socket.getChannel().register(selector, SelectionKey.OP_READ, socket));
                    }
                } catch (IOException e) {}
            }
        }
    }
}
