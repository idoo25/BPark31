package client;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import controllers.KioskController;

public class BParkKioskApp extends BParkBaseApp {

    @Override
    public void start(Stage primaryStage) throws Exception {
        connectToServer();
        showKioskScreen(primaryStage);
    }

    private void showKioskScreen(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/KioskMain.fxml"));
        Parent root = loader.load();

        // Set main stage in the controller to allow screen switching
        KioskController.setMainStage(stage);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/BParkStyle.css").toExternalForm());
        stage.setTitle("BPark - Kiosk Terminal");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void connectToServer() {
        BParkBaseApp.connectToServer();
    }

    public static void sendMessage(entities.Message msg) {
        BParkBaseApp.sendMessage(msg);
    }

    public static void sendStringMessage(String msg) {
        BParkBaseApp.sendStringMessage(msg);
    }

    public static void main(String[] args) {
        launch(args);
    }
}