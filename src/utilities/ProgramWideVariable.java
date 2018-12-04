package utilities;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ProgramWideVariable {

    public interface InsertMethod{
        void insert(String key, Object content, boolean finalVar);
    }

    public interface GlobalVariablesPreset{
        void initialize(InsertMethod insertToMap);
    }

    private static Map<String, Object> changingVariablesMap = new HashMap<>();
    private static Map<String, Object> finalVariablesMap = new HashMap<>();
    private static Boolean initialized = false;

    private static void insertIfAbsent(String key, Object content, boolean finalVar){
        if(finalVar){
            finalVariablesMap.putIfAbsent(key, content);
        }
        else{
            changingVariablesMap.putIfAbsent(key, content);
        }
    }

    public static Set<String> dumpFinalVarsKeys() {
        initialized();
        return finalVariablesMap.keySet();
    }
    public static Set<String> dumpChangingKeys(){
        initialized();
        return changingVariablesMap.keySet();
    }

    public static boolean existsInChanging(String key){
        initialized();
        return changingVariablesMap.containsKey(key);
    }
    public static boolean existsInFinalVars(String key) {
        initialized();
        return finalVariablesMap.containsKey(key);
    }

    public static void initializeDefaults(GlobalVariablesPreset... presets){
        if(!initialized) {
            GBUILibGlobals.initialize(ProgramWideVariable::insertIfAbsent);
            for (GlobalVariablesPreset gvp : presets) {
                gvp.initialize(ProgramWideVariable::insertIfAbsent);
            }
            initialized = true;
        }
        else{
            throw new Error("Cannot initialize global variables twice!");
        }
    }

    public static Object getChangingVariable(String key){
        initialized();
        return changingVariablesMap.get(key);
    }
    public static <T> T getChangingVariableSafe(String key, Class<T> expectedType) throws ProgramWideVariableInvalidType{
        Object ret = getChangingVariable(key);
        if(expectedType.isAssignableFrom(ret.getClass()) || ret == null){
            return expectedType.cast(ret);
        }
        throw new ProgramWideVariableInvalidType("Attempted to get an object of type " + expectedType.getName() + " from the changing variables map in key " + key + " but ended up getting an object of type " + ret.getClass().getName());
    }
    public static Object getFinalVariable(String key){
        initialized();
        return finalVariablesMap.get(key);
    }
    public static <T> T getFinalVariableSafe(String key, Class<T> expectedType) throws ProgramWideVariableInvalidType{
        Object ret = getFinalVariable(key);
        if(expectedType.isAssignableFrom(ret.getClass()) || ret == null){
            return expectedType.cast(ret);
        }
        throw new ProgramWideVariableInvalidType("Attempted to get an object of type " + expectedType.getName() + " from the final variables map in key " + key + " but ended up getting an object of type " + ret.getClass().getName());
    }

    public static void setValueInChangingVariablesMap(String key, Object value) {
        initialized();
        changingVariablesMap.put(key, value);
    }

    public static boolean putIfAbsentInChangingVariablesMap(String key, Object value){
        initialized();
        return null == changingVariablesMap.putIfAbsent(key, value);
    }

    public static boolean

    private static void initialized(){
        if(!initialized) {
            throw new Error("Global variables have not been initialized!");
        }
    }
}