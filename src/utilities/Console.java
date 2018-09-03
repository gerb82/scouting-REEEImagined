package utilities;

public class Console extends ConsoleImplementation{

    static Console asConsole(Object console){
        return (Console) console;
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
    public boolean isConsole() {
        return false;
    }

    @Override
    public boolean isLogFile() {
        return false;
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
    public void consoleMessage(String console) {

    }

    @Override
    public void consoleWarn(String warning) {

    }

    @Override
    public void consoleError(String error) {

    }
}
