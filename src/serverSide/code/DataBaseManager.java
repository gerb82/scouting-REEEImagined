package serverSide.code;

import connectionIndependent.FullScoutingEvent;
import connectionIndependent.ScoutingEvent;
import gbuiLib.GBUILibGlobals;

import java.io.Closeable;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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

    private Connection database;

    // database mapping available at https://www.dbdesigner.net/designer/schema/221463
    private enum Columns {
        eventTypeID, eventName, teamSpecific, // event types
        containerEventType, eventContainedType, // containers table
        teamNumbers, teamNames, participatedIn, // teams table
        gameNumbers, mapConfiguration, competition, redAllianceScore, blueAllianceScore, redAllianceRP, blueAllianceRP, teamNumber1, teamNumber2, teamNumber3, teamNumber4, teamNumber5, teamNumber6, // games table
        competitionID, competitionName, // competitions
        eventChainID, eventLocationInChain, eventType, timeStamps, // stamps table
        chainID, gameNumber, competitionNumber, teamNumber, alliance, startingLocation, // main events table
        commentContent, associatedTeam, associatedGame, associatedChain, timeStamp // comments table
    }

    private enum Tables {
        eventTypes, containableEvents, teamNumbers, events, games, competitions, eventFrames, comments
    }

    public DataBaseManager() {
        File databaseDirectory = GBUILibGlobals.getDataBaseDirectory();
        databaseDirectory.mkdirs();
        File databaseFile = new File(databaseDirectory, "database.sqlite");
        try {
            database = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath().toString());
            database.setAutoCommit(false);
            Statement statement = database.createStatement();

            // eventDefinitionsList
            statement.execute("CREATE TABLE IF NOT EXISTS " + Tables.eventTypes + "(" + System.lineSeparator() +
                    Columns.eventTypeID + " integer primary key AUTO INCREMENT," + System.lineSeparator() +
                    Columns.eventName + " text NOT NULL UNIQUE," +
                    Columns.teamSpecific + " integer NOT NULL);");

            // competitions
            statement.execute("CREATE TABLE IF NOT EXISTS " + Tables.competitions + "(" + System.lineSeparator() +
                    Columns.competitionID + " integer primary key AUTO INCREMENT," + System.lineSeparator() +
                    Columns.competitionName + " text NOT NULL);");

            // containableEventsList
            statement.execute("CREATE TABLE IF NOT EXISTS " + Tables.containableEvents + "(" + System.lineSeparator() +
                    Columns.containerEventType + " integer NOT NULL," + System.lineSeparator() +
                    Columns.eventContainedType + " integer NOT NULL," + System.lineSeparator() +
                    "PRIMARY KEY(" + Columns.containerEventType + "," + Columns.eventContainedType + ")." + System.lineSeparator() +
                    "FOREIGN KEY(" + Columns.containerEventType + ") REFERENCES " + Tables.eventTypes + "(" + Columns.eventTypeID + ")," + System.lineSeparator() +
                    "FOREIGN KEY(" + Columns.eventContainedType + ") REFERENCES " + Tables.eventTypes + "(" + Columns.eventTypeID + "));");

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
                    Columns.chainID + " integer primary key AUTO INCREMENT," + System.lineSeparator() +
                    Columns.gameNumber + " integer NOT NULL," + System.lineSeparator() +
                    Columns.competitionNumber + " integer NOT NULL," + System.lineSeparator() +
                    Columns.teamNumber + " integer," + System.lineSeparator() +
                    Columns.alliance + " integer NOT NULL," + System.lineSeparator() +
                    Columns.startingLocation + " integer NOT NULL," + System.lineSeparator() +
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
                    Columns.timeStamp + " integer" + System.lineSeparator() +
                    "FOREIGN KEY(" + Columns.associatedTeam + ") REFERENCES " + Tables.teamNumbers + " (" + Columns.teamNumbers + ")," + System.lineSeparator() +
                    "FOREIGN KEY(" + Columns.associatedGame + ") REFERENCES " + Tables.games + " (" + Columns.gameNumbers + ")," + System.lineSeparator() +
                    "FOREIGN KEY(" + Columns.associatedChain + ") REFERENCES " + Tables.eventFrames + " (" + Columns.chainID + "));");

            database.commit();
            statement.close();
        } catch (SQLException e) {
            try {
                if (database != null) {
                    database.rollback();
                    throw new Error("Failed to initialize the database!");
                } else {
                    throw new Error("Could not connect to the database!");
                }
            } catch (SQLException e1) {
                throw new Error("Failed to roll back the database!");
            }
        }
    }

    private boolean configValidator() {
        return true;
    }

    private String competitionNameToID(String competition) {
        return "SELECT " + Columns.competitionID + " from " + Tables.competitions + "where " + Columns.competitionName + " = '" + competition + "'";
    }

    private String allianceToAllianceID(boolean blueAlliance) {
        return blueAlliance ? "1" : "2";
    }

    private String matchEventGameAndCompetition(short game, String competition){
        return Columns.gameNumber + " = " + game + " AND " + Columns.competitionNumber + " = " + competitionNameToID(competition);
    }

    private String matchEventTeam(short team){
        return Columns.teamNumber + " = " + team;
    }

    private String matchEventType(byte type){
        return Columns.eventType + " IN " + "(SELECT " + Columns.eventChainID + " FROM " + Tables.events + " WHERE " + Columns.eventType + " = " + type + ")";
    }

    private String matchTeamAlliance(short team){
        return "((" + Columns.alliance + " = " + allianceToAllianceID(true) + " AND (" + Columns.teamNumber1 + " = " + team + " OR " + Columns.teamNumber2 + " = " + team + " OR " + Columns.teamNumber3 + " = " + team + "))" + System.lineSeparator() +
        "OR (" + Columns.alliance + " = " + allianceToAllianceID(false) + " AND (" + Columns.teamNumber4 + " = " + team + " OR " + Columns.teamNumber5 + " = " + team + " OR " + Columns.teamNumber6 + " = " + team + ")))";
    }

    protected void addNewCompetition(String name, ScoutedTeam[] teams) {
        try (Statement statement = database.createStatement()) {
            statement.execute("INSERT INTO " + Tables.competitions + "(" + Columns.competitionName + ") VALUES(" + name + ");");
            statement.execute("ALTER TABLE " + Tables.teamNumbers + " ADD COLUMN " + Columns.participatedIn + competitionNameToID(name) + " integer NOT NULL DEFAULT 0;");
            String insertTeams = "INSERT OR IGNORE INTO " + Tables.teamNumbers + "(" + Columns.teamNumbers + "," + Columns.teamNames + "," + Columns.participatedIn + competitionNameToID(name) + ") VALUES";
            String signUpTeams = "UPDATE " + Tables.teamNumbers + " SET " + Columns.participatedIn + competitionNameToID(name) + " = 1 WHERE ";
            boolean first = true;
            for (ScoutedTeam team : teams) {
                insertTeams += (!first ? "," : "") + "(" + team.getNumber() + ",'" + team.getName() + "'," + "1)";
                signUpTeams += (!first ? " OR " : "") + Columns.teamNumbers + " = " + team.getNumber();
                first = false;
            }
            statement.execute(insertTeams + ";");
            statement.execute(signUpTeams + ";");
            database.commit();
        } catch (SQLException e) {
            try {
                database.rollback();
            } catch (SQLException e1) {
                throw new Error("could not roll back database changes!", e);
            }
            throw new IllegalArgumentException("The transaction could not be completed.", e);
        }
    }

    protected void addTeam(ScoutedTeam team, String[] competitions) {
        try (Statement statement = database.createStatement()) {
            String competitionsColumns = "";
            for (String competition : competitions) {
                competitionsColumns += "," + Columns.participatedIn + competitionNameToID(competition);
            }
            statement.execute("INSERT INTO " + Tables.teamNumbers + "(" + Columns.teamNumbers + "," + Columns.teamNames + competitionsColumns + ")" + System.lineSeparator() +
                    "VALUES(" + team.getNumber() + ",'" + team.getName() + "'" + String.join("", Collections.nCopies(competitionsColumns.length(), ",1")) + ");");
            database.commit();
        } catch (SQLException e) {
            try {
                database.rollback();
            } catch (SQLException e1) {
                throw new Error("could not roll back database changes!", e);
            }
            throw new IllegalArgumentException("The transaction could not be completed.", e);
        }
    }

    protected void updateEvents(int game, String competition, int team, FullScoutingEvent[] events) {
        try (Statement statement = database.createStatement()) {
            Set<Integer> numberedEvents = new HashSet<>();
            boolean first = true;
            String idList = "(";
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
            String stampsCleaner = "DELETE FROM " + Tables.events + System.lineSeparator() +
                    "WHERE " + Columns.eventChainID + " in " +
                    ("SELECT " + Columns.chainID + " from " + Tables.eventFrames + System.lineSeparator() +
                            "where " + Columns.gameNumber + " = " + game + System.lineSeparator() + "" + System.lineSeparator() +
                            "AND " + Columns.competitionNumber + " = " + competitionNameToID(competition) +
                            " AND " + Columns.teamNumber + " = " + team) + ");";

            String framesCleaner = "DELETE FROM " + Tables.eventFrames + System.lineSeparator() +
                    "where " + Columns.gameNumber + " = " + game + System.lineSeparator() +
                    "AND " + Columns.competitionNumber + " = " + competitionNameToID(competition) + System.lineSeparator() +
                    "AND " + Columns.teamNumber + " = " + team + System.lineSeparator() +
                    "AND " + Columns.chainID + " NOT IN " + idList + ";";

            statement.execute(stampsCleaner + " " + framesCleaner);
            String framesFixer = "INSERT OR REPLACE INTO " + Tables.eventFrames + "(" + Columns.chainID + "," + Columns.alliance + "," + Columns.gameNumber + "," + Columns.competitionNumber + "," + Columns.teamNumber + "," + Columns.startingLocation + ")" + System.lineSeparator() +
                    "VALUES"; // continues in the for-loop
            String stampsFixer = "INSERT INTO " + Tables.events + "(" + Columns.eventChainID + "," + Columns.eventLocationInChain + "," + Columns.eventType + "," + Columns.timeStamps + ")" + System.lineSeparator() +
                    "values"; // continues in the for-loop
            statement.execute("SELECT " + Columns.chainID + " FROM " + Tables.eventFrames + " ORDER BY " + Columns.chainID + " DESC LIMIT 1;");
            ResultSet set = statement.getResultSet();
            set.next();
            Integer lastID = set.getInt(Columns.chainID.toString());
            first = true;
            for (FullScoutingEvent event : events) {
                if (event.getEvent().getChainID() == -1) {
                    event.getEvent().setChainID(++lastID);
                }
                framesFixer += (!first ? "," : "") + "("
                        + event.getEvent().getChainID() + ","
                        + event.getGame() + ","
                        + event.getCompetition() + ","
                        + event.getTeam() + ","
                        + event.getAlliance() + ","
                        + event.getStartingLocation() + ")";
                int location = 1;
                for (ScoutingEvent.EventTimeStamp stamp : event.getEvent().getStamps()) {
                    stampsFixer += (!first ? "," : "") + "("
                            + event.getEvent().getChainID() + ","
                            + location++ + ","
                            + event.getEvent().getType() + ","
                            + String.valueOf(stamp.getTimeStamp()) + ")";
                    first = false;
                }
            }
            statement.execute(framesFixer + "; " + stampsFixer);
            database.commit();
        } catch (SQLException e) {
            try {
                database.rollback();
            } catch (SQLException e1) {
                throw new Error("could not roll back database changes!", e);
            }
            throw new IllegalArgumentException("The transaction could not be completed.", e);
        }
    }

    protected void addGame(int gameNumber, String competition, String mapConfiguration, Integer[] teams) {
        try (Statement statement = database.createStatement()) {
            assert (teams.length == 6);
            ArrayList<Integer> testList = new ArrayList<>();
            for (Integer teamNum : teams) {
                if (!testList.contains(teamNum) || teamNum == null) {
                    testList.add(teamNum);
                } else {
                    throw new IllegalArgumentException("The same team was supplied twice for the given new game!");
                }
            }
            String gameCreate = "INSERT INTO " + Tables.games + "(" + Columns.gameNumbers + "," + Columns.competition + "," + Columns.mapConfiguration + "," + Columns.teamNumber1 + "," + Columns.teamNumber2 + "," + Columns.teamNumber3 + "," + Columns.teamNumber4 + "," + Columns.teamNumber5 + "," + Columns.teamNumber6 + ") values(" + gameNumber + "," + "(Select " + Columns.competitionID + " from " + Tables.competitions + " where " + Columns.competitionName + " = '" + competition + "')" + "," + mapConfiguration;
            for (Integer teamNum : teams) {
                gameCreate += "," + teamNum;
            }
            statement.execute(gameCreate + ";");
            database.commit();
        } catch (AssertionError e) {
            throw new IllegalArgumentException("The teams array was not of the right size!");
        } catch (SQLException e) {
            try {
                database.rollback();
            } catch (SQLException e1) {
                throw new Error("could not roll back database changes!", e);
            }
            throw new IllegalArgumentException("The transaction could not be completed.", e);
        }
    }

    protected void addTeamsToCompetitions(ScoutedTeam[] teams, String[] competitions) {
    }




    private String formatEventsSelect(String conditions) {
        return "SELECT " + Columns.eventChainID + "," + Columns.eventLocationInChain + "," + Columns.eventType + "," + Columns.timeStamps + "," + Columns.chainID + "," + Columns.alliance + "," + Columns.gameNumber + "," + Columns.competitionNumber + "," + Columns.startingLocation + " FROM " + Tables.events + System.lineSeparator() +
                "INNER JOIN " + Tables.eventFrames + " ON " + Tables.eventFrames + "." + Columns.chainID + " = " + Tables.events + "." + Columns.eventChainID + System.lineSeparator() +
                "INNER JOIN " + Tables.games + " ON " + Tables.games + "." + Columns.gameNumber + " = " + Tables.eventFrames + "." + Columns.gameNumber + System.lineSeparator() +
                (conditions == null ? "" : "WHERE " + conditions + System.lineSeparator()) +
                "ORDER BY " + Columns.eventChainID + " ASC, " + Columns.eventLocationInChain + " ASC;";
    }

    protected ArrayList<FullScoutingEvent> getEventsByGame(short game, String competition) {
        try (Statement statement = database.createStatement()) {
            statement.execute(formatEventsSelect(matchEventGameAndCompetition(game, competition)));

            return convertResultSetToEvents(statement.getResultSet());
        } catch (SQLException e) {
            throw new IllegalArgumentException("The transaction could not be completed.", e);
        }
    }

    protected ArrayList<FullScoutingEvent> getAllTeamEventsByGame(short game, String competition, short team) {
        try (Statement statement = database.createStatement()) {
            statement.execute(formatEventsSelect( matchEventGameAndCompetition(game, competition) + System.lineSeparator() +
                    "AND " + matchEventTeam(team)));

            return convertResultSetToEvents(statement.getResultSet());
        } catch (SQLException e) {
            throw new IllegalArgumentException("The transaction could not be completed.", e);
        }
    }

    protected ArrayList<FullScoutingEvent> getTeamAllianceEvents(short team) {
        try (Statement statement = database.createStatement()) {
            statement.execute(formatEventsSelect(matchTeamAlliance(team)));

            return convertResultSetToEvents(statement.getResultSet());
        } catch (SQLException e) {
            throw new IllegalArgumentException("The transaction could not be completed.", e);
        }
    }

    protected ArrayList<FullScoutingEvent> getTeamAliienceEventsByGame(short game, String competition, short team) {
        try (Statement statement = database.createStatement()) {
            statement.execute(formatEventsSelect(matchEventGameAndCompetition(game, competition) + System.lineSeparator() +
                    "AND " + matchTeamAlliance(team)));

            return convertResultSetToEvents(statement.getResultSet());
        } catch (SQLException e) {
            throw new IllegalArgumentException("The transaction could not be completed.", e);
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

    protected ArrayList<FullScoutingEvent> getAllienceEvents() {
        return null;
    }

    protected ArrayList<FullScoutingEvent> getGameResult() {
        return null;
    }

    protected ArrayList<FullScoutingEvent> getGamesResult() {
        return null;
    }

    protected ArrayList<FullScoutingEvent> getGamesList() {
        return null;
    }

    protected ArrayList<FullScoutingEvent> getCompetitionsList() {
        return null;
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
                            this.addProgress(set.getByte(Columns.eventType.toString()), set.getShort(Columns.timeStamps.toString()) == 0 ? null : set.getShort(Columns.timeStamps.toString()));
                        }},
                        set.getShort(Columns.teamNumber.toString()) == 0 ? null : set.getShort(Columns.teamNumber.toString()),
                        set.getShort(Columns.gameNumber.toString()),
                        set.getShort(Columns.competitionNumber.toString()),
                        set.getString(Columns.mapConfiguration.toString()),
                        set.getByte(Columns.startingLocation.toString()),
                        (byte) (set.getByte(Columns.alliance.toString()) - 1)
                );
                result.add(buffer);
            } else {
                buffer.getEvent().addProgress(set.getByte(Columns.eventType.toString()), set.getShort(Columns.timeStamp.toString()));
            }
        }
        set.close();
        return result;
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