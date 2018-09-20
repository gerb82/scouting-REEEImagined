package utilities;

public class Utils {

    public static void warn(String string){
        ConsoleManager.warn(string);
    }

    public static void print(Object message){
        System.out.println(message);
    }

    public enum SpecialChars{
        TAB, SPACE, NEW_LINE, TEXT, INVALID, BACK_SPACE, NULL, DELETE
    }

    public static SpecialChars IdentifyChar(char c){
        if(c == 0x7F) return SpecialChars.DELETE;
        if(c > 0x20) return SpecialChars.TEXT;
        switch(c){
            case 0x9:
                return SpecialChars.TAB;
            case 0x8:
                return SpecialChars.BACK_SPACE;
            case 0x20:
                return SpecialChars.SPACE;
            case 0xA:
                return SpecialChars.NEW_LINE;
            case 0x0:
                return SpecialChars.NULL;
            default:
                return SpecialChars.INVALID;
        }
    }
}
