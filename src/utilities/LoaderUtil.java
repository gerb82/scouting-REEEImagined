package utilities;

import javafx.fxml.FXMLLoader;
import javafx.fxml.LoadException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

/**
 * A class containing utilities involving the FXMLLoader that should be used whenever the loader is used.
 */
public class LoaderUtil {

    /**
     * Method used to create an FXMLLoader. Creates the FXMLLoader based on the name of the controller.
     * @param clazz - Given in the form of "ControllerName.class" (without the ").
     * From then, it will find an FXML file with the same name as the controller, without the Controller part.
     * @return - FXMLLoader that has been loaded, with an fxml file location and a controller location set.
     * he file it will look for will not be in the exact same location, and will be in the appropriate resources package.
     * So for example: "side/code/MainController" will use the FXML file in "side/resources/Main.fxml".
     * And: "side/code/sub/SubController" will use the FXML file in side/resources/sub/Sub.fxml".
     * The FXML file must not have a set controller, as it will be set by this method, and the loader will throw an exception otherwise.
     * @throws YouIdiotException - If the controller was set on the FXML file.
     * @throws YouIdiotException - If the associated FXML file does not exist in the specified location
     */
    public static FXMLLoader set(Class clazz) throws YouIdiotException{
        URL path = pathMaker(clazz, "fxml");
        if(path == null){
            throw new YouIdiotException("the FXML file appropriate for the " + clazz.toString() + " does not exist in the right place");
        }
        FXMLLoader loader = new FXMLLoader(path);
        try {
            loader.setController(clazz.getConstructors()[0].newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            loader.load();
        } catch (LoadException e) {
            if(e.getMessage().contains("Controller value already specified")) {
                throw new YouIdiotException("the controller was already set for the file at " + path.toString());
            }
            else{
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return loader;
    }

    /**
     * Method used to get the relative location of the controller to the load location, and then get the location of the requested FXML file/directory.
     * @param clazz - The class of the controller.
     * @param fileType - The type of file to look for. If fileType is empty it will look for a directory.
     * @return - The path relative to the load location of the FXML file.
     */
    private static String resource(Class<?> clazz, String fileType) {
        final String className = clazz.getCanonicalName();
        String output = className.replace('.', '/');
        switch(fileType){
            case "fxml":
                output = output.replace("Controller", "") + ".fxml";
                output = output.replace("code", "resources");
                break;
            case "":
                output = output.replace(clazz.getSimpleName(), "");
                break;
            case "GBDConfig":
                output = output.replace("code", "configuration");
                output = output + ".GBD";
        }
        return output;
    }

    /**
     * Method used to get the absolute location of the load location and add to it the relative location of the requested file.
     * @param clazz - The class of the controller.
     * @param fileType - The type of file to look for. If fileType is empty it will look for a directory.
     * @return - The full path of the FXML file.
     */
    public static URL pathMaker(Class clazz, String fileType){
        String targetFile = resource(clazz, fileType);
        return LoaderUtil.class.getClassLoader().getResource(targetFile);
    }
}
