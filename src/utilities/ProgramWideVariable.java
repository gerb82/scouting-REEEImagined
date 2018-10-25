package utilities;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ProgramWideVariable<T> {

    public interface InsertMethod{
        void insert(String key, Object content);
    }

    public interface GlobalVariablesPreset{
        void initialize(InsertMethod insertToMap);
    }

    private static Map<String, Object> variableMap = new HashMap<>()    ;
    private static Boolean initialized = false;

    public static Set<String> dumpKeys(){
        return variableMap.keySet();
    }

    public static boolean exists(String key){
        return variableMap.containsKey(key);
    }

    public static void initalizeDefaults(GlobalVariablesPreset... presets){
        if(!initialized) {
            GBUILibGlobals.initalize(variableMap::putIfAbsent);
            for (GlobalVariablesPreset gvp : presets) {
                gvp.initialize(variableMap::putIfAbsent);
            }
            initialized = true;
        }
    }

    public static Object gerVariableWithDefaultSafe(String key, Object value, Class<?> type){
        if(initialized) {
            try {
                Object result = type.cast(variableMap.putIfAbsent(key, value));
                if (result == null) {
                    return value;
                }
                return result;
            } catch (ClassCastException e) {
                throw new ProgramWideVariableInvalidType(key, type, variableMap.get(key).getClass());
            }
        }
        return value;
    }

    private final String key;
    private T value = null;
    private Class<?> type;
    private boolean changing;

    public ProgramWideVariable(String key, T value, boolean changingVal, boolean override){
        SmartAssert.makeSure(key != null, "Can't have a global variable name with an identifier of null");
        if(initialized) {
            this.key = key;
            if (override) {
                variableMap.put(key, value);
            } else {
                variableMap.putIfAbsent(key, value);
            }
            this.type = value.getClass();
            this.changing = changingVal;
            if (changingVal) {
                try {
                    this.value = (T) (variableMap.get(key));
                } catch (ClassCastException e) {
                    throw new ProgramWideVariableInvalidType(key, this.type, variableMap.get(key).getClass());
                }
            }
        }
        else{
            this.key = null;
            this.changing = false;
            this.value = value;
        }
    }

    public Object getValue(){
        if(changing){
            return variableMap.get(key);
        }
        return value;
    }

    public T getValueSafe(){
        if(changing){
            try{
                return (T)variableMap.get(key);
            } catch (ClassCastException e){

                throw new ProgramWideVariableInvalidType(key, type, variableMap.get(key).getClass());
            }
        }
        return value;
    }

    public void changeValue(T value){
        if(changing){
            variableMap.put(key, value);
        }
        else{
            this.value = value;
        }
    }
}