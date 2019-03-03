package connectionIndependent.Scrawings;

import javafx.scene.image.Image;
import javafx.util.Callback;

public class EmbeddedImage extends Image {

    private static Callback<String, String> urlObtainer;

    public static void setUrlObtainer(Callback<String, String> obtainer){
        urlObtainer = obtainer;
    }

    public EmbeddedImage(String url) {
        super(urlObtainer.call(url));
    }
}
