package gbuiLib;

public class ProgramWideVariableInvalidType extends RuntimeException {

    public ProgramWideVariableInvalidType(String message){
        super(message);
    }

    public ProgramWideVariableInvalidType(String key, Class<?> expected, Class<?> found){
        super("Expected to get a " + expected.getName()+ " value with the key " + key + ", but there was already a value of type " + found.getName() + " saved in there");
    }
}
