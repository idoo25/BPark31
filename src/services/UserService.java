package services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
    
    /**
     * Gets user name by username and user ID.
     * @param userName Username to search for
     * @param userID User ID to match
     * @return User's full name or null if not found
     */
    public String getNameByUsernameAndUserID(String userName, int userID) {
        Connection conn = DBController.getInstance().getConnection();
        String query = "SELECT Name FROM users WHERE UserName = ? AND User_ID = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, userName);
            stmt.setInt(2, userID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Name");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting name by username and userID: " + e.getMessage());
        } finally {
            DBController.getInstance().releaseConnection(conn);
        }
        
        return null;
    }
    
    /**
     * Gets user name by user ID.
     * @param userID User ID to search for
     * @return User's full name or null if not found
     */
    public String getNameByUserID(int userID) {
        Connection conn = DBController.getInstance().getConnection();
        String query = "SELECT Name FROM users WHERE User_ID = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Name");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting name by userID: " + e.getMessage());
        } finally {
            DBController.getInstance().releaseConnection(conn);
        }
        
        return null;
    }
    
    /**
     * Gets all subscribers in the system.
     * @return ArrayList of all parking subscribers
     */
    public ArrayList<ParkingSubscriber> getAllSubscribers() {
        ArrayList<ParkingSubscriber> subscribers = new ArrayList<>();
        Connection conn = DBController.getInstance().getConnection();
        String query = "SELECT * FROM users WHERE UserTypeEnum = 'sub' ORDER BY Name";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ParkingSubscriber subscriber = createSubscriberFromResultSet(rs);
                    subscribers.add(subscriber);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting all subscribers: " + e.getMessage());
        } finally {
            DBController.getInstance().releaseConnection(conn);
        }
        
        return subscribers;
    }
    
    /**
     * Sends lost parking code to user email by user ID.
     * @param userID User ID requesting lost code
     * @return Success message or error description
     */
    public String sendLostParkingCodeByUserID(int userID) {
        Connection conn = DBController.getInstance().getConnection();
        String query = """
                SELECT pi.Code, u.Email, u.Name
                FROM parkinginfo pi
                JOIN users u ON pi.User_ID = u.User_ID
                WHERE u.User_ID = ? AND pi.statusEnum = 'active' AND pi.Actual_end_time IS NULL
                """;
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userID);
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
            System.err.println("Error sending lost code by userID: " + e.getMessage());
        } finally {
            DBController.getInstance().releaseConnection(conn);
        }
        
        return "No active parking session found";
    }
}