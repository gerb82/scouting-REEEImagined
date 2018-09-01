package utilities;

import javafx.scene.Node;

import java.io.File;
import java.io.OutputStream;

/**
 * A very basic and abstract implementation of everything needed for a console.
 * There are a few things to remember when implementing a console.
 * Unless you fully know what you are doing, and you are using a ConsoleImplementation for a super specific purpose, you will need to add it to the list of ConsoleImplementations in {@link Utils}, and perhaps also make it the current one being used (more on that in the {@link Utils} javadoc).
 * How you add it, when do you add it, and everything else depends on your specific implementation, but usually you'll want to do it during the constructor/initialization.
 * {@link ConsoleImplementation#message(String)}, {@link ConsoleImplementation#warn(String)} and {@link ConsoleImplementation#error(String)} are the main methods through which the console implementation will be accessed.
 * You can also make the other methods public and use them directly, but that will not work through the {@link Utils} methods, meaning you'll either have to write your own "wrapper", use the console object directly, or do a LOT of casting. All three ways are not recommended if you are looking to make a standard console.
 * 
 */
public abstract class ConsoleImplementation {

    /**
     * A boolean representing whether a console
     */
    private boolean console;
    private boolean logFile;
    private boolean errorLogFile;
    private Node consoleRoot;
    private Object consoleController;
    private OutputStream logFileStream;
    private OutputStream errorLogFileStream;
    private File logFileLocation;
    private File errorLogFileLocation;

    private void initiateConsole(){}
    private void initiateLog(){}
    abstract boolean isConsole();
    abstract boolean isLogFile();
    public void message(String message){
        consoleMessage(message);
        logMessage(message);
    }
    public void warn(String warning){
        consoleWarn(warning);
        logWarn(warning);
    }
    public void error(String error){
        consoleError(error);
        logError(error);
    }
    abstract void logMessage(String message);
    abstract void logWarn(String warning);
    abstract void logError(String error);
    abstract void consoleMessage(String console);
    abstract void consoleWarn(String warning);
    abstract void consoleError(String error);
}
