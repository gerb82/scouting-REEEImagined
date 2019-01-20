package serverSide.code;

import connectionIndependent.FullScoutingEvent;
import connectionIndependent.ScoutingEvent;
import connectionIndependent.ScoutingEventDefinition;
import connectionIndependent.ScoutingPackets;
import gbuiLib.GBSockets.ActionHandler;
import gbuiLib.GBSockets.BadPacketException;
import javafx.beans.value.ObservableValue;

import java.util.ArrayList;
import java.util.HashMap;

public class ScoutersManager {

    private class ScoutIdentifier {

        private String competition;
        private short game;
        private Short team;
        private Boolean alliance;
        private String mapConfiguration;
        private Byte startingLocation;

        private ScoutIdentifier(String competition, short game, Object specific) {
            this.competition = competition;
            this.game = game;
            if(specific instanceof Boolean){
                this.alliance = (Boolean) specific;
                this.team = null;
            } else if(specific instanceof Short) {
                this.team = (Short) specific;
                this.alliance = null;
            } else {
                throw new IllegalArgumentException("The scouted target identifier was invalid!");
            }
        }

        private boolean isEqualTo(ScoutIdentifier o) {
            return this.competition == o.competition && this.game == o.game && this.alliance == o.alliance && this.team == o.team;
        }
    }

    private HashMap<ObservableValue<Boolean>, ScoutIdentifier> currentlyScouting = new HashMap<>();
    private javafx.beans.value.ChangeListener<Boolean> listener = (observable, oldValue, newValue) -> {
        if (!newValue) unlockScout((ObservableValue<Boolean>) observable);
    };

    private DataBaseManager dataBase;
    private String currentCompetition;
    private ActionHandler handler;

    protected ActionHandler getHandler() {
        return handler;
    }

    protected void pickCurrentCompetition(String comp) {
        currentCompetition = comp;
    }

    private HashMap<String, HashMap<Short, String[]>> gamesStructure = new HashMap<>(); // competitions to games to teams in game
    private HashMap<String, HashMap<Short, ArrayList<String>>> alreadyScoutedGamesStructure = new HashMap<>(); // competitions to games to teams in game. Only contains Non-Scouted competitions/games/teams in game.

    protected void addCompetition(String competition) {
        gamesStructure.put(competition, new HashMap<>());
    }

    protected void addGame(String competiion, short game, Short[] teams) {
        ArrayList<String> Steams = new ArrayList<>();
        for(Short team : teams){
            Steams.add(String.valueOf(team));
        }
        if(ScoutingVars.allowAllianceEvents()){
            Steams.add("Blue Alliance");
            Steams.add("Red Alliance");
        }
        gamesStructure.get(competiion).put(game, Steams.toArray(new String[0]));
    }


    protected ScoutersManager(DataBaseManager dataBase) {
        handler = new ActionHandler();
        handler.setHandler(ScoutingPackets.SCOUTER_SUBMITGAME.toString(), this::scoutFinished);
        handler.setHandler(ScoutingPackets.SCOUTER_LOADGAME.toString(), this::startScout);
        handler.setHandler(ScoutingPackets.SCOUTER_SYNC_COMPS.toString(), this::sendCompetitions);
        handler.setHandler(ScoutingPackets.SCOUTER_SYNC_GAMES.toString(), this::sendGames);
        handler.setHandler(ScoutingPackets.SCOUTER_SYNC_TEAMS.toString(), this::sendTeams);
        this.dataBase = dataBase;
        for (String comp : dataBase.getCompetitionsList()) {
            HashMap games = new HashMap<>();
            gamesStructure.put(comp, games);
            for (DataBaseManager.ScoutedGame game : dataBase.getGamesList(comp)) {
                games.put(game.getGame(), game.getTeamsArray());
            }
        }
        handler.setHandler(ScoutingPackets.SCOUTER_SYNC_TEAMS.toString(), this::sendTeams);
        handler.setHandler(ScoutingPackets.SCOUTER_SYNC_GAMES.toString(), this::sendGames);
        handler.setHandler(ScoutingPackets.SCOUTER_SYNC_COMPS.toString(), this::sendCompetitions);
        handler.setHandler(ScoutingPackets.SCOUTER_LOADGAME.toString(), this::startScout);
        handler.setHandler(ScoutingPackets.SCOUTER_SUBMITGAME.toString(), this::scoutFinished);
    }

    private void sendCompetitions(ActionHandler.PacketOut packet) throws BadPacketException {
        try {
        packet.ack(gamesStructure.keySet().toArray(new String[0]), ScoutingPackets.SCOUTER_SYNC_COMPS.toString(), currentCompetition);
        } catch (BadPacketException e){
            throw e;
        } catch (Exception e){
            throw new BadPacketException("There was a problem fetching the competitions list!");
        }
    }

