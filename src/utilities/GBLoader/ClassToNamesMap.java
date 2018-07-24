package utilities.GBLoader;

import utilities.GBLoader.GBDYouIdiotException;

import java.io.File;
import java.util.HashMap;

public class ClassToNamesMap {

    private static GBDReader reader = new GBDReader();
    protected static HashMap<String, Class<?>> names = new HashMap<>();

    public static void addToDictionary(File... mapSources) {
        for (File file : mapSources) {
            reader.restartScanner(file);
            if (!reader.readHeader().equals("ClassDictionary")) {
                throw new GBDYouIdiotException("the data file given as a class dictionary (" + file.getPath() + ") has an invalid header (it is not \"$$ClassDictionary/$$\")");
            }
            reader.readClassDictionaryFile(names, file);
        }
    }
}
