package serverSide.code;

import connectionIndependent.FullScoutingEvent;
import connectionIndependent.ScoutingEvent;
import gbuiLib.GBUILibGlobals;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

public class DataBaseManager implements Closeable {

    private Connection database;

    public DataBaseManager(){
        File databaseDirectory = GBUILibGlobals.getDataBaseDirectory();
        databaseDirectory.mkdirs();
        File databaseFile = new File(databaseDirectory, "database.sqlite");
        try {
            databaseFile.createNewFile();
        } catch (IOException e) {
            throw new Error("Database did not exist and could not be created!");
        }
        try {
            database = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath().toString());
        } catch (SQLException e) {
            throw new Error("Could not connect to the database!");
        }
    }

    public ArrayList<FullScoutingEvent> getAllEventsForGameForTeam(short game, short team){
        return null;
    }

    public void setAllEventsForGameForTream(ArrayList<ScoutingEvent> events, short game, short team){

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