package controllers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

import entities.ParkingOrder;
import entities.ParkingSubscriber;
import server.DBController;
import services.UserService;
import services.ParkingSpotService;
import services.ReservationService;
import services.ValidationService;

/**
 * SmartParkingController provides enhanced parking allocation with smart algorithms.
 * Refactored to follow Single Responsibility Principle by delegating to service layer.
 */
public class SmartParkingController {
    
    private static final int TOTAL_PARKING_SPOTS = 10;
    private static final double AVAILABILITY_THRESHOLD = 0.4;
    private static final int PREFERRED_WINDOW_HOURS = 8;
    private static final int STANDARD_BOOKING_HOURS = 4;
    private static final int MINIMUM_SPONTANEOUS_HOURS = 2;
    
    public int successFlag;

    /**
     * Initializes the smart parking controller with database connection.
     * @param dbname Database name
     * @param pass Database password
     */
    public SmartParkingController(String dbname, String pass) {
        DBController.initializeConnection(dbname, pass);
        ParkingSpotService.getInstance().initializeParkingSpots();
        successFlag = 1;
    }

    /**
     * Registers a new subscriber in the system.
     * @param name Full name of the subscriber
     * @param phone Phone number
     * @param email Email address
     * @param carNumber Car license number
     * @param userName Unique username
     * @return Success message or error description
     */
    public String registerNewSubscriber(String name, String phone, String email, String carNumber, String userName) {
        return UserService.getInstance().registerNewSubscriber(name, phone, email, carNumber, userName);
    }

    /**
     * Updates subscriber information.
     * @param updateData Comma-separated data: userName,phone,email
     * @return Success message or error description
     */
    public String updateSubscriberInfo(String updateData) {
        String[] data = updateData.split(",");
        if (data.length != 3) {
            return "Invalid update data format";
        }
        
        String userName = data[0];
        String phone = data[1];
        String email = data[2];
        
        return UserService.getInstance().updateSubscriberInfo(userName, phone, email);
    }

    /**
     * Gets subscriber information by username.
     * @param userName Username to retrieve
     * @return ParkingSubscriber object or null if not found
     */
    public ParkingSubscriber getSubscriberByUserName(String userName) {
        return UserService.getInstance().getSubscriberByUserName(userName);
    }

    /**
     * Gets subscriber information by name.
     * @param name Name to search
     * @return ParkingSubscriber object or null if not found
     */
    public ParkingSubscriber getSubscriberByName(String name) {
        return UserService.getInstance().getSubscriberByName(name);
    }

    /**
     * Makes a smart parking reservation with optimized time allocation.
     * @param userName Username making reservation
     * @param preferredDate Preferred parking date
     * @param preferredStartTime Preferred start time
     * @param durationHours Duration in hours
     * @return Reservation result with code or error message
     */
    public String makeSmartReservation(String userName, LocalDate preferredDate, 
                                     LocalTime preferredStartTime, int durationHours) {
        if (!ValidationService.getInstance().isValidDuration(durationHours)) {
            return "Invalid duration. Must be between 1-12 hours.";
        }

        if (!canMakeReservation()) {
            return "Reservations not available - insufficient parking spots";
        }

        ParkingSubscriber subscriber = UserService.getInstance().getSubscriberByUserName(userName);
        if (subscriber == null) {
            return "User not found";
        }

        LocalTime endTime = preferredStartTime.plusHours(durationHours);
        
        int reservationCode = ReservationService.getInstance().createReservation(
            subscriber.getSubscriberID(), preferredDate, preferredStartTime, endTime);
        
        if (reservationCode > 0) {
            return "Smart reservation successful. Code: " + reservationCode;
        } else {
            return "Reservation failed - no spots available";
        }
    }

    /**
     * Cancels a parking reservation.
     * @param reservationCode Reservation code to cancel
     * @return Success message or error description
     */
    public String cancelReservation(int reservationCode) {
        return ReservationService.getInstance().cancelReservation(reservationCode, "User requested cancellation");
    }

    /**
     * Handles car entry to parking with smart spot allocation.
     * @param userID User ID entering parking
     * @return Entry result message with parking code
     */
    public String enterParking(int userID) {
        return ReservationService.getInstance().enterParking(userID);
    }

    /**
     * Handles car exit from parking.
     * @param parkingCodeStr Parking code as string
     * @return Exit result message
     */
    public String exitParking(String parkingCodeStr) {
        if (!ValidationService.getInstance().isValidParkingCode(parkingCodeStr)) {
            return "Invalid parking code format";
        }
        
        int parkingCode = Integer.parseInt(parkingCodeStr.trim());
        return ReservationService.getInstance().exitParking(parkingCode);
    }

