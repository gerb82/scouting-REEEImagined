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
            database.close();
        } catch (SQLException e) {
            throw new Error("Could not close the database");
        }
    }

    private Connection database;

    private enum columns{
        eventTypeID, eventName, // event types
        containerEventType, eventContainedType, // containers table
        teamNumbers, teamNames, participatedIn, // teams table
        gameNumbers, mapConfiguration, competition, teamNumber1, teamNumber2, teamNumber3, teamNumber4, teamNumber5, teamNumber6, // games table
        competitionID, competitionName, // competitions
        eventChainID, eventLocationInChain, eventType, timeStamps, // stamps table
        chainID, gameNumber, competitionNumber, teamNumber, alliance, startingLocation // main events table
    }

    private enum tables{
        eventTypes, containableEvents, teamNumbers, events, games, competitions, eventFrames
    }

    public DataBaseManager(){
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
                    columns.eventName + " text NOT NULL UNIQUE);");

            // competitions
            statement.execute("CREATE TABLE IF NOT EXISTS " + tables.competitions + "(" + System.lineSeparator() +
                    columns.competitionID + " integer primary key AUTO INCREMENT," + System.lineSeparator() +
                    columns.competitionName + " text NOT NULL);");

            // containableEventsList
            statement.execute("CREATE TABLE IF NOT EXISTS "+ tables.containableEvents + "(" + System.lineSeparator() +
                    columns.containerEventType + " integer," + System.lineSeparator() +
                    columns.eventContainedType + " integer," + System.lineSeparator() +
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
                    columns.mapConfiguration + " text NOT NULL," + System.lineSeparator() +
                    columns.competition + " integer NOT NULL," + System.lineSeparator() +
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
                    columns.eventChainID + " integer ," + System.lineSeparator() +
                    columns.eventLocationInChain + " integer," + System.lineSeparator() +
                    columns.eventType + " integer NOT NULL," + System.lineSeparator() +
                    columns.timeStamps + " integer," + System.lineSeparator() +
                    "PRIMARY KEY(" + columns.eventChainID + "," + columns.eventLocationInChain + ")," + System.lineSeparator() +
                    "FOREIGN KEY(" + columns.eventType + ") REFERENCES " + tables.eventTypes + "(" + columns.eventTypeID + ")," + System.lineSeparator() +
                    "FOREIGN KEY(" + columns.eventChainID + ") REFERENCES " + tables.eventFrames + "(" + columns.chainID + "));");
            database.commit();
            statement.close();
        } catch (SQLException e) {
            try {
                if (database != null){
                    database.rollback();
                    throw new Error("Failed to initialize the database!");
                } else {
                    throw new Error("Could not connect to the database!");
                }
            } catch (SQLException e1){
                throw new Error("Failed to initialize the database!");
            }
        }
    }

    public ArrayList<FullScoutingEvent> getAllEventsForGameForTeam(short game, short team){
        return null;
    }

    private boolean configValidator(){
        return true;
    }

    protected void addNewCompetition(String name, ScoutedTeam[] teams){
        try(Statement statement = database.createStatement()) {
            statement.execute("INSERT INTO " + tables.competitions + "(" + columns.competitionName + ") VALUES(" + name + ");");
            statement.execute("ALTER TABLE " + tables.teamNumbers + " ADD COLUMN " + columns.participatedIn + name + " number NOT NULL DEFAULT 0;");
            String insertTeams = "INSERT OR IGNORE INTO " + tables.teamNumbers + "(" + columns.teamNumbers + "," + columns.teamNames + "," + columns.participatedIn + name + ") VALUES";
            String signUpTeams = "UPDATE " + tables.teamNumbers + " SET " + columns.participatedIn + name + " = 1 WHERE ";
            boolean first = true;
            for (ScoutedTeam team : teams) {
                insertTeams += (!first ? "," : "") + "(" + team.getNumber() + ",'" + team.getName() + "'" + "," + "1)";
                signUpTeams += (!first ? " OR " : "") + columns.teamNumbers  + " = " + team.getNumber();
                first = false;
            }
            statement.execute(insertTeams + ";");
            statement.execute(signUpTeams + ";");
            database.commit();
        } catch (SQLException e){
            try {
                database.rollback();
            } catch (SQLException e1) {
                throw new Error("Could not roll back database changes!");
            }
            throw new IllegalArgumentException("The transaction could not be completed.", e);
        }
    }

    protected void addTeam(ScoutedTeam team, String[] competitions){
        try(Statement statement = database.createStatement()){
            String competitionsColumns = "";
            for(String competition : competitions){
                competitionsColumns += "," + columns.participatedIn + competition;
            }
            statement.execute("INSERT INTO " + tables.teamNumbers + "(" + columns.teamNumbers + "," + columns.teamNames + competitionsColumns + ") VALUES(" + team.getNumber() + ",'" + team.getName() + "'" + String.join("",Collections.nCopies(competitionsColumns.length(), ",1")) + ");");
            database.commit();
        } catch (SQLException e){
            try {
                database.rollback();
            } catch (SQLException e1) {
                throw new Error("Could not roll back database changes!");
            }
            throw new IllegalArgumentException("The transaction could not be completed.", e);
        }
    }

    protected void updateEvents(int game, String competition, FullScoutingEvent[] events){
        try(Statement statement = database.createStatement()){
            Set<Integer> numberedEvents = new HashSet<>();
            boolean first = true;
            String idList = "(";
            for(FullScoutingEvent event : events){
                if(event.getEvent().getChainID() != -1){
                    if(numberedEvents.contains(event.getEvent().getChainID())){
                        throw new IllegalArgumentException("A duplicate event was detected!");
                    } else {
                        numberedEvents.add(event.getEvent().getChainID());
                        idList += (!first ? "," : "") + event.getEvent().getChainID();
                    }
                }
            }
            // delete all relevant stamps. delete all event frames that aren't in the new list. insert or replace with new frames. add the new stamps. newest chain id is loaded from the database by getting the highest 1 chain id right now and then assigning them in an increasing size to the frames.
            String framesFixer = "DELETE FROM " + tables.events + " WHERE " + columns.eventChainID + " in " + ("SELECT " + columns.chainID + " from " + tables.eventFrames + " where " + columns.gameNumber + " = " + game + " AND " + columns.competitionNumber + " = " + ("SELECT " + columns.competitionID + " from " + tables.competitions + " where " + columns.competitionName + " = " + competition)) + ")";
            String framesInserter = "INSERT INTO " + tables.eventFrames + "(" + columns.chainID + "," + columns.gameNumber + "," + columns.competitionNumber + "," + columns.teamNumber + "," + columns.startingLocation + ") values";
            String stampsFixer = "DELETE FROM " + tables.events + " WHERE ";
            String stampsInserter = "INSERT INTO " + tables.events + "(" + columns.eventChainID + "," + columns.eventLocationInChain + "," + columns.eventType + "," + columns.timeStamps + ") values";
            boolean first = true;
            for(FullScoutingEvent event : events){
                framesInserter += (!first ? "," : "") + "(" + event.getEvent().getChainID() + "," + event.getGame() + "," + event.getCompetition() + "," + event.getTeam() + "," + event.getAlliance() + "," + event.getStartingLocation() + ")";
                stampsFixer += (!first ? " OR " : "") + columns.eventChainID + " = " + event.getEvent().getChainID();
                int location = 0;
                for(ScoutingEvent.EventTimeStamp stamp : event.getEvent().getStamps()){
                    stampsInserter += (!first ? "," : "") + "(" + event.getEvent().getChainID() + "," + location++ + "," + event.getEvent().getType() + "," + (stamp.getTimeStamp() == null ? "NULL" : stamp.getTimeStamp()) + ")";
                    first = false;
                }
            }
            statement.execute(framesInserter + "; " + stampsFixer + "; " + stampsInserter + ";");
            database.commit();
        } catch (SQLException e){
            try {
                database.rollback();
            } catch (SQLException e1) {
                throw new Error("Could not roll back database changes!");
            }
            throw new IllegalArgumentException("The transaction could not be completed.", e);
        }
    }

    protected void addGame(int gameNumber, String competition, String mapConfiguration, Integer[] teams){
        try (Statement statement = database.createStatement()){
            assert (teams.length == 6);
            ArrayList<Integer> testList = new ArrayList<>();
            for(Integer teamNum : teams){
                if(!testList.contains(teamNum) || teamNum == null){
                    testList.add(teamNum);
                } else {
                    throw new IllegalArgumentException("The same team was supplied twice for the given new game!");
                }
            }
            String gameCreate = "INSERT INTO " + tables.games + "(" + columns.gameNumbers + "," + columns.competition + "," + columns.mapConfiguration + "," + columns.teamNumber1 + "," + columns.teamNumber2 + "," + columns.teamNumber3 + "," + columns.teamNumber4 + "," + columns.teamNumber5 + "," + columns.teamNumber6 + ") values(" + gameNumber + "," + "(Select " + columns.competitionID + " from " + tables.competitions + " where " + columns.competitionName + " = '" + competition + "')" + "," + mapConfiguration;
            for(Integer teamNum : teams){
                gameCreate += "," + teamNum;
            }
            statement.execute(gameCreate + ";");
            database.commit();
        } catch (AssertionError e){
            throw new IllegalArgumentException("The teams array was not of the right size!");
        } catch (SQLException e) {
            try {
                database.rollback();
            } catch (SQLException e1) {
                throw new Error("Could not roll back database changes!");
            }
            throw new IllegalArgumentException("The transaction could not be completed.", e);
        }
    }

    protected void






























    public class ScoutedTeam{

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