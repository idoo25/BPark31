package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

import client.BParkClientApp;
import entities.Message;
import entities.Message.MessageType;
import entities.ParkingOrder;

public class SubscriberController implements Initializable {

    // Main menu buttons
    @FXML private Button btnCheckAvailability;
    @FXML private Button btnMakeReservation;
    @FXML private Button btnViewHistory;
    @FXML private Button btnLostCode;
    @FXML private Button btnUpdateProfile;
    @FXML private Button btnLogout;

    // Reservation controls
    @FXML private DatePicker datePickerReservation;
    @FXML private ComboBox<String> comboTimeSlot;
    @FXML private Label lblReservationStatus;

    // Parking history table
    @FXML private TableView<ParkingOrder> tableParkingHistory;
    @FXML private TableColumn<ParkingOrder, String> colDate;
    @FXML private TableColumn<ParkingOrder, String> colEntry;
    @FXML private TableColumn<ParkingOrder, String> colExit;
    @FXML private TableColumn<ParkingOrder, String> colSpot;
    @FXML private TableColumn<ParkingOrder, String> colStatus;

    // Profile update
    @FXML private TextField txtPhone;
    @FXML private TextField txtEmail;

    // Status label
    @FXML private Label lblAvailableSpots;

    // Current view container
    @FXML private VBox mainContent;

    private ObservableList<ParkingOrder> parkingHistory = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupUI();
        loadInitialData();
    }

    private void setupUI() {
        // Initialize time slots for reservation (15-minute intervals)
        ObservableList<String> timeSlots = FXCollections.observableArrayList();
        for (int hour = 6; hour <= 22; hour++) {
            for (int minute = 0; minute < 60; minute += 15) {
                timeSlots.add(String.format("%02d:%02d", hour, minute));
            }
        }
        if (comboTimeSlot != null) {
            comboTimeSlot.setItems(timeSlots);
        }

        // Set date picker constraints (1-7 days from today)
        if (datePickerReservation != null) {
            datePickerReservation.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    LocalDate today = LocalDate.now();
                    setDisable(empty || date.isBefore(today.plusDays(1)) || date.isAfter(today.plusDays(7)));
                }
            });
        }

        // Setup parking history table
        if (tableParkingHistory != null) {
            tableParkingHistory.setItems(parkingHistory);
            // Setup column cell value factories here
        }
    }

    private void loadInitialData() {
        // Check parking availability on startup
        checkParkingAvailability();
    }

    // ===== Action Handlers =====

    @FXML
    private void checkParkingAvailability() {
        Message msg = new Message(MessageType.CHECK_PARKING_AVAILABILITY, null);
        BParkClientApp.sendMessage(msg);
    }

    @FXML
    private void handleMakeReservation() {
        LocalDate selectedDate = datePickerReservation.getValue();
        String selectedTime = comboTimeSlot.getValue();

        if (selectedDate == null || selectedTime == null) {
            showAlert("Error", "Please select both date and time");
            return;
        }

        String dateTimeStr = selectedDate.toString() + " " + selectedTime;
        String reservationData = BParkClientApp.getCurrentUser() + "," + dateTimeStr;

        Message msg = new Message(MessageType.RESERVE_PARKING, reservationData);
        BParkClientApp.sendMessage(msg);
    }

    @FXML
    private void handleCancelReservation() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Cancel Reservation");
        dialog.setHeaderText("Enter reservation code to cancel:");
        dialog.setContentText("Code:");

        dialog.showAndWait().ifPresent(code -> {
            if (!code.trim().isEmpty()) {
                String cancellationData = BParkClientApp.getCurrentUser() + "," + code;
                Message msg = new Message(MessageType.CANCEL_RESERVATION, cancellationData);
                BParkClientApp.sendMessage(msg);
            }
        });
    }

    @FXML
    private void handleViewHistory() {
        Message msg = new Message(MessageType.GET_PARKING_HISTORY, BParkClientApp.getCurrentUser());
        BParkClientApp.sendMessage(msg);
    }

    @FXML
    private void handleLostCode() {
        Message msg = new Message(MessageType.REQUEST_LOST_CODE, BParkClientApp.getCurrentUser());
        BParkClientApp.sendMessage(msg);
    }

    @FXML
    private void handleExtendParking() {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Extend Parking Time");
        dialog.setHeaderText("Enter parking code and extension hours:");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField codeField = new TextField();
        ComboBox<String> hoursCombo = new ComboBox<>();
        hoursCombo.getItems().addAll("1", "2", "3", "4");
        hoursCombo.setValue("1");

        grid.add(new Label("Parking Code:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("Extension Hours:"), 0, 1);
        grid.add(hoursCombo, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return new String[]{codeField.getText(), hoursCombo.getValue()};
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result[0] != null && !result[0].trim().isEmpty()) {
                String extensionData = result[0] + "," + result[1];
                Message msg = new Message(MessageType.EXTEND_PARKING, extensionData);
                BParkClientApp.sendMessage(msg);
            }
        });
    }

    @FXML
    private void handleUpdateProfile() {
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();

        if (phone.isEmpty() || email.isEmpty()) {
            showAlert("Error", "Please fill in all fields");
            return;
        }

        String updateData = BParkClientApp.getCurrentUser() + "," + phone + "," + email;
        Message msg = new Message(MessageType.UPDATE_SUBSCRIBER_INFO, updateData);
        BParkClientApp.sendMessage(msg);
    }

    @FXML
    private void handleLogout() {
        BParkClientApp.sendStringMessage("LoggedOut " + BParkClientApp.getCurrentUser());

        try {
            btnLogout.getScene().getWindow().hide();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showReservationView() {
        showAlert("Make Reservation", "Select date and time for your reservation");
    }

    @FXML
    private void showProfileView() {
        showAlert("Update Profile", "Update your phone and email information");
    }

    // ===== UI Update Methods =====

    public void updateAvailableSpots(int spots) {
        if (lblAvailableSpots != null) {
            lblAvailableSpots.setText("Available Spots: " + spots);

            boolean canReserve = spots >= (100 * 0.4); // 40% rule
            if (btnMakeReservation != null) {
                btnMakeReservation.setDisable(!canReserve);
            }
            if (lblReservationStatus != null && !canReserve) {
                lblReservationStatus.setText("Reservations unavailable (less than 40% spots free)");
            }
        }
    }

    public void updateParkingHistory(ObservableList<ParkingOrder> history) {
        this.parkingHistory.clear();
        this.parkingHistory.addAll(history);
    }

    // ===== Utility Methods =====

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
