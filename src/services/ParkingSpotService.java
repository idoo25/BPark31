package services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalTime;
import server.DBController;

/**
 * ParkingSpotService handles all parking spot operations following Single Responsibility Principle.
 * Manages parking spot availability, allocation, and tracking.
 */
public class ParkingSpotService {
    
    private static ParkingSpotService instance;
    private static final int TOTAL_PARKING_SPOTS = 10;
    
    /**
     * Private constructor for singleton pattern.
     */
    private ParkingSpotService() {}
    
    /**
     * Returns singleton instance of ParkingSpotService.
     * @return ParkingSpotService instance
     */
    public static synchronized ParkingSpotService getInstance() {
        if (instance == null) {
            instance = new ParkingSpotService();
        }
        return instance;
    }
    
    /**
     * Initializes parking spots in the database.
     */
    public void initializeParkingSpots() {
        Connection conn = DBController.getInstance().getConnection();
        String checkQuery = "SELECT COUNT(*) FROM parkingspot";
        
        try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String insertQuery = "INSERT INTO parkingspot (ParkingSpot_ID, isOccupied) VALUES (?, FALSE)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                        for (int i = 1; i <= TOTAL_PARKING_SPOTS; i++) {
                            insertStmt.setInt(1, i);
                            insertStmt.executeUpdate();
                        }
                        System.out.println("Initialized " + TOTAL_PARKING_SPOTS + " parking spots");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error initializing parking spots: " + e.getMessage());
        } finally {
            DBController.getInstance().releaseConnection(conn);
        }
    }
    
    /**
     * Gets the number of available parking spots.
     * @return Number of available spots
     */
    public int getAvailableSpots() {
        Connection conn = DBController.getInstance().getConnection();
        String query = "SELECT COUNT(*) FROM parkingspot WHERE isOccupied = FALSE";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting available spots: " + e.getMessage());
        } finally {
            DBController.getInstance().releaseConnection(conn);
        }
        
        return 0;
    }
    
    /**
     * Checks if parking is full.
     * @return true if no spots available, false otherwise
     */
    public boolean isParkingFull() {
        return getAvailableSpots() == 0;
    }
    
    /**
     * Allocates an available parking spot.
     * @return Spot ID if successful, -1 if no spots available
     */
    public int allocateSpot() {
        Connection conn = DBController.getInstance().getConnection();
        String selectQuery = "SELECT ParkingSpot_ID FROM parkingspot WHERE isOccupied = FALSE LIMIT 1";
        
        try (PreparedStatement selectStmt = conn.prepareStatement(selectQuery)) {
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    int spotId = rs.getInt("ParkingSpot_ID");
                    
                    String updateQuery = "UPDATE parkingspot SET isOccupied = TRUE WHERE ParkingSpot_ID = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setInt(1, spotId);
                        updateStmt.executeUpdate();
                        return spotId;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error allocating spot: " + e.getMessage());
        } finally {
            DBController.getInstance().releaseConnection(conn);
        }
        
        return -1;
    }
    
    /**
     * Releases a parking spot by spot ID.
     * @param spotId Parking spot ID to release
     * @return true if successful, false otherwise
     */
    public boolean releaseSpot(int spotId) {
        Connection conn = DBController.getInstance().getConnection();
        String updateQuery = "UPDATE parkingspot SET isOccupied = FALSE WHERE ParkingSpot_ID = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
            stmt.setInt(1, spotId);
            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            System.err.println("Error releasing spot: " + e.getMessage());
        } finally {
            DBController.getInstance().releaseConnection(conn);
        }
        
        return false;
    }
    
    /**
     * Releases a parking spot by reservation code.
     * @param reservationCode Parking reservation code
     * @return true if successful, false otherwise
     */
    public boolean releaseSpotByReservation(int reservationCode) {
        Connection conn = DBController.getInstance().getConnection();
        String query = """
                UPDATE parkingspot ps
                JOIN parkinginfo pi ON ps.ParkingSpot_ID = pi.ParkingSpot_ID
                SET ps.isOccupied = FALSE
                WHERE pi.ParkingInfo_ID = ?
                """;
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, reservationCode);
            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            System.err.println("Error freeing spot for reservation: " + e.getMessage());
        } finally {
            DBController.getInstance().releaseConnection(conn);
        }
        
        return false;
    }
    
    /**
     * Gets the spot ID for a given parking code.
     * @param parkingCode Parking code
     * @return Spot ID or -1 if not found
     */
    public int getSpotIdByParkingCode(int parkingCode) {
        Connection conn = DBController.getInstance().getConnection();
        String query = """
                SELECT ps.ParkingSpot_ID 
                FROM parkinginfo pi 
                JOIN parkingspot ps ON pi.ParkingSpot_ID = ps.ParkingSpot_ID 
                WHERE pi.Code = ? AND pi.Actual_end_time IS NULL
                """;
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, parkingCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ParkingSpot_ID");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting spot ID by parking code: " + e.getMessage());
        } finally {
            DBController.getInstance().releaseConnection(conn);
        }
        
        return -1;
    }
    
    /**
     * Checks if a specific spot is occupied.
     * @param spotId Spot ID to check
     * @return true if occupied, false if available
     */
    public boolean isSpotOccupied(int spotId) {
        Connection conn = DBController.getInstance().getConnection();
        String query = "SELECT isOccupied FROM parkingspot WHERE ParkingSpot_ID = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, spotId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("isOccupied");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking spot occupancy: " + e.getMessage());
        } finally {
            DBController.getInstance().releaseConnection(conn);
        }
        
        return true; // Default to occupied for safety
    }
}