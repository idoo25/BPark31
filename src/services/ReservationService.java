package services;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Random;

import entities.ParkingOrder;
import entities.ParkingSubscriber;
import server.DBController;

/**
 * ReservationService handles all parking reservation operations following Single Responsibility Principle.
 * Manages parking reservations, cancellations, extensions, and parking session lifecycle.
 */
public class ReservationService {
    
    private static ReservationService instance;
    private static final int MINIMUM_EXTENSION_HOURS = 2;
    private static final int MAXIMUM_EXTENSION_HOURS = 4;
    private static final Random random = new Random();
    
    /**
     * Private constructor for singleton pattern.
     */
    private ReservationService() {}
    
    /**
     * Returns singleton instance of ReservationService.
     * @return ReservationService instance
     */
    public static synchronized ReservationService getInstance() {
        if (instance == null) {
            instance = new ReservationService();
        }
        return instance;
    }
    
    /**
     * Creates a new parking reservation.
     * @param userID User ID making the reservation
     * @param parkingDate Date of parking
     * @param startTime Start time
     * @param endTime End time
     * @return Reservation code if successful, negative value if failed
     */
    public int createReservation(int userID, LocalDate parkingDate, LocalTime startTime, LocalTime endTime) {
        if (ParkingSpotService.getInstance().isParkingFull()) {
            return -1; // No spots available
        }
        
        int spotId = ParkingSpotService.getInstance().allocateSpot();
        if (spotId == -1) {
            return -2; // Failed to allocate spot
        }
        
        Connection conn = DBController.getInstance().getConnection();
        String insertQuery = """
                INSERT INTO parkinginfo (User_ID, ParkingSpot_ID, Date, Start_time, Estimated_end_time, 
                                       Entry_time, statusEnum, ReservationType, IsExtended) 
                VALUES (?, ?, ?, ?, ?, NOW(), 'active', 'pre_order', 'no')
                """;
        
        try (PreparedStatement stmt = conn.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, userID);
            stmt.setInt(2, spotId);
            stmt.setDate(3, Date.valueOf(parkingDate));
            stmt.setTime(4, Time.valueOf(startTime));
            stmt.setTime(5, Time.valueOf(endTime));
            
            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int reservationId = generatedKeys.getInt(1);
                        
                        // Generate and update parking code
                        int code = generateParkingCode();
                        updateParkingCode(reservationId, code);
                        
                        // Send confirmation email
                        ParkingSubscriber subscriber = UserService.getInstance().getSubscriberByUserName(String.valueOf(userID));
                        if (subscriber != null) {
                            NotificationService.getInstance().sendReservationConfirmation(
                                subscriber.getEmail(), 
                                subscriber.getFirstName(), 
                                code,
                                parkingDate.toString(),
                                startTime.toString(),
                                endTime.toString()
                            );
                        }
                        
                        return code;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating reservation: " + e.getMessage());
            // Release the allocated spot on failure
            ParkingSpotService.getInstance().releaseSpot(spotId);
        } finally {
            DBController.getInstance().releaseConnection(conn);
        }
        
