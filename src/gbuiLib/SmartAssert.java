package gbuiLib;

public class SmartAssert {

    public static void makeSure(boolean bully, String why){
        try{
            assert(bully);
        } catch (AssertionError e){
            throw new AssertionYouDimwitException("assertion failed. this is a problem because " + why);
        }
    }
}
