package utilities.GBDB;

import utilities.GBLoader.GBDYouIdiotException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GBDB {

    Connection connection = null;

    public boolean Connect(String pathToDatabase){
        try {
            String url = "jdbc:sqlite:" + pathToDatabase;
            connection = DriverManager.getConnection(url);
        } catch (SQLException e){

        }
    }

    public String checkProperty(String property){
        if (property != null){
            return " " + property;
        }
        return "";
    }

    public ResultSet selectDistinct(String columns, String tables, String join, String filter, String sortBy, String limit, String groupBy, String having){
        return select("DISTINCT " + columns, tables, join, filter, sortBy, limit, groupBy, having);
    }

    public ResultSet select(String columns, String tables, String join, String filter, String sortBy, String limit, String groupBy, String having){
        String request = "SELECT " + columns + " FROM " + tables;
        request += checkProperty(join) + checkProperty(filter) + checkProperty(sortBy) + checkProperty(limit) + checkProperty(groupBy) + checkProperty(having);
        try {
            return connection.createStatement().executeQuery(request);
        } catch (SQLException e) {
            throw new GBDYouIdiotException("invalid sql request" + e.getCause().toString() + "\n" + request + "\n" + e.getStackTrace());
        }
    }
}
