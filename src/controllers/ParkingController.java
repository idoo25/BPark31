package controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import entities.ParkingOrder;
import entities.ParkingSubscriber;
import server.DBController;
import services.UserService;
import services.ParkingSpotService;
import services.ReservationService;
import services.ValidationService;
import services.NotificationService;
import controllers.SimpleAutoCancellationService;

/**
 * ParkingController handles parking operations coordination.
 * Refactored to follow Single Responsibility Principle by delegating to service layer.
 */
public class ParkingController {

    private static final int TOTAL_PARKING_SPOTS = 10;
    private static final double RESERVATION_THRESHOLD = 0.4;
    
    private SimpleAutoCancellationService autoCancellationService;
    public int successFlag;
    
    /**
     * Initializes the parking controller.
     */
    public ParkingController() {
        autoCancellationService = new SimpleAutoCancellationService(this);
        ParkingSpotService.getInstance().initializeParkingSpots();
        successFlag = 1;
    }
    
    /**
     * Initializes the parking controller with database connection.
     * @param dbname Database name
     * @param pass Database password
     */
    public ParkingController(String dbname, String pass) {
        DBController.initializeConnection(dbname, pass);
        autoCancellationService = new SimpleAutoCancellationService(this);
        ParkingSpotService.getInstance().initializeParkingSpots();
        successFlag = 1;
    }

    /**
     * User role enumeration for access control.
     */
    public enum UserRole {
        SUBSCRIBER("sub"),
        ATTENDANT("emp"), 
        MANAGER("mng");
        
        private final String dbValue;
        
        UserRole(String dbValue) {
            this.dbValue = dbValue;
        }
        
        public String getDbValue() {
            return dbValue;
        }
        
        public static UserRole fromDbValue(String dbValue) {
            for (UserRole role : values()) {
                if (role.dbValue.equals(dbValue)) {
                    return role;
                }
            }
            return null;
        }
    }

    /**
     * Starts the automatic cancellation monitoring service.
     */
    public void startAutoCancellationService() {
        if (autoCancellationService != null) {
            autoCancellationService.startService();
            System.out.println("✅ Auto-monitoring service started");
        }
    }

    /**
     * Stops the automatic cancellation monitoring service.
     */
    public void stopAutoCancellationService() {
        if (autoCancellationService != null) {
            autoCancellationService.stopService();
            System.out.println("⛔ Auto-monitoring service stopped");
        }
    }

    /**
     * Checks user login credentials.
     * @param userName Username to check
     * @param userCode User code/ID
     * @return User type if valid, "None" if invalid
     */
    public String checkLogin(String userName, String userCode) {
        Connection conn = DBController.getInstance().getConnection();
        String qry = "SELECT UserTypeEnum FROM users WHERE UserName = ? AND User_ID = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(qry)) {
            stmt.setString(1, userName);
            stmt.setString(2, userCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("UserTypeEnum");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking login: " + e.getMessage());
        } finally {
            DBController.getInstance().releaseConnection(conn);
        }
        return "None";
    }

    /**
     * Gets user information by username.
     * @param userName Username to retrieve
     * @return ParkingSubscriber object or null if not found
     */
    public ParkingSubscriber getUserInfo(String userName) {
        return UserService.getInstance().getSubscriberByUserName(userName);
    }

    /**
     * Gets subscriber by name.
     * @param name Name to search
     * @return ParkingSubscriber object or null if not found
     */
    public ParkingSubscriber getSubscriberByName(String name) {
        return UserService.getInstance().getSubscriberByName(name);
    }

    /**
     * Checks if reservation is possible based on availability threshold.
     * @return true if reservation can be made, false otherwise
     */
    public boolean canMakeReservation() {
        int availableSpots = getAvailableParkingSpots();
        return availableSpots >= (TOTAL_PARKING_SPOTS * RESERVATION_THRESHOLD);
    }

    /**
     * Makes a parking reservation.
     * @param userName Username making reservation
     * @param reservationDateTimeStr Date and time string for reservation
     * @return Success message or error description
     */
    public String makeReservation(String userName, String reservationDateTimeStr) {
        if (!canMakeReservation()) {
            return "Reservations not available - insufficient parking spots";
        }

        ParkingSubscriber subscriber = UserService.getInstance().getSubscriberByUserName(userName);
        if (subscriber == null) {
            return "User not found";
        }

        try {
            String[] parts = reservationDateTimeStr.split(",");
            if (parts.length != 3) {
                return "Invalid date/time format";
            }
            
            LocalDate date = LocalDate.parse(parts[0].trim());
            LocalTime startTime = LocalTime.parse(parts[1].trim());
            LocalTime endTime = LocalTime.parse(parts[2].trim());
            
            int reservationCode = ReservationService.getInstance().createReservation(
                subscriber.getSubscriberID(), date, startTime, endTime);
            
            if (reservationCode > 0) {
                return "Reservation successful. Code: " + reservationCode;
            } else {
                return "Reservation failed";
            }
        } catch (Exception e) {
            System.err.println("Error making reservation: " + e.getMessage());
            return "Invalid date/time format";
        }
    }

    /**
     * Cancels a parking reservation.
     * @param subscriberUserName Username requesting cancellation
     * @param reservationCode Reservation code to cancel
     * @return Success message or error description
     */
    public String cancelReservation(String subscriberUserName, int reservationCode) {
        return ReservationService.getInstance().cancelReservation(reservationCode, "User requested cancellation");
    }

