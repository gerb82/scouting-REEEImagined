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
    private enum columns {
        eventTypeID, eventName, teamSpecific, // event types
        containerEventType, eventContainedType, // containers table
        teamNumbers, teamNames, participatedIn, // teams table
        gameNumbers, mapConfiguration, competition, redAllianceScore, blueAllianceScore, redAllianceRP, blueAllianceRP, teamNumber1, teamNumber2, teamNumber3, teamNumber4, teamNumber5, teamNumber6, // games table
        competitionID, competitionName, // competitions
        eventChainID, eventLocationInChain, eventType, timeStamps, // stamps table
        chainID, gameNumber, competitionNumber, teamNumber, alliance, startingLocation, // main events table
        commentContent, associatedTeam, associatedGame, associatedChain, timeStamp // comments table
    }

    private enum tables {
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
            statement.execute("CREATE TABLE IF NOT EXISTS " + tables.eventTypes + "(" + System.lineSeparator() +
                    columns.eventTypeID + " integer primary key AUTO INCREMENT," + System.lineSeparator() +
                    columns.eventName + " text NOT NULL UNIQUE," +
                    columns.teamSpecific + " integer NOT NULL);");

            // competitions
            statement.execute("CREATE TABLE IF NOT EXISTS " + tables.competitions + "(" + System.lineSeparator() +
                    columns.competitionID + " integer primary key AUTO INCREMENT," + System.lineSeparator() +
                    columns.competitionName + " text NOT NULL);");

            // containableEventsList
            statement.execute("CREATE TABLE IF NOT EXISTS " + tables.containableEvents + "(" + System.lineSeparator() +
                    columns.containerEventType + " integer NOT NULL," + System.lineSeparator() +
                    columns.eventContainedType + " integer NOT NULL," + System.lineSeparator() +
                    "PRIMARY KEY(" + columns.containerEventType + "," + columns.eventContainedType + ")." + System.lineSeparator() +
                    "FOREIGN KEY(" + columns.containerEventType + ") REFERENCES " + tables.eventTypes + "(" + columns.eventTypeID + ")," + System.lineSeparator() +
                    "FOREIGN KEY(" + columns.eventContainedType + ") REFERENCES " + tables.eventTypes + "(" + columns.eventTypeID + "));");

            // teamsList
            statement.execute("CREATE TABLE IF NOT EXISTS " + tables.teamNumbers + "(" + System.lineSeparator() +
                    columns.teamNumbers + " integer primary key," + System.lineSeparator() +
                    columns.teamNames + " text NOT NULL UNIQUE);");

            // gamesList
            statement.execute("CREATE TABLE IF NOT EXISTS " + tables.games + "(" + System.lineSeparator() +
                    columns.gameNumbers + " integer NOT NULL," + System.lineSeparator() +
                    columns.mapConfiguration + " text," + System.lineSeparator() +
                    columns.competition + " integer NOT NULL," + System.lineSeparator() +
                    columns.redAllianceScore + " integer NOT NULL," + System.lineSeparator() +
                    columns.redAllianceRP + " integer NOT NULL," + System.lineSeparator() +
                    columns.blueAllianceScore + " integer NOT NULL," + System.lineSeparator() +
                    columns.blueAllianceRP + " integer NOT NULL," + System.lineSeparator() +
                    columns.teamNumber1 + " integer," + System.lineSeparator() +
                    columns.teamNumber2 + " integer," + System.lineSeparator() +
                    columns.teamNumber3 + " integer," + System.lineSeparator() +
                    columns.teamNumber4 + " integer," + System.lineSeparator() +
                    columns.teamNumber5 + " integer," + System.lineSeparator() +
                    columns.teamNumber6 + " integer," + System.lineSeparator() +
                    "PRIMARY KEY(" + columns.gameNumbers + "," + columns.competition + ")," + System.lineSeparator() +
                    "FOREIGN KEY(" + columns.competition + ") REFERENCES " + tables.competitions + "(" + columns.competitionID + ")," + System.lineSeparator() +
                    "FOREIGN KEY(" + columns.teamNumber1 + ") REFERENCES " + tables.teamNumbers + "(" + columns.teamNumbers + ")," + System.lineSeparator() +
                    "FOREIGN KEY(" + columns.teamNumber2 + ") REFERENCES " + tables.teamNumbers + "(" + columns.teamNumbers + ")," + System.lineSeparator() +
                    "FOREIGN KEY(" + columns.teamNumber3 + ") REFERENCES " + tables.teamNumbers + "(" + columns.teamNumbers + ")," + System.lineSeparator() +
                    "FOREIGN KEY(" + columns.teamNumber4 + ") REFERENCES " + tables.teamNumbers + "(" + columns.teamNumbers + ")," + System.lineSeparator() +
                    "FOREIGN KEY(" + columns.teamNumber5 + ") REFERENCES " + tables.teamNumbers + "(" + columns.teamNumbers + ")," + System.lineSeparator() +
                    "FOREIGN KEY(" + columns.teamNumber6 + ") REFERENCES " + tables.teamNumbers + "(" + columns.teamNumbers + "));");

            // eventFrames
            statement.execute("CREATE TABLE IF NOT EXISTS " + tables.eventFrames + "(" + System.lineSeparator() +
                    columns.chainID + " integer primary key AUTO INCREMENT," + System.lineSeparator() +
                    columns.gameNumber + " integer NOT NULL," + System.lineSeparator() +
                    columns.competitionNumber + " integer NOT NULL," + System.lineSeparator() +
                    columns.teamNumber + " integer," + System.lineSeparator() +
                    columns.alliance + " integer NOT NULL," + System.lineSeparator() +
                    columns.startingLocation + " integer NOT NULL," + System.lineSeparator() +
                    "FOREIGN KEY(" + columns.gameNumber + "," + columns.competitionNumber + ") REFERENCES " + tables.games + "(" + columns.gameNumbers + "," + columns.competition + ")," + System.lineSeparator() +
                    "FOREIGN KEY(" + columns.teamNumber + ") REFERENCES " + tables.teamNumbers + "(" + columns.teamNumbers + "));");

            // eventsList
            statement.execute("CREATE TABLE IF NOT EXISTS " + tables.events + "(" + System.lineSeparator() +
                    columns.eventChainID + " integer NOT NULL," + System.lineSeparator() +
                    columns.eventLocationInChain + " integer NOT NULL," + System.lineSeparator() +
                    columns.eventType + " integer NOT NULL," + System.lineSeparator() +
                    columns.timeStamps + " integer," + System.lineSeparator() +
                    "PRIMARY KEY(" + columns.eventChainID + "," + columns.eventLocationInChain + ")," + System.lineSeparator() +
                    "FOREIGN KEY(" + columns.eventType + ") REFERENCES " + tables.eventTypes + "(" + columns.eventTypeID + ")," + System.lineSeparator() +
                    "FOREIGN KEY(" + columns.eventChainID + ") REFERENCES " + tables.eventFrames + "(" + columns.chainID + "));");

            // commentsList
            statement.execute("CREATE TABLE IF NOT EXISTS " + tables.comments + "(" + System.lineSeparator() +
                    columns.commentContent + " text NOT NULL," + System.lineSeparator() +
                    columns.associatedTeam + " integer," + System.lineSeparator() +
                    columns.associatedGame + " integer," + System.lineSeparator() +
                    columns.associatedChain + " integer," + System.lineSeparator() +
                    columns.timeStamp + " integer" + System.lineSeparator() +
                    "FOREIGN KEY(" + columns.associatedTeam + ") REFERENCES " + tables.teamNumbers + " (" + columns.teamNumbers + ")," + System.lineSeparator() +
                    "FOREIGN KEY(" + columns.associatedGame + ") REFERENCES " + tables.games + " (" + columns.gameNumbers + ")," + System.lineSeparator() +
                    "FOREIGN KEY(" + columns.associatedChain + ") REFERENCES " + tables.eventFrames + " (" + columns.chainID + "));");

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
        return "SELECT " + columns.competitionID + " from " + tables.competitions + "where " + columns.competitionName + " = '" + competition + "'";
    }

    protected void addNewCompetition(String name, ScoutedTeam[] teams) {
        try (Statement statement = database.createStatement()) {
            statement.execute("INSERT INTO " + tables.competitions + "(" + columns.competitionName + ") VALUES(" + name + ");");
            statement.execute("ALTER TABLE " + tables.teamNumbers + " ADD COLUMN " + columns.participatedIn + competitionNameToID(name) + " integer NOT NULL DEFAULT 0;");
            String insertTeams = "INSERT OR IGNORE INTO " + tables.teamNumbers + "(" + columns.teamNumbers + "," + columns.teamNames + "," + columns.participatedIn + competitionNameToID(name) + ") VALUES";
            String signUpTeams = "UPDATE " + tables.teamNumbers + " SET " + columns.participatedIn + competitionNameToID(name) + " = 1 WHERE ";
            boolean first = true;
            for (ScoutedTeam team : teams) {
                insertTeams += (!first ? "," : "") + "(" + team.getNumber() + ",'" + team.getName() + "'," + "1)";
                signUpTeams += (!first ? " OR " : "") + columns.teamNumbers + " = " + team.getNumber();
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
                competitionsColumns += "," + columns.participatedIn + competitionNameToID(competition);
            }
            statement.execute("INSERT INTO " + tables.teamNumbers + "(" + columns.teamNumbers + "," + columns.teamNames + competitionsColumns + ")" + System.lineSeparator() +
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
            String stampsCleaner = "DELETE FROM " + tables.events + System.lineSeparator() +
                    "WHERE " + columns.eventChainID + " in " +
                    ("SELECT " + columns.chainID + " from " + tables.eventFrames + System.lineSeparator() +
                            "where " + columns.gameNumber + " = " + game + System.lineSeparator() + "" + System.lineSeparator() +
                            "AND " + columns.competitionNumber + " = " + competitionNameToID(competition) +
                            " AND " + columns.teamNumber + " = " + team) + ");";

            String framesCleaner = "DELETE FROM " + tables.eventFrames + System.lineSeparator() +
                    "where " + columns.gameNumber + " = " + game + System.lineSeparator() +
                    "AND " + columns.competitionNumber + " = " + competitionNameToID(competition) + System.lineSeparator() +
                    "AND " + columns.teamNumber + " = " + team + System.lineSeparator() +
                    "AND " + columns.chainID + " NOT IN " + idList + ";";

            statement.execute(stampsCleaner + " " + framesCleaner);
            String framesFixer = "INSERT OR REPLACE INTO " + tables.eventFrames + "(" + columns.chainID + "," + columns.alliance + "," + columns.gameNumber + "," + columns.competitionNumber + "," + columns.teamNumber + "," + columns.startingLocation + ")" + System.lineSeparator() +
                    "VALUES"; // continues in the for-loop
            String stampsFixer = "INSERT INTO " + tables.events + "(" + columns.eventChainID + "," + columns.eventLocationInChain + "," + columns.eventType + "," + columns.timeStamps + ")" + System.lineSeparator() +
                    "values"; // continues in the for-loop
            statement.execute("SELECT " + columns.chainID + " FROM " + tables.eventFrames + " ORDER BY " + columns.chainID + " DESC LIMIT 1;");
            ResultSet set = statement.getResultSet();
            set.next();
            Integer lastID = set.getInt(columns.chainID.toString());
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
            String gameCreate = "INSERT INTO " + tables.games + "(" + columns.gameNumbers + "," + columns.competition + "," + columns.mapConfiguration + "," + columns.teamNumber1 + "," + columns.teamNumber2 + "," + columns.teamNumber3 + "," + columns.teamNumber4 + "," + columns.teamNumber5 + "," + columns.teamNumber6 + ") values(" + gameNumber + "," + "(Select " + columns.competitionID + " from " + tables.competitions + " where " + columns.competitionName + " = '" + competition + "')" + "," + mapConfiguration;
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



    private String formatEventsSelect(String conditions){
        return null;
    }

    protected ArrayList<FullScoutingEvent> getEventsByGame() {
        return null;
    }

    protected ArrayList<FullScoutingEvent> getAllTeamEventsByGame() {
        return null;
    }

    protected ArrayList<FullScoutingEvent> getTeamAllianceEvents() {
        return null;
    }

    protected ArrayList<FullScoutingEvent> getTeamAliienceEventsByGame() {
        return null;
    }

    protected ArrayList<FullScoutingEvent> getAllTeamEventsOfType() {
        return null;
    }

    protected ArrayList<FullScoutingEvent> getAllTeamEventsOfTypeByGame() {
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
        while(set.next()){
            result.add(new ScoutedGame(
                    set.getShort(columns.gameNumbers.toString()),
                    set.getByte(columns.competition.toString()),
                    set.getShort(columns.redAllianceScore.toString()),
                    set.getShort(columns.blueAllianceScore.toString()),
                    set.getByte(columns.redAllianceRP.toString()),
                    set.getByte(columns.blueAllianceRP.toString()),
                    set.getString(columns.mapConfiguration.toString()),
                    set.getShort(columns.teamNumber1.toString()),
                    set.getShort(columns.teamNumber2.toString()),
                    set.getShort(columns.teamNumber3.toString()),
                    set.getShort(columns.teamNumber4.toString()),
                    set.getShort(columns.teamNumber5.toString()),
                    set.getShort(columns.teamNumber6.toString()))
            );
        }
        return result;
    }

    private ArrayList<ScoutedTeam> convertResultSetToTeams(ResultSet set) throws SQLException {
        ArrayList<ScoutedTeam> result = new ArrayList<>();
        while(set.next()){
            result.add(new ScoutedTeam(
                    set.getShort(columns.teamNumbers.toString()),
                    set.getString(columns.teamNames.toString()))
            );
        }
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