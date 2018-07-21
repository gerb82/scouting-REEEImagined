package utilities;

public class YouIdiotException extends RuntimeException {

    /**
     * An exception used to signify that something is misused in a terrible way.
     * Also prints said reason.
     * @param reason - The reason the exception was thrown.
     */
    public YouIdiotException(String reason){
        System.out.println(reason);
    }
}
