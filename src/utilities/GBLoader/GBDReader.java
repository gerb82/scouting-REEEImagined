package utilities.GBLoader;

import utilities.GBLoader.GBDYouIdiotException;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Pattern;

public class GBDReader {

    // variables
    private Scanner scanner = null;
    String currentLine;

    // constructor
    protected GBDReader() {
    }

    // scanner re-setter
    protected void restartScanner(File file) {
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new GBDYouIdiotException("the file (" + file.getPath() + ") that was given as data doesn't exist");
        }
    }

    // header reader
    protected String readHeader() {
        scanner.useDelimiter(Pattern.compile("[/$$]"));
        scanner.next();
        String output = scanner.next();
        currentLine = scanner.next();
        return output;
    }

    // class finished
    private void finishedFile() {
        scanner = null;
    }

    // class dictionary reader
    protected void readClassDictionaryFile(HashMap<String, Class<?>> names, File file) {
        scanner.reset();
        scanner.useDelimiter(Pattern.compile(";"));
        int lineCount = 1;
        currentLine = scanner.next();
        while (scanner.hasNext()) {
            currentLine = scanner.next();
            lineCount += currentLine.split(System.lineSeparator()).length;
            currentLine = currentLine.replaceAll("[^A-Za-z:=.]", "");
            try {
                assert (currentLine.chars().filter(ch -> ch == ':').count() == 1);
            } catch (AssertionError e) {
                throw new GBDYouIdiotException("line number " + lineCount + " in the file " + file.getPath() + " does not have exactly one ':'. It is important you put exactly one in the right format for the interpreter to understand your file.");
            }
            try {
                assert (currentLine.chars().filter(ch -> ch == '=').count() == 1);
            } catch (AssertionError e) {
                throw new GBDYouIdiotException("line number " + lineCount + " in the file " + file.getPath() + " does not have exactly one '='. It is important you put exactly one in the right format for the interpreter to understand your file.");
            }
            try {
                assert (!names.containsKey(currentLine.substring(0, currentLine.indexOf(':') - 1)));
                names.put(currentLine.substring(0, currentLine.indexOf(':')), Class.forName(currentLine.substring(currentLine.indexOf('=') + 1)));
            } catch (ClassNotFoundException e) {
                throw new GBDYouIdiotException("in the file " + file.getPath() + " line number " + lineCount + "'s class path is invalid. Please recheck that it is the right path. The path read: " + (currentLine.substring(currentLine.indexOf('=') + 1)));
            } catch (AssertionError e) {
                throw new GBDYouIdiotException("in the file " + file.getPath() + " line number " + lineCount + "'s name is already defined in the dictionary. Please check for duplicates in your file/change it to a different name. List of currently used names:" + names.keySet());
            }
        }
        System.out.println("Successfully loaded the file " + file.getPath() + " as a GBDictionary");
        finishedFile();
    }

    // full code interpreter
    private class Interpreter{

        private class Variable{

            private Class<?> type;
            private Object value;

            private Variable(Class<?> type){
                this.type = type;
            }

            private void ChangeValue(Object value){
                if(value.getClass().equals(type)){
                    this.value = value;
                }
                else{
                    throw new GBDYouIdiotException("");
                }
            }
        }

        private HashMap<String, Object> C_vars = new HashMap<>();
        private HashMap<String, Object> T_vars = new HashMap<>();
        private HashMap<String, Object> P_vars = new HashMap<>();

        private Interpreter(){

        }

        private void Interpret(String line){
            line = line.replaceFirst(";", "");
            if(line.chars().filter(ch -> ch == '=').count() == 1){
                equals(line.split("=")[0]);
            }
        }

        private void equals(String line){
            interpretName(line.split("=")[0]);
        }

        private String interpretName(String line){
            String[] leftSide = null;
            if(line.chars().filter(ch -> ch == '=').count() == 1){
                line = line.replaceAll("[^A-Za-z1-9_]", "");
                leftSide = line.split(":");
                leftSide[0].replaceAll("[1-9]", "");
                leftSide[1].replaceAll("_", "");
                Class<?> type = ClassToNamesMap.names.get(leftSide[0].substring(2));
                if(type == null){
                    throw new GBDYouIdiotException("type argument is wrong");
                }
                switch(leftSide[0].substring(0,2)){
                    case "C_":
                        C_vars.put(leftSide[1], new Variable(type));
                        break;
                    case "P_":
                        P_vars.put(leftSide[1], new Variable(type));
                        break;
                    case "T_":
                        T_vars.put(leftSide[1], new Variable(type));
                        break;
                    default:
                        throw new GBDYouIdiotException("type prefix is invalid");
                }
            }
            return leftSide[0];
        }
    }
}