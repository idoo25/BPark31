package services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import entities.ParkingSubscriber;
import server.DBController;
import services.ValidationService;
import services.NotificationService;

/**
 * UserService handles all user-related operations following Single Responsibility Principle.
 * Manages user registration, authentication, profile updates, and user data retrieval.
 */
public class UserService {
    
    private static UserService instance;
    
    /**
     * Private constructor for singleton pattern.
     */
    private UserService() {}
    
    /**
     * Returns singleton instance of UserService.
     * @return UserService instance
     */
    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
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
        if (!ValidationService.getInstance().validateUserRegistration(name, phone, email, userName)) {
            return "Invalid user data provided";
        }
        
        if (isUserNameTaken(userName)) {
            return "Username already exists";
        }
        
        Connection conn = DBController.getInstance().getConnection();
        String insertQry = "INSERT INTO users (UserName, Name, Phone, Email, CarNum, UserTypeEnum) VALUES (?, ?, ?, ?, ?, 'sub')";
        
        try (PreparedStatement stmt = conn.prepareStatement(insertQry)) {
            stmt.setString(1, userName);
            stmt.setString(2, name);
            stmt.setString(3, phone);
            stmt.setString(4, email);
            stmt.setString(5, carNumber);
            
            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                NotificationService.getInstance().sendRegistrationConfirmation(email, name, userName);
                return "Registration successful";
            }
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
        } finally {
            DBController.getInstance().releaseConnection(conn);
        }
        
        return "Registration failed";
    }
    
    /**
     * Updates subscriber information.
     * @param userName Username to update
     * @param phone New phone number
     * @param email New email address
     * @return Success message or error description
     */
    public String updateSubscriberInfo(String userName, String phone, String email) {
        if (!ValidationService.getInstance().validateContactInfo(phone, email)) {
            return "Invalid contact information";
        }
        
        Connection conn = DBController.getInstance().getConnection();
        String updateQry = "UPDATE users SET Phone = ?, Email = ? WHERE UserName = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(updateQry)) {
            stmt.setString(1, phone);
            stmt.setString(2, email);
            stmt.setString(3, userName);
            
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                return "Subscriber information updated successfully";
            }
        } catch (SQLException e) {
            System.err.println("Error updating subscriber info: " + e.getMessage());
        } finally {
            DBController.getInstance().releaseConnection(conn);
        }
        
        return "Failed to update subscriber information";
    }
    
    /**
     * Retrieves subscriber by username.
     * @param userName Username to search
     * @return ParkingSubscriber object or null if not found
     */
    public ParkingSubscriber getSubscriberByUserName(String userName) {
        Connection conn = DBController.getInstance().getConnection();
        String query = "SELECT * FROM users WHERE UserName = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, userName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return createSubscriberFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving subscriber: " + e.getMessage());
        } finally {
            DBController.getInstance().releaseConnection(conn);
        }
        
        return null;
    }
    
    /**
     * Retrieves subscriber by name.
     * @param name Name to search
     * @return ParkingSubscriber object or null if not found
     */
    public ParkingSubscriber getSubscriberByName(String name) {
        Connection conn = DBController.getInstance().getConnection();
        String query = "SELECT * FROM users WHERE Name = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return createSubscriberFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving subscriber by name: " + e.getMessage());
        } finally {
            DBController.getInstance().releaseConnection(conn);
        }
        
        return null;
    }
    
    /**
     * Authenticates user login credentials.
     * @param userName Username
     * @param password Password (if applicable)
     * @return Authentication result message
     */
    public String authenticateUser(String userName, String password) {
        ParkingSubscriber subscriber = getSubscriberByUserName(userName);
        if (subscriber != null) {
            return "Login successful";
        }
        return "Invalid credentials";
    }
    
    /**
     * Checks if username is already taken.
     * @param userName Username to check
     * @return true if taken, false if available
     */
    private boolean isUserNameTaken(String userName) {
        Connection conn = DBController.getInstance().getConnection();
        String checkQry = "SELECT COUNT(*) FROM users WHERE UserName = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(checkQry)) {
            stmt.setString(1, userName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking username: " + e.getMessage());
        } finally {
            DBController.getInstance().releaseConnection(conn);
        }
        
        return false;
    }
    
    /**
     * Creates ParkingSubscriber object from ResultSet.
     * @param rs ResultSet from database query
     * @return ParkingSubscriber object
     * @throws SQLException if database error occurs
     */
    private ParkingSubscriber createSubscriberFromResultSet(ResultSet rs) throws SQLException {
        return new ParkingSubscriber(
            rs.getInt("User_ID"),
            rs.getString("UserName"),
            rs.getString("Name"),
            rs.getString("Phone"),
            rs.getString("Email"),
            rs.getString("CarNum"),
            rs.getString("UserTypeEnum")
        );
    }
}