package serverSide;

import connectionIndependent.ScoutingPackets;
import connectionIndependent.scouted.*;
import gbuiLib.GBSockets.ActionHandler;
import gbuiLib.GBSockets.BadPacketException;
import javafx.beans.value.ObservableValue;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public class ScoutersManager {

    private HashMap<ObservableValue<Boolean>, ScoutIdentifier> currentlyScouting = new HashMap<>();
    private javafx.beans.value.ChangeListener<Boolean> listener = (observable, oldValue, newValue) -> {
        if (!newValue) unlockScout((ObservableValue<Boolean>) observable);
    };

    private DataBaseManager dataBase;
    private ActionHandler handler;

    protected ActionHandler getHandler() {
        return handler;
    }

    protected ScoutersManager(DataBaseManager dataBase) {
        handler = new ActionHandler();
        this.dataBase = dataBase;
        handler.setHandler(ScoutingPackets.SCOUTER_SYNC_GAMES.toString(), this::sendGames);
        handler.setHandler(ScoutingPackets.SCOUTER_SYNC_COMPS.toString(), this::sendCompetitions);
        handler.setHandler(ScoutingPackets.SCOUTER_LOADGAME.toString(), this::startScout);
        handler.setHandler(ScoutingPackets.SCOUTER_SUBMITGAME.toString(), this::scoutFinished);
    }

    private void sendCompetitions(ActionHandler.PacketOut packet) throws BadPacketException {
        try {
            packet.ack(dataBase.getCompetitionsList(), ScoutingPackets.SCOUTER_SYNC_COMPS.toString(), null);
        } catch (BadPacketException e) {
            throw e;
        } catch (Exception e) {
            throw new BadPacketException("There was a problem fetching the competitions list!");
        }
    }

    private void sendGames(ActionHandler.PacketOut packet) throws BadPacketException {
        try {
            Object[] content = new Object[]{
                    dataBase.getGamesList(packet.getContentType(), true),
                    dataBase.getIdentifiersForCompetition(true, packet.getContentType())
            };
            packet.ack(content, ScoutingPackets.SCOUTER_SYNC_GAMES.toString(), "ping");
        } catch (BadPacketException e) {
            throw e;
        } catch (Exception e) {
            throw new BadPacketException("There was a problem fetching the games list!");
        }
    }

    private synchronized void startScout(ActionHandler.PacketOut packet) throws BadPacketException {
        try {
            ScoutIdentifier identifier = (ScoutIdentifier) packet.getContent();
            for (ScoutIdentifier scout : currentlyScouting.values()) {
                if (scout.equals(identifier)) throw new IllegalArgumentException("Game is already being scouted!");
            }
            if (currentlyScouting.putIfAbsent(packet.getSocket().isConnected, identifier) != null) {
                throw new IllegalArgumentException("You are already scouting a game!");
            }
            packet.getSocket().isConnected.addListener(listener);
            ArrayList<FullScoutingEvent> unfiltered;
            try {
                unfiltered = dataBase.getAllTeamEventsByGame(identifier.getGame(), identifier.getCompetition(), Short.valueOf(identifier.getTeam()));
            } catch (NumberFormatException e) {
                currentlyScouting.remove(identifier);
                throw new IllegalArgumentException("Alliance scouting is disabled!");
            }
            ArrayList<ScoutingEvent> events = new ArrayList<>();
            for (FullScoutingEvent event : unfiltered) {
                events.add(event.getEvent());
            }
            ArrayList<ScoutingEventDefinition> initialEvents;
            initialEvents = dataBase.getTeamStartDefinitions();
            Byte[] startEvents = new Byte[initialEvents.size()];
            int i = 0;
            for (ScoutingEventDefinition def : initialEvents) {
                startEvents[i] = def.getName();
                i++;
            }
            Object[] content = new Object[]{
                    dataBase.getTeamDefinitions(),
                    initialEvents,
//                    getTeamViews(),
                    events,
                    dataBase.getGame(identifier.getGame(), identifier.getCompetition()).getVideoOffset()
            };
            packet.ack(content, ScoutingPackets.SCOUTER_LOADGAME.toString(), "video?competition=" + identifier.getCompetition() + "&game=" + identifier.getGame());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new BadPacketException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadPacketException("Invalid game identifier!");
        }
    }

    private void scoutFinished(ActionHandler.PacketOut packet) throws BadPacketException {
        try {
            ObservableValue<Boolean> socket = packet.getSocket().isConnected;
            ScoutIdentifier identifier = unlockScout(socket);
            if (identifier == null) {
                throw new IllegalArgumentException("You were not scouting!");
            }
            if (packet.getContent() != null) {
                ArrayList<FullScoutingEvent> finalized = new ArrayList<>();
                ScoutedGame game = dataBase.getGame(identifier.getGame(), identifier.getCompetition());
                Pair<String, Byte> startLoc = dataBase.getTeamConfiguration(identifier.getGame(), identifier.getCompetition(), Short.valueOf(identifier.getTeam()));
                ArrayList<ScouterCommentEvent> comments = new ArrayList<>();
                for (ScoutingEvent event : ((Pair<ArrayList<ScoutingEvent>, ArrayList<ScouterCommentEvent>>) packet.getContent()).getKey()) {
                    finalized.add(new FullScoutingEvent(event, Short.valueOf(game.getTeamsArray()[startLoc.getValue() - 1]), identifier.getGame(), dataBase.getCompetitionFromName(identifier.getCompetition()), startLoc.getKey(), startLoc.getValue()));
                    comments.addAll(event.getComments());
                }
                comments.addAll(((Pair<ArrayList<ScoutingEvent>, ArrayList<ScouterCommentEvent>>) packet.getContent()).getValue());
                dataBase.updateEventsOnGame(Boolean.valueOf(packet.getContentType()), comments, finalized.toArray(new FullScoutingEvent[0]));
            }
            packet.ack();
        } catch (IllegalArgumentException e) {
            throw new BadPacketException(e.getMessage());
        } catch (Exception e) {
            throw new BadPacketException("There was a problem saving the events!");
        } finally {
            unlockScout(packet.getSocket().isConnected);
        }
    }

    private ScoutIdentifier unlockScout(ObservableValue<Boolean> socket) {
        socket.removeListener(listener);
        return currentlyScouting.remove(socket);
    }

    public String getTeamViews() {
        return "hi";
    }

    public String getAllianceViews() {
        return "bye";
    }
}
