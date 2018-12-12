package utilities;

import utilities.MethodTypes.BlankMethod;

import java.io.File;
import java.util.ArrayList;

public class GBUILibGlobals {

    public static void initialize(ProgramWideVariable.InsertMethod insertToMap) {
        //sockets
        insertToMap.insert(GBUILibVariables.GBUILIB_GBSOCKET_ALLOWUNSAFE.toString(), false, true);
        insertToMap.insert(GBUILibVariables.GBUILIB_GBSOCKET_TIMEOUTTIMER.toString(), 30, true);
        insertToMap.insert(GBUILibVariables.GBUILIB_GBSOCKET_MAXSERVERHOSTCONNECTIONS.toString(), -1, true);
        insertToMap.insert(GBUILibVariables.GBUILIB_GBSOCKET_HEARTBEATRATE.toString(), 5, true);
        insertToMap.insert(GBUILibVariables.GBUILIB_GBSOCKET_ALLWAYSHEARTBEAT.toString(), false, true);
        insertToMap.insert(GBUILibVariables.GBUILIB_GBSOCKET_FULLDELETETIMER.toString(), 120, true);
        insertToMap.insert(GBUILibVariables.GBUILIB_GBSOCKET_INCOMINGPACKETMANAGEMENTTHREADCOUNT.toString(), 5, true);
        insertToMap.insert(GBUILibVariables.GBUILIB_GBSOCKET_LOGGERCLEANUPTIMER.toString(), 2, true);
        insertToMap.insert(GBUILibVariables.GBUILIB_GBSOCKET_KEEPUNFINISHEDPACKETTIMER.toString(), 5, true);
        insertToMap.insert(GBUILibVariables.GBUILIB_GBSOCKET_AUTODISCARDACKEDPACKETS.toString(), true, true);
        insertToMap.insert(GBUILibVariables.GBUILIB_GBSOCKET_MAXRECEIVEPACKETSIZE.toString(), 65507, true);
        insertToMap.insert(GBUILibVariables.GBUILIB_GBSOCKET_PACKETTIMEOUTTIMER.toString(), 24, true);
        insertToMap.insert(GBUILibVariables.GBUILIB_GBSOCKET_PACKETATTEMPTSTOSEND.toString(), 8, true);
        insertToMap.insert(GBUILibVariables.GBUILIB_GBSOCKET_AMOUNTOFSOCKETCONNECTIONS.toString(), 0, false);
        insertToMap.insert(GBUILibVariables.GBUILIB_GBSOCKET_SOCKETSLOGSDIRECTORY.toString(), new File(System.getProperty("user.dir")), true);
        insertToMap.insert(GBUILibVariables.GBUILIB_GBSOCKET_WRITEPACKETSTOLOG.toString(), true, true);
        insertToMap.insert(GBUILibVariables.GBUILIB_GBSOCKET_TIMETOSENDPACKET.toString(), 5, true);


        //console
        insertToMap.insert(GBUILibVariables.GBUILIB_CONSOLE_ENABLED.toString(), true, true);


        //util
        insertToMap.insert(GBUILibVariables.GBUILIB_UTIL_SHUTDOWNCOMMANDSTORUN.toString(), new ArrayList<BlankMethod>(), true);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (BlankMethod method : (ArrayList<BlankMethod>) ProgramWideVariable.getFinalVariable(GBUILibVariables.GBUILIB_UTIL_SHUTDOWNCOMMANDSTORUN.toString())) {
                method.execute();
            }
        }));
    }

    public enum GBUILibVariables {
        GBUILIB_GBSOCKET_ALLOWUNSAFE,
        GBUILIB_GBSOCKET_TIMEOUTTIMER,
        GBUILIB_GBSOCKET_MAXSERVERHOSTCONNECTIONS,
        GBUILIB_GBSOCKET_HEARTBEATRATE,
        GBUILIB_GBSOCKET_ALLWAYSHEARTBEAT,
        GBUILIB_GBSOCKET_FULLDELETETIMER,
        GBUILIB_GBSOCKET_INCOMINGPACKETMANAGEMENTTHREADCOUNT,
        GBUILIB_GBSOCKET_LOGGERCLEANUPTIMER,
        GBUILIB_GBSOCKET_KEEPUNFINISHEDPACKETTIMER,
        GBUILIB_GBSOCKET_AUTODISCARDACKEDPACKETS,
        GBUILIB_GBSOCKET_MAXRECEIVEPACKETSIZE,
        GBUILIB_GBSOCKET_PACKETTIMEOUTTIMER,
        GBUILIB_GBSOCKET_PACKETATTEMPTSTOSEND,
        GBUILIB_GBSOCKET_TIMETOSENDPACKET,
        GBUILIB_GBSOCKET_AMOUNTOFSOCKETCONNECTIONS,
        GBUILIB_GBSOCKET_SOCKETSLOGSDIRECTORY,
        GBUILIB_GBSOCKET_WRITEPACKETSTOLOG,

        GBUILIB_CONSOLE_ENABLED,

        GBUILIB_UTIL_SHUTDOWNCOMMANDSTORUN
    }

    public static Boolean unsafeSockets() {
        return ProgramWideVariable.getFinalVariableSafe(GBUILibVariables.GBUILIB_GBSOCKET_ALLOWUNSAFE.toString(), Boolean.class);
    }

    public static Boolean alwaysHeartBeat() {
        return ProgramWideVariable.getFinalVariableSafe(GBUILibVariables.GBUILIB_GBSOCKET_ALLWAYSHEARTBEAT.toString(), Boolean.class);
    }

    public static void addShutdownCommand(BlankMethod method) {
        ProgramWideVariable.getFinalVariableSafe(GBUILibVariables.GBUILIB_UTIL_SHUTDOWNCOMMANDSTORUN.toString(), ArrayList.class).add(method);
    }

    public static void removeShutdownCommand(BlankMethod method) {
        ProgramWideVariable.getFinalVariableSafe(GBUILibVariables.GBUILIB_UTIL_SHUTDOWNCOMMANDSTORUN.toString(), ArrayList.class).remove(method);
    }

    public static Integer getSocketTimeout() {
        return ProgramWideVariable.getFinalVariableSafe(GBUILibVariables.GBUILIB_GBSOCKET_TIMEOUTTIMER.toString(), Integer.class);
    }


    public static Integer getMaxServerConnections() {
        return ProgramWideVariable.getFinalVariableSafe(GBUILibVariables.GBUILIB_GBSOCKET_MAXSERVERHOSTCONNECTIONS.toString(), Integer.class);
    }

    public static Integer getHeartBeatRate() {
        return ProgramWideVariable.getFinalVariableSafe(GBUILibVariables.GBUILIB_GBSOCKET_HEARTBEATRATE.toString(), Integer.class);
    }

    public static Integer fullDeleteServerSocket() {
        return ProgramWideVariable.getFinalVariableSafe(GBUILibVariables.GBUILIB_GBSOCKET_FULLDELETETIMER.toString(), Integer.class);
    }

    public static Integer getInputThreadCount() {
        return ProgramWideVariable.getFinalVariableSafe(GBUILibVariables.GBUILIB_GBSOCKET_INCOMINGPACKETMANAGEMENTTHREADCOUNT.toString(), Integer.class);
    }

    public static Integer getLoggerCleanupTimer() {
        return ProgramWideVariable.getFinalVariableSafe(GBUILibVariables.GBUILIB_GBSOCKET_LOGGERCLEANUPTIMER.toString(), Integer.class);
    }

    public static Integer getUnfinishedPacketDiscardTimer() {
        return ProgramWideVariable.getFinalVariableSafe(GBUILibVariables.GBUILIB_GBSOCKET_LOGGERCLEANUPTIMER.toString(), Integer.class);
    }

    public static Boolean getAutoDiscardFinishedPackets(){
        return ProgramWideVariable.getFinalVariableSafe(GBUILibVariables.GBUILIB_GBSOCKET_AUTODISCARDACKEDPACKETS.toString(), Boolean.class);
    }

    public static int getMaxReceivePacketSize(){
        return ProgramWideVariable.getChangingVariableSafe(GBUILibVariables.GBUILIB_GBSOCKET_MAXRECEIVEPACKETSIZE.toString(), Integer.class);
    }

    public static int getTimeToPacketTimeout(){
        return ProgramWideVariable.getChangingVariableSafe(GBUILibVariables.GBUILIB_GBSOCKET_PACKETTIMEOUTTIMER.toString(), Integer.class);
    }

    public static int getPacketSendAttempts(){
        return ProgramWideVariable.getChangingVariableSafe(GBUILibVariables.GBUILIB_GBSOCKET_PACKETATTEMPTSTOSEND.toString(), Integer.class);
    }

    public static int getSocketCount(){
        return ProgramWideVariable.getChangingVariableSafe(GBUILibVariables.GBUILIB_GBSOCKET_AMOUNTOFSOCKETCONNECTIONS.toString(), Integer.class);
    }

    public static int newSocketFormed(){
        int counter = ProgramWideVariable.getChangingVariableSafe(GBUILibVariables.GBUILIB_GBSOCKET_AMOUNTOFSOCKETCONNECTIONS.toString(), Integer.class);
        ProgramWideVariable.setValueInChangingVariablesMap(GBUILibVariables.GBUILIB_GBSOCKET_AMOUNTOFSOCKETCONNECTIONS.toString(), counter++);
        return counter;
    }

    public static File getSocketLogsDirectory(){
        return ProgramWideVariable.getFinalVariableSafe(GBUILibVariables.GBUILIB_GBSOCKET_SOCKETSLOGSDIRECTORY.toString(), File.class);
    }

    public static boolean writePacketsToFile(){
        return ProgramWideVariable.getFinalVariableSafe(GBUILibVariables.GBUILIB_GBSOCKET_WRITEPACKETSTOLOG.toString(), Boolean.class);
    }

    public static int getTimeToSendPacket(){
        return ProgramWideVariable.getFinalVariableSafe(GBUILibVariables.GBUILIB_GBSOCKET_TIMETOSENDPACKET.toString(), Integer.class);
    }
}