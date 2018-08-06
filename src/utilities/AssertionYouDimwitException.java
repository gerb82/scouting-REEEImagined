package utilities;

public class AssertionYouDimwitException extends RuntimeException{

    public final String reason;

    public AssertionYouDimwitException(String reason){
        this.reason = reason;
    }
}
