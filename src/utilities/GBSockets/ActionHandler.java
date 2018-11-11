package utilities.GBSockets;

import java.util.HashMap;

public class ActionHandler {

    private enum PacketHandler {
        STANDARD{
            void handle(GBSocket socket, Packet packet){
                ((StandardMethod)method).hanlde(socket, packet.content, packet.contentType);
            }

            PacketHandler create(AbstractPacketHandler method){
                this.method = method;
                return this;
            }
        },
        STANDARD_WITH_IDS{
            void handle(GBSocket socket, Packet packet){
                ((StandardMethodWithIDs)method).hanlde(socket, packet.content, packet.contentType, packet.ids);
            }

            PacketHandler create(AbstractPacketHandler method){
                this.method = method;
                return this;
            }
        },
        UNSAFE{
            void handle(GBSocket socket, Packet packet){
                ((UnsafeMethod)method).hanlde(socket, packet);
            }

            PacketHandler create(AbstractPacketHandler method){
                this.method = method;
                return this;
            }
        };
        AbstractPacketHandler method;
        abstract void handle(GBSocket socket, Packet packet);
        abstract PacketHandler create(AbstractPacketHandler method);
    }

    public interface AbstractPacketHandler{
    }

    public interface StandardMethod extends AbstractPacketHandler{
        void hanlde(GBSocket socket, Object content, String contentType);
    }

    public interface UnsafeMethod extends AbstractPacketHandler{
        void hanlde(GBSocket socket, Packet packet);
    }

    public interface StandardMethodWithIDs extends AbstractPacketHandler{
        void hanlde(GBSocket socket, Object content, String contentType, int[] IDs);
    }

    private HashMap<String, PacketHandler> handlers;

    public void addHandler(String packetType, StandardMethod method){
        handlers.put(packetType, PacketHandler.STANDARD.create(method));
    }

    public void addHandler(String packetType, StandardMethodWithIDs method){
        handlers.put(packetType, PacketHandler.STANDARD_WITH_IDS.create(method));
    }

    public void addHandler(String packetType, UnsafeMethod method){
        handlers.put(packetType, PacketHandler.UNSAFE.create(method));
    }

    protected void handlePacket(GBSocket socket, Packet packet) throws BadPacketException{
        handlers.get(packet.packetType).handle(socket, packet);
    }
}