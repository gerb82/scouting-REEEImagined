package utilities.GBSockets;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class ActionHandler {

    public interface HandlerType{}

    private class Handlers{

        private PacketHandler handler;
        private Class<? extends HandlerType> handlerType;

        private Handlers(PacketHandler handler, Class<? extends HandlerType> handlerType){
            this.handler = handler;
            this.handlerType = handlerType;
        }
    }

    public class StandardHandler implements HandlerType{

        private Object content;
        private String contentType;
        private GBSocket socket;

        public StandardHandler(GBSocket socket, Packet packet){
            content = packet.content;
            contentType = packet.contentType;
            this.socket = socket;
        }

        public Object getContent() {
            return content;
        }

        public String getContentType() {
            return contentType;
        }

        public GBSocket getSocket() {
            return socket;
        }
    }

    public class StandardHandlerWithIDs extends StandardHandler{

        private int[] ids;

        public StandardHandlerWithIDs(GBSocket socket, Packet packet) {
            super(socket, packet);
            ids = packet.ids;
        }

        public int[] getIds() {
            return ids;
        }
    }

    public class UnsafeHandler implements HandlerType{

        private Packet packet;
        private GBSocket socket;

        public UnsafeHandler(GBSocket socket, Packet packet){
            this.packet = packet;
            this.socket = socket;
        }

        public Packet getPacket() {
            return packet;
        }

        public GBSocket getSocket() {
            return socket;
        }
    }

    public interface PacketHandler{
        void handle(HandlerType handler);
    }

    private HashMap<String, Handlers> handlers;

    public void addMethod(String packetType, PacketHandler handler, Class<? extends HandlerType> handlerType){
        handlers.put(packetType, new Handlers(handler, handlerType));
    }

    protected void handlePacket(GBSocket socket, Packet packet) throws BadPacketException {
        if(handlers.containsKey(packet.packetType)){
            try {
                Handlers handler = handlers.get(packet.packetType);
                handler.handler.handle(handler.handlerType.getDeclaredConstructor(GBSocket.class, Packet.class).newInstance(socket, packet));
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        else{
            throw new BadPacketException("The packet type was invalid. No action handler for this packet type exists.");
        }
    }
}