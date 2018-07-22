package utilities;

import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class ConsoleController {

    @FXML
    private TextFlow console;

    public void updateConsole(String string){
        console.getChildren().add(new Text(string));
    }
}
