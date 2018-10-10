package utilities;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ProgramWideVariable {

    private static Map<String, Object> variableMap = new HashMap<>();

    public static Set<String> dumpKeys(){
        return variableMap.keySet();
    }
    public static Object getValue(String key){
        return variableMap.get(key);
    }

    public static boolean exists(String key){
        return variableMap.containsKey(key);
    }

    public static void initalizeDefaults(String[] keys, Object[] values){
        if(keys.length < values.length){
            throw new IllegalArgumentException("Cannot initialize default values when there are more values than keys. the lenghts of each array: keys - " + keys.length + " values - " + values.length);
        }
        else{
            for(int i = 0; i<keys.length; i++){
                try{
                    variableMap.putIfAbsent(keys[i], values[i]);
                }
                catch (IndexOutOfBoundsException e){
                    variableMap.putIfAbsent(keys[i], null);
                }
            }
        }
    }

    private final String key;

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
}