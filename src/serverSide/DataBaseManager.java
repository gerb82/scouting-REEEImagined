package serverSide;

import connectionIndependent.eventsMapping.*;
import connectionIndependent.scouted.*;
import javafx.scene.Node;
import javafx.util.Pair;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
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

    protected byte getCompetitionFromName(String competition) {
        return competitionsMap.get(competition);
    }

    protected ArrayList<String> getCompetitionsList() {
        return new ArrayList<>(competitionsMap.keySet());
    }

    private enum Columns {
        eventTypeID, eventName, followStamp, teamSpecific, // event types
        containerEventType, eventContainedType, // containers table
        competitionID, competitionName, // competitions
        teamNumbers, teamNames, participatedIn, // teams table
        gameNumbers, mapConfiguration, gameName, wasCompleted, competition, redAllianceScore, blueAllianceScore, redAllianceRP, blueAllianceRP, teamNumber1, teamNumber2, teamNumber3, teamNumber4, teamNumber5, teamNumber6, videoOffset, // games table
        whichGame, whichCompetition, teamId, currentState, scoutPriority, // games identifiers table
        chainID, gameNumber, competitionNumber, teamNumber, startingLocation, // main events table
        eventChainID, eventLocationInChain, eventType, timeStamps, // stamps table
        commentID, dateOfEntry, commentContent, associatedTeam, associatedGame, associatedChain, timeStamp // comments table
    }

    private enum Tables {
        eventTypes, containableEvents, // event definers
        competitions, teamNumbers, games, // basis data
        gamesProgress, eventFrames, events, comments // event data
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
        if (!databaseFile.exists()) {
            try {
                database = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath().toString());
                database.setAutoCommit(false);
                Statement statement = database.createStatement();

                // eventDefinitionsList
                statement.execute("CREATE TABLE " + Tables.eventTypes + "(" + System.lineSeparator() +
                        Columns.eventTypeID + " integer primary key AUTOINCREMENT," + System.lineSeparator() +
                        Columns.eventName + " text NOT NULL," + System.lineSeparator() +
                        Columns.followStamp + " boolean NOT NULL," + System.lineSeparator() +
                        Columns.teamSpecific + " boolean NOT NULL);");

                // containableEventsList
                statement.execute("CREATE TABLE " + Tables.containableEvents + "(" + System.lineSeparator() +
                        Columns.containerEventType + " integer NOT NULL," + System.lineSeparator() +
                        Columns.eventContainedType + " integer NOT NULL," + System.lineSeparator() +
                        "PRIMARY KEY(" + Columns.containerEventType + "," + Columns.eventContainedType + ")," + System.lineSeparator() +
                        "FOREIGN KEY(" + Columns.containerEventType + ") REFERENCES " + Tables.eventTypes + "(" + Columns.eventTypeID + ")," + System.lineSeparator() +
                        "FOREIGN KEY(" + Columns.eventContainedType + ") REFERENCES " + Tables.eventTypes + "(" + Columns.eventTypeID + "));");

                // competitions
                statement.execute("CREATE TABLE " + Tables.competitions + "(" + System.lineSeparator() +
                        Columns.competitionID + " integer primary key AUTOINCREMENT," + System.lineSeparator() +
                        Columns.competitionName + " text UNIQUE NOT NULL);");

                // teamsList
                statement.execute("CREATE TABLE " + Tables.teamNumbers + "(" + System.lineSeparator() +
                        Columns.teamNumbers + " integer NOT NULL primary key," + System.lineSeparator() +
                        Columns.teamNames + " text NOT NULL UNIQUE);");

                // gamesList
                statement.execute("CREATE TABLE " + Tables.games + "(" + System.lineSeparator() +
                        Columns.gameNumbers + " integer NOT NULL," + System.lineSeparator() +
                        Columns.competition + " integer NOT NULL," + System.lineSeparator() +
                        Columns.gameName + " text NOT NULL," + System.lineSeparator() +
                        Columns.wasCompleted + " boolean NOT NULL," + System.lineSeparator() +
                        Columns.mapConfiguration + " text," + System.lineSeparator() +
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
                        Columns.videoOffset + " integer," + System.lineSeparator() +
                        "PRIMARY KEY(" + Columns.gameNumbers + "," + Columns.competition + ")," + System.lineSeparator() +
                        "FOREIGN KEY(" + Columns.competition + ") REFERENCES " + Tables.competitions + "(" + Columns.competitionID + ")," + System.lineSeparator() +
                        "FOREIGN KEY(" + Columns.teamNumber1 + ") REFERENCES " + Tables.teamNumbers + "(" + Columns.teamNumbers + ")," + System.lineSeparator() +
                        "FOREIGN KEY(" + Columns.teamNumber2 + ") REFERENCES " + Tables.teamNumbers + "(" + Columns.teamNumbers + ")," + System.lineSeparator() +
                        "FOREIGN KEY(" + Columns.teamNumber3 + ") REFERENCES " + Tables.teamNumbers + "(" + Columns.teamNumbers + ")," + System.lineSeparator() +
                        "FOREIGN KEY(" + Columns.teamNumber4 + ") REFERENCES " + Tables.teamNumbers + "(" + Columns.teamNumbers + ")," + System.lineSeparator() +
                        "FOREIGN KEY(" + Columns.teamNumber5 + ") REFERENCES " + Tables.teamNumbers + "(" + Columns.teamNumbers + ")," + System.lineSeparator() +
                        "FOREIGN KEY(" + Columns.teamNumber6 + ") REFERENCES " + Tables.teamNumbers + "(" + Columns.teamNumbers + "));");

                // gameProgress
                statement.execute("CREATE TABLE " + Tables.gamesProgress + "(" + System.lineSeparator() +
                        Columns.whichGame + " integer NOT NULL," + System.lineSeparator() +
                        Columns.whichCompetition + " integer NOT NULL," + System.lineSeparator() +
                        Columns.teamId + " integer NOT NULL," + System.lineSeparator() +
                        Columns.currentState + " integer NOT NULL DEFAULT 0," + System.lineSeparator() +
                        Columns.scoutPriority + " integer NOT NULL DEFAULT 0," + System.lineSeparator() +
                        "PRIMARY KEY(" + Columns.whichGame + "," + Columns.whichCompetition + "," + Columns.teamId + ")," + System.lineSeparator() +
                        "FOREIGN KEY(" + Columns.whichGame + "," + Columns.whichCompetition + ") REFERENCES " + Tables.games + "(" + Columns.gameNumbers + "," + Columns.competition + "));");

                // eventFrames
                statement.execute("CREATE TABLE " + Tables.eventFrames + "(" + System.lineSeparator() +
                        Columns.chainID + " integer primary key AUTOINCREMENT," + System.lineSeparator() +
                        Columns.gameNumber + " integer NOT NULL," + System.lineSeparator() +
                        Columns.competitionNumber + " integer NOT NULL," + System.lineSeparator() +
                        Columns.teamNumber + " integer," + System.lineSeparator() +
                        Columns.startingLocation + " integer," + System.lineSeparator() +
                        "FOREIGN KEY(" + Columns.gameNumber + "," + Columns.competitionNumber + ") REFERENCES " + Tables.games + "(" + Columns.gameNumbers + "," + Columns.competition + ")," + System.lineSeparator() +
                        "FOREIGN KEY(" + Columns.teamNumber + ") REFERENCES " + Tables.teamNumbers + "(" + Columns.teamNumbers + "));");

                // eventsList
                statement.execute("CREATE TABLE " + Tables.events + "(" + System.lineSeparator() +
                        Columns.eventChainID + " integer NOT NULL," + System.lineSeparator() +
                        Columns.eventLocationInChain + " integer NOT NULL," + System.lineSeparator() +
                        Columns.eventType + " integer NOT NULL," + System.lineSeparator() +
                        Columns.timeStamps + " integer," + System.lineSeparator() +
                        "PRIMARY KEY(" + Columns.eventChainID + "," + Columns.eventLocationInChain + ")," + System.lineSeparator() +
                        "FOREIGN KEY(" + Columns.eventType + ") REFERENCES " + Tables.eventTypes + "(" + Columns.eventTypeID + ")," + System.lineSeparator() +
                        "FOREIGN KEY(" + Columns.eventChainID + ") REFERENCES " + Tables.eventFrames + "(" + Columns.chainID + "));");

                // commentsList
                statement.execute("CREATE TABLE " + Tables.comments + "(" + System.lineSeparator() +
                        Columns.commentID + " integer PRIMARY KEY," + System.lineSeparator() +
                        Columns.dateOfEntry + " time," + System.lineSeparator() +
                        Columns.commentContent + " text NOT NULL," + System.lineSeparator() +
                        Columns.associatedTeam + " integer," + System.lineSeparator() +
                        Columns.associatedGame + " integer," + System.lineSeparator() +
                        Columns.associatedChain + " integer," + System.lineSeparator() +
                        Columns.timeStamp + " integer," + System.lineSeparator() +
                        "FOREIGN KEY(" + Columns.associatedTeam + ") REFERENCES " + Tables.teamNumbers + " (" + Columns.teamNumbers + ")," + System.lineSeparator() +
                        "FOREIGN KEY(" + Columns.associatedGame + ") REFERENCES " + Tables.games + " (" + Columns.gameNumbers + ")," + System.lineSeparator() +
                        "FOREIGN KEY(" + Columns.associatedChain + ") REFERENCES " + Tables.eventFrames + " (" + Columns.chainID + "));");

                configEnforcer(statement);
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
            } catch (IOException e) {
                throw new Error("Failed to load scouting config!", e);
            }
        } else {
            try {
                database = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath().toString());
                database.setAutoCommit(false);
                database.rollback();
                Statement statement = database.createStatement();
                configEnforcer(statement);
                statement.execute("SELECT " + Columns.competitionID + "," + Columns.competitionName + " FROM " + Tables.competitions + ";");
                ResultSet set = statement.getResultSet();
                while (set.next()) {
                    competitionsMap.put(set.getString(Columns.competitionName.toString()), set.getByte(Columns.competitionID.toString()));
                }
                statement.close();
                database.commit();
            } catch (SQLException e) {
                throw new Error("Failed to initialize the database connection!", e);
            } catch (IOException e) {
                throw new Error("An error has occurred while loading the event configs for the database!", e);
            }
        }
    }

    private ArrayList<ScoutingEventDefinition> teamDefinitions;
    private ArrayList<ScoutingEventDefinition> teamStartDefinitions;

    protected ArrayList<ScoutingEventDefinition> getTeamDefinitions() {
        return teamDefinitions;
    }

    protected ArrayList<ScoutingEventDefinition> getTeamStartDefinitions() {
        return teamStartDefinitions;
    }

    private class ConfigFormat {
        private class EventDefinition {
            private byte type;
            private String name;
            private boolean followStamps;

            private EventDefinition(byte type, String name, boolean followStamps) {
                this.type = type;
                this.name = name;
                this.followStamps = followStamps;
            }
        }


        private HashMap<Byte, ArrayList<Byte>> chains = new HashMap<>();

        private ConfigFormat(ArrayList<Pair<String, ScoutingEventTree>> trees) {
            ArrayList<EventDefinition> teamEvents = new ArrayList<>();
            ArrayList<Byte> events = new ArrayList<>();
            ArrayList<EventDefinition> teamStart = new ArrayList<>();

            for (Pair<String, ScoutingEventTree> nodeT : trees) {
                ScoutingEventTree tree = nodeT.getValue();
                boolean first = true;
                for (Node nodeA : tree.getArrows()) {
                    ScoutingEventDirection arrow = (ScoutingEventDirection) nodeA;
                    if (!chains.containsKey(arrow.getStart().getUnitID())) {
                        chains.put(arrow.getStart().getUnitID(), new ArrayList<>());
                    }
                    chains.get(arrow.getStart().getUnitID()).add(arrow.getEnd().getUnitID());
                }
                for (Node nodeL : tree.getLayers()) {
                    ScoutingEventLayer layer = (ScoutingEventLayer) nodeL;
                    for (Node nodeU : layer.getUnits()) {
                        ScoutingEventUnit unit = (ScoutingEventUnit) nodeU;
                        EventDefinition def = new EventDefinition(unit.getUnitID(), unit.getName(), unit.getStamp());
                        if (!events.contains(unit.getUnitID())) {
                            events.add(unit.getUnitID());
                        } else {
                            throw new Error("Duplicate event found. There are two events numbered " + unit.getUnitID());
                        }
                        if (first) {
                            teamStart.add(def);
                            first = false;
                        }
                        teamStart.add(def);
                    }
                }
            }
            teamDefinitions = eventDefToScoutingEventDef(teamEvents);
            teamStartDefinitions = eventDefToScoutingEventDef(teamStart);
        }

        private ArrayList<ScoutingEventDefinition> eventDefToScoutingEventDef(ArrayList<EventDefinition> array) {
            ArrayList<ScoutingEventDefinition> output = new ArrayList<>();
            HashMap<Byte, byte[]> map = new HashMap<>();
            for (EventDefinition def : array) {
                byte bite = def.type;
                if (chains.containsKey(bite)) {
                    byte[] bites = new byte[chains.get(bite).size()];
                    for (int i = 0; i < bites.length; i++) {
                        bites[i] = chains.get(bite).get(i);
                    }
                    map.put(bite, bites);
                }
            }
            for (EventDefinition definition : array) {
                output.add(new ScoutingEventDefinition(map.get(definition.type), definition.type, definition.followStamps, definition.name));
            }
            return output;
        }
    }

    private void configEnforcer(Statement statement) throws IOException, SQLException {
        ConfigFormat format = new ConfigFormat(ScoutingTreesManager.getInstance().loadDirectory(ScoutingVars.getConfigDirectory()));
        String eventTypes = "INSERT OR REPLACE INTO " + Tables.eventTypes + " (" + Columns.eventTypeID + "," + Columns.eventName + "," + Columns.followStamp + "," + Columns.teamSpecific + ") VALUES (";
        boolean first = true;
        for (ScoutingEventDefinition def : teamDefinitions) {
            eventTypes += (!first ? ",(" : "") + byteFixer(def.getName(), false) + ",'" + def.getTextName() + "'," + booleanFixer(def.followStamp()) + "," + booleanFixer(true) + ")";
            first = false;
        }
        statement.execute(eventTypes + ";");
        String chains = "INSERT OR REPLACE INTO " + Tables.containableEvents + " (" + Columns.containerEventType + "," + Columns.eventContainedType + ") VALUES(";
        first = true;
        for (Byte bite : format.chains.keySet()) {
            for (Byte biter : format.chains.get(bite)) {
                chains += (!first ? ",(" : "") + byteFixer(bite, false) + "," + byteFixer(biter, false) + ")";
                first = false;
            }
        }
        statement.execute(chains + ";");
    }


    protected void addNewCompetition(String name) {
        try (Statement statement = database.createStatement()) {
            writeLock.lock();
            statement.execute("INSERT INTO " + Tables.competitions + "(" + Columns.competitionName + ") VALUES('" + name + "');");
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

    protected void teamsChanged(ArrayList<ScoutedTeam> add, ArrayList<ScoutedTeam> remove) {
        try (Statement statement = database.createStatement()) {
            writeLock.lock();
            String competitionColumns = "";
            String values = "";
            String removeVals = "";
            for (String comp : getCompetitionsList()) {
                competitionColumns += "," + participatedIn(comp);
            }
            boolean first = true;
            for (ScoutedTeam team : add) {
                values += (!first ? "," : "") + "(" + team.getNumber() + ",'" + team.getName() + "'";
                for (String comp : getCompetitionsList()) {
                    values += "," + (team.getCompetitions().contains(comp) ? 1 : 0);
                }
                values += ")";
                first = false;
            }
            if (values != "")
                statement.execute("INSERT OR REPLACE INTO " + Tables.teamNumbers + "(" + Columns.teamNumbers + "," + Columns.teamNames + competitionColumns + ") values" + values + ";");
            first = true;
            for (ScoutedTeam rip : remove) {
                removeVals += (!first ? " OR " : "") + Columns.teamNumbers + " = " + rip.getNumber();
                first = false;
            }
            if (removeVals != "") statement.execute("DELETE FROM " + Tables.teamNumbers + " WHERE " + removeVals + ";");
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

//    protected void removeTeam(ScoutedTeam team) {
//        try (Statement statement = database.createStatement()) {
//            writeLock.lock();
//            statement.execute("DELETE FROM " + Tables.teamNumbers + " WHERE " + Columns.teamNumbers + " = " + team.getNumber() + " AND " + Columns.teamNames + " = '" + team.getName() + "';");
//            database.commit();
//        } catch (SQLException e) {
//            try {
//                database.rollback();
//            } catch (SQLException e1) {
//                throw new Error("could not roll back database changes!", e1);
//            }
//            throw new IllegalArgumentException("The transaction could not be completed.", e);
//        } finally {
//            writeLock.unlock();
//        }
//    }

    protected void updateEventsOnGame(boolean wasFinished, ArrayList<ScouterCommentEvent> comments, FullScoutingEvent... events) {
        try (Statement statement = database.createStatement()) {
            if (events.length <= 0) return;
            writeLock.lock();
            Short team = events[0].getTeam();
            byte competition = events[0].getCompetition();
            short game = events[0].getGame();
            Byte startingLocation = events[0].getStartingLocation();
            boolean cleanup = events[0].getEvent().getChainID() == -2;
            statement.execute("INSERT OR REPLACE INTO " + Tables.gamesProgress + "(" + Columns.whichGame + "," + Columns.whichCompetition + "," + Columns.teamId + "," + Columns.currentState + (wasFinished ? "," + Columns.scoutPriority : "") + ") values" + System.lineSeparator() +
                    "(" + game + "," + competition + "," + startingLocation + "," + (cleanup ? 0 + "," + 0 : (wasFinished ? 2 + "," + 0 : 1 + "," + ("SELECT " + Columns.scoutPriority + " FROM " + Tables.gamesProgress + " WHERE " + Columns.whichGame + " = " + game + "," + Columns.whichCompetition + " = " + competition + "," + Columns.teamId + " = " + team))) + ");"
            );
            String stampsCleaner = "DELETE FROM " + Tables.events + System.lineSeparator() +
                    "WHERE " + Columns.eventChainID + " in " +
                    ("(SELECT " + Columns.chainID + " from " + Tables.eventFrames + System.lineSeparator() +
                            "where " + Columns.gameNumber + " = " + game + System.lineSeparator() +
                            "AND " + Columns.competitionNumber + " = " + competitionsMap.get(competition) + System.lineSeparator() +
                            "AND " + Columns.teamNumber + " = " + team) + ");";

            String framesCleaner = "DELETE FROM " + Tables.eventFrames + System.lineSeparator() +
                    "where " + Columns.gameNumber + " = " + game + System.lineSeparator() +
                    "AND " + Columns.competitionNumber + " = " + competitionsMap.get(competition) + System.lineSeparator() +
                    "AND " + Columns.teamNumber + " = " + team + System.lineSeparator() +
                    "AND " + Columns.chainID + " NOT IN " +
                    ("SELECT " + Columns.commentID + " FROM " + Tables.eventFrames + "where " + Columns.gameNumber + " = " + game + System.lineSeparator() +
                            "AND " + Columns.competitionNumber + " = " + competitionsMap.get(competition) + System.lineSeparator() +
                            "AND " + Columns.teamNumber + " = " + team) + System.lineSeparator() +
                    ");";

            String commentsRemover = "DELETE FROM " + Tables.comments + System.lineSeparator() +
                    "WHERE " + Columns.associatedGame + " = " + game + System.lineSeparator() +
                    "AMD " + Columns.associatedTeam + " = " + team + ";";

            statement.execute(stampsCleaner);
            statement.execute(framesCleaner);
            statement.execute(commentsRemover);


            if (cleanup) {
                database.commit();
                writeLock.unlock();
                return;
            }
            String framesFixer = "INSERT INTO " + Tables.eventFrames + "(" + Columns.chainID + "," + Columns.gameNumber + "," + Columns.competitionNumber + "," + Columns.teamNumber + "," + Columns.startingLocation + ")" + System.lineSeparator() +
                    "VALUES"; // continues in the for-loop
            String stampsFixer = "INSERT INTO " + Tables.events + "(" + Columns.eventChainID + "," + Columns.eventLocationInChain + "," + Columns.eventType + "," + Columns.timeStamps + ")" + System.lineSeparator() +
                    "values"; // continues in the for-loop
            statement.execute("SELECT " + Columns.chainID + " FROM " + Tables.eventFrames + " ORDER BY " + Columns.chainID + " DESC LIMIT 1;");
            ResultSet set = statement.getResultSet();
            int lastID;
            if (set.next()) lastID = set.getInt(Columns.chainID.toString());
            else lastID = 0;
            set.close();
            boolean first = true;
            for (FullScoutingEvent event : events) {
                if (event.getEvent().getChainID() == -1) {
                    event.getEvent().setChainID(++lastID);
                }
                framesFixer += (!first ? "," : "") + "("
                        + event.getEvent().getChainID() + ","
                        + game + ","
                        + competition + ","
                        + team + ","
                        + startingLocation + ")";
                int location = 1;
                for (ScoutingEvent.EventTimeStamp stamp : event.getEvent().getStamps()) {
                    stampsFixer += (!first ? "," : "") + "("
                            + event.getEvent().getChainID() + ","
                            + location++ + ","
                            + byteFixer(stamp.getType(), false) + ","
                            + stamp.getTimeStamp() + ")";
                    first = false;
                }
            }
            statement.execute(framesFixer + ";");
            statement.execute(stampsFixer + ";");
            if (comments.size() > 0) {
                String commentsS = "INSERT INTO " + Tables.comments + "(" + Columns.commentID + "," + Columns.commentContent + "," + Columns.associatedGame + "," + Columns.associatedTeam + "," + Columns.associatedChain + "," + Columns.timeStamp + ") VALUES";
                commentsS += String.join("", Collections.nCopies(comments.size(), "(?,?" + game + "," + team + ",?,?)"));
                PreparedStatement addComments = database.prepareStatement(commentsS);
                statement.execute("SELECT " + Columns.commentID + " FROM " + Tables.comments + " ORDER BY " + Columns.commentID + " DESC LIMIT 1;");
                set = statement.getResultSet();
                if (set.next()) lastID = set.getInt(Columns.chainID.toString());
                else lastID = 0;
                for (int i = 0; i/4 < comments.size();) {
                    ScouterCommentEvent comment = comments.get(i/4);
                    addComments.setInt(++i, comment.getCommentID() == -1 ? ++lastID : comment.getCommentID());
                    addComments.setString(++i, comment.getText());
                    if (comment.getAssociatedChain() != null) addComments.setInt(++i, comment.getAssociatedChain().getChainID()); else addComments.setNull(++i, Types.INTEGER);
                    if (comment.getTimeStamp() != null) addComments.setShort(++i, comment.getTimeStamp()); else addComments.setNull(++i, Types.INTEGER);
                }
                addComments.execute();
            }
            database.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                database.rollback();
            } catch (SQLException e1) {
                throw new Error("could not roll back database changes!", e1);
            }
            throw new IllegalArgumentException("The transaction could not be completed.", e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            writeLock.unlock();
        }
    }

    protected synchronized ArrayList<ScoutIdentifier> getIdentifiersForCompetition(boolean showCompleted, String competition) {
        try (Statement statement = database.createStatement()) {
            readLock.lock();
            statement.execute("SELECT " + Columns.whichGame + "," + Columns.whichCompetition + "," + Columns.currentState + "," + Columns.scoutPriority + " FROM " + Tables.gamesProgress + " WHERE " + Columns.whichCompetition + " = " + getCompetitionFromName(competition) + " ORDER BY " + Columns.whichGame + " ASC;");
            ResultSet set = statement.getResultSet();
            ArrayList<ScoutIdentifier> out = new ArrayList<>();
            while (set.next()) {
                ScoutIdentifier test = new ScoutIdentifier(competition, set.getShort(Columns.whichGame.toString()), set.getShort(Columns.teamId.toString()), set.getByte(Columns.scoutPriority.toString()), set.getByte(Columns.currentState.toString()));
                if (showCompleted || test.getState() != 0) out.add(test);
            }
            return out;
        } catch (SQLException e) {
            throw new IllegalArgumentException("The query could not be completed.", e);
        } finally {
            readLock.unlock();
        }
    }

    protected ScoutedGame getGame(short game, String competition) {
        try (Statement statement = database.createStatement()) {
            readLock.lock();
            statement.execute(formatGamesSelect(Columns.gameNumbers + " = " + game + " AND " + Columns.competition + " = " + getCompetitionFromName(competition), null));
            return convertResultSetToGames(statement.getResultSet()).get(0);
        } catch (SQLException e) {
            throw new IllegalArgumentException("The query could not be completed.", e);
        } finally {
            readLock.unlock();
        }
    }

//    protected void removeEvent(FullScoutingEvent event) {
//        try (Statement statement = database.createStatement()) {
//            writeLock.lock();
//            statement.execute("DELETE FROM " + Tables.events + " WHERE " + Columns.chainID + " = " + event.getEvent().getChainID() + ";");
//            statement.execute("DELETE FROM " + Tables.eventFrames + " WHERE " + Columns.eventChainID + " = " + event.getEvent().getChainID() + ";");
//            database.commit();
//        } catch (SQLException e) {
//            try {
//                database.rollback();
//            } catch (SQLException e1) {
//                throw new Error("could not roll back database changes!", e1);
//            }
//            throw new IllegalArgumentException("The transaction could not be completed.", e);
//        } finally {
//            writeLock.unlock();
//        }
//    }

//    protected void cleanUpGameEventsForTeam(short game, String competition, Short team, boolean alliance) {
//        writeLock.lock();
//        ScoutingEvent event = new ScoutingEvent();
//        event.setChainID(-2);
//        updateEventsOnGame(new FullScoutingEvent(event, team, game, competitionsMap.get(competition), null, (byte) -1, alliance));
//    }

    protected void refreshGames(ArrayList<ScoutedGame> games) {
        try (Statement statement = database.createStatement()) {
            writeLock.lock();
            String gameUpdate = "INSERT OR REPLACE INTO " + Tables.games + "(" + Columns.gameNumbers + "," + Columns.competition + "," + Columns.gameName + "," + Columns.wasCompleted + "," + Columns.mapConfiguration + "," + Columns.blueAllianceScore + "," + Columns.redAllianceScore + "," + Columns.blueAllianceRP + "," + Columns.redAllianceRP + "," + Columns.teamNumber1 + "," + Columns.teamNumber2 + "," + Columns.teamNumber3 + "," + Columns.teamNumber4 + "," + Columns.teamNumber5 + "," + Columns.teamNumber6 + "," + Columns.videoOffset + ") values";
            String gameStatus = "INSERT OR IGNORE INTO " + Tables.gamesProgress + "(" + Columns.whichGame + "," + Columns.whichCompetition + "," + Columns.teamId + ") values";
            boolean first = true;
            for (ScoutedGame game : games) {
                ArrayList<Short> testList = new ArrayList<>();
                for (Short teamNum : game.teamsArray()) {
                    if (!testList.contains(teamNum) || teamNum == null) {
                        testList.add(teamNum);
                    } else {
                        continue;
                    }
                }
                gameUpdate += (!first ? "," : "") + "(" + game.getGame() + "," + game.getCompetition() + ",'" + game.getName();
                if (game.didHappen()) {
                    gameUpdate += "'," + booleanFixer(true) + "," + (game.getMapConfiguration() == null ? null : game.getMapConfiguration().equals("") ? null : game.getMapConfiguration()) + "," + game.getBlueAllianceScore() + "," + game.getRedAllianceScore() + "," + game.getBlueAllianceRP() + "," + game.getRedAllianceRP();
                } else {
                    gameUpdate += "'," + booleanFixer(false) + "," + null + "," + null + "," + null + "," + null;
                }
                for (Short teamNum : game.teamsArray()) {
                    gameStatus += (!first ? "," : "") + "(" + game.getGame() + "," + game.getCompetition() + "," + teamNum + ")";
                    first = false;
                    gameUpdate += "," + String.valueOf(teamNum);
                }
                gameUpdate += "," + (game.didHappen() ? game.getVideoOffset() : null) + ")";
            }
            statement.execute(gameUpdate + ";");
            statement.execute(gameStatus + ";");
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


    private String participatedIn(String competition) {
        return Columns.participatedIn.toString() + competitionsMap.get(competition);
    }

    private String booleanFixer(boolean condition) {
        return condition ? "1" : "0";
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

    private String formatEventsSelect(String conditions, Integer limit) {
        return "SELECT " + Columns.eventChainID + "," + Columns.eventLocationInChain + "," + Columns.eventType + "," + Columns.timeStamps + "," + Columns.chainID + "," + Columns.gameNumber + "," + Columns.competitionNumber + "," + Columns.startingLocation + " FROM " + Tables.events + System.lineSeparator() +
                "INNER JOIN " + Tables.eventFrames + " ON " + Tables.eventFrames + "." + Columns.chainID + " = " + Tables.events + "." + Columns.eventChainID + System.lineSeparator() +
                "INNER JOIN " + Tables.games + " ON " + Tables.games + "." + Columns.gameNumbers + " = " + Tables.eventFrames + "." + Columns.gameNumber + System.lineSeparator() +
                (conditions == null ? "" : "WHERE " + conditions + System.lineSeparator()) +
                "ORDER BY " + Columns.eventChainID + " ASC, " + Columns.eventLocationInChain + " ASC" + System.lineSeparator() +
                (limit == null ? "" : "LIMIT " + limit) + ";";
    }

    private String getSpecificGame(short game, String competition) {
        return Columns.gameNumbers + " = " + game + " AND " + Columns.competition + " = " + competitionsMap.get(competition);
    }

    private String formatGamesSelect(String conditions, Integer limit) {
        return "SELECT " + Columns.gameNumbers + "," + Columns.competition + "," + Columns.gameName + "," + Columns.wasCompleted + "," + Columns.mapConfiguration + "," + Columns.blueAllianceScore + "," + Columns.redAllianceScore + "," + Columns.blueAllianceRP + "," + Columns.redAllianceRP + "," + Columns.teamNumber1 + "," + Columns.teamNumber2 + "," + Columns.teamNumber3 + "," + Columns.teamNumber4 + "," + Columns.teamNumber5 + "," + Columns.teamNumber6 + "," + Columns.videoOffset + " FROM " + Tables.games + System.lineSeparator() +
                (conditions == null ? "" : "WHERE " + conditions + System.lineSeparator()) +
                (limit == null ? "" : "LIMIT " + limit) + ";";
    }

    private String formatTeamsSelect(String conditions, Integer limit) {
        String competitionsColumns = "";
        for (String comp : getCompetitionsList()) {
            competitionsColumns += "," + participatedIn(comp);
        }
        return "SELECT " + Columns.teamNumbers + "," + Columns.teamNames + competitionsColumns + " FROM " + Tables.teamNumbers + System.lineSeparator() +
                (conditions == null ? "" : "WHERE " + conditions + System.lineSeparator()) +
                (limit == null ? "" : "LIMIT " + limit) + ";";
    }


    protected Pair<String, Byte> getTeamConfiguration(short game, String competition, short team) {
        try (Statement statement = database.createStatement()) {
            readLock.lock();
            statement.execute(formatGamesSelect(getSpecificGame(game, competition), 1));

            ScoutedGame result = convertResultSetToGames(statement.getResultSet()).get(0);
            Byte startLoc = null;
            if (result.getTeamNumber1() == team) {
                startLoc = 1;
            } else if (result.getTeamNumber2() == team) {
                startLoc = 2;
            } else if (result.getTeamNumber3() == team) {
                startLoc = 3;
            } else if (result.getTeamNumber4() == team) {
                startLoc = 4;
            } else if (result.getTeamNumber5() == team) {
                startLoc = 5;
            } else if (result.getTeamNumber6() == team) {
                startLoc = 6;
            }
            return new Pair<>(result.getMapConfiguration(), startLoc);
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

    protected ArrayList<FullScoutingEvent> getAllTeamEventsOfType() {
        return null;
    }

    protected ArrayList<FullScoutingEvent> getTeamEventsOfTypeByGame() {
        return null;
    }

    protected ArrayList<FullScoutingEvent> getAllTeamEvents() {
        return null;
    }

    protected ArrayList<FullScoutingEvent> getGameResult() {
        return null;
    }

    protected ArrayList<FullScoutingEvent> getGamesResult() {
        return null;
    }

    protected ArrayList<ScoutedGame> getGamesList(String competition, boolean mustHappen) {
        try (Statement statement = database.createStatement()) {
            readLock.lock();
            statement.execute(formatGamesSelect(Columns.competition + " = " + competitionsMap.get(competition) + (mustHappen ? " AND " + Columns.wasCompleted + " = " + booleanFixer(true) : ""), null));

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

    protected ArrayList<ScoutedTeam> getTeamsList() {
        try (Statement statement = database.createStatement()) {
            readLock.lock();
            statement.execute(formatTeamsSelect(null, null));
            return convertResultSetToTeams(statement.getResultSet());
        } catch (SQLException e) {
            throw new IllegalArgumentException("The query could not be completed.", e);
        } finally {
            readLock.unlock();
        }
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

    protected ArrayList<ScoutedTeam> getAllTeamsForCompetition(String competition) {
        try (Statement statement = database.createStatement()) {
            readLock.lock();
            statement.execute(formatTeamsSelect(participatedIn(competition) + " = " + booleanFixer(true), null));
            return convertResultSetToTeams(statement.getResultSet());
        } catch (SQLException e) {
            throw new IllegalArgumentException("The query could not be completed.", e);
        } finally {
            readLock.unlock();
        }
    }


    private ArrayList<ScoutedGame> convertResultSetToGames(ResultSet set) throws SQLException {
        ArrayList<ScoutedGame> result = new ArrayList<>();
        while (set.next()) {
            result.add(set.getBoolean(Columns.wasCompleted.toString()) ?
                    new ScoutedGame(
                            set.getShort(Columns.gameNumbers.toString()),
                            set.getByte(Columns.competition.toString()),
                            set.getString(Columns.gameName.toString()),
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
                            set.getShort(Columns.teamNumber6.toString()),
                            set.getShort(Columns.videoOffset.toString())) :
                    new ScoutedGame(
                            set.getShort(Columns.gameNumbers.toString()),
                            set.getByte(Columns.competition.toString()),
                            set.getString(Columns.gameName.toString()),
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
            ArrayList<String> competitions = new ArrayList<>();
            for (String comp : getCompetitionsList()) {
                if (set.getBoolean(participatedIn(comp)))
                    competitions.add(comp);
            }
            result.add(new ScoutedTeam(
                    set.getShort(Columns.teamNumbers.toString()),
                    set.getString(Columns.teamNames.toString()),
                    competitions
            ));
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
                            this.addProgress((byte) byteFixer(set.getShort(Columns.eventType.toString()), true), set.getShort(Columns.timeStamps.toString()) == 0 ? null : set.getShort(Columns.timeStamps.toString()));
                        }},
                        set.getShort(Columns.teamNumber.toString()) == 0 ? null : set.getShort(Columns.teamNumber.toString()),
                        set.getShort(Columns.gameNumber.toString()),
                        set.getByte(Columns.competitionNumber.toString()),
                        set.getString(Columns.mapConfiguration.toString()),
                        set.getByte(Columns.startingLocation.toString())
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
     *
     * @param toFix The value to fix
     * @param down  Should it be lowered (aka it's being pulled from sqlite) or should it be increased (aka it's being pushed into sqlite)
     * @return The converted value, in Short form (it then has to be cast)
     */
    private short byteFixer(short toFix, boolean down) throws IllegalArgumentException {
        if (toFix < 1 && down || toFix > 256 && !down)
            throw new IllegalStateException("The byte was out of bounds");
        return (short) (toFix + (down ? -129 : 129));
    }

}