package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * BPark Launcher - Main entry point for the BPark system.
 * Allows users to choose between Client Portal and Kiosk Terminal modes.
 */
public class BParkLauncher extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            primaryStage.setTitle("BPark System Launcher");
            
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/Launcher.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/BParkStyle.css").toExternalForm());
            
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}