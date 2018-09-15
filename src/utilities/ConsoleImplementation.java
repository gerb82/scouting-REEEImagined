package utilities;

import javafx.scene.Node;
import javafx.scene.paint.Paint;

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
 * There is also a static method that should be implemented should you want to directly use any of these methods, as it will automatically cast the console into the appropriate version in an easy to use way, to avoid ugly casting whenever it can be avoided.
 * The method should be called asConsole({@link ConsoleImplementation} console).
 * There is a list of parameters you should implement, as they will be useful for the methods in your implementation. They should be private with getters and setters.
 */
public abstract class ConsoleImplementation {

    /**
     * A boolean representing whether a console has been created already.
     */
    protected boolean console;
    /**
     * A boolean representing whether a regular log file has been created already.
     */
    protected boolean logFile;
    /**
     * A boolean representing whether an error log file has been created already.
     */
    protected boolean errorLogFile;
    /**
     * The root node in the console hierarchy tree.
     */
    protected Node consoleRoot;
    /**
     * The controller object for the console.
     */
    protected Object consoleController;
    /**
     * The output stream connected to the normal log file.
     */
    protected OutputStream logFileStream;
    /**
     * The output stream connected to the error log file.
     */
    protected OutputStream errorLogFileStream;
    /**
     * The file object representing the normal log file.
     */
    protected File logFileLocation;
    /**
     * The file object representing the error log file.
     */
    protected File errorLogFileLocation;

    /**
     * The method used to create a console.
     */
    abstract void initiateConsole();
    /**
     * The method used to create a normal log file.
     */
    abstract void initiateLog();
    /**
     * The method used to create an error log file.
     */
    abstract void initiateErrorLog();

    /**
     * The method used to post a normal message.
     * @param message - The String message to be posted.
     */
    abstract void message(String message);

    /**
     * The method used to post a warning message.
     * @param warning - The String warning to be posted.
     */
    abstract void warn(String warning);

    /**
     * The method used to post a warning message, as well as the related errors.
     * @param warning - The String warning to be posted.
     * @param exceptions - The exceptions relevant to the error.
     */
    abstract void warn(String warning, Exception[] exceptions);
    /**
     * The method used to post an error message.
     * @param error - The String error to be posted.
     */
    abstract void error(String error);

    /**
     * The method used to post an error message, as well as the related errors.
     * @param error - The String error to be posted.
     * @param exceptions - The exceptions relevant to the error.
     */
    abstract void error(String error, Exception[] exceptions);

    /**
     * The method used to post a message to just the log.
     * @param message - The String message to be posted.
     */
    abstract void logMessage(String message);
    /**
     * The method used to post a warning to just the log.
     * @param warning - The String warning to be posted.
     */
    abstract void logWarn(String warning);
    /**
     * The method used to post an error to just the log.
     * @param error - The String error to be posted.
     */
    abstract void logError(String error);
    /**
     * The method used to post a custom message to just the log.
     * @param tag - The tag to be used for the message
     * @param message - The String message to be posted.
     */
    abstract void logCustom(String tag, String message);

    /**
     * The method used to post a message to just the error log.
     * @param message - The String message to be posted.
     */
    abstract void errorLogMessage(String message);
    /**
     * The method used to post a warning to just the error log.
     * @param warning - The String warning to be posted.
     */
    abstract void errorLogWarn(String warning);
    /**
     * The method used to post an error to just the error log.
     * @param error - The String error to be posted.
     */
    abstract void errorLogError(String error);
    /**
     * The method used to post a custom message to just the error log.
     * @param tag - The tag to be used for the message.
     * @param message - The String message to be posted.
     */
    abstract void errorLogCustom(String tag, String message);

    /**
     * The method used to post a message to just the console.
     * @param message - The String message to be posted.
     */
    abstract void consoleMessage(String message);
    /**
     * The method used to post a warning to just the console.
     * @param warning - The String warning to be posted.
     */
    abstract void consoleWarn(String warning);
    /**
     * The method used to post an error to just the console.
     * @param error - The String error to be posted.
     */
    abstract void consoleError(String error);

    /**
     * The method used to post a custom message to just the console.
     * @param time - A boolean signaling if the time should be printed in the message.
     * @param tag - The tag to be used for the message.
     * @param message - The String message to be posted.
     * @param color - The color the message should have.
     */
    abstract void consoleCustom(boolean time, String tag, String message, Paint color);
}
