package gbuiLib;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Utils {

//    public static void warn(String string){
//        ConsoleManager.warn(string);
//    }

    public static void print(Object message){
        System.out.println(message);
    }

    public enum SpecialChars{
        TAB, SPACE, NEW_LINE, TEXT, INVALID, BACK_SPACE, NULL, DELETE
    }

    public static String instantToTimestamp(Instant inst, boolean includeSecs){
        LocalDateTime time = LocalDateTime.ofInstant(inst, ZoneId.systemDefault());
        return String.format("%d of %s %d at %d-%d", time.getDayOfMonth(), time.getMonth() ,time.getYear(), time.getHour(), time.getMinute()) + (includeSecs ? "_" + time.getSecond() : "");
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
