package serverSide.code;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

public class MainController {
    @FXML
    private Button submit;

    @FXML
    private TextArea message;

    @FXML
    private void submitMessage(Event event){
        System.out.println(message.getText());
        event.consume();
    }
}
