package serverSide.code;

import connectionIndependent.ScoutingConnections;
import gbuiLib.GBSockets.GBServerSocket;
import gbuiLib.GBSockets.PacketLogger;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;

public class ServerManager {

    private ScoutersManager scouters;
    private DataBaseManager database;

    protected ServerManager(){
        database = new DataBaseManager();
        scouters = new ScoutersManager(database);
        PacketLogger.setDirectory(ScoutingVars.getMainDirectory());
        GBServerSocket socket = new GBServerSocket(4590, null, null, false, "LocalNetwork");
        socket.addConnectionType(ScoutingConnections.SCOUTER.toString(), scouters.getHandler());
        socket.initSelector();
    }

    private void addGame(String competition, short game, Short[] teams, String mapConfig){
        database.addGame(game, competition, mapConfig, teams);
        scouters.addGame(competition, game, teams);
    }

    private void addCompetition(String competition){
        database.addNewCompetition(competition);
        scouters.addCompetition(competition);
    }

    @FXML
    private void pickCurrentComp(Event event){
        scouters.pickCurrentCompetition(((MenuItem)event.getTarget()).getText());
    }
}
