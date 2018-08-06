package utilities;

public class YouDimwitException extends RuntimeException {

    /**
     * An exception used to signify that something is misused in a terrible way.
     * Also prints said reason.
     * Also crashes the program. If it's called it is probably a good enough reason to do that.
     * @param reason - The reason the exception was thrown.
     */
    public YouDimwitException(String reason){
        System.out.println("The program has crashed because " + reason);
        System.err.println("The program has crashed because " + reason);
    }
}
