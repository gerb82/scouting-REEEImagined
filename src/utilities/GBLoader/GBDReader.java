package utilities.GBLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GBDReader {

    // variables
    private Scanner scanner = null;
    String currentLine;
    String prefHeader = "";

    // constructor
    protected GBDReader() {
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
        scanner.close();
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
            private java.lang.Object value;

            private Variable(Class<?> type){
                this.type = type;
            }

            private void ChangeValue(java.lang.Object value){
                if(value.getClass().equals(type)){
                    this.value = value;
                }
                else{
                    throw new GBDYouIdiotException("invalid cast. cannot cast " + value.getClass() + " into " + type);
                }
            }
        }

        private class Signature{

            private List<Class<?>> arguments;
            private String name;

            public Signature(List<Class<?>> arguments, String name){
                this.arguments = arguments;
                this.name = name;
            }

            public boolean canInvoke(Signature signature){
                if(signature.arguments.size() != arguments.size() || !signature.name.equals(name)){
                    return false;
                }
                for(int i = 0; i<arguments.size(); i++){
                    try {
                        java.lang.Object test = arguments.get(i).cast(arguments.get(i).newInstance());
                        Class<?> type = signature.arguments.get(i);
                        if(!type.isInstance(test)){
                            return false;
                        }
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        }

        private class Function{

            private Class<?> returnType;
            private Signature signature;
            private boolean partOf;
            private String code;

            public Function(Class<?> returnType, Signature signature, String code, boolean partOf){
                this.partOf = partOf;
                this.returnType = returnType;
                this.signature = signature;
                this.code = code;
            }

            public boolean canInvoke(Signature signature){
                return this.signature.canInvoke(signature);
            }

            public java.lang.Object invoke(Object object, java.lang.Object... arguments){
                // interpreter will do later
            }
        }

        private class Constructor extends Function{

            public Constructor(List<Class<?>> arguments, String code){
                super(Object.class, new Signature(arguments, ""), code, true);
            }

            @Override
            public Object invoke(Object object, java.lang.Object... arguments) {
                // interpreter will do later
            }
        }

        private class ObjectRecipe{

            public String type;
            public List<Function> functions;
            public List<Constructor> constructors;
            public HashMap<String, Variable> variables;
            public HashMap<String, Variable> presetValues;

            public ObjectRecipe(String type){
                this.type = type;
            }

            public void addConstructor(Constructor constructor){
                constructors.add(constructor);
            }

            public void addFunction(Function function, Signature functionName){
                functions.add(function);
            }

            public void addVariable(String name, Variable type){
                variables.put(name, type);
            }

            public void addPresetValue(String name, Variable value){
                presetValues.put(name, value);
            }
        }

        private class Object{

            private ObjectRecipe recipe;

            public Object(ObjectRecipe recipe){
                this.recipe = recipe;
                for (String name : this.recipe.presetValues.keySet()){
                    this.recipe.variables.put(name, this.recipe.presetValues.get(name));
                }
            }

            public Object construct(Signature signature, java.lang.Object... arguments){
                for(Constructor constructor : recipe.constructors){
                    if(constructor.canInvoke(signature)){
                        return constructor.invoke(this, arguments);
                    }
                }
                throw new GBDYouIdiotException("no fitting constructor found");
            }

            public java.lang.Object invoke(Signature signature, java.lang.Object... arguments){
                for(Function function : recipe.functions){
                    if(function.canInvoke(signature)){
                        return function.invoke(this, arguments);
                    }
                }
                throw new GBDYouIdiotException("no fitting function found");
            }
        }

        private HashMap<String, Variable> C_vars = new HashMap<>();
        private HashMap<String, Variable> T_vars = new HashMap<>();
        private HashMap<String, Variable> P_vars = new HashMap<>();
        private HashMap<String, String> names = new HashMap<>();
        private HashMap<Integer, Character> chars = new HashMap<>();
        private HashMap<Character, Integer> charsNums = new HashMap<>();
        private HashMap<Integer, String> strings = new HashMap<>();
        private HashMap<String, Integer> stringsNums = new HashMap<>();
        private int charsCounter = 0;
        private int stringsCounter = 0;

        private Interpreter(){

        }

        private void setFileReaderProtocol(File file) {
            try {
                List<String> script = Files.readAllLines(file.toPath());
                StringBuilder finalScript = new StringBuilder("");
                String header = script.get(0);
                Matcher matcher = Pattern.compile("\\Q$$\\E(.*)\\Q/$$\\E").matcher(header);
                matcher.find();
                header = matcher.group(1);
                if (!prefHeader.equals(header)) {
                    throw new GBDYouIdiotException("file header is incorrect");
                }
                for (String string : script) {
                    finalScript.append(string + System.lineSeparator());
                }
                int lastIndex = -1;
                int lastStart = 0;
                boolean inQuotes = false;
                try {
                    while (finalScript.indexOf("\"", lastIndex + 1) > -1) {
                        lastIndex = finalScript.indexOf("\"", lastIndex + 1);
                        switch (finalScript.charAt(lastIndex - 1)) {
                            case '\\':
                                break;
                            case '\'':
                                if (!inQuotes) {
                                    if (finalScript.indexOf("\'", lastIndex) == lastIndex + 1) {
                                        break;
                                    }
                                }
                            default:
                                inQuotes = !inQuotes;
                                if (inQuotes) {
                                    lastStart = lastIndex + 1;
                                } else {
                                    Integer test = stringsNums.putIfAbsent(finalScript.substring(lastStart, lastIndex), stringsCounter);
                                    if(test != null){
                                        finalScript.replace(lastStart, lastIndex, "@S" + test);
                                    }
                                    else {
                                        strings.putIfAbsent(stringsCounter, finalScript.substring(lastStart, lastIndex));
                                        finalScript.replace(lastStart, lastIndex, "@S" + stringsCounter);
                                        lastIndex += Integer.toString(stringsCounter).length() + 2 - (finalScript.substring(lastIndex - lastStart)).length();
                                        stringsCounter++;
                                    }
                                }
                        }
                    }
                    if(inQuotes){
                        throw new GBDYouIdiotException("invalid file formatting. check that all \" have a closing \"");
                    }
                    lastIndex = -1;
                    while (finalScript.indexOf("\'", lastIndex + 1) > -1){
                        lastIndex = finalScript.indexOf("\'", lastIndex + 1);
                        if(finalScript.charAt(lastIndex+2) == '\''){
                            char current = finalScript.charAt(lastIndex+1);
                            Integer test = charsNums.putIfAbsent(current, charsCounter);
                            if(test != null){
                                finalScript.replace(lastIndex, lastIndex + 2, "\'" + "@C" + test + "\'");
                            }
                            else{
                                chars.putIfAbsent(charsCounter, current);
                                finalScript.replace(lastIndex, lastIndex + 2, "\'" + "@C" + charsCounter + "\'");
                                lastIndex = Integer.toString(charsCounter).length() + 2 - 1;
                                charsCounter++;
                            }
                        }
                        else {
                            throw new GBDYouIdiotException("invalid file formatting. check that all opening ' have a closing '");
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException e){
                    throw new GBDYouIdiotException("invalid file formatting. might be caused by a lack of a ; at the end of the last line of the file");
                }

            } catch (IOException e) {
                throw new GBDYouIdiotException("The file doesn't exist");
            }
        }

        private int getNextIndexOfWord(String string, String word, int startIndex){
            Pattern pattern = Pattern.compile("[1-9a-zA-Z]");
            boolean failed = false;
            while(true) {
                int firstIndex = string.indexOf(word, startIndex);
                if (firstIndex != -1) {
                    if (firstIndex != 0) {
                        String before = Character.toString(string.charAt(firstIndex - 1));
                        Matcher matcher = pattern.matcher(before);
                        if (matcher.find()) {
                            failed = true;
                        }
                    }
                    if (firstIndex != string.length()-1){
                        String after = Character.toString(string.charAt(firstIndex + 1));
                        Matcher matcher = pattern.matcher(after);
                        if (matcher.find()) {
                            failed = true;
                        }
                    }
                    if(!failed) {
                        return firstIndex;
                    }
                    startIndex = firstIndex + 1;
                }
                else {
                    return -1;
                }
            }
        }

        // unchecked

        private void Interpret(String line) {
            line = line.replaceFirst(";", "");
            if (countInString(line, '=') == 1) {
                LeftSide(line.split("=")[0]);
            }
        }

        private String LeftSide(String line){
            String output;
            if(countInString(line, ':') == 1){
                String[] leftSide;
                line = line.replaceAll("[^A-Za-z1-9_:]", "");
                leftSide = line.split(":");
                leftSide[0] = leftSide[0].replaceAll("[1-9_]", "");
                Class<?> type = ClassToNamesMap.names.get(leftSide[0].substring(2));
                if(type == null){
                    throw new GBDYouIdiotException("type argument is not an existing class");
                }
                addNameToMemory(leftSide[1], type);
                output = leftSide[1];
            }
            else{
                line = line.replaceAll("[^A-Za-z1-9]", "");
                if(names.containsKey(line)){
                    output = line;
                }
                else{
                    throw new GBDYouIdiotException("variable " + line + " does not exist");
                }
            }
            return output;
        }

        private void addNameToMemory(String name, Class<?> type){
            String AName = name.substring(2).replaceAll("_", "");
            if(names.containsKey(AName)){
                throw new GBDYouIdiotException("name is already taken");
            }
            else {
                switch (name.substring(0, 2)) {
                    case "C_":
                        C_vars.put(AName, new Variable(type));
                        names.put(AName, "C_");
                        break;
                    case "P_":
                        P_vars.put(AName, new Variable(type));
                        names.put(AName, "P_");
                        break;
                    case "T_":
                        T_vars.put(AName, new Variable(type));
                        names.put(AName, "T_");
                        break;
                    default:
                        throw new GBDYouIdiotException("type prefix is invalid");
                }
            }
        }

        private int countInString(String string, char filter){
            string = string.replaceAll("\".*\"","").replaceAll("\'.*\'","");
            return (int)string.chars().filter(ch -> ch == filter).count();
        }

        private String readNextLine(){
            scanner.useDelimiter("\"(?:[^\\\"]|\".)*\"|(;)");
            Pattern pattern = Pattern.compile();
            Matcher matcher = pattern.matcher("test123");
            MatchResult result = scanner.forEachRemaining();
        }

        private String convertToOperateableString(String string){
            // pattern.next("(?:[^"\\]|\\.)*|;")
        }
    }
}