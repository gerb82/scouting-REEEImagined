package utilities;

import utilities.MethodTypes.BlankMethod;

import java.util.ArrayList;

public class GBUILibGlobals {

    public static void initalize(ProgramWideVariable.InsertMethod insertToMap) {
        //sockets
        insertToMap.insert(GBUILibVariables.GBUILIB_GBSOCKET_ALLOWUNSAFE.toString(), false);
        insertToMap.insert(GBUILibVariables.GBUILIB_GBSOCKET_TIMEOUTTIMER.toString(), 30);
        insertToMap.insert(GBUILibVariables.GBUILIB_GBSOCKET_RECEIVESTREAMSIZE.toString(), 8192);
        insertToMap.insert(GBUILibVariables.GBUILIB_GBSOCKET_MAXSERVERHOSTCONNECTIONS.toString(), -1);
        insertToMap.insert(GBUILibVariables.GBUILIB_GBSOCKET_HEARTBEATRATE.toString(), 5);
        insertToMap.insert(GBUILibVariables.GBUILIB_GBSOCKET_ALLWAYSHEARTBEAT.toString(), false);
        insertToMap.insert(GBUILibVariables.GBUILIB_GBSOCKET_FULLDELETETIMER.toString(), 120);

        //console
        insertToMap.insert(GBUILibVariables.GBUILIB_CONSOLE_ENABLED.toString(), true);


        //util
        insertToMap.insert(GBUILibVariables.GBUILIB_UTIL_SHUTDOWNCOMMANDSTORUN.toString(), new ArrayList<BlankMethod>());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for(BlankMethod method : new ProgramWideVariable<ArrayList<BlankMethod>>(GBUILibVariables.GBUILIB_UTIL_SHUTDOWNCOMMANDSTORUN.toString(), new ArrayList<BlankMethod>(), true, false).getValueSafe()){
                method.execute();
            }
        }));
    }

    public enum GBUILibVariables{
        GBUILIB_GBSOCKET_ALLOWUNSAFE,
        GBUILIB_GBSOCKET_TIMEOUTTIMER,
        GBUILIB_GBSOCKET_RECEIVESTREAMSIZE,
        GBUILIB_GBSOCKET_MAXSERVERHOSTCONNECTIONS,
        GBUILIB_GBSOCKET_HEARTBEATRATE,
        GBUILIB_GBSOCKET_ALLWAYSHEARTBEAT,
        GBUILIB_GBSOCKET_FULLDELETETIMER,

        GBUILIB_CONSOLE_ENABLED,

        GBUILIB_UTIL_SHUTDOWNCOMMANDSTORUN
    }

    public static Boolean getBooleanVar(GBUILibVariables key, boolean defaultValue){
        return (boolean)ProgramWideVariable.gerVariableWithDefaultSafe(key.toString(), defaultValue, Boolean.class);
    }

    public static Boolean unsafeSockcets(){
        return getBooleanVar(GBUILibVariables.GBUILIB_GBSOCKET_ALLOWUNSAFE, false);
    }

    public static void addShutdownCommand(BlankMethod method){
        ((ArrayList<BlankMethod>)ProgramWideVariable.gerVariableWithDefaultSafe(GBUILibVariables.GBUILIB_UTIL_SHUTDOWNCOMMANDSTORUN.toString(), new ArrayList<BlankMethod>(), ArrayList.class)).add(method);
    }

    public static void removeShutdownCommand(BlankMethod method){
        ((ArrayList<BlankMethod>)ProgramWideVariable.gerVariableWithDefaultSafe(GBUILibVariables.GBUILIB_UTIL_SHUTDOWNCOMMANDSTORUN.toString(), new ArrayList<BlankMethod>(), ArrayList.class)).remove(method);
    }

    public static Integer getIntVar(GBUILibVariables key, int defaultValue){
        return (Integer)ProgramWideVariable.gerVariableWithDefaultSafe(key.toString(), defaultValue, Integer.class);
    }

    public static Integer getSocketTimeout(){
        return getIntVar(GBUILibVariables.GBUILIB_GBSOCKET_TIMEOUTTIMER, 30);
    }

    public static Integer getSocketReceiveStreamSize(){
        return getIntVar(GBUILibVariables.GBUILIB_GBSOCKET_RECEIVESTREAMSIZE, 8192);
    }

    public static Integer getMaxServerConnections(){
        return getIntVar(GBUILibVariables.GBUILIB_GBSOCKET_MAXSERVERHOSTCONNECTIONS, -1);
    }

    public static Integer getHeartBeatRate(){
        return getIntVar(GBUILibVariables.GBUILIB_GBSOCKET_HEARTBEATRATE, 5);
    }

    public static Integer fullDeleteServerSocket(){
        return getIntVar(GBUILibVariables.GBUILIB_GBSOCKET_FULLDELETETIMER, 120);
    }
}