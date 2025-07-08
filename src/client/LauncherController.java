package client;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * Controller for the BPark Launcher interface
 */
public class LauncherController {
    
    @FXML
    private Button clientButton;
    
    @FXML
    private Button kioskButton;
    
    @FXML
    private void launchClientMode() {
        try {
            // Launch client application in new window
            BParkClientApp clientApp = new BParkClientApp();
            Stage clientStage = new Stage();
            clientApp.start(clientStage);
        } catch (Exception e) {
            showError("Failed to launch Client Portal", e.getMessage());
        }
    }
    
    @FXML
    private void launchKioskMode() {
        try {
            // Launch kiosk application in new window
            BParkKioskApp kioskApp = new BParkKioskApp();
            Stage kioskStage = new Stage();
            kioskApp.start(kioskStage);
        } catch (Exception e) {
            showError("Failed to launch Kiosk Terminal", e.getMessage());
        }
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}