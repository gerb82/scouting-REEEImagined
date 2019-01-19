package serverSide.code;

import connectionIndependent.EventGroup;
import connectionIndependent.FullScoutingEvent;
import connectionIndependent.ScoutingEvent;
import connectionIndependent.ScoutingEventDefinition;
import gbuiLib.GBUILibGlobals;

import java.io.Closeable;
import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DataBaseManager implements Closeable {

    @Override
    public void close() {
        try {
            database.rollback();
            database.close();
        } catch (SQLException e) {
            throw new Error("Could not close the database");
        }
    }

    // database mapping available at https://www.dbdesigner.net/designer/schema/221463
    private Connection database;
    private HashMap<String, Byte> competitionsMap = new HashMap<>();

    protected byte getCompetitionFromName(String competition){
        return competitionsMap.get(competition);
    }

    protected String[] getCompetitionsList(){
        return competitionsMap.keySet().toArray(new String[0]);
    }

    private enum Columns {
        eventTypeID, eventName, followStamp, teamSpecific, // event types
        containerEventType, eventContainedType, // containers table
        groupNumber, groupText, // group definitions table
        groupEventType, containedGroupID, groupID, // groups containers table
        competitionID, competitionName, // competitions
        teamNumbers, teamNames, participatedIn, // teams table
        gameNumbers, mapConfiguration, competition, redAllianceScore, blueAllianceScore, redAllianceRP, blueAllianceRP, teamNumber1, teamNumber2, teamNumber3, teamNumber4, teamNumber5, teamNumber6, // games table
        chainID, gameNumber, competitionNumber, teamNumber, alliance, startingLocation, // main events table
        eventChainID, eventLocationInChain, eventType, timeStamps, // stamps table
        commentContent, associatedTeam, associatedGame, associatedChain, timeStamp // comments table
    }

    private enum Tables {
        eventTypes, containableEvents, // event definers
        groupDefinitions, eventGroups, // group definers
        competitions, teamNumbers, games, // basis data
        eventFrames, events, comments // event data
    }

    private ReentrantReadWriteLock accessLimiter;
    private ReentrantReadWriteLock.ReadLock readLock;
    private ReentrantReadWriteLock.WriteLock writeLock;

    public DataBaseManager() {
        File databaseDirectory = ScoutingVars.getDatabaseDirectory();
        databaseDirectory.mkdirs();
        File databaseFile = new File(databaseDirectory, "database.sqlite");
        accessLimiter = new ReentrantReadWriteLock();
        readLock = accessLimiter.readLock();
        writeLock = accessLimiter.writeLock();
        try {
            database = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath().toString());
            database.setAutoCommit(false);
            Statement statement = database.createStatement();

            // eventDefinitionsList
            statement.execute("CREATE TABLE IF NOT EXISTS " + Tables.eventTypes + "(" + System.lineSeparator() +
                    Columns.eventTypeID + " integer primary key AUTOINCREMENT," + System.lineSeparator() +
                    Columns.eventName + " text NOT NULL UNIQUE," + System.lineSeparator() +
                    Columns.followStamp + " integer NOT NULL," + System.lineSeparator() +
                    Columns.teamSpecific + " integer NOT NULL);");

            // containableEventsList
            statement.execute("CREATE TABLE IF NOT EXISTS " + Tables.containableEvents + "(" + System.lineSeparator() +
                    Columns.containerEventType + " integer NOT NULL," + System.lineSeparator() +
                    Columns.eventContainedType + " integer NOT NULL," + System.lineSeparator() +
                    "PRIMARY KEY(" + Columns.containerEventType + "," + Columns.eventContainedType + ")," + System.lineSeparator() +
                    "FOREIGN KEY(" + Columns.containerEventType + ") REFERENCES " + Tables.eventTypes + "(" + Columns.eventTypeID + ")," + System.lineSeparator() +
                    "FOREIGN KEY(" + Columns.eventContainedType + ") REFERENCES " + Tables.eventTypes + "(" + Columns.eventTypeID + "));");

            // groupDefinitions
            statement.execute("CREATE TABLE IF NOT EXISTS " + Tables.groupDefinitions + "(" + System.lineSeparator() +
                    Columns.groupNumber + " integer PRIMARY KEY," + System.lineSeparator() +
                    Columns.groupText + " text NOT NULL);");

            // groupContents
            statement.execute("CREATE TABLE IF NOT EXISTS " + Tables.eventGroups + "(" + System.lineSeparator() +
                    Columns.groupEventType + " integer," + System.lineSeparator() +
                    Columns.containedGroupID + " integer," + System.lineSeparator() +
                    Columns.groupID + " integer NOT NULL," + System.lineSeparator() +
                    "FOREIGN KEY(" + Columns.groupEventType + ") REFERENCES " + Tables.eventTypes + " (" + Columns.eventTypeID + ")," + System.lineSeparator() +
                    "FOREIGN KEY(" + Columns.groupID + ") REFERENCES " + Tables.groupDefinitions + " (" + Columns.groupNumber + ")," + System.lineSeparator() +
                    "FOREIGN KEY(" + Columns.containedGroupID + ") REFERENCES " + Tables.eventGroups + " (" + Columns.groupID + ")," + System.lineSeparator() +
                    "PRIMARY KEY(" + Columns.groupEventType + "," + Columns.containedGroupID + "," + Columns.groupID + "));");

            // competitions
            statement.execute("CREATE TABLE IF NOT EXISTS " + Tables.competitions + "(" + System.lineSeparator() +
                    Columns.competitionID + " integer primary key AUTOINCREMENT," + System.lineSeparator() +
                    Columns.competitionName + " text UNIQUE NOT NULL);");

            // teamsList
            statement.execute("CREATE TABLE IF NOT EXISTS " + Tables.teamNumbers + "(" + System.lineSeparator() +
                    Columns.teamNumbers + " integer primary key," + System.lineSeparator() +
                    Columns.teamNames + " text NOT NULL UNIQUE);");

            // gamesList
            statement.execute("CREATE TABLE IF NOT EXISTS " + Tables.games + "(" + System.lineSeparator() +
                    Columns.gameNumbers + " integer NOT NULL," + System.lineSeparator() +
                    Columns.mapConfiguration + " text," + System.lineSeparator() +
                    Columns.competition + " integer NOT NULL," + System.lineSeparator() +
                    Columns.blueAllianceScore + " integer NOT NULL," + System.lineSeparator() +
                    Columns.redAllianceScore + " integer NOT NULL," + System.lineSeparator() +
                    Columns.blueAllianceRP + " integer NOT NULL," + System.lineSeparator() +
                    Columns.redAllianceRP + " integer NOT NULL," + System.lineSeparator() +
                    Columns.teamNumber1 + " integer," + System.lineSeparator() +
                    Columns.teamNumber2 + " integer," + System.lineSeparator() +
                    Columns.teamNumber3 + " integer," + System.lineSeparator() +
                    Columns.teamNumber4 + " integer," + System.lineSeparator() +
                    Columns.teamNumber5 + " integer," + System.lineSeparator() +
                    Columns.teamNumber6 + " integer," + System.lineSeparator() +
                    "PRIMARY KEY(" + Columns.gameNumbers + "," + Columns.competition + ")," + System.lineSeparator() +
                    "FOREIGN KEY(" + Columns.competition + ") REFERENCES " + Tables.competitions + "(" + Columns.competitionID + ")," + System.lineSeparator() +
                    "FOREIGN KEY(" + Columns.teamNumber1 + ") REFERENCES " + Tables.teamNumbers + "(" + Columns.teamNumbers + ")," + System.lineSeparator() +
                    "FOREIGN KEY(" + Columns.teamNumber2 + ") REFERENCES " + Tables.teamNumbers + "(" + Columns.teamNumbers + ")," + System.lineSeparator() +
                    "FOREIGN KEY(" + Columns.teamNumber3 + ") REFERENCES " + Tables.teamNumbers + "(" + Columns.teamNumbers + ")," + System.lineSeparator() +
                    "FOREIGN KEY(" + Columns.teamNumber4 + ") REFERENCES " + Tables.teamNumbers + "(" + Columns.teamNumbers + ")," + System.lineSeparator() +
                    "FOREIGN KEY(" + Columns.teamNumber5 + ") REFERENCES " + Tables.teamNumbers + "(" + Columns.teamNumbers + ")," + System.lineSeparator() +
                    "FOREIGN KEY(" + Columns.teamNumber6 + ") REFERENCES " + Tables.teamNumbers + "(" + Columns.teamNumbers + "));");

            // eventFrames
            statement.execute("CREATE TABLE IF NOT EXISTS " + Tables.eventFrames + "(" + System.lineSeparator() +
                    Columns.chainID + " integer primary key AUTOINCREMENT," + System.lineSeparator() +
                    Columns.gameNumber + " integer NOT NULL," + System.lineSeparator() +
                    Columns.competitionNumber + " integer NOT NULL," + System.lineSeparator() +
                    Columns.teamNumber + " integer," + System.lineSeparator() +
                    Columns.alliance + " integer NOT NULL," + System.lineSeparator() +
                    Columns.startingLocation + " integer," + System.lineSeparator() +
                    "FOREIGN KEY(" + Columns.gameNumber + "," + Columns.competitionNumber + ") REFERENCES " + Tables.games + "(" + Columns.gameNumbers + "," + Columns.competition + ")," + System.lineSeparator() +
                    "FOREIGN KEY(" + Columns.teamNumber + ") REFERENCES " + Tables.teamNumbers + "(" + Columns.teamNumbers + "));");

            // eventsList
            statement.execute("CREATE TABLE IF NOT EXISTS " + Tables.events + "(" + System.lineSeparator() +
                    Columns.eventChainID + " integer NOT NULL," + System.lineSeparator() +
                    Columns.eventLocationInChain + " integer NOT NULL," + System.lineSeparator() +
                    Columns.eventType + " integer NOT NULL," + System.lineSeparator() +
                    Columns.timeStamps + " integer," + System.lineSeparator() +
                    "PRIMARY KEY(" + Columns.eventChainID + "," + Columns.eventLocationInChain + ")," + System.lineSeparator() +
                    "FOREIGN KEY(" + Columns.eventType + ") REFERENCES " + Tables.eventTypes + "(" + Columns.eventTypeID + ")," + System.lineSeparator() +
                    "FOREIGN KEY(" + Columns.eventChainID + ") REFERENCES " + Tables.eventFrames + "(" + Columns.chainID + "));");

            // commentsList
            statement.execute("CREATE TABLE IF NOT EXISTS " + Tables.comments + "(" + System.lineSeparator() +
                    Columns.commentContent + " text NOT NULL," + System.lineSeparator() +
                    Columns.associatedTeam + " integer," + System.lineSeparator() +
                    Columns.associatedGame + " integer," + System.lineSeparator() +
                    Columns.associatedChain + " integer," + System.lineSeparator() +
                    Columns.timeStamp + " integer," + System.lineSeparator() +
                    "FOREIGN KEY(" + Columns.associatedTeam + ") REFERENCES " + Tables.teamNumbers + " (" + Columns.teamNumbers + ")," + System.lineSeparator() +
                    "FOREIGN KEY(" + Columns.associatedGame + ") REFERENCES " + Tables.games + " (" + Columns.gameNumbers + ")," + System.lineSeparator() +
                    "FOREIGN KEY(" + Columns.associatedChain + ") REFERENCES " + Tables.eventFrames + " (" + Columns.chainID + "));");

//            configEnforcer();
            statement.execute("SELECT " + Columns.competitionID + "," + Columns.competitionName + " FROM " + Tables.competitions + ";");
            ResultSet set = statement.getResultSet();
            while (set.next()) {
                competitionsMap.put(set.getString(Columns.competitionName.toString()), set.getByte(Columns.competitionID.toString()));
            }
            database.commit();
            statement.close();
        } catch (SQLException e) {
            try {
                if (database != null) {
                    database.rollback();
                    throw new Error("Failed to initialize the database!", e);
                } else {
                    throw new Error("Could not connect to the database!", e);
                }
            } catch (SQLException e1) {
                throw new Error("Failed to roll back the database!", e1);
            }
        }
    }

    private ArrayList<ScoutingEventDefinition> teamDefinitions;
    private ArrayList<ScoutingEventDefinition> allianceDefinitions;
    private ArrayList<EventGroup> groups;
    private ArrayList<ScoutingEventDefinition> teamStartDefinitions;
    private ArrayList<ScoutingEventDefinition> allianceStartDefinitions;

    protected ArrayList<ScoutingEventDefinition> getTeamDefinitions() {
        return teamDefinitions;
    }

    protected ArrayList<ScoutingEventDefinition> getAllianceDefinitions() {
        return allianceDefinitions;
    }

    protected ArrayList<EventGroup> getGroups() {
        return groups;
    }

    protected ArrayList<ScoutingEventDefinition> getTeamStartDefinitions(){
        return teamStartDefinitions;
    }

    protected ArrayList<ScoutingEventDefinition> getAllianceStartDefinitions(){
        return allianceStartDefinitions;
    }

    private class ConfigFormat {
        private class EventDefinition {
            private short type;
            private String name;
            private boolean followStamps;
            private boolean teamSpecific;

            private EventDefinition(short type, String name, boolean followStamps, boolean teamSpecific) {
                this.type = type;
                this.name = name;
                this.followStamps = followStamps;
                this.teamSpecific = teamSpecific;
            }
        }

        private class EventContainmentChain {
            private short container;
            private short contained;

            private EventContainmentChain(short container, short contained) {
                this.container = container;
                this.contained = contained;
            }
        }

        private class GroupDefinition {
            private byte number;
            private String name;

            private GroupDefinition(byte number, String name) {
                this.number = number;
                this.name = name;
            }
        }

        private class GroupContainmentChain {
            private Byte containedEvent;
            private Byte containedGroup;
            private byte groupID;

            private GroupContainmentChain(Byte containedGroup, byte groupID) {
                this.containedGroup = containedGroup;
                this.containedEvent = null;
                this.groupID = groupID;
            }

            private GroupContainmentChain(byte groupID, Byte containedEvent) {
                this.containedGroup = null;
                this.containedEvent = containedEvent;
                this.groupID = groupID;
            }
        }

        private ArrayList<EventDefinition> definitions;
        private ArrayList<EventContainmentChain> chains;
        private ArrayList<GroupDefinition> groupDefinitions;
        private ArrayList<GroupContainmentChain> groupChains;

        private ConfigFormat() {
        }

        private ArrayList<EventDefinition> getDefinitions() {
            return definitions;
        }

        private void setDefinitions(ArrayList<EventDefinition> definitions) {
            this.definitions = definitions;
        }

        private ArrayList<EventContainmentChain> getChains() {
            return chains;
        }

        private void setChains(ArrayList<EventContainmentChain> chains) {
            this.chains = chains;
        }

        private ArrayList<GroupDefinition> getGroupDefinitions() {
            return groupDefinitions;
        }

        private void setGroupDefinitions(ArrayList<GroupDefinition> groupDefinitions) {
            this.groupDefinitions = groupDefinitions;
        }

        private ArrayList<GroupContainmentChain> getGroupChains() {
            return groupChains;
        }

        private void setGroupChains(ArrayList<GroupContainmentChain> groupChains) {
            this.groupChains = groupChains;
        }


        private ArrayList<ScoutingEventDefinition> generateTeamEvents() {
            ArrayList<ScoutingEventDefinition> output = new ArrayList<>();
            HashMap<Byte, ArrayList<Byte>> map = new HashMap<>();
            for (EventDefinition definition : definitions) {
                if (definition.teamSpecific) {
                    map.putIfAbsent((byte) byteFixer(definition.type, true), new ArrayList<>());
                }
            }
            for (EventContainmentChain chain : chains) {
                if (map.containsKey(chain.container)) {
                    map.get((byte)byteFixer(chain.container, true)).add((byte) byteFixer(chain.contained, true));
                }
            }
            for (EventDefinition definition : definitions) {
                Object[] contained = map.remove(definition.type).toArray();
                byte[] realContained = new byte[contained.length];
                for (int i = 0; i < contained.length; i++) {
                    realContained[i] = (byte) contained[i];
                }
                output.add(new ScoutingEventDefinition(realContained, (byte) byteFixer(definition.type, true), definition.followStamps, definition.name));
            }
            return output;
        }

        private ArrayList<ScoutingEventDefinition> generateAllianceEvents() {
            ArrayList<ScoutingEventDefinition> output = new ArrayList<>();
            HashMap<Byte, ArrayList<Byte>> map = new HashMap<>();
            for (EventDefinition definition : definitions) {
                if (!definition.teamSpecific) {
                    map.putIfAbsent((byte) byteFixer(definition.type, true), new ArrayList<>());
                }
            }
            for (EventContainmentChain chain : chains) {
                if (map.containsKey(chain.container)) {
                    map.get((byte)byteFixer(chain.container, true)).add((byte) byteFixer(chain.contained, true));
                }
            }
            for (EventDefinition definition : definitions) {
                Object[] contained = map.remove(definition.type).toArray();
                byte[] realContained = new byte[contained.length];
                for (int i = 0; i < contained.length; i++) {
                    realContained[i] = (byte) contained[i];
                }
                output.add(new ScoutingEventDefinition(realContained, (byte) byteFixer(definition.type, true), definition.followStamps, definition.name));
            }
            return output;
        }

        private ArrayList<EventGroup> generateGroups() {
            return null;
        }
    }

    private void configEnforcer() {
        ConfigFormat format = new ConfigFormat();
        // do format stuff

        teamDefinitions = format.generateTeamEvents();
        allianceDefinitions = format.generateAllianceEvents();
        groups = format.generateGroups();
    }


    protected void addNewCompetition(String name) {
        try (Statement statement = database.createStatement()) {
            writeLock.lock();
            statement.execute("INSERT INTO " + Tables.competitions + "(" + Columns.competitionName + ") VALUES(" + name + ");");
            statement.execute("SELECT " + Columns.competitionID + " FROM " + Tables.competitions + " WHERE " + Columns.competitionName + " = '" + name + "';");
            ResultSet set = statement.getResultSet();
            set.next();
            byte number = set.getByte(Columns.competitionID.toString());
            statement.execute("ALTER TABLE " + Tables.teamNumbers + " ADD COLUMN " + Columns.participatedIn + number + " integer NOT NULL DEFAULT 0;");
            database.commit();
            competitionsMap.put(name, number);
        } catch (SQLException e) {
            try {
                database.rollback();
            } catch (SQLException e1) {
                throw new Error("could not roll back database changes!", e1);
            }
            throw new IllegalArgumentException("The transaction could not be completed.", e);
        } finally {
            writeLock.unlock();
        }
    }

    protected void removeCompetition(String name) {
        try (Statement statement = database.createStatement()) {
            writeLock.lock();
            int number = competitionsMap.get(name);
            statement.execute("DELETE FROM " + Tables.competitions + " WHERE " + Columns.competitionName + " = '" + name + "' AND " + Columns.competitionID + " = " + number + ";");
            database.commit();
            competitionsMap.remove(name, number);
        } catch (SQLException e) {
            try {
                database.rollback();
            } catch (SQLException e1) {
                throw new Error("could not roll back database changes!", e1);
            }
            throw new IllegalArgumentException("The transaction could not be completed.", e);
        } finally {
            writeLock.unlock();
        }
    }

    protected void addTeams(String[] competitions, ScoutedTeam... teams) {
        try (Statement statement = database.createStatement()) {
            writeLock.lock();
            String competitionsColumns = "";
            String participatedInS = "";
            for (String competition : competitions) {
                competitionsColumns += "," + participatedIn(competition);
                participatedInS += ",1";
            }
            String statementString = "INSERT INTO " + Tables.teamNumbers + "(" + Columns.teamNumbers + "," + Columns.teamNames + competitionsColumns + ") VALUES";
            boolean first = true;
            for (ScoutedTeam team : teams) {
                statementString += (!first ? "," : "") + "(" + team.getNumber() + ",'" + team.getName() + "'" + participatedInS + ")";
                first = false;
            }
            statement.execute(statementString);
            database.commit();
        } catch (SQLException e) {
            try {
                database.rollback();
            } catch (SQLException e1) {
                throw new Error("could not roll back database changes!", e1);
            }
            throw new IllegalArgumentException("The transaction could not be completed.", e);
        } finally {
            writeLock.unlock();
        }
    }

    protected void removeTeam(ScoutedTeam team) {
        try (Statement statement = database.createStatement()) {
            writeLock.lock();
            statement.execute("DELETE FROM " + Tables.teamNumbers + " WHERE " + Columns.teamNumbers + " = " + team.getNumber() + " AND " + Columns.teamNames + " = '" + team.getName() + "';");
            database.commit();
        } catch (SQLException e) {
            try {
                database.rollback();
            } catch (SQLException e1) {
                throw new Error("could not roll back database changes!", e1);
            }
            throw new IllegalArgumentException("The transaction could not be completed.", e);
        } finally {
            writeLock.unlock();
        }
    }

    protected void updateEventsOnGame(FullScoutingEvent... events) {
        try (Statement statement = database.createStatement()) {
            if(!writeLock.isHeldByCurrentThread()) writeLock.lock();
            Short team = events[0].getTeam();
            byte competition = events[0].getCompetition();
            String alliance = allianceToAllianceID(events[0].getAlliance());
            short game = events[0].getGame();
            byte startingLocation = events[0].getStartingLocation();
            Set<Integer> numberedEvents = new HashSet<>();
            boolean cleanup = events[0].getEvent().getChainID() == -2;
            boolean first = true;
            String idList = "(";
            if (!cleanup) {
                for (FullScoutingEvent event : events) {
                    if (event.getEvent().getChainID() != -1) {
                        if (numberedEvents.contains(event.getEvent().getChainID())) {
                            throw new IllegalArgumentException("A duplicate event was detected!");
                        } else {
                            numberedEvents.add(event.getEvent().getChainID());
                            idList += (!first ? "," : "") + event.getEvent().getChainID();
                        }
                    }
                }
            }
            String stampsCleaner = "DELETE FROM " + Tables.events + System.lineSeparator() +
                    "WHERE " + Columns.eventChainID + " in " +
                    ("SELECT " + Columns.chainID + " from " + Tables.eventFrames + System.lineSeparator() +
                            "where " + Columns.gameNumber + " = " + game + System.lineSeparator() + "" + System.lineSeparator() +
                            "AND " + Columns.competitionNumber + " = " + competitionsMap.get(competition) + System.lineSeparator() +
                            "AND " + Columns.teamNumber + " = " + String.valueOf(team) + System.lineSeparator() +
                            "AND " + Columns.alliance + " = " + alliance) + ");";

            String framesCleaner = "DELETE FROM " + Tables.eventFrames + System.lineSeparator() +
                    "where " + Columns.gameNumber + " = " + game + System.lineSeparator() +
                    "AND " + Columns.competitionNumber + " = " + competitionsMap.get(competition) + System.lineSeparator() +
                    "AND " + Columns.teamNumber + " = " + String.valueOf(team) + System.lineSeparator() +
                    "AND " + Columns.alliance + " = " + alliance + System.lineSeparator() +
                    "AND " + Columns.chainID + " NOT IN " + idList + ");";

            statement.execute(stampsCleaner + " " + framesCleaner);
            if (cleanup) {
                database.commit();
                return;
            }
            String framesFixer = "INSERT OR REPLACE INTO " + Tables.eventFrames + "(" + Columns.chainID + "," + Columns.alliance + "," + Columns.gameNumber + "," + Columns.competitionNumber + "," + Columns.teamNumber + "," + Columns.startingLocation + ")" + System.lineSeparator() +
                    "VALUES"; // continues in the for-loop
            String stampsFixer = "INSERT INTO " + Tables.events + "(" + Columns.eventChainID + "," + Columns.eventLocationInChain + "," + Columns.eventType + "," + Columns.timeStamps + ")" + System.lineSeparator() +
                    "values"; // continues in the for-loop
            statement.execute("SELECT " + Columns.chainID + " FROM " + Tables.eventFrames + " ORDER BY " + Columns.chainID + " DESC LIMIT 1;");
            ResultSet set = statement.getResultSet();
            set.next();
            Integer lastID = set.getInt(Columns.chainID.toString());
            set.close();
            first = true;
            for (FullScoutingEvent event : events) {
                if (event.getEvent().getChainID() == -1) {
                    event.getEvent().setChainID(++lastID);
                }
                framesFixer += (!first ? "," : "") + "("
                        + event.getEvent().getChainID() + ","
                        + game + ","
                        + competitionsMap.get(competition) + ","
                        + team + ","
                        + alliance + ","
                        + startingLocation + ")";
                int location = 1;
                for (ScoutingEvent.EventTimeStamp stamp : event.getEvent().getStamps()) {
                    stampsFixer += (!first ? "," : "") + "("
                            + event.getEvent().getChainID() + ","
                            + location++ + ","
                            + byteFixer(event.getEvent().getType(), false) + ","
                            + String.valueOf(stamp.getTimeStamp()) + ")";
                    first = false;
                }
            }
            statement.execute(framesFixer + "; " + stampsFixer + ";");
            database.commit();
        } catch (SQLException e) {
            try {
                database.rollback();
            } catch (SQLException e1) {
                throw new Error("could not roll back database changes!", e1);
            }
            throw new IllegalArgumentException("The transaction could not be completed.", e);
        } finally {
            writeLock.unlock();
        }
    }

    protected void removeEvent(FullScoutingEvent event) {
        try (Statement statement = database.createStatement()) {
            writeLock.lock();
            statement.execute("DELETE FROM " + Tables.events + " WHERE " + Columns.chainID + " = " + event.getEvent().getChainID() + ";");
            statement.execute("DELETE FROM " + Tables.eventFrames + " WHERE " + Columns.eventChainID + " = " + event.getEvent().getChainID() + ";");
            database.commit();
        } catch (SQLException e) {
            try {
                database.rollback();
            } catch (SQLException e1) {
                throw new Error("could not roll back database changes!", e1);
            }
            throw new IllegalArgumentException("The transaction could not be completed.", e);
        } finally {
            writeLock.unlock();
        }
    }

    protected void cleanUpGameEventsForTeam(short game, String competition, Short team, boolean alliance) {
        writeLock.lock();
        ScoutingEvent event = new ScoutingEvent();
        event.setChainID(-2);
        updateEventsOnGame(new FullScoutingEvent(event, team, game, competitionsMap.get(competition), null, (byte) -1, alliance));
    }

    protected void addGame(short gameNumber, String competition, String mapConfiguration, Short[] teams) {
        try (Statement statement = database.createStatement()) {
            writeLock.lock();
            assert (teams.length == 6);
            ArrayList<Short> testList = new ArrayList<>();
            for (Short teamNum : teams) {
                if (!testList.contains(teamNum) || teamNum == null) {
                    testList.add(teamNum);
                } else {
                    throw new IllegalArgumentException("The same team was supplied twice for the given new game!");
                }
            }
            String gameCreate = "INSERT INTO " + Tables.games + "(" + Columns.gameNumbers + "," + Columns.competition + "," + Columns.mapConfiguration + "," + Columns.teamNumber1 + "," + Columns.teamNumber2 + "," + Columns.teamNumber3 + "," + Columns.teamNumber4 + "," + Columns.teamNumber5 + "," + Columns.teamNumber6 + ") values(" + gameNumber + "," + competitionsMap.get(competition) + "," + String.valueOf(mapConfiguration);
            for (Short teamNum : teams) {
                gameCreate += "," + String.valueOf(teamNum);
            }
            statement.execute(gameCreate + ";");
            database.commit();
        } catch (AssertionError e) {
            throw new IllegalArgumentException("The teams array was not of the right size!");
        } catch (SQLException e) {
            try {
                database.rollback();
            } catch (SQLException e1) {
                throw new Error("could not roll back database changes!", e1);
            }
            throw new IllegalArgumentException("The transaction could not be completed.", e);
        } finally {
            writeLock.unlock();
        }
    }


    protected void addTeamsToCompetitions(ScoutedTeam[] teams, String[] competitions) {

    }


    private String participatedIn(String competition) {
        return Columns.participatedIn.toString() + competitionsMap.get(competition);
    }

    private String allianceToAllianceID(boolean blueAlliance) {
        return blueAlliance ? "0" : "1";
    }

    private String matchEventGameAndCompetition(short game, String competition) {
        return Columns.gameNumber + " = " + game + " AND " + Columns.competitionNumber + " = " + competitionsMap.get(competition);
    }

    private String matchEventTeam(short team) {
        return Columns.teamNumber + " = " + team;
    }

    private String matchEventType(byte type) {
        return Columns.eventChainID + " IN " + "(SELECT " + Columns.eventChainID + " FROM " + Tables.events + " WHERE " + Columns.eventType + " = " + byteFixer(type, false) + ")";
    }

    private String matchTeamAlliance(short team) {
        return "((" + Columns.alliance + " = " + allianceToAllianceID(true) + " AND (" + Columns.teamNumber1 + " = " + team + " OR " + Columns.teamNumber2 + " = " + team + " OR " + Columns.teamNumber3 + " = " + team + "))" + System.lineSeparator() +
                "OR (" + Columns.alliance + " = " + allianceToAllianceID(false) + " AND (" + Columns.teamNumber4 + " = " + team + " OR " + Columns.teamNumber5 + " = " + team + " OR " + Columns.teamNumber6 + " = " + team + ")))";
    }

    private String formatEventsSelect(String conditions, Integer limit) {
        return "SELECT " + Columns.eventChainID + "," + Columns.eventLocationInChain + "," + Columns.eventType + "," + Columns.timeStamps + "," + Columns.chainID + "," + Columns.alliance + "," + Columns.gameNumber + "," + Columns.competitionNumber + "," + Columns.startingLocation + " FROM " + Tables.events + System.lineSeparator() +
                "INNER JOIN " + Tables.eventFrames + " ON " + Tables.eventFrames + "." + Columns.chainID + " = " + Tables.events + "." + Columns.eventChainID + System.lineSeparator() +
                "INNER JOIN " + Tables.games + " ON " + Tables.games + "." + Columns.gameNumber + " = " + Tables.eventFrames + "." + Columns.gameNumber + System.lineSeparator() +
                (conditions == null ? "" : "WHERE " + conditions + System.lineSeparator()) +
                "ORDER BY " + Columns.eventChainID + " ASC, " + Columns.eventLocationInChain + " ASC" + System.lineSeparator() +
                (limit == null ? "" : "LIMIT " + limit) + ";";
    }

    private String getSpecificGame(short game, String competition){
        return Columns.gameNumbers + " = " + game + " AND " + Columns.competition + " = " + competitionsMap.get(competition);
    }

    private String formatGamesSelect(String conditions, Integer limit) {
        return "SELECT " + Columns.gameNumbers + "," + Columns.competition + "," + Columns.mapConfiguration + "," + Columns.blueAllianceScore + "," + Columns.redAllianceScore + "," + Columns.blueAllianceRP + "," + Columns.redAllianceRP + "," + Columns.teamNumber1 + "," + Columns.teamNumber2 + "," + Columns.teamNumber3 + "," + Columns.teamNumber4 + "," + Columns.teamNumber5 + "," + Columns.teamNumber6 + " FROM " + Tables.games + System.lineSeparator() +
                (conditions == null ? "" : "WHERE " + conditions + System.lineSeparator()) +
                (limit == null ? "" : "LIMIT " + limit) + ";";
    }


    protected Object[] getTeamConfiguration(short game, String competition, Short team){
        try (Statement statement = database.createStatement()) {
            readLock.lock();
            statement.execute(formatGamesSelect(getSpecificGame(game, competition), 1));

            ScoutedGame result = convertResultSetToGames(statement.getResultSet()).get(0);
            Byte startLoc = null;
            if(team != null){
                if(result.getTeamNumber1() == team){
                    startLoc = 1;
                } else if(result.getTeamNumber2() == team){
                    startLoc = 2;
                } else if(result.getTeamNumber3() == team){
                    startLoc = 3;
                } else if(result.getTeamNumber4() == team){
                    startLoc = 4;
                } else if(result.getTeamNumber5() == team){
                    startLoc = 5;
                } else if(result.getTeamNumber6() == team){
                    startLoc = 6;
                }
            }
            return new Object[]{result.mapConfiguration, startLoc};
        } catch (SQLException e) {
            throw new IllegalArgumentException("The query could not be completed.", e);
        } finally {
            readLock.unlock();
        }
    }

    protected ArrayList<FullScoutingEvent> getEventsByGame(short game, String competition) {
        try (Statement statement = database.createStatement()) {
            readLock.lock();
            statement.execute(formatEventsSelect(matchEventGameAndCompetition(game, competition), null));

            return convertResultSetToEvents(statement.getResultSet());
        } catch (SQLException e) {
            throw new IllegalArgumentException("The query could not be completed.", e);
        } finally {
            readLock.unlock();
        }
    }

    protected ArrayList<FullScoutingEvent> getAllTeamEventsByGame(short game, String competition, short team) {
        try (Statement statement = database.createStatement()) {
            readLock.lock();
            statement.execute(formatEventsSelect(matchEventGameAndCompetition(game, competition) + System.lineSeparator() +
                    "AND " + matchEventTeam(team), null));

            return convertResultSetToEvents(statement.getResultSet());
        } catch (SQLException e) {
            throw new IllegalArgumentException("The query could not be completed.", e);
        } finally {
            readLock.unlock();
        }
    }

    protected ArrayList<FullScoutingEvent> getTeamAllianceEvents(short team) {
        try (Statement statement = database.createStatement()) {
            readLock.lock();
            statement.execute(formatEventsSelect(matchTeamAlliance(team), null));

            return convertResultSetToEvents(statement.getResultSet());
        } catch (SQLException e) {
            throw new IllegalArgumentException("The query could not be completed.", e);
        } finally {
            readLock.unlock();
        }
    }

    protected ArrayList<FullScoutingEvent> getTeamAllianceEventsByGame(short game, String competition, short team) {
        try (Statement statement = database.createStatement()) {
            readLock.lock();
            statement.execute(formatEventsSelect(matchEventGameAndCompetition(game, competition) + System.lineSeparator() +
                    "AND " + matchTeamAlliance(team), null));

            return convertResultSetToEvents(statement.getResultSet());
        } catch (SQLException e) {
            throw new IllegalArgumentException("The query could not be completed.", e);
        } finally {
            readLock.unlock();
        }
    }

    protected ArrayList<FullScoutingEvent> getAllianceEventsByGame(short game, String competition, boolean alliance) {
        try (Statement statement = database.createStatement()) {
            readLock.lock();
            statement.execute(formatEventsSelect(matchEventGameAndCompetition(game, competition) + System.lineSeparator() +
                    "AND " +  Columns.alliance + " = " + allianceToAllianceID(alliance), null));

            return convertResultSetToEvents(statement.getResultSet());
        } catch (SQLException e) {
            throw new IllegalArgumentException("The query could not be completed.", e);
        } finally {
            readLock.unlock();
        }
    }

    protected ArrayList<FullScoutingEvent> getAllTeamEventsOfType() {
        return null;
    }

    protected ArrayList<FullScoutingEvent> getTeamEventsOfTypeByGame() {
        return null;
    }

    protected ArrayList<FullScoutingEvent> getAllTeamEvents() {
        return null;
    }

    protected ArrayList<FullScoutingEvent> getAllianceEvents() {
        return null;
    }

    protected ArrayList<FullScoutingEvent> getGameResult() {
        return null;
    }

    protected ArrayList<FullScoutingEvent> getGamesResult() {
        return null;
    }

    protected ArrayList<ScoutedGame> getGamesList(String competition) {
        try (Statement statement = database.createStatement()) {
            readLock.lock();
            statement.execute(formatGamesSelect(Columns.competition + " = " + competitionsMap.get(competition),null));

            return convertResultSetToGames(statement.getResultSet());
        } catch (SQLException e) {
            throw new IllegalArgumentException("The query could not be completed.", e);
        } finally {
            readLock.unlock();
        }
    }

    protected ArrayList<FullScoutingEvent> getEventsTypesList() {
        return null;
    }

    protected ArrayList<FullScoutingEvent> getTeamsList() {
        return null;
    }

    protected ArrayList<FullScoutingEvent> getRPTotalForTeam() {
        return null;
    }

    protected ArrayList<FullScoutingEvent> getAverageRPForTeam() {
        return null;
    }

    protected ArrayList<FullScoutingEvent> getAverageScoreForTeam() {
        return null;
    }

    protected ArrayList<FullScoutingEvent> getAllGamesForTeam() {
        return null;
    }

    protected ArrayList<FullScoutingEvent> getAllCompetitionsForTeam() {
        return null;
    }

    protected ArrayList<FullScoutingEvent> getAllTeamsForCompetition() {
        return null;
    }


    private ArrayList<ScoutedGame> convertResultSetToGames(ResultSet set) throws SQLException {
        ArrayList<ScoutedGame> result = new ArrayList<>();
        while (set.next()) {
            result.add(new ScoutedGame(
                    set.getShort(Columns.gameNumbers.toString()),
                    set.getByte(Columns.competition.toString()),
                    set.getShort(Columns.redAllianceScore.toString()),
                    set.getShort(Columns.blueAllianceScore.toString()),
                    set.getByte(Columns.redAllianceRP.toString()),
                    set.getByte(Columns.blueAllianceRP.toString()),
                    set.getString(Columns.mapConfiguration.toString()),
                    set.getShort(Columns.teamNumber1.toString()),
                    set.getShort(Columns.teamNumber2.toString()),
                    set.getShort(Columns.teamNumber3.toString()),
                    set.getShort(Columns.teamNumber4.toString()),
                    set.getShort(Columns.teamNumber5.toString()),
                    set.getShort(Columns.teamNumber6.toString()))
            );
        }
        set.close();
        return result;
    }

    private ArrayList<ScoutedTeam> convertResultSetToTeams(ResultSet set) throws SQLException {
        ArrayList<ScoutedTeam> result = new ArrayList<>();
        while (set.next()) {
            result.add(new ScoutedTeam(
                    set.getShort(Columns.teamNumbers.toString()),
                    set.getString(Columns.teamNames.toString()))
            );
        }
        set.close();
        return result;
    }

    private ArrayList<FullScoutingEvent> convertResultSetToEvents(ResultSet set) throws SQLException {
        ArrayList<FullScoutingEvent> result = new ArrayList<>();
        FullScoutingEvent buffer = null;
        while (set.next()) {
            if (set.getInt(Columns.eventLocationInChain.toString()) == 1) {
                buffer = new FullScoutingEvent(
                        new ScoutingEvent() {{
                            this.addProgress((byte)byteFixer(set.getShort(Columns.eventType.toString()), true), set.getShort(Columns.timeStamps.toString()) == 0 ? null : set.getShort(Columns.timeStamps.toString()));
                        }},
                        set.getShort(Columns.teamNumber.toString()) == 0 ? null : set.getShort(Columns.teamNumber.toString()),
                        set.getShort(Columns.gameNumber.toString()),
                        set.getByte(Columns.competitionNumber.toString()),
                        set.getString(Columns.mapConfiguration.toString()),
                        set.getByte(Columns.startingLocation.toString()),
                        set.getBoolean(Columns.alliance.toString())
                );
                result.add(buffer);
            } else {
                buffer.getEvent().addProgress(set.getByte(Columns.eventType.toString()), set.getShort(Columns.timeStamp.toString()));
            }
        }
        set.close();
        return result;
    }

    /**
     * Since sqlite counts from 1, and we can store a maximum of 256 events (byte's size is 256, from -128 to 127), we end up with problematic values.
     * We can have a value of 256, even though {@link Byte#MAX_VALUE} can not fit that.
     * We can also try to insert a value of {@link Byte#MIN_VALUE} even though sqlite starts counting from 1.
     * To fix that, any conversion to and from sqlite of event types passes through this method.
     * If the method needs to pull from sqlite, it will reduce the value by 129, so that the lowest value sqlite can supply (1), will become {@link Byte#MIN_VALUE}, and the highest value that it can supply, which is 256, will become {@link Byte#MAX_VALUE}.
     * If the value given from sqlite is above 256 or the value given to sqlite is below 1, the method will throw an {@link IllegalArgumentException} in order to avoid derpy values.
     * @param toFix The value to fix
     * @param down Should it be lowered (aka it's being pulled from sqlite) or should it be increased (aka it's being pushed into sqlite)
     * @return The converted value, in Short form (it then has to be cast)
     */
    private short byteFixer(short toFix, boolean down) throws IllegalArgumentException{
        if(toFix < 1 && down || toFix > 256 && !down) throw new IllegalStateException("The byte was out of bounds");
        return (short) (toFix + (down ? - 129 : 129));
    }

    public static class ScoutedGame {

        private short game;
        private byte competition;
        private short redAllianceScore;
        private short blueAllianceScore;
        private byte redAllianceRP;
        private byte blueAllianceRP;
        private String mapConfiguration;
        private short teamNumber1;
        private short teamNumber2;
        private short teamNumber3;
        private short teamNumber4;
        private short teamNumber5;
        private short teamNumber6;

        public ScoutedGame(short game, byte competition, short redAllianceScore, short blueAllianceScore, byte redAllianceRP, byte blueAllianceRP, String mapConfiguration, short teamNumber1, short teamNumber2, short teamNumber3, short teamNumber4, short teamNumber5, short teamNumber6) {
            this.game = game;
            this.competition = competition;
            this.redAllianceScore = redAllianceScore;
            this.blueAllianceScore = blueAllianceScore;
            this.redAllianceRP = redAllianceRP;
            this.blueAllianceRP = blueAllianceRP;
            this.mapConfiguration = mapConfiguration;
            this.teamNumber1 = teamNumber1;
            this.teamNumber2 = teamNumber2;
            this.teamNumber3 = teamNumber3;
            this.teamNumber4 = teamNumber4;
            this.teamNumber5 = teamNumber5;
            this.teamNumber6 = teamNumber6;
        }

        public short getGame() {
            return game;
        }

        public void setGame(short game) {
            this.game = game;
        }

        public byte getCompetition() {
            return competition;
        }

        public void setCompetition(byte competition) {
            this.competition = competition;
        }

        public short getRedAllianceScore() {
            return redAllianceScore;
        }

        public void setRedAllianceScore(short redAllianceScore) {
            this.redAllianceScore = redAllianceScore;
        }

        public short getBlueAllianceScore() {
            return blueAllianceScore;
        }

        public void setBlueAllianceScore(short blueAllianceScore) {
            this.blueAllianceScore = blueAllianceScore;
        }

        public byte getRedAllianceRP() {
            return redAllianceRP;
        }

        public void setRedAllianceRP(byte redAllianceRP) {
            this.redAllianceRP = redAllianceRP;
        }

        public byte getBlueAllianceRP() {
            return blueAllianceRP;
        }

        public void setBlueAllianceRP(byte blueAllianceRP) {
            this.blueAllianceRP = blueAllianceRP;
        }

        public String getMapConfiguration() {
            return mapConfiguration;
        }

        public void setMapConfiguration(String mapConfiguration) {
            this.mapConfiguration = mapConfiguration;
        }

        public short getTeamNumber1() {
            return teamNumber1;
        }

        public void setTeamNumber1(short teamNumber1) {
            this.teamNumber1 = teamNumber1;
        }

        public short getTeamNumber2() {
            return teamNumber2;
        }

        public void setTeamNumber2(short teamNumber2) {
            this.teamNumber2 = teamNumber2;
        }

        public short getTeamNumber3() {
            return teamNumber3;
        }

        public void setTeamNumber3(short teamNumber3) {
            this.teamNumber3 = teamNumber3;
        }

        public short getTeamNumber4() {
            return teamNumber4;
        }

        public void setTeamNumber4(short teamNumber4) {
            this.teamNumber4 = teamNumber4;
        }

        public short getTeamNumber5() {
            return teamNumber5;
        }

        public void setTeamNumber5(short teamNumber5) {
            this.teamNumber5 = teamNumber5;
        }

        public short getTeamNumber6() {
            return teamNumber6;
        }

        public void setTeamNumber6(short teamNumber6) {
            this.teamNumber6 = teamNumber6;
        }

        public Short[] getTeamsArray(){
            return new Short[]{teamNumber1, teamNumber2, teamNumber3, teamNumber4, teamNumber5, teamNumber6};
        }
    }

    public class ScoutedTeam {

        private short number;
        private String name;

        public ScoutedTeam(short number, String name) {
            this.number = number;
            this.name = name;
        }

        public short getNumber() {
            return number;
        }

        public void setNumber(short number) {
            this.number = number;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

}