    /**
     * Extends parking session.
     * @param parkingCodeStr Parking code as string
     * @param additionalHours Hours to extend
     * @return Success message or error description
     */
    public String requestParkingExtension(String parkingCodeStr, int additionalHours) {
        if (!ValidationService.getInstance().isValidParkingCode(parkingCodeStr)) {
            return "Invalid parking code";
        }
        
        int parkingCode = Integer.parseInt(parkingCodeStr.trim());
        return ReservationService.getInstance().extendParking(parkingCode, additionalHours);
    }

    /**
     * Handles car entry to parking.
     * @param userID User ID entering parking
     * @return Entry result message
     */
    public String enterParking(int userID) {
        return ReservationService.getInstance().enterParking(userID);
    }

    /**
     * Handles car exit from parking.
     * @param parkingCode Parking code for exit
     * @return Exit result message
     */
    public String retrieveCarByCode(int parkingCode) {
        return ReservationService.getInstance().exitParking(parkingCode);
    }

    /**
     * Sends lost parking code to user email.
     * @param userName Username requesting lost code
     * @return Success message or error description
     */
    public String sendLostParkingCode(String userName) {
        Connection conn = DBController.getInstance().getConnection();
        String query = """
                SELECT pi.Code, u.Email, u.Name
                FROM parkinginfo pi
                JOIN users u ON pi.User_ID = u.User_ID
                WHERE u.UserName = ? AND pi.statusEnum = 'active' AND pi.Actual_end_time IS NULL
                """;
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, userName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int parkingCode = rs.getInt("Code");
                    String email = rs.getString("Email");
                    String name = rs.getString("Name");
                    
                    NotificationService.getInstance().sendParkingCodeRecovery(email, name, parkingCode);
                    return "Parking code sent to your email";
                }
            }
        } catch (SQLException e) {
            System.err.println("Error sending lost code: " + e.getMessage());
        } finally {
            DBController.getInstance().releaseConnection(conn);
        }
        
        return "No active parking session found";
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
     * Registers a new subscriber.
     * @param name Full name
     * @param phone Phone number
     * @param email Email address
     * @param carNumber Car license number
     * @param userName Username
     * @return Success message or error description
     */
    public String registerNewSubscriber(String name, String phone, String email, String carNumber, String userName) {
        return UserService.getInstance().registerNewSubscriber(name, phone, email, carNumber, userName);
    }

    /**
     * Updates subscriber information.
     * @param updateData Comma-separated update data (userName,phone,email)
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
     * Gets user role from database.
     * @param userName Username to check
     * @return UserRole or null if not found
     */
    private UserRole getUserRole(String userName) {
        Connection conn = DBController.getInstance().getConnection();
        String qry = "SELECT UserTypeEnum FROM users WHERE UserName = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(qry)) {
            stmt.setString(1, userName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String userType = rs.getString("UserTypeEnum");
                    return UserRole.fromDbValue(userType);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting user role: " + e.getMessage());
        } finally {
            DBController.getInstance().releaseConnection(conn);
        }
        return null;
    }

    /**
     * Checks if user has required role.
     * @param userName Username to check
     * @param requiredRole Required role
     * @return true if user has role, false otherwise
     */
    private boolean hasRole(String userName, UserRole requiredRole) {
        UserRole userRole = getUserRole(userName);
        return userRole == requiredRole;
    }

    /**
     * Gets user name by username and user ID.
     * @param userName Username to search for
     * @param userID User ID to match
     * @return User's full name or null if not found
     */
    public String getNameByUsernameAndUserID(String userName, int userID) {
        return UserService.getInstance().getNameByUsernameAndUserID(userName, userID);
    }
    
    /**
     * Gets user name by user ID.
     * @param userID User ID to search for
     * @return User's full name or null if not found
     */
    public String getNameByUserID(int userID) {
        return UserService.getInstance().getNameByUserID(userID);
    }
    
    /**
     * Gets all subscribers in the system.
     * @return List of all parking subscribers
     */
    public ArrayList<ParkingSubscriber> getAllSubscribers() {
        return UserService.getInstance().getAllSubscribers();
    }
    
    /**
     * Initializes parking spots in the database.
     */
    public void initializeParkingSpots() {
        ParkingSpotService.getInstance().initializeParkingSpots();
    }
    
    /**
     * Enters parking with an existing reservation.
     * @param reservationID Reservation ID to activate
     * @return Success message or error description
     */
    public String enterParkingWithReservation(int reservationID) {
        return ReservationService.getInstance().enterParkingWithReservation(reservationID);
    }
    
    /**
     * Extends parking time for an active session.
     * @param parkingCodeStr Parking code as string
     * @param additionalHours Hours to extend
     * @return Success message or error description
     */
    public String extendParkingTime(String parkingCodeStr, int additionalHours) {
        return requestParkingExtension(parkingCodeStr, additionalHours);
    }
    
    /**
     * Sends lost parking code to user email (by user ID).
     * @param userID User ID requesting lost code
     * @return Success message or error description
     */
    public String sendLostParkingCode(int userID) {
        return UserService.getInstance().sendLostParkingCodeByUserID(userID);
    }
    
    /**
     * Registers a new subscriber (overloaded with 6 parameters).
     * @param attendantUserName Username of attendant registering subscriber
     * @param name Full name
     * @param phone Phone number
     * @param email Email address
     * @param carNumber Car license number
     * @param userName Username
     * @return Success message or error description
     */
    public String registerNewSubscriber(String attendantUserName, String name, String phone, String email, String carNumber, String userName) {
        // Delegate to the 5-parameter version, ignoring attendant for now
        return registerNewSubscriber(name, phone, email, carNumber, userName);
    }

    /**
     * Cleanup method - call when shutting down the controller.
     */
    public void shutdown() {
        if (autoCancellationService != null) {
            autoCancellationService.shutdown();
        }
    }
}