package scouterSide;

import connectionIndependent.ConnectionFinder;
import connectionIndependent.ScoutingConnections;
import connectionIndependent.ScoutingPackets;
import connectionIndependent.scouted.ScoutIdentifier;
import connectionIndependent.scouted.ScoutingEvent;
import gbuiLib.GBSockets.*;
import gbuiLib.ProgramWideVariable;
import javafx.application.Application;
import javafx.application.Platform;
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
    private Stage stage;

    public void setShowing(boolean show) {
        Platform.runLater(() -> {
            if (show && !stage.isShowing()) stage.show();
            else if (!show) stage.hide();
        });
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainScreen.fxml"));
        root = loader.load();
        primaryStage.setScene(new Scene(root));
        ScouterUI controller = loader.getController();
        controller.setMain(this);
        controller.setRoot(root);
        stage.show();
//        primaryStage.setFullScreen(true);

        self = this;
        ProgramWideVariable.initializeDefaults();
        PacketLogger.setDirectory();
        ActionHandler handler = new ActionHandler();
        handler.setHandler(ScoutingPackets.SCOUTER_LOADGAME.toString(), controller::loadNewView);
        handler.setHandler(ScoutingPackets.SCOUTER_SYNC_COMPS.toString(), controller::competitions);
        handler.setHandler(ScoutingPackets.SCOUTER_SYNC_GAMES.toString(), controller::games);
        handler.setHandler(ScoutingPackets.SCOUTER_SUBMITGAME.toString(), controller::scoutOver);
        SelectorManager selector = new SelectorManager();
        socket = new GBSocket(addressSetter(), ScoutingConnections.SCOUTER.toString(), false, selector, handler, new GBSocket.SocketConfig(), false);
        socket.isConnected.addListener((observable, oldValue, newValue) -> {
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
        controller.init();
    }

    public InetSocketAddress addressSetter() {
        InetSocketAddress address = ConnectionFinder.getLocalNetHost(4590);
        host = address.getAddress().getHostAddress();
        return address;
    }

    public void getCompetitions() throws BadPacketException {
        socket.sendAsPacket(null, null, ScoutingPackets.SCOUTER_SYNC_COMPS.toString(), false);
    }

    public void getGames(String competition) throws BadPacketException {
        socket.sendAsPacket(null, competition, ScoutingPackets.SCOUTER_SYNC_GAMES.toString(), false);
    }

    public PacketLogger.ObservablePacketStatus loadGame(ScoutIdentifier identifier) throws BadPacketException {
        return socket.sendAsPacket(identifier, null, ScoutingPackets.SCOUTER_LOADGAME.toString(), false);
    }

    public static void main(String[] args) {
        launch(args);
        self.socket.close();
    }

    public PacketLogger.ObservablePacketStatus cancelScout() throws BadPacketException {
        return socket.sendAsPacket(null, null, ScoutingPackets.SCOUTER_SUBMITGAME.toString(), false);
    }

    public PacketLogger.ObservablePacketStatus submitScout(ArrayList<ScoutingEvent> events) throws BadPacketException {
        return socket.sendAsPacket(events, String.valueOf(true), ScoutingPackets.SCOUTER_SUBMITGAME.toString(), false);
    }

    // sync up with games


    // manage UI
}
