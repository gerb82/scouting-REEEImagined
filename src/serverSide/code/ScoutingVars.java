package serverSide.code;

import gbuiLib.ProgramWideVariable;

import java.io.File;

public class ScoutingVars {

    public static void initialize(ProgramWideVariable.InsertMethod inserter){
        File directory = Main.runningDirectory;
        inserter.insert(Variables.DIRECTORIES_MAIN.toString(), directory, true);
        inserter.insert(Variables.DIRECTORIES_VIDEOS.toString(), new File(directory, "videos"), true);
        inserter.insert(Variables.DIRECTORIES_DATABASE.toString(), new File(directory, "database"), true);
        inserter.insert(Variables.ALLIANCEEVENTS.toString(), false, true);
    }

    public enum Variables{
        DIRECTORIES_MAIN,
        DIRECTORIES_VIDEOS,
        DIRECTORIES_DATABASE,

        ALLIANCEEVENTS
    }

    public static File getMainDirectory(){
        return ProgramWideVariable.getFinalVariableSafe(Variables.DIRECTORIES_MAIN.toString(), File.class);
    }

    public static File getVideosDirectory(){
        return ProgramWideVariable.getFinalVariableSafe(Variables.DIRECTORIES_VIDEOS.toString(), File.class);
    }

    public static File getDatabaseDirectory(){
        return ProgramWideVariable.getFinalVariableSafe(Variables.DIRECTORIES_DATABASE.toString(), File.class);
    }

    public static boolean allowAllianceEvents(){
        return ProgramWideVariable.getFinalVariableSafe(Variables.ALLIANCEEVENTS.toString(), Boolean.class);
    }
}
