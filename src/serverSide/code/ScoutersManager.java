package serverSide.code;

import connectionIndependent.FullScoutingEvent;
import connectionIndependent.ScoutingEvent;
import connectionIndependent.ScoutingPackets;
import gbuiLib.GBSockets.ActionHandler;
import gbuiLib.GBSockets.BadPacketException;
import javafx.beans.value.ObservableValue;

import java.util.ArrayList;
import java.util.HashMap;

public class ScoutersManager {

    private class ScoutIdentifier{

        private String competition;
        private short game;
        private Short team;
        private boolean alliance;
        private String mapConfiguration;
        private Byte startingLocation;

        private ScoutIdentifier(String competition, short game, Short team, boolean alliance) {
            this.competition = competition;
            this.game = game;
            this.team = team;
            this.alliance = alliance;
        }
    }

    private HashMap<ObservableValue<Boolean>, ScoutIdentifier> currentlyScouting = new HashMap<>();
    private javafx.beans.value.ChangeListener<Boolean> listener = (observable, oldValue, newValue) -> {
        if(!newValue) unlockScout((ObservableValue<Boolean>) observable);
    };

    private DataBaseManager dataBase;

    public ScoutersManager(DataBaseManager dataBase){
        this.dataBase = dataBase;
    }

    private void startScout(ActionHandler.PacketOut packet) throws BadPacketException {
        try {
            Object[] arguments = (Object[]) packet.getContent();
            ScoutIdentifier identifier = new ScoutIdentifier((String) arguments[0], (Short) arguments[1], (Short) arguments[2], (Boolean) arguments[3]);
            if(currentlyScouting.putIfAbsent(packet.getSocket().isConnected, identifier) != null){
                throw new IllegalArgumentException("Game is already being scouted!");
            }
            packet.getSocket().isConnected.addListener(listener);
            ArrayList<FullScoutingEvent> unfiltered;
            if(identifier.team == null && ScoutingVars.allowAllianceEvents()){
                unfiltered = dataBase.getAllianceEventsByGame(identifier.game, identifier.competition, identifier.alliance);
            } else if (identifier.team != null){
                unfiltered = dataBase.getAllTeamEventsByGame(identifier.game, identifier.competition, identifier.team);
            } else {
                throw new IllegalArgumentException("Alliance scouting is disabled!");
            }
            Object[] params = dataBase.getTeamConfiguration(identifier.game, identifier.competition, identifier.team);
            identifier.mapConfiguration = (String) params[0];
            identifier.startingLocation = (Byte) params[1];
            ArrayList<ScoutingEvent> events = new ArrayList<>();
            for(FullScoutingEvent event : unfiltered){
                events.add(event.getEvent());
            }
            packet.ack(new Object[]{events, (identifier.team == null ? dataBase.getAllianceDefinitions() : dataBase.getTeamDefinitions())}, ScoutingPackets.SCOUTER_LOADGAME.toString(), "video?competition=" + identifier.competition + "&game=" + identifier.game);
        } catch (IllegalArgumentException e) {
            throw new BadPacketException(e.getMessage());
        } catch (Exception e){
            throw new BadPacketException("Invalid game identifier!");
        }
    }

    private void scoutFinished(ActionHandler.PacketOut packet) throws BadPacketException{
        try{
            ObservableValue<Boolean> socket = packet.getSocket().isConnected;
            ScoutIdentifier identifier = currentlyScouting.get(socket);
            ArrayList<FullScoutingEvent> finalized = new ArrayList<>();
            for(ScoutingEvent event : (ArrayList<ScoutingEvent>)packet.getContent()){
                finalized.add(new FullScoutingEvent(event, identifier.team, identifier.game, dataBase.getCompetitionFromName(identifier.competition), identifier.mapConfiguration, identifier.startingLocation, identifier.alliance));
            }
            dataBase.updateEventsOnGame(finalized.toArray(new FullScoutingEvent[0]));
        } catch (Exception e){
            throw new BadPacketException("There was a problem saving the events!");
        }
    }

    private void unlockScout(ObservableValue<Boolean> socket){
        socket.removeListener(listener);
        currentlyScouting.remove(socket);
    }
}