    private void sendGames(ActionHandler.PacketOut packet) throws BadPacketException {
        try {
            packet.ack(gamesStructure.get(packet.getContentType()).keySet().toArray(new Short[0]), ScoutingPackets.SCOUTER_SYNC_GAMES.toString(), packet.getContentType());
        } catch (BadPacketException e) {
            throw e;
        } catch (Exception e) {
            throw new BadPacketException("There was a problem fetching the games list!");
        }
    }

    private void sendTeams(ActionHandler.PacketOut packet) throws BadPacketException {
        try {
            packet.ack(gamesStructure.get(packet.getContentType()).get(packet.getContent()), ScoutingPackets.SCOUTER_SYNC_TEAMS.toString(), packet.getContent().toString());
        } catch (BadPacketException e) {
            throw e;
        } catch (Exception e) {
            throw new BadPacketException("There was a problem fetching the teams list!");
        }
    }

    private void startScout(ActionHandler.PacketOut packet) throws BadPacketException {
        try {
            Object[] arguments = (Object[]) packet.getContent();
            ScoutIdentifier identifier = new ScoutIdentifier((String) arguments[0], (Short) arguments[1], arguments[3]);
            if (currentlyScouting.putIfAbsent(packet.getSocket().isConnected, identifier) != null) {
                throw new IllegalArgumentException("You are already scouting a game!");
            }
            for (ScoutIdentifier scout : currentlyScouting.values()) {
                if (scout.isEqualTo(identifier)) throw new IllegalArgumentException("Game is already being scouted!");
            }
            packet.getSocket().isConnected.addListener(listener);
            ArrayList<FullScoutingEvent> unfiltered;
            if (identifier.team == null && ScoutingVars.allowAllianceEvents()) {
                unfiltered = dataBase.getAllianceEventsByGame(identifier.game, identifier.competition, identifier.alliance);
            } else if (identifier.team != null) {
                unfiltered = dataBase.getAllTeamEventsByGame(identifier.game, identifier.competition, identifier.team);
            } else {
                throw new IllegalArgumentException("Alliance scouting is disabled!");
            }
            Object[] params = dataBase.getTeamConfiguration(identifier.game, identifier.competition, identifier.team);
            identifier.mapConfiguration = (String) params[0];
            identifier.startingLocation = (Byte) params[1];
            ArrayList<ScoutingEvent> events = new ArrayList<>();
            for (FullScoutingEvent event : unfiltered) {
                events.add(event.getEvent());
            }
            ArrayList<ScoutingEventDefinition> initialEvents;
            if(identifier.team == null){
                initialEvents = dataBase.getAllianceDefinitions();
            } else {
                initialEvents = dataBase.getTeamStartDefinitions();
            }
            byte[] startEvenets = new byte[initialEvents.size()];
            int i = 0;
            for(ScoutingEventDefinition def : initialEvents){
                startEvenets[i] = def.getName();
                i++;
            }
            packet.ack(new Object[]{events, (identifier.team == null ? dataBase.getAllianceDefinitions() : dataBase.getTeamDefinitions()), startEvenets}, ScoutingPackets.SCOUTER_LOADGAME.toString(), "video?competition=" + identifier.competition + "&game=" + identifier.game);
        } catch (IllegalArgumentException e) {
            throw new BadPacketException(e.getMessage());
        } catch (Exception e) {
            throw new BadPacketException("Invalid game identifier!");
        }
    }

    private void scoutFinished(ActionHandler.PacketOut packet) throws BadPacketException {
        try {
            if(Boolean.valueOf(packet.getContentType())) {
                ObservableValue<Boolean> socket = packet.getSocket().isConnected;
                ScoutIdentifier identifier = currentlyScouting.get(socket);
                ArrayList<FullScoutingEvent> finalized = new ArrayList<>();
                for (ScoutingEvent event : (ArrayList<ScoutingEvent>) packet.getContent()) {
                    finalized.add(new FullScoutingEvent(event, identifier.team, identifier.game, dataBase.getCompetitionFromName(identifier.competition), identifier.mapConfiguration, identifier.startingLocation, (identifier.alliance == null ? identifier.startingLocation < 4 : identifier.alliance)));
                }
                dataBase.updateEventsOnGame(finalized.toArray(new FullScoutingEvent[0]));
            }
        } catch (Exception e) {
            throw new BadPacketException("There was a problem saving the events!");
        } finally {
            unlockScout(packet.getSocket().isConnected);
        }
    }

    private void unlockScout(ObservableValue<Boolean> socket) {
        socket.removeListener(listener);
        currentlyScouting.remove(socket);
    }
}