    /**
     * Requests parking extension with smart validation.
     * @param parkingCodeStr Parking code as string
     * @param extensionHours Hours to extend
     * @return Extension result message
     */
    public String requestParkingExtension(String parkingCodeStr, int extensionHours) {
        if (!ValidationService.getInstance().isValidParkingCode(parkingCodeStr)) {
            return "Invalid parking code format";
        }
        
        if (!ValidationService.getInstance().isValidExtension(extensionHours)) {
            return "Invalid extension duration. Must be 1-4 hours.";
        }
        
        int parkingCode = Integer.parseInt(parkingCodeStr.trim());
        return ReservationService.getInstance().extendParking(parkingCode, extensionHours);
    }

    /**
     * Sends lost parking code to user's email.
     * @param userName Username requesting lost code
     * @return Success message or error description
     */
    public String sendLostParkingCode(String userName) {
        ParkingSubscriber subscriber = UserService.getInstance().getSubscriberByUserName(userName);
        if (subscriber == null) {
            return "User not found";
        }

        // Delegate to ParkingController for this functionality
        ParkingController parkingController = new ParkingController();
        return parkingController.sendLostParkingCode(userName);
    }

    /**
     * Gets parking history for a user.
     * @param userName Username to get history for
     * @return List of parking orders
     */
    public ArrayList<ParkingOrder> getParkingHistory(String userName) {
        return ReservationService.getInstance().getParkingHistory(userName);
    }

    /**
     * Gets all active parking sessions.
     * @return List of active parking orders
     */
    public ArrayList<ParkingOrder> getActiveParkings() {
        return ReservationService.getInstance().getActiveParkings();
    }

    /**
     * Gets number of available parking spots.
     * @return Number of available spots
     */
    public int getAvailableParkingSpots() {
        return ParkingSpotService.getInstance().getAvailableSpots();
    }

    /**
     * Checks if parking is full.
     * @return true if no spots available, false otherwise
     */
    public boolean isParkingFull() {
        return ParkingSpotService.getInstance().isParkingFull();
    }

    /**
     * Checks if reservations are possible based on availability threshold.
     * @return true if reservations can be made, false otherwise
     */
    public boolean canMakeReservation() {
        int availableSpots = getAvailableParkingSpots();
        return availableSpots >= (TOTAL_PARKING_SPOTS * AVAILABILITY_THRESHOLD);
    }

    /**
     * Initializes parking spots in the database.
     */
    public void initializeParkingSpots() {
        ParkingSpotService.getInstance().initializeParkingSpots();
    }

    /**
     * Calculates optimal parking duration based on historical patterns.
     * @param requestedHours Originally requested hours
     * @return Optimized duration in hours
     */
    public int calculateOptimalDuration(int requestedHours) {
        if (requestedHours <= 0) {
            return MINIMUM_SPONTANEOUS_HOURS;
        }
        
        if (requestedHours > PREFERRED_WINDOW_HOURS) {
            return PREFERRED_WINDOW_HOURS;
        }
        
        return requestedHours;
    }

    /**
     * Provides smart parking recommendations based on current conditions.
     * @param requestedDate Requested parking date
     * @param requestedTime Requested parking time
     * @return Recommendation message
     */
    public String getSmartRecommendation(LocalDate requestedDate, LocalTime requestedTime) {
        int availableSpots = getAvailableParkingSpots();
        
        if (availableSpots == 0) {
            return "No spots available. Try booking for a later time.";
        }
        
        if (availableSpots <= 2) {
            return "Limited spots available. Consider booking immediately.";
        }
        
        if (availableSpots >= 5) {
            return "Good availability. You can book with confidence.";
        }
        
        return "Moderate availability. Book soon to secure your spot.";
    }

    /**
     * Validates smart parking parameters.
     * @param userName Username
     * @param date Parking date
     * @param time Parking time
     * @param duration Duration in hours
     * @return true if all parameters are valid, false otherwise
     */
    public boolean validateSmartParkingParams(String userName, LocalDate date, LocalTime time, int duration) {
        return ValidationService.getInstance().isValidUserName(userName) &&
               date != null && !date.isBefore(LocalDate.now()) &&
               time != null &&
               ValidationService.getInstance().isValidDuration(duration);
    }
}