package gbuiLib.GBSockets;

import java.util.HashMap;

public interface ActionHandlerRecipe {

    default void addHandler(String packetType, ActionHandler.PacketHandler handler){
        handlers.putIfAbsent(packetType, handler);
    }

    HashMap<String, ActionHandler.PacketHandler> handlers = new HashMap<>();

    default HashMap<String, ActionHandler.PacketHandler> getHandlers(){
        return handlers;
    }
}
