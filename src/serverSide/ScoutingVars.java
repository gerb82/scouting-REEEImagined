package serverSide;

import gbuiLib.ProgramWideVariable;

import java.io.File;

public class ScoutingVars {

    public static void initialize(ProgramWideVariable.InsertMethod inserter){
        File directory = Main.runningDirectory;
        inserter.insert(Variables.DIRECTORIES_MAIN.toString(), directory, true);
        inserter.insert(Variables.DIRECTORIES_VIDEOS.toString(), new File(directory, "videos"), true);
        inserter.insert(Variables.DIRECTORIES_DATABASE.toString(), new File(directory, "database"), true);
        inserter.insert(Variables.DIRECTORIES_CONFIG.toString(), new File(directory, "config"), true);
    }

    public enum Variables{
        DIRECTORIES_MAIN,
        DIRECTORIES_VIDEOS,
        DIRECTORIES_DATABASE,
        DIRECTORIES_CONFIG,

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

    public static File getConfigDirectory(){
        return ProgramWideVariable.getFinalVariableSafe(Variables.DIRECTORIES_CONFIG.toString(), File.class);
    }
}
