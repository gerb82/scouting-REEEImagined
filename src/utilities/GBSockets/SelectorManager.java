package utilities.GBSockets;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.PriorityBlockingQueue;

public class SelectorManager implements AutoCloseable{

    private Selector selector;
    private boolean alive;
    private boolean server;
    private PriorityBlockingQueue<GBServerSocket.PacketToProcess> serverQueue;

    protected SelectorManager(boolean server, PriorityBlockingQueue<GBServerSocket.PacketToProcess> queue){
        try{
            selector = Selector.open();
            alive = true;
            this.server = server;
            if(server) {
                this.serverQueue = queue;
            }
        } catch (IOException e) {
            new IOException("Selector could not be opened", e).printStackTrace();
        }
    }

    protected SelectionKey registerSocket(GBSocket socket){
        try {
            socket.getChannel().configureBlocking(false);
            return socket.getChannel().register(selector, SelectionKey.OP_READ, socket);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void close() {
        try {
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
                    if(selector.select() > 0) {
                        for (SelectionKey key : selector.selectedKeys()) {
                            GBSocket socket = ((GBSocket) key.attachment());
                            boolean flag = true;
                            while(flag) {
                                try {
                                    Packet packet = socket.readPacket();
                                    if(packet == null){
                                        flag = false;
                                        continue;
                                    }
                                    if (server) {
                                        serverQueue.add(new GBServerSocket.PacketToProcess(packet, socket));
                                    } else {
                                        socket.receivePacket(packet);
                                    }
                                } catch (BadPacketException e) {
                                    if(socket.isServer()){
                                        socket.stopServerConnection();
                                    } else {
                                        socket.close();
                                    }
                                }
                            }
                        }
                    }
                } catch (IOException e) {}
            }
        }
    }
}
