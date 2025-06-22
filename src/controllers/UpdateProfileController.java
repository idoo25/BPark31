package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import client.BParkClientApp;
import entities.Message;
import entities.Message.MessageType;
import entities.ParkingSubscriber;

public class UpdateProfileController {
    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;
    
    @FXML
    private TextField carNumberField;

    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
    	   String userId = BParkClientApp.getCurrentUser();
           Message requestMsg = new Message(MessageType.UPDATE_SUBSCRIBER_INFO, userId);
          // BParkClientApp.sendMessage(requestMsg);
    }
    

@FXML
private void handleUpdate() {
	// Get user input from the text fields
    String email = emailField.getText().trim();
    String phone = phoneField.getText().trim();
    String carNumber = carNumberField.getText().trim();
    String userId = BParkClientApp.getCurrentUser();

    // Check if all fields are empty — if they are empty, don't allow the update
    if (email.isEmpty() && phone.isEmpty() && carNumber.isEmpty()) {
        statusLabel.setText("Please fill in all fields.");
        statusLabel.setStyle("-fx-text-fill: red;");
        return;
    }

    String data = userId  + "," + phone + "," +  email + "," + carNumber;
    // Create a message to send to the server to update subscriber info
    Message msg = new Message(MessageType.UPDATE_SUBSCRIBER_INFO, data);
    BParkClientApp.sendMessage(msg);

    statusLabel.setText("Profile update sent.");
    statusLabel.setStyle("-fx-text-fill: green;");
}

// Add public setters so the ClientMessageHandler can set fields
public void setEmail(String email) {
    emailField.setText(email);
}

public void setPhone(String phone) {
    phoneField.setText(phone);
}


}
