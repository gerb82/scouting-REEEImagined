package utilities;

import javafx.scene.paint.Paint;

public class Console extends ConsoleImplementation{

    static Console asConsole(ConsoleImplementation console){
        return (Console) console;
    }

    @Override
    public void message(String message){
        logMessage(message);
        consoleMessage(message);
    }

    @Override
    public void warn(String warning){
        logWarn(warning);
        consoleWarn(warning);
        errorLogWarn(warning);
    }

    @Override
    public void warn(String warning, String[] ErrorTitles, StackTraceElement[][] elements) {

    }

    @Override
    public void error(String error){
        logError(error);
        consoleError(error);
        errorLogError(error);
    }

    @Override
    public void error(String error, String[] ErrorTitles, StackTraceElement[][] elements) {

    }

    @Override
    public void initiateConsole(){

    }

    @Override
    public void initiateLog(){

    }

    @Override
    public void initiateErrorLog() {

    }

    @Override
    public void logMessage(String message) {

    }

    @Override
    public void logWarn(String warning) {

    }

    @Override
    public void logError(String error) {

    }

    @Override
    public void logCustom(String tag, String message) {

    }

    @Override
    public void errorLogMessage(String message) {

    }

    @Override
    public void errorLogWarn(String message) {

    }

    @Override
    public void errorLogError(String messaage) {

    }

    @Override
    public void errorLogCustom(String tag, String message) {

    }

    @Override
    public void consoleMessage(String console) {

    }

    @Override
    public void consoleWarn(String warning) {

    }

    @Override
    public void consoleError(String error) {

    }

    @Override
    public void consoleCustom(boolean time, String tag, String message, Paint color) {

    }
}
