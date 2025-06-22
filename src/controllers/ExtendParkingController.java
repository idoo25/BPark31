package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import client.BParkClientApp;
import entities.Message;
import entities.Message.MessageType;

public class ExtendParkingController {

    @FXML private TextField codeField;
    @FXML private ComboBox<String> hoursCombo;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        hoursCombo.getItems().addAll("1", "2", "3", "4");
        hoursCombo.setValue("1");
    }

    @FXML
    private void handleSubmit() {
        String code = codeField.getText();
        String hours = hoursCombo.getValue();

        if (code == null || code.trim().isEmpty()) {
            statusLabel.setText("Please enter a valid code.");
            return;
        }

        String extensionData = code + "," + hours;
        Message msg = new Message(MessageType.REQUEST_EXTENSION, extensionData);
        BParkClientApp.sendMessage(msg);
        //statusLabel.setText("Extension request sent.");
    }
    
    
}