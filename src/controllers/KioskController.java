package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.application.Platform;

import java.util.Optional;

import entities.Message;
import static entities.Message.MessageType.*;
import client.BParkKioskApp; // Fixed to match actual client class used

public class KioskController {

    @FXML
    private Button btnLoginByID;

    @FXML
    private Button btnLoginByRF;

    @FXML
    private void handleLoginByID() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Login by ID");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter your username:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(username -> {
            Message msg = new Message(KIOSK_ID_LOGIN, username);
            BParkKioskApp.sendMessage(msg);
        });
    }

    @FXML
    private void handleLoginByRF() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Login by RF");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter your User ID:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(idStr -> {
            try {
                int userID = Integer.parseInt(idStr);
                Message msg = new Message(KIOSK_RF_LOGIN, userID);
                BParkKioskApp.sendMessage(msg);
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid numeric User ID.");
            }
        });
    }

    public static void handleKioskLoginResult(Object content) {
        if (content instanceof String name && !name.equals("")) {
            showAlertStatic("Login Success", "Welcome " + name + "!");
        } else {
            showAlertStatic("Login Failed", "Invalid credentials or user not found.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static void showAlertStatic(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
