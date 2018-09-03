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
 * So, the three different main methods: message, warn, error.
 * {@link ConsoleImplementation#message(String)} - a normal, regular message to the console/log.
 * {@link ConsoleImplementation#warn(String)} - a warning message, about non-fatal errors/possible issues with the code, that should not stop the program from running, but should probably be dealt with.
 * {@link ConsoleImplementation#error(String)} - an error message, about any fatal/non-fatal errors that have an actual impact on the code (crashing a necessary thread, an invalid input that causes an {@link AssertionYouDimwitException}/{@link AssertionError}/{@link IllegalArgumentException}, etc).
 * All three of those 3 methods have a version that writes only to the log or only to the console, if such a thing should be desired.
 * There is also a static method that should be implemented should you want any of these methods, as it will auto-cast the console into the appropriate version in an easy to use way, to avoid ugly casting whenever it can be avoided.
 * The method should be called asConsole(Object console).
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

    abstract void initiateConsole();
    abstract void initiateLog();
    abstract void initiateErrorLog();
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
