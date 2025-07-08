package services;

/**
 * NotificationService handles all notification operations following Single Responsibility Principle.
 * Manages email notifications, system messages, and communication with users.
 */
public class NotificationService {
    
    private static NotificationService instance;
    
    /**
     * Private constructor for singleton pattern.
     */
    private NotificationService() {}
    
    /**
     * Returns singleton instance of NotificationService.
     * @return NotificationService instance
     */
    public static synchronized NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }
    
    /**
     * Sends registration confirmation email.
     * @param email Recipient email address
     * @param name User's name
     * @param userName Username
     * @return true if sent successfully, false otherwise
     */
    public boolean sendRegistrationConfirmation(String email, String name, String userName) {
        System.out.println("Registration confirmation sent to: " + email + " for user: " + name);
        return true;
    }
    
    /**
     * Sends reservation confirmation email.
     * @param email Recipient email address
     * @param name User's name
     * @param reservationCode Reservation code
     * @param parkingDate Parking date
     * @param startTime Start time
     * @param endTime End time
     * @return true if sent successfully, false otherwise
     */
    public boolean sendReservationConfirmation(String email, String name, int reservationCode, 
                                             String parkingDate, String startTime, String endTime) {
        System.out.println("Reservation confirmation sent to: " + email + " for code: " + reservationCode);
        return true;
    }
    
    /**
     * Sends parking code recovery email.
     * @param email Recipient email address
     * @param name User's name
     * @param parkingCode Lost parking code
     * @return true if sent successfully, false otherwise
     */
    public boolean sendParkingCodeRecovery(String email, String name, int parkingCode) {
        System.out.println("Parking code recovery sent to: " + email + " with code: " + parkingCode);
        return true;
    }
    
    /**
     * Sends reservation cancellation email.
     * @param email Recipient email address
     * @param name User's name
     * @param reservationCode Cancelled reservation code
     * @return true if sent successfully, false otherwise
     */
    public boolean sendReservationCancellation(String email, String name, int reservationCode) {
        System.out.println("Reservation cancellation sent to: " + email + " for code: " + reservationCode);
        return true;
    }
    
    /**
     * Sends parking extension confirmation email.
     * @param email Recipient email address
     * @param name User's name
     * @param parkingCode Parking code
     * @param newEndTime New end time after extension
     * @return true if sent successfully, false otherwise
     */
    public boolean sendExtensionConfirmation(String email, String name, int parkingCode, String newEndTime) {
        System.out.println("Extension confirmation sent to: " + email + " for code: " + parkingCode);
        return true;
    }
    
    /**
     * Sends late pickup notification email.
     * @param email Recipient email address
     * @param name User's name
     * @param parkingCode Parking code
     * @param lateMinutes Minutes overdue
     * @return true if sent successfully, false otherwise
     */
    public boolean sendLatePickupNotification(String email, String name, int parkingCode, int lateMinutes) {
        System.out.println("Late pickup notification sent to: " + email + " for code: " + parkingCode);
        return true;
    }
    
    /**
     * Sends parking expired notification email.
     * @param email Recipient email address
     * @param name User's name
     * @param parkingCode Parking code
     * @return true if sent successfully, false otherwise
     */
    public boolean sendParkingExpiredNotification(String email, String name, int parkingCode) {
        System.out.println("Parking expired notification sent to: " + email + " for code: " + parkingCode);
        return true;
    }
    
    /**
     * Sends welcome message to new users.
     * @param email Recipient email address
     * @param name User's name
     * @return true if sent successfully, false otherwise
     */
    public boolean sendWelcomeMessage(String email, String name) {
        System.out.println("Welcome message sent to: " + email + " for user: " + name);
        return true;
    }
}
