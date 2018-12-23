//package utilities.GBDB;
//
//import gbuiLib.AssertionYouDimwitException;
//import gbuiLib.SmartAssert;
//import gbuiLib.YouDimwitException;
//
//import java.sql.*;
//import java.util.ArrayList;
//
//public class GBDB {
//
//    private Connection connection = null;
//    private boolean connected = false;
//
//    public void connect(String pathToDatabase){
//        try {
//            String url = "jdbc:sqlite:" + pathToDatabase;
//            connection = DriverManager.getConnection(url);
//            connected = true;
//        } catch (SQLException e){
//            throw new YouDimwitException("could not connect to the database");
//        }
//    }
//
//    public boolean isConnected(){
//        if(connected) {
//            try {
//                if (!connection.isClosed()) {
//                    return true;
//                }
//                connected = false;
//                return false;
//            } catch (SQLException e) {
//                throw new YouDimwitException("isConnected just threw an sqlException. i'm confused");
//            }
//        }
//        return false;
//    }
//
//    public ResultSet select(String statement){
//        if(isConnected()){
//            try (ResultSet result = connection.createStatement().executeQuery(statement)){
//                return result;
//            } catch (SQLException e) {
//                throw new YouDimwitException("invalid sql select statement " + statement);
//            }
//        }
//        return null;
//    }
//
//    public static String[] groupStrings(String... values){
//        return values;
//    }
//
//    public static String joinsLogic(String... values){
//
//    }
//
//    public static String formatSelect(String type, String[]... arguments){
//        SmartAssert.makeSure(arguments[0] != null, type + " requires at least 1 column to work with");
//        SmartAssert.makeSure(arguments[0].length > 0, type + " requires at least 1 column to work with");
//        SmartAssert.makeSure(arguments[1] != null, type + " requires at least 1 table to work with");
//        SmartAssert.makeSure(arguments[1].length == 1, type + " requires at least 1 table to work with");
//        if(type.contains("SELECT")) {
//            SmartAssert.makeSure(arguments.length == 8, type + " requires exactly 8 lists of arguments, with all the lists after the 3rd being optional (put null instead to avoid using them). the lists are: columns list, table (only 1), join (and how)(optional), where (optional), order by (optional)(if left out will sort by id), limit (first cell is the limit, 2nd cell is the offset, to only use a number limit use a length 1 array), group by (optional), having (optional)");
//        }
//        String joins = " ";
//        if(arguments[2] != null){
//            SmartAssert.makeSure(arguments[2].length > 0, type + " requires at least 1 join condition when the join argument is not null");
//            joins += makeIntoOne(false, arguments[2]);
//        }
//        String where = " ";
//        if(arguments[3] != null){
//            SmartAssert.makeSure(arguments[3].length > 0, type + " requires at least 1 where filter when the where filter argument is not null");
//            where += makeIntoOne(false, arguments[3]);
//        }
//        String order = " ";
//        if(arguments[4] != null){
//            SmartAssert.makeSure(arguments[4].length > 0, type + " requires at least 1 order by statement when the order by argument is not null");
//            order += makeIntoOne(false, arguments[4]);
//        }
//        String limit = " ";
//        if(arguments[5] != null){
//            SmartAssert.makeSure(arguments[5].length == 1 || arguments[5].length == 2, type + " requires either 1 or 2 limit arguments when the limit argument is not null");
//            limit += "LIMIT " + arguments[5][0];
//            if(arguments[5].length == 2){
//                limit += " OFFSET " + arguments[5][1];
//            }
//        }
//        String group = " ";
//        if(arguments[6] != null){
//            SmartAssert.makeSure(arguments[6].length > 0, type + " requires at least 1 column to group by when the group by argument is not null");
//            group += "GROUP BY " + makeIntoOne(true, arguments[6]);
//        }
//        String having = " ";
//        if(arguments[7] != null){
//            SmartAssert.makeSure(arguments[7].length > 0, type + " requires at least 1 having statement when the having argument is not null");
//            having += makeIntoOne(false, arguments[7]);
//        }
//        return " " + makeIntoOne(true, arguments[0]) + " FROM " + arguments[1][0] + joins + where + order + limit + group;
//    }
//
//    public static String newStatement(StatementType type, String[]... arguments){
//        StringBuilder output = new StringBuilder("");
//        switch (type) {
//            case SELECT:
//                output.append("SELECT" + formatSelect("SELECT", arguments));
//                break;
//            case SELECT_DISTINCT:
//                output.append("SELECT DISTINCT" + formatSelect("SELECT_DISTINCT", arguments));
//            case INSERT:
//                SmartAssert.makeSure(arguments[0] != null, "INSERT requires at least one table to work with");
//                SmartAssert.makeSure(arguments[0].length > 0, "INSERT requires at least one table to work with");
//                SmartAssert.makeSure(arguments[2] != null, "INSERT requires at least one set of values to work with");
//                SmartAssert.makeSure(arguments[2].length > 0, "INSERT requires at least one set of values to work with");
//                if(arguments[1] == null){
//                    SmartAssert.makeSure(arguments[3] != null, "INSERT with the list of columns to insert into dropped requires the amount of columns in the original tables");
//                    SmartAssert.makeSure(arguments[3].length == 1, "INSERT with the list of columns to insert into dropped requires the amount of columns in the original tables");
//                    SmartAssert.makeSure(arguments[2].length%Integer.valueOf(arguments[3][0]) == 0, "INSERT with the list of columns to insert into dropped requires the amount of values to be divide-able by the amount of columns in the original table");
//                    SmartAssert.makeSure(arguments.length == 4, "INSERT with the list of columns to insert into dropped requires exactly 4 lists of arguments, one of which is of length 1: tables, columns(NULL), values, and the amount of columns in the original tables as a list of length 1");
//                }
//                else{
//                    SmartAssert.makeSure(arguments[1] != null, "INSERT with a list of columns to insert into requires at least one such column");
//                    SmartAssert.makeSure(arguments[1].length > 0, "INSERT with a list of columns to insert into requires at least one such column");
//                    SmartAssert.makeSure(arguments[2].length%arguments[1].length == 0, "INSERT with a list of columns to insert into requires the amount of values to be divide-able by the amount of columns");
//                    SmartAssert.makeSure(arguments.length == 3, "INSERT with a list of columns to insert into requires exactly 3 lists of arguments: tables, columns to insert into, and values");
//                }
//                StringBuilder values = new StringBuilder("");
//                for(int i = 0; i<arguments[2].length; i += arguments[1].length){
//                    ArrayList<String> temp = new ArrayList<>();
//                    for(int j = 0; j<arguments[1].length; j++){
//                        temp.add(arguments[2][i+j]);
//                    }
//                    String string = makeIntoOne(true, (String[])temp.toArray());
//                    values.append(" ( " + string + " )");
//                }
//                for(String table : arguments[0]) {
//                    output.append("INSERT INTO " + table + " (" + makeIntoOne(true, arguments[1]) + " ) VALUES" + values.toString());
//                }
//                break;
//            case INSERT_DEFAULT:
//                SmartAssert.makeSure(arguments[0] != null, "INSERT_DEFAULT requires at least one table to work with");
//                SmartAssert.makeSure(arguments[0].length > 0, "INSERT_DEFAULT requires at least one table to work with");
//                SmartAssert.makeSure(arguments.length == 1, "INSERT_DEFAULT accepts exactly 1 list of arguments: tables");
//                for(String string : arguments[0]){
//                    output.append("INSERT INTO " + string + " DEFAULT VALUES;");
//                }
//            case INSERT_SELECT:
//                SmartAssert.makeSure(arguments[0] != null, "INSERT_SELECT requires at least one table to work with");
//                SmartAssert.makeSure(arguments[0].length > 0, "INSERT_SELECT requires at least one table to work with");
//                SmartAssert.makeSure(arguments[1] != null, "INSERT_SELECT requires exactly one select statement to work with");
//                SmartAssert.makeSure(arguments[1].length == 1, "INSERT_SELECT requires exactly one select statement to work with");
//                SmartAssert.makeSure(arguments.length == 2, "INSERT_SELECT accepts exactly 1 list of arguments and one single argument: tables, and a select statement");
//                for(String string : arguments[0]){
//                    output.append("INSERT INTO " + string + " " + arguments[1][0]);
//                }
//                break;
//            case DELETE:
//                SmartAssert.makeSure(arguments.length == 4, "DELETE accepts exactly 4 lists of arguments, three of which are optional and can be set to null: table (exactly one), where (optional), order by (optional), limit (optional)(length of one for just a line limit, and of two for a length with an offset)");
//                output.append("DELETE" + formatSelect("DELETE",new String[]{""}, arguments[0], null, arguments[1], arguments[2], arguments[3], null, null));
//                break;
//            case REPLACE:
//                output.append("REPLACE");
//                break;
//            case UPDATE:
//                output.append("UPDATE");
//                break;
//            case CREATE:
//                output.append("CREATE");
//                break;
//            case ALTER_ADD_COLUMN:
//                SmartAssert.makeSure(arguments[0] != null, "ALTER_ADD_COLUMN requires at least one table to work with");
//                SmartAssert.makeSure(arguments[1] != null, "ALTER_ADD_COLUMN requires at least one column to add");
//                SmartAssert.makeSure(arguments[0].length > 0, "ALTER_ADD_COLUMN requires at least one table to work with");
//                SmartAssert.makeSure(arguments[1].length > 0, "ALTER_ADD_COLUMN requires at least one column to add");
//                SmartAssert.makeSure(arguments.length == 2, "ALTER_ADD_COLUMN accepts exactly 2 lists of arguments: tables, and columns to add");
//                String columns = makeIntoOne(true, arguments[1]);
//                for (String table : arguments[0]) {
//                    output.append("ALTER TABLE " + table + " ADD COLUMN " + columns);
//                }
//                break;
//            case ALTER_NAME:
//                SmartAssert.makeSure(arguments[0] != null, "ALTER_NAME requires exactly one table in order to work");
//                SmartAssert.makeSure(arguments[0].length == 1, "ALTER_NAME requires exactly one table in order to work");
//                SmartAssert.makeSure(arguments[1] != null, "ALTER_NAME requires exactly one new table name in order to work");
//                SmartAssert.makeSure(arguments[1].length == 1, "ALTER_NAME requires exactly one new table name in order to work");
//                SmartAssert.makeSure(arguments.length == 2, "ALTER_NAME accepts exactly 2 lists of length 1 of arguments: table, and new name for that table");
//                output.append("ALTER TABLE " + arguments[0][0] + " RENAME TO " + arguments[1][0]);
//                break;
//
//
//            case CREATE_TEMP_VIEW:
//                SmartAssert.makeSure(arguments[0] != null, "CREATE_TEMP_VIEW requires exactly one name in order to work");
//                SmartAssert.makeSure(arguments[0].length == 1, "CREATE_TEMP_VIEW requires exactly one name in order to work");
//                SmartAssert.makeSure(arguments[1] != null, "CREATE_TEMP_VIEW requires exactly one select statement in order to work");
//                SmartAssert.makeSure(arguments[1].length == 1, "CREATE_TEMP_VIEW requires exactly one select statement in order to work");
//                SmartAssert.makeSure(arguments.length == 2, "CREATE_TEMP_VIEW accepts exactly 2 lists of length 1 of arguments: name, and the select statement it is based on");
//                output.append("CREATE TEMP VIEW IF NOT EXISTS " + arguments[0][0] + " AS " + arguments[1][0]);
//                break;
//            case CREATE_VIEW:
//                SmartAssert.makeSure(arguments[0] != null, "CREATE_VIEW requires exactly one name in order to work");
//                SmartAssert.makeSure(arguments[0].length == 1, "CREATE_VIEW requires exactly one name in order to work");
//                SmartAssert.makeSure(arguments[1] != null, "CREATE_VIEW requires exactly one select statement in order to work");
//                SmartAssert.makeSure(arguments[1].length == 1, "CREATE_VIEW requires exactly one select statement in order to work");
//                SmartAssert.makeSure(arguments.length == 2, "CREATE_VIEW accepts exactly 2 lists of length 1 of arguments: name, and the select statement it is based on");
//                output.append("CREATE VIEW IF NOT EXISTS " + arguments[0][0] + " AS " + arguments[1][0]);
//                break;
//            case DROP_VIEW:
//                SmartAssert.makeSure(arguments[0] != null, "DROP_VIEW requires exactly one name in order to work");
//                SmartAssert.makeSure(arguments[0].length == 1, "DROP_VIEW requires exactly one name in order to work");
//                SmartAssert.makeSure(arguments.length == 1, "DROP_VIEW requires exactly one list of length 1 of arguments: name");
//                output.append("DROP VIEW IF EXISTS " + arguments[0][0]);
//                break;
//        }
//        return output.toString();
//    }
//
//    public static String union(String... statements){
//        StringBuilder output = new StringBuilder("");
//        if(statements.length > 0) {
//            for (String string : statements) {
//                if (output.toString() == "") {
//                    output.append(string);
//                } else {
//                    output.append(" UNION " + string);
//                }
//            }
//        }
//        return output.toString();
//
//    }
//
//    public static String unionAll(String... statements){
//        StringBuilder output = new StringBuilder("");
//        if(statements.length > 0) {
//            for (String string : statements) {
//                if (output.toString() == "") {
//                    output.append(string);
//                } else {
//                    output.append(" UNION ALL " + string);
//                }
//            }
//        }
//        return output.toString();
//    }
//
//    private static String makeIntoOne(boolean periods, String... strings){
//        StringBuilder output = new StringBuilder("");
//        if(strings.length > 0) {
//            for (String string : strings) {
//                if (output.toString() == "") {
//                    output.append(string);
//                } else {
//                    if(periods) {
//                        output.append(", " + string);
//                    }
//                    else{
//                        output.append(" " + strings);
//                    }
//                }
//            }
//        }
//        return output.toString();
//    }
//
//    public enum StatementType{
//        SELECT, SELECT_DISTINCT,
//        INSERT, INSERT_DEFAULT, INSERT_SELECT,
//        DELETE,
//        REPLACE,
//        UPDATE,
//        CREATE,
//        ALTER_NAME, ALTER_ADD_COLUMN,
//
//        CREATE_VIEW, CREATE_TEMP_VIEW,
//        DROP_VIEW
//    }
//}
