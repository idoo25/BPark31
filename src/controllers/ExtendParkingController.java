package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import client.BParkClientApp;
import entities.Message;
import entities.Message.MessageType;

public class ExtendParkingController {
	
	private static ExtendParkingController instance;
	private SubscriberController parentController;

	public ExtendParkingController() {
	    instance = this;
	}

	public static ExtendParkingController getInstance() {
	    return instance;
	}
	
	public void setParentController(SubscriberController controller) {
	    this.parentController = controller;
	}
	
	public void onExtensionSuccess() {
	    statusLabel.setText("Parking extended successfully.");

	    if (parentController != null) {
	        parentController.disableExtendButton();  // disables left menu button
	    }
	}


    @FXML private TextField codeField;
    @FXML private ComboBox<String> hoursCombo;
    @FXML private Label statusLabel;
    @FXML private Button extendButton;

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