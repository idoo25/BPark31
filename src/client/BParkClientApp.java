package client;

import controllers.AttendantController;
import controllers.ExtendParkingController;
import controllers.ManagerController;
import controllers.SubscriberController;
import controllers.UpdateProfileController;
import entities.Message;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;



public class BParkClientApp extends BParkBaseApp {
    
  	private static AttendantController attendantController;
	  private static ManagerController managerController;
	
    // Current user info
    private static String currentUser;
    private static String userType; // "sub", "emp", "mng"
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Start with login screen
        showLoginScreen(primaryStage);
    }
    
    public static UpdateProfileController getUpdateProfileController() {
        return UpdateProfileController.instance;
    }
    
    public static ExtendParkingController getExtendParkingController() {
        return ExtendParkingController.instance;
    }
    
    private void showLoginScreen(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/Login.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/BParkStyle.css").toExternalForm());
        stage.setTitle("BPark - Login");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
    
    public static void connectToServer() {
        BParkBaseApp.connectToServer();
    }
    
    public static boolean isConnected() {
        return BParkBaseApp.isConnected();
    }
    
    public static void setServerIP(String ip) {
        BParkBaseApp.setServerIP(ip);
    }
    
    public static void switchToMainScreen(String userType) {
        try {
            Stage stage = new Stage();
            Parent root = null;
            
            switch (userType) {
            case "sub":
            	FXMLLoader subLoader = new FXMLLoader(BParkClientApp.class.getResource("/client/SubscriberMain.fxml"));
            	root = subLoader.load();
            	SubscriberController controller = subLoader.getController();

            	// ✅ Move loadHomeView AFTER controller is fully loaded
            	Platform.runLater(controller::loadHomeView);

                // Set the user name in the bottom label
                controller.setUserName(getCurrentUser());

                stage.setTitle("BPark - Subscriber Portal");
                break;
                
                    
                case "emp":
                	FXMLLoader empLoader = new FXMLLoader(BParkClientApp.class.getResource("/client/AttendantMain.fxml"));
                	root = empLoader.load();
                	AttendantController attendantController = empLoader.getController(); 
                	attendantController.setUserName(getCurrentUser());
                	stage.setTitle("BPark - Attendant Portal");
                    break;
                    
                case "mng":
                    FXMLLoader mngLoader = new FXMLLoader(BParkClientApp.class.getResource("/client/ManagerMain.fxml"));
                    root = mngLoader.load();
                    stage.setTitle("BPark - Manager Portal");
                    break;
            }
            
            if (root != null) {
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Utility methods for sending messages
    public static void sendMessage(Message msg) {
        BParkBaseApp.sendMessage(msg);
    }
    
    public static void sendStringMessage(String msg) {
        BParkBaseApp.sendStringMessage(msg);
    }
    
    // Getters and setters
    
    public static BParkClient getClient() {
        return (BParkClient) BParkBaseApp.getClient();
    } 
    public static String getCurrentUser() {
        return currentUser;
    }
    
    public static void setCurrentUser(String user) {
        currentUser = user;
    }
    
    public static String getUserType() {
        return userType;
    }
    
    public static void setUserType(String type) {
        userType = type;
    }
    
    @Override
    public void stop() throws Exception {
        // Clean up when application closes
        super.stop();
    }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
  	
	public static void setAttendantController(AttendantController controller) {
		attendantController = controller;
	}

	public static AttendantController getAttendantController() {
		return attendantController;
	}

	public static void setManagerController(controllers.ManagerController controller) {
		managerController = controller;
	}

	public static controllers.ManagerController getManagerController() {
		return managerController;
	}
}