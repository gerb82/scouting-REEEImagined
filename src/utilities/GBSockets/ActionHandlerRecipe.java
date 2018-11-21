package utilities.GBSockets;

import java.util.HashMap;

public interface ActionHandlerRecipe {

    default void addHandler(String PacketType, ActionHandler.PacketHandler handler){
        handlers.putIfAbsent(PacketType, handler);
    }

    HashMap<String, ActionHandler.PacketHandler> handlers = new HashMap<>();

    default HashMap<String, ActionHandler.PacketHandler> getHandlers(){
        return handlers;
    }
}
