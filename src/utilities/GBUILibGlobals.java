package utilities;

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
    }

    public enum GBUILibVariables{
        GBUILIB_GBSOCKET_ALLOWUNSAFE,
        GBUILIB_GBSOCKET_TIMEOUTTIMER,
        GBUILIB_GBSOCKET_RECEIVESTREAMSIZE,
        GBUILIB_GBSOCKET_MAXSERVERHOSTCONNECTIONS,
        GBUILIB_GBSOCKET_HEARTBEATRATE,
        GBUILIB_GBSOCKET_ALLWAYSHEARTBEAT,
        GBUILIB_GBSOCKET_FULLDELETETIMER,

        GBUILIB_CONSOLE_ENABLED
    }

    public static boolean getBooleanVar(GBUILibVariables key, boolean defaultValue){
        return (boolean)ProgramWideVariable.gerVariableWithDefaultSafe(key.toString(), defaultValue, Boolean.class);
    }

    public static boolean unsafeSockcets(){
        return getBooleanVar(GBUILibVariables.GBUILIB_GBSOCKET_ALLOWUNSAFE, false);
    }
}