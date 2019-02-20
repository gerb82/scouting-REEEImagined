package scouterSide;

import connectionIndependent.ConnectionFinder;
import connectionIndependent.ScoutingConnections;
import connectionIndependent.ScoutingPackets;
import connectionIndependent.scouted.ScoutingEvent;
import gbuiLib.GBSockets.*;
import gbuiLib.ProgramWideVariable;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.InetSocketAddress;
import java.util.ArrayList;

public class MainLogic extends Application {

    private GBSocket socket;
    private static MainLogic self;
    protected String host;
    private Parent root;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainScreen.fxml"));
        root = loader.load();
        primaryStage.setScene(new Scene(root));
        ScouterUI controller = loader.getController();
        controller.setMain(this);
        controller.setRoot(root);
        controller.setActivePane(false);
        primaryStage.show();
//        primaryStage.setFullScreen(true);

        self = this;
        ProgramWideVariable.initializeDefaults();
        PacketLogger.setDirectory();
        ActionHandler handler = new ActionHandler();
        handler.setHandler(ScoutingPackets.SCOUTER_LOADGAME.toString(), controller::loadNewView);
        handler.setHandler(ScoutingPackets.SCOUTER_SYNC_COMPS.toString(), controller::competitions);
        handler.setHandler(ScoutingPackets.SCOUTER_SYNC_GAMES.toString(), controller::games);
        handler.setHandler(ScoutingPackets.SCOUTER_SYNC_TEAMS.toString(), controller::teams);
        handler.setHandler(ScoutingPackets.SCOUTER_SUBMITGAME.toString(), controller::scoutOver);
        SelectorManager selector = new SelectorManager();
        socket = new GBSocket(addressSetter(), ScoutingConnections.SCOUTER.toString(), true, selector, handler, new GBSocket.SocketConfig(), false);
        socket.isConnected.addListener((observable, oldValue, newValue) -> {
            System.out.println(newValue);
            while (!observable.getValue()) {
                try {
//                root.setDisable(true);
                    socket.setAddress(addressSetter());
                } catch (IllegalAccessException e) {
                    root.setDisable(false);
                    return;
                }
                socket.startConnection();
            }
        });
        socket.startConnection();
    }

    public InetSocketAddress addressSetter(){
        InetSocketAddress address = ConnectionFinder.getLocalNetHost(4590);
        host = address.getAddress().getHostAddress();
        return address;
    }

    public void getCompetitions() throws BadPacketException {
        socket.sendAsPacket(null, null, ScoutingPackets.SCOUTER_SYNC_COMPS.toString(), false);
    }

    public void getGames(String competition) throws BadPacketException {
        socket.sendAsPacket(competition, null, ScoutingPackets.SCOUTER_SYNC_GAMES.toString(), false);
    }

    public void getTeams(String competition, short game) throws BadPacketException {
        socket.sendAsPacket(game, competition, ScoutingPackets.SCOUTER_SYNC_TEAMS.toString(), false);
    }

    public PacketLogger.ObservablePacketStatus loadGame(String competition, short gameCount, String teamIdentifier) throws BadPacketException {
        return socket.sendAsPacket(new Object[]{competition, gameCount, (teamIdentifier.equals("Blue Alliance") ? true : (teamIdentifier.equals("Red Alliance") ? false : Short.valueOf(teamIdentifier)))}, null, ScoutingPackets.SCOUTER_LOADGAME.toString(), false);
    }

    public static void main(String[] args) {
        launch(args);
        self.socket.close();
    }

    public PacketLogger.ObservablePacketStatus cancelScout() throws BadPacketException {
        return socket.sendAsPacket(null, String.valueOf(false), ScoutingPackets.SCOUTER_SUBMITGAME.toString(), false);
    }

    public PacketLogger.ObservablePacketStatus submitScout(ArrayList<ScoutingEvent> events) throws BadPacketException {
        return socket.sendAsPacket(events, String.valueOf(false), ScoutingPackets.SCOUTER_SUBMITGAME.toString(), false);
    }

    // sync up with games


    // manage UI
}
