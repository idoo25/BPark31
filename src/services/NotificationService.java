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
        return EmailService.sendNotification(
            EmailService.NotificationType.REGISTRATION_CONFIRMATION, 
            email, 
            name, 
            userName
        );
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
        return EmailService.sendNotification(
            EmailService.NotificationType.RESERVATION_CONFIRMATION, 
            email, 
            name, 
            reservationCode, 
            parkingDate, 
            startTime, 
            endTime
        );
    }
    
    /**
     * Sends parking code recovery email.
     * @param email Recipient email address
     * @param name User's name
     * @param parkingCode Lost parking code
     * @return true if sent successfully, false otherwise
     */
    public boolean sendParkingCodeRecovery(String email, String name, int parkingCode) {
        return EmailService.sendNotification(
            EmailService.NotificationType.PARKING_CODE_RECOVERY, 
            email, 
            name, 
            parkingCode
        );
    }
    
    /**
     * Sends reservation cancellation email.
     * @param email Recipient email address
     * @param name User's name
     * @param reservationCode Cancelled reservation code
     * @return true if sent successfully, false otherwise
     */
    public boolean sendReservationCancellation(String email, String name, int reservationCode) {
        return EmailService.sendNotification(
            EmailService.NotificationType.RESERVATION_CANCELLED, 
            email, 
            name, 
            reservationCode
        );
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
        return EmailService.sendNotification(
            EmailService.NotificationType.EXTENSION_CONFIRMATION, 
            email, 
            name, 
            parkingCode, 
            newEndTime
        );
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
        return EmailService.sendNotification(
            EmailService.NotificationType.LATE_PICKUP, 
            email, 
            name, 
            parkingCode, 
            lateMinutes
        );
    }
    
    /**
     * Sends parking expired notification email.
     * @param email Recipient email address
     * @param name User's name
     * @param parkingCode Parking code
     * @return true if sent successfully, false otherwise
     */
    public boolean sendParkingExpiredNotification(String email, String name, int parkingCode) {
        return EmailService.sendNotification(
            EmailService.NotificationType.PARKING_EXPIRED, 
            email, 
            name, 
            parkingCode
        );
    }
    
    /**
     * Sends welcome message to new users.
     * @param email Recipient email address
     * @param name User's name
     * @return true if sent successfully, false otherwise
     */
    public boolean sendWelcomeMessage(String email, String name) {
        return EmailService.sendNotification(
            EmailService.NotificationType.WELCOME_MESSAGE, 
            email, 
            name
        );
    }
}