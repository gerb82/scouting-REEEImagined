package utilities;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ProgramWideVariable {

    public interface InsertMethod{
        void insert(String key, Object content);
    }

    public interface GlobalVariablesPreset{
        void initalize(InsertMethod insertToMap);
    }

    private static Map<String, Object> variableMap = new HashMap<>();
    private static Boolean initialized = false;

    public static boolean isInitialized(){
        return initialized;
    }

    public static Set<String> dumpKeys(){
        return variableMap.keySet();
    }
    public static Object getValue(String key){
        return variableMap.get(key);
    }

    public static boolean exists(String key){
        return variableMap.containsKey(key);
    }

    public static void initalizeDefaults(GlobalVariablesPreset... presets){
        if(!initialized) {
            addGBUILibDefaults();
            for (GlobalVariablesPreset gvp : presets) {
                gvp.initalize(variableMap::putIfAbsent);
            }
            initialized = true;
        }
    }

    private static void addGBUILibDefaults(){
        variableMap.putIfAbsent(GBUILibVariables.GBUILIB_GBSOCKET_ALLOWUNSAFE.toString(), false);
        variableMap.putIfAbsent(GBUILibVariables.GBUILIB_CONSOLE_ENABLED.toString(), true);
    }

    private final String key;

    public static Object getVariableWithDefault(String key, Object value){
        ProgramWideVariable var = new ProgramWideVariable(key, value, false);
        return var.getValue();
    }

    public static Object gerVariableWithDefaultSafe(String key, Object value, Class<?> type){
        Object result = getVariableWithDefault(key, value);
        if(result.getClass().isInstance(type)){
            return result;
        }
        else{
            return value;
        }
    }

    public static boolean getBooleanWithDefault(String key, boolean value){
        Object result = getVariableWithDefault(key, value);
        if(result instanceof Boolean){
            return (boolean)result;
        }
        else{
            return value;
        }
    }

    public ProgramWideVariable(String key, Object value, boolean override){
        this.key = key;
        if(override){
            variableMap.put(key, value);
        }
        else{
            variableMap.putIfAbsent(key, value);
        }
    }

    public Object getValue() {
        return variableMap.get(key);
    }

    public Object safeGetValue(Class<?> type){
        if(variableMap.get(key).getClass().equals(type)){
            return variableMap.get(key);
        }
        return null;
    }

    public enum GBUILibVariables{
        GBUILIB_GBSOCKET_ALLOWUNSAFE, GBUILIB_CONSOLE_ENABLED;
    }
}