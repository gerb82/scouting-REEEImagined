package utilities.GBLoader;

public class GBDYouIdiotException extends RuntimeException {
    /**
     * An exception used to signify that something is misused in a terrible way.
     * Also prints said reason.
     * Also crashes the program. If it's called it is probably a good enough reason to do that.
     *
     * @param reason - The reason the exception was thrown.
     */

    public final String reason;

    public GBDYouIdiotException(String reason) {
        this.reason = reason;
        System.out.println(reason);
    }
}
