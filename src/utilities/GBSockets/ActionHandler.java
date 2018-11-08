package utilities.GBSockets;

import java.util.HashMap;

public class ActionHandler {

    public interface PacketHandler {}

    public interface standard extends PacketHandler{
        void testMethod(GBSocket socket, Object content, String contentType, String packetType);
    }

    private HashMap<String, PacketHandler> handlers;

    public static <T extends PacketHandler> void addAction(T action, String packetType){

    }

    private static void testMeothod(String hi){

    }

    {
        addAction(ActionHandler::testMeothod, "test");
    }


}
