package serverSide.code;

import connectionIndependent.FullScoutingEvent;
import connectionIndependent.ScoutingEvent;
import gbuiLib.GBUILibGlobals;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DataBaseManager implements Closeable {

    private Connection database;

    private enum columns{
        eventTypeID, eventName, // event types
        containerEventType, eventContainedType, // containers table
        teamNumbers, teamNames, // teams table
        gameNumbers, mapConfiguration, competition, teamNumber1, teamNumber2, teamNumber3, teamNumber4, teamNumber5, teamNumber6, // games table
        competitionID, competitionName, // competitions
        eventAbsoluteID, eventChainID, eventLocationInChain, eventType, timeStamps, // stamps table
        chainID, gameNumber, competitionNumber, teamNumber, startingLocation // main events table
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
            if(!databaseFile.exists()) {
                Statement statement = database.createStatement();

                // eventDefinitionsList
                statement.execute("CREATE TABLE " + tables.eventTypes + "(" +
                        columns.eventTypeID + " integer NOT NULL PRIMARY KEY," + System.lineSeparator() +
                        columns.eventName + " text NOT NULL UNIQUE);");

                // competitions
                statement.execute("CREATE TABLE " + tables.competitions + "(" + System.lineSeparator() +
                        columns.competitionID + " integer NOT NULL PRIMARY KEY," + System.lineSeparator() +
                        columns.competitionName + " text NOT NULL);");

                // containableEventsList
                statement.execute("CREATE TABLE "+ tables.containableEvents + "(" + System.lineSeparator() +
                        columns.containerEventType + " integer NOT NULL," + System.lineSeparator() +
                        columns.eventContainedType + " integer NOT NULL," + System.lineSeparator() +
                        "FOREIGN KEY(" + columns.containerEventType + ") REFERENCES " + tables.eventTypes + "(" + columns.eventTypeID + ")," + System.lineSeparator() +
                        "FOREIGN KEY(" + columns.eventContainedType + ") REFERENCES " + tables.eventTypes + "(" + columns.eventTypeID + "));");

                // teamsList
                statement.execute("CREATE TABLE " + tables.teamNumbers + "(" + System.lineSeparator() +
                        columns.teamNumbers + " integer NOT NULL PRIMARY KEY," + System.lineSeparator() +
                        columns.teamNames + " text NOT NULL UNIQUE);");

                // gamesList
                statement.execute("CREATE TABLE " + tables.games + "(" + System.lineSeparator() +
                        columns.gameNumbers + " integer NOT NULL," + System.lineSeparator() +
                        columns.mapConfiguration + " text NOT NULL," + System.lineSeparator() +
                        columns.competition + " integer NOT NULL," + System.lineSeparator() +
                        columns.teamNumber1 + " integer NOT NULL," + System.lineSeparator() +
                        columns.teamNumber2 + " integer NOT NULL," + System.lineSeparator() +
                        columns.teamNumber3 + " integer NOT NULL," + System.lineSeparator() +
                        columns.teamNumber4 + " integer NOT NULL," + System.lineSeparator() +
                        columns.teamNumber5 + " integer NOT NULL," + System.lineSeparator() +
                        columns.teamNumber6 + " integer NOT NULL," + System.lineSeparator() +
                        "PRIMARY KEY(" + columns.gameNumbers + "," + columns.competition + ")," + System.lineSeparator() +
                        "FOREIGN KEY(" + columns.competition + ") REFERNCES " + tables.competitions + "(" + columns.competitionID + ")," + System.lineSeparator() +
                        "FOREIGN KEY(" + columns.teamNumber1 + ") REFERNCES " + tables.teamNumbers + "(" + columns.teamNumbers + ")," + System.lineSeparator() +
                        "FOREIGN KEY(" + columns.teamNumber2 + ") REFERNCES " + tables.teamNumbers + "(" + columns.teamNumbers + ")," + System.lineSeparator() +
                        "FOREIGN KEY(" + columns.teamNumber3 + ") REFERNCES " + tables.teamNumbers + "(" + columns.teamNumbers + ")," + System.lineSeparator() +
                        "FOREIGN KEY(" + columns.teamNumber4 + ") REFERNCES " + tables.teamNumbers + "(" + columns.teamNumbers + ")," + System.lineSeparator() +
                        "FOREIGN KEY(" + columns.teamNumber5 + ") REFERNCES " + tables.teamNumbers + "(" + columns.teamNumbers + ")," + System.lineSeparator() +
                        "FOREIGN KEY(" + columns.teamNumber6 + ") REFERNCES " + tables.teamNumbers + "(" + columns.teamNumbers + "));");

                // eventFrames
                statement.execute("CREATE TABLE " + tables.eventFrames + "(" + System.lineSeparator() +
                        columns.chainID + " integer NOT NULL PRIMARY KEY," + System.lineSeparator() +
                        columns.gameNumber + " integer NOT NULL," + System.lineSeparator() +
                        columns.competitionNumber + " integer NOT NULL," + System.lineSeparator() +
                        columns.teamNumber + " integer NOT NULL," + System.lineSeparator() +
                        columns.startingLocation + " integer NOT NULL," + System.lineSeparator() +
                        "FOREIGN KEY(" + columns.gameNumber + "," + columns.competitionNumber + ") REFERENCES " + tables.games + "(" + columns.gameNumbers + "," + columns.competition + ")," + System.lineSeparator() +
                        "FOREIGN KEY(" + columns.teamNumber + ") REFERENCES " + tables.teamNumbers + "(" + columns.teamNumbers + "));");

                // eventsList
                statement.execute("CREATE TABLE " + tables.events + "(" + System.lineSeparator() +
                        columns.eventAbsoluteID + " integer NOT NULL PRIMARY KEY," + System.lineSeparator() +
                        columns.eventChainID + " integer NOT NULL," + System.lineSeparator() +
                        columns.eventLocationInChain + " integer NOT NULL," + System.lineSeparator() +
                        columns.eventType + " integer NOT NULL," + System.lineSeparator() +
                        columns.timeStamps + " integer," + System.lineSeparator() +
                        "FOREIGN KEY(" + columns.eventType + ") REFERENCES " + tables.eventTypes + "(" + columns.eventTypeID + ")," + System.lineSeparator() +
                        "FOREIGN KEY(" + columns.eventChainID + ") REFERENCES " + tables.eventFrames + "(" + columns.chainID + "));");

            }
        } catch (SQLException e) {
            throw new Error("Could not create or connect to the database!");
        }
    }

    public ArrayList<FullScoutingEvent> getAllEventsForGameForTeam(short game, short team){
        return null;
    }

    public void setAllEventsForGameForTeam(ArrayList<ScoutingEvent> events, short game, short team){
        for(ScoutingEvent event : events){
            if(event.getUniqueID() != -1){

            }
        }
    }

    @Override
    public void close() {
        try {
            database.close();
        } catch (SQLException e) {
            throw new Error("Could not close the database");
        }
    }
}