package utilities;

public class GBUILibGlobals {

    public static void initalize(ProgramWideVariable.InsertMethod insertToMap) {
        insertToMap.insert(GBUILibVariables.GBUILIB_GBSOCKET_ALLOWUNSAFE.toString(), false);
        insertToMap.insert(GBUILibVariables.GBUILIB_CONSOLE_ENABLED.toString(), true);
    }

    public enum GBUILibVariables{
        GBUILIB_GBSOCKET_ALLOWUNSAFE, GBUILIB_CONSOLE_ENABLED;
    }

    public static boolean getBooleanVar(GBUILibVariables key, boolean defaultValue){
        return (boolean)ProgramWideVariable.gerVariableWithDefaultSafe(key.toString(), defaultValue, Boolean.class);
    }

    public static boolean unsafeSockcets(){
        return getBooleanVar(GBUILibVariables.GBUILIB_GBSOCKET_ALLOWUNSAFE, false);
    }
}
