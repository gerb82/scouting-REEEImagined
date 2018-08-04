package utilities.GBLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GBDReader {

    // variables
    private static Interpreter interpreter = new Interpreter();

    public static void readDictionaries(File... file){
        for (File dictionary : file)
        interpreter.setFileReaderProtocol(dictionary, "ClassDictionary");
    }
    // full code interpreter
    private static class Interpreter{

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

        private static HashMap<String, Class<?>> typeMap = new HashMap<>();
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

        private void classDictionary(String finalScript){
            String[] types = finalScript.toString().replaceAll("[^a-zA-Z:;=.]", "").split(";");
            Pattern pattern = Pattern.compile("(.*):=(.*)");
            Matcher matcher = null;
            int lineCount = 0;
            try {
                for (String type : types) {
                    if(type.isEmpty()){
                        throw new GBDYouIdiotException("empty definition at line " + lineCount + ". note that the line count counts the amount of definitions up to this point, and does not consider enters");
                    }
                    assert(type.chars().filter(ch -> ch ==':').count() == 1);
                    assert(type.chars().filter(ch -> ch =='=').count() == 1);
                    matcher = pattern.matcher(type);
                    matcher.find();
                    assert(matcher.group(1) == null && matcher.group(2) == null);
                    typeMap.put(matcher.group(1), Class.forName(matcher.group(2)));
                }
            } catch (AssertionError e){
                throw new GBDYouIdiotException("invalid formatting in line " + lineCount + ". note that the line count counts the amount of definitions up to this point, and does not consider enters");
            } catch (ClassNotFoundException e){
                throw new GBDYouIdiotException("the class " + matcher.group(2) + " in line " + lineCount + " is not a valid class. note that the line count counts the amount of definitions up to this point, and does not consider enters");
            }
            System.out.println(typeMap);
            System.out.println(finalScript);
        }

        private void scriptFile(StringBuilder finalScript, String header){
            Pattern quotes = Pattern.compile("(?:'\"')|(?:\"((?:[^\\\"]|\\\"|\\[^\"])*)\")|(\")");
            String finalV = finalScript.toString();
            Matcher matcher = quotes.matcher(finalScript);
            while(matcher.find()){
                System.out.println(matcher.group(0));
                for(int i = 1; i<= matcher.groupCount(); i++){
                    if(i == 1 && matcher.group(1) != null) {
                        Integer test = stringsNums.putIfAbsent(matcher.group(1), stringsCounter);
                        if (test != null) {
                            finalV = finalV.replace("\"" + matcher.group(1) + "\"","\"" + "@S" + test + "\"");
                            System.out.println("replaced1");
                        } else {
                            finalV = finalV.replace("\"" + matcher.group(1) + "\"","\"" + "@S" + stringsCounter + "\"");
                            strings.put(stringsCounter, matcher.group(1));
                            stringsCounter++;
                            System.out.println("replaced2");
                        }
                    }
                    if (i == 2 && matcher.group(2) != null){
                        throw new GBDYouIdiotException("invalid amount of \". check that each one has a closing one");
                    }
                }
            }
            Pattern charsP = Pattern.compile("(?s)('.')|(')");
            finalScript = new StringBuilder(finalV);
            matcher = charsP.matcher(finalScript);
            while(matcher.find()){
                System.out.println(matcher.group(0));
                for(int i = 1; i<= matcher.groupCount(); i++){
                    if(i == 1 && matcher.group(1) != null) {
                        Integer test = charsNums.putIfAbsent(Character.valueOf(matcher.group(1).charAt(1)), charsCounter);
                        if (test != null) {
                            finalV = finalV.replace(matcher.group(1),"'" + "@C" + test + "'");
                        } else {
                            finalV = finalV.replace(matcher.group(1), "'" + "@C" + charsCounter + "'");
                            chars.put(charsCounter, Character.valueOf(matcher.group(1).charAt(1)));
                            charsCounter++;
                        }
                    }
                    if (i == 2 && matcher.group(2) != null){
                        throw new GBDYouIdiotException("invalid amount of \'. check that each one has a closing one");
                    }
                }
            }
        }

        private void setFileReaderProtocol(File file, String prefHeader) {
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
                script.remove(0);
                for (String string : script) {
                    finalScript.append(string + System.lineSeparator());
                }
                switch(header) {
                    case "ClassDictionary":
                        classDictionary(finalScript.toString());
                        break;
                    case "DataFile":
                        scriptFile(finalScript, header);
                        break;
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
                return null;
            }
        }

        private class Constructor extends Function{

            public Constructor(List<Class<?>> arguments, String code){
                super(Object.class, new Signature(arguments, ""), code, true);
            }

            @Override
            public Object invoke(Object object, java.lang.Object... arguments) {
                // interpreter will do later
                return null;
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
    }
}