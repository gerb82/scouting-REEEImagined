package connectionIndependent;

import java.io.IOException;
import java.net.*;

public class ConnectionFinder {

    public static SocketAddress getLocalNetHost(int port) {
        try (MulticastSocket socket = new MulticastSocket(8000)){
            byte[] buf = new byte[15];
            InetAddress group = InetAddress.getByName("234.0.0.0");
            socket.joinGroup(group);
            InetSocketAddress result = null;
            while (result == null) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                if (received.equals("Hi I'm hosting")) result = new InetSocketAddress(packet.getAddress().getHostAddress(), port);
            }
            socket.leaveGroup(group);
            socket.close();
            return result;
        } catch (IOException e) {
            throw new Error("Program is already running!");
        }
    }
}
