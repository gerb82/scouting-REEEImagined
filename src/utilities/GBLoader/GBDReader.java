package utilities.GBLoader;

import utilities.YouIdiotException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class GBDReader {

    private Scanner scanner;

    protected String readHeader(){
        scanner.useDelimiter(Pattern.compile("/##"));
        return scanner.next();
    }

    protected GBDReader(File file){
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new YouIdiotException("the file (" + file.getPath() + ") that was given as data doesn't exist");
        }
    }
}
