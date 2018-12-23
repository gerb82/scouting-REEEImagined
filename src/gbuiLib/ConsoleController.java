package gbuiLib;

import javafx.fxml.FXML;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class ConsoleController {

    @FXML
    private TextFlow console;

    public void updateConsole(String string, Paint fill){
        Text text = new Text(string);
        text.setFill(fill);
        console.getChildren().add(text);
    }
}