        return -3; // Database error
    }
    
    /**
     * Cancels a parking reservation.
     * @param reservationCode Reservation code to cancel
     * @param reason Cancellation reason
     * @return Success message or error description
     */
    public String cancelReservation(int reservationCode, String reason) {
        Connection conn = DBController.getInstance().getConnection();
        String selectQuery = """
                SELECT pi.*, u.Email, u.Name
                FROM parkinginfo pi
                JOIN users u ON pi.User_ID = u.User_ID
                WHERE pi.Code = ? AND pi.statusEnum = 'active'
                """;
        
        try (PreparedStatement selectStmt = conn.prepareStatement(selectQuery)) {
            selectStmt.setInt(1, reservationCode);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    int parkingInfoId = rs.getInt("ParkingInfo_ID");
                    int spotId = rs.getInt("ParkingSpot_ID");
                    String userEmail = rs.getString("Email");
                    String userName = rs.getString("Name");
                    
                    // Update reservation status
                    String updateQuery = """
                            UPDATE parkinginfo 
                            SET statusEnum = 'cancelled', Actual_end_time = NOW() 
                            WHERE ParkingInfo_ID = ?
                            """;
                    
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setInt(1, parkingInfoId);
                        updateStmt.executeUpdate();
                    }
                    
                    // Release parking spot
                    ParkingSpotService.getInstance().releaseSpot(spotId);
                    
                    // Send cancellation email
                    NotificationService.getInstance().sendReservationCancellation(userEmail, userName, reservationCode);
                    
                    return "Reservation cancelled successfully";
                }
            }
        } catch (SQLException e) {
            System.err.println("Error cancelling reservation: " + e.getMessage());
        } finally {
            DBController.getInstance().releaseConnection(conn);
        }
        
        return "Reservation not found or already cancelled";
    }
    
    /**
     * Extends a parking session.
     * @param parkingCode Parking code to extend
     * @param additionalHours Hours to extend
     * @return Success message or error description
     */
    public String extendParking(int parkingCode, int additionalHours) {
        if (!ValidationService.getInstance().isValidExtension(additionalHours)) {
            return "Invalid extension duration";
        }
        
        Connection conn = DBController.getInstance().getConnection();
        String selectQuery = """
                SELECT pi.*, u.Email, u.Name
                FROM parkinginfo pi
                JOIN users u ON pi.User_ID = u.User_ID
                WHERE pi.Code = ? AND pi.statusEnum = 'active' AND pi.Actual_end_time IS NULL
                """;
        
        try (PreparedStatement selectStmt = conn.prepareStatement(selectQuery)) {
            selectStmt.setInt(1, parkingCode);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp currentEstimatedEnd = rs.getTimestamp("Estimated_end_time");
                    String userEmail = rs.getString("Email");
                    String userName = rs.getString("Name");
                    int parkingInfoId = rs.getInt("ParkingInfo_ID");
                    
                    LocalDateTime newEstimatedEnd = currentEstimatedEnd.toLocalDateTime()
                                                        .plusHours(additionalHours);
                    
                    String updateQuery = """
                            UPDATE parkinginfo
                            SET Estimated_end_time = ?, IsExtended = 'yes'
                            WHERE ParkingInfo_ID = ?
                            """;
                    
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setTimestamp(1, Timestamp.valueOf(newEstimatedEnd));
                        updateStmt.setInt(2, parkingInfoId);
                        
                        int rowsUpdated = updateStmt.executeUpdate();
                        if (rowsUpdated > 0) {
                            // Send extension confirmation email
                            NotificationService.getInstance().sendExtensionConfirmation(
                                userEmail, 
                                userName, 
                                parkingCode, 
                                newEstimatedEnd.toString()
                            );
                            
                            return "Parking extended successfully until " + newEstimatedEnd.toLocalTime();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error extending parking: " + e.getMessage());
        } finally {
            DBController.getInstance().releaseConnection(conn);
        }
        
        return "Invalid parking code or parking session not found";
    }
    
    /**
     * Handles car entry to parking.
     * @param userID User ID entering parking
     * @return Entry result message with parking code
     */
    public String enterParking(int userID) {
        if (ParkingSpotService.getInstance().isParkingFull()) {
            return "PARKING_FULL";
        }
        
        int spotId = ParkingSpotService.getInstance().allocateSpot();
        if (spotId == -1) {
            return "NO_SPOT_AVAILABLE";
        }
        
        Connection conn = DBController.getInstance().getConnection();
        String insertQuery = """
                INSERT INTO parkinginfo (User_ID, ParkingSpot_ID, Date, Start_time, Estimated_end_time, 
                                       Entry_time, statusEnum, ReservationType, IsExtended) 
                VALUES (?, ?, CURDATE(), CURTIME(), ADDTIME(CURTIME(), '04:00:00'), NOW(), 'active', 'spontaneous', 'no')
                """;
        
        try (PreparedStatement stmt = conn.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, userID);
            stmt.setInt(2, spotId);
            
            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int parkingInfoId = generatedKeys.getInt(1);
                        int code = generateParkingCode();
                        updateParkingCode(parkingInfoId, code);
                        
                        return "ENTRY_SUCCESS:" + code;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error handling entry: " + e.getMessage());
            ParkingSpotService.getInstance().releaseSpot(spotId);
        } finally {
            DBController.getInstance().releaseConnection(conn);
        }
        
        return "ENTRY_FAILED";
    }
    
    /**
     * Handles car exit from parking.
     * @param parkingCode Parking code for exit
     * @return Exit result message
     */
    public String exitParking(int parkingCode) {
        Connection conn = DBController.getInstance().getConnection();
        String selectQuery = """
                SELECT pi.*, ps.ParkingSpot_ID 
                FROM ParkingInfo pi 
                JOIN ParkingSpot ps ON pi.ParkingSpot_ID = ps.ParkingSpot_ID 
                WHERE pi.Code = ? AND pi.Actual_end_time IS NULL
                """;
        
        try (PreparedStatement selectStmt = conn.prepareStatement(selectQuery)) {
            selectStmt.setInt(1, parkingCode);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    int parkingInfoID = rs.getInt("ParkingInfo_ID");
                    int spotID = rs.getInt("ParkingSpot_ID");
                    Time estimatedEndTime = rs.getTime("Estimated_end_time");
                    
                    LocalTime now = LocalTime.now();
                    LocalTime estimatedEnd = estimatedEndTime.toLocalTime();
                    
                    // Update parking session as completed
                    String updateQuery = """
                            UPDATE parkinginfo 
                            SET Actual_end_time = NOW(), statusEnum = 'completed' 
                            WHERE ParkingInfo_ID = ?
                            """;
                    
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setInt(1, parkingInfoID);
                        updateStmt.executeUpdate();
                    }
                    
                    // Release parking spot
                    ParkingSpotService.getInstance().releaseSpot(spotID);
                    
                    // Check if exit is late
                    if (now.isAfter(estimatedEnd)) {
                        return "EXIT_LATE";
                    } else {
                        return "EXIT_SUCCESS";
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error handling exit: " + e.getMessage());
        } finally {
            DBController.getInstance().releaseConnection(conn);
        }
        
        return "INVALID_CODE";
    }
    
    /**
     * Retrieves parking history for a user.
     * @param userName Username to get history for
     * @return List of parking orders
     */
    public ArrayList<ParkingOrder> getParkingHistory(String userName) {
        ArrayList<ParkingOrder> history = new ArrayList<>();
        Connection conn = DBController.getInstance().getConnection();
        String query = """
                SELECT pi.*, u.Name, u.Email, u.Phone, u.CarNum
                FROM parkinginfo pi
                JOIN users u ON pi.User_ID = u.User_ID
                WHERE u.UserName = ?
                ORDER BY pi.Entry_time DESC
                """;
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, userName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ParkingOrder order = createParkingOrderFromResultSet(rs);
                    history.add(order);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting parking history: " + e.getMessage());
        } finally {
            DBController.getInstance().releaseConnection(conn);
        }
        
        return history;
    }
    
    /**
     * Retrieves all active parking sessions.
     * @return List of active parking orders
     */
    public ArrayList<ParkingOrder> getActiveParkings() {
        ArrayList<ParkingOrder> activeParkings = new ArrayList<>();
        Connection conn = DBController.getInstance().getConnection();
        String query = """
                SELECT pi.*, u.Name, u.Email, u.Phone, u.CarNum
                FROM parkinginfo pi
                JOIN users u ON pi.User_ID = u.User_ID
                WHERE pi.statusEnum = 'active'
                ORDER BY pi.Entry_time DESC
                """;
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ParkingOrder order = createParkingOrderFromResultSet(rs);
                    activeParkings.add(order);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting active parkings: " + e.getMessage());
        } finally {
            DBController.getInstance().releaseConnection(conn);
        }
        
        return activeParkings;
    }
    
    /**
     * Generates a unique parking code.
     * @return Generated parking code
     */
    private int generateParkingCode() {
        return 100000 + random.nextInt(900000); // 6-digit code
    }
    
    /**
     * Updates parking code in database.
     * @param parkingInfoId Parking info ID
     * @param code Generated code
     */
    private void updateParkingCode(int parkingInfoId, int code) {
        Connection conn = DBController.getInstance().getConnection();
        String updateQuery = "UPDATE parkinginfo SET Code = ? WHERE ParkingInfo_ID = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
            stmt.setInt(1, code);
            stmt.setInt(2, parkingInfoId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating parking code: " + e.getMessage());
        } finally {
            DBController.getInstance().releaseConnection(conn);
        }
    }
    
    /**
     * Creates ParkingOrder object from ResultSet.
     * @param rs ResultSet from database query
     * @return ParkingOrder object
     * @throws SQLException if database error occurs
     */
    private ParkingOrder createParkingOrderFromResultSet(ResultSet rs) throws SQLException {
        ParkingOrder order = new ParkingOrder();
        order.setOrderID(rs.getInt("ParkingInfo_ID"));
        order.setParkingCode(String.valueOf(rs.getInt("Code")));
        order.setSubscriberName(rs.getString("Name"));
        order.setOrderType(rs.getString("ReservationType"));
        order.setSpotNumber("Spot " + rs.getInt("ParkingSpot_ID"));
        order.setStatus(rs.getString("statusEnum"));
        order.setExtended("yes".equals(rs.getString("IsExtended")));
        
        // Convert Timestamps to LocalDateTime
        if (rs.getTimestamp("Entry_time") != null) {
            order.setEntryTime(rs.getTimestamp("Entry_time").toLocalDateTime());
        }
        if (rs.getTimestamp("Actual_end_time") != null) {
            order.setExitTime(rs.getTimestamp("Actual_end_time").toLocalDateTime());
        }
        if (rs.getTimestamp("Estimated_end_time") != null) {
            order.setExpectedExitTime(rs.getTimestamp("Estimated_end_time").toLocalDateTime());
        }
        
        return order;
    }
    
    /**
     * Enters parking with an existing reservation.
     * @param reservationID Reservation ID to activate
     * @return Success message or error description
     */
    public String enterParkingWithReservation(int reservationID) {
        Connection conn = DBController.getInstance().getConnection();
        
        // First check if reservation exists and is in preorder status
        String checkQuery = "SELECT * FROM parkinginfo WHERE ParkingInfo_ID = ? AND statusEnum = 'preorder'";
        
        try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, reservationID);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (!rs.next()) {
                    return "Reservation not found or already activated";
                }
                
                // Update reservation to active status
                String updateQuery = """
                        UPDATE parkinginfo 
                        SET statusEnum = 'active', Entry_time = CURRENT_TIMESTAMP, Code = ?
                        WHERE ParkingInfo_ID = ?
                        """;
                
                int parkingCode = generateParkingCode();
                
                try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                    updateStmt.setInt(1, parkingCode);
                    updateStmt.setInt(2, reservationID);
                    
                    int updatedRows = updateStmt.executeUpdate();
                    if (updatedRows > 0) {
                        return "Reservation activated successfully. Your parking code is: " + parkingCode;
                    } else {
                        return "Failed to activate reservation";
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error activating reservation: " + e.getMessage());
            return "Error activating reservation: " + e.getMessage();
        } finally {
            DBController.getInstance().releaseConnection(conn);
        }
    }
